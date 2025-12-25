package com.bmn.adapter;

import com.bmn.core.model.LLMMessage;
import com.bmn.core.model.ToolInfo;
import com.bmn.core.port.LLMClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OpenAI implements LLMClient {

    private static final String CHAT_ENDPOINT =
            "https://api.openai.com/v1/chat/completions";

    private static final String EMBEDDING_ENDPOINT =
            "https://api.openai.com/v1/embeddings";

    private final String apiKey;
    private final String chatModel;
    private final String embeddingModel;
    private final ObjectMapper mapper = new ObjectMapper();

    // -----------------------------
    // generate()
    // -----------------------------
    @Override
    public List<LLMMessage> generate(List<LLMMessage> messages) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(CHAT_ENDPOINT);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            ObjectNode request = mapper.createObjectNode()
                    .objectNode()
                    .put("model", chatModel)
                    .set("messages", adaptOpenAIMessage(messages));

            String fakeTool = """
                    {
                        "type": "function",
                        "function": {
                            "name": "writeFile",
                            "description": "Ghi vào file mới",
                            "parameters": {
                                "type": "object",
                                "properties": {
                                    "location": {
                                        "type": "string",
                                        "description": "Vị trí file "
                                    },
                                    "content": {
                                        "type": "string",
                                        "description": "Nội dung file"
                                    }
                                },
                                "required": [
                                    "location"
                                ]
                            }
                        }
                    }
                    """;

            request.set(
                    "tools",
                    mapper.createArrayNode().addRawValue(new  RawValue(fakeTool))
            );

            post.setEntity(
                    new StringEntity(mapper.writeValueAsString(request), StandardCharsets.UTF_8)
            );

            JsonNode response = client.execute(post,
                    httpResponse -> mapper.readTree(httpResponse.getEntity().getContent())
            );
            System.out.println(response.toString());

            List<LLMMessage> result = new ArrayList<>();
            ArrayNode choices = (ArrayNode) response.get("choices");
            for (JsonNode choice : choices) {
                JsonNode message = choice.get("message");
                String role = message.get("role").asText();
                String content = message.get("content").asText();
                ToolInfo toolInfo = null;

                JsonNode toolCalls = message.get("tool_calls");
                if (toolCalls != null && toolCalls.isArray()) {
                    ArrayNode tools = (ArrayNode) toolCalls;
                    for (JsonNode tool : tools) {
                        String callId = tool.get("id").asText();
                        JsonNode function = tool.get("function");
                        toolInfo = new ToolInfo(
                                callId,
                                function.get("name").asText(),
                                mapper.readTree(function.get("arguments").asText())
                        );
                    }
                }

                result.add(new LLMMessage(role, content, toolInfo));
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("OpenAI generate failed", e);
        }
    }

    // -----------------------------
    // embedding()
    // -----------------------------
    @Override
    public float[] embedding(String content) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(EMBEDDING_ENDPOINT);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            JsonNode request = mapper.createObjectNode()
                    .put("model", embeddingModel)
                    .put("input", content);

            post.setEntity(
                    new StringEntity(mapper.writeValueAsString(request), StandardCharsets.UTF_8)
            );

            JsonNode response = client.execute(post,
                    httpResponse -> mapper.readTree(httpResponse.getEntity().getContent())
            );

            JsonNode embeddingNode =
                    response.at("/data/0/embedding");

            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = (float) embeddingNode.get(i).asDouble();
            }

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("OpenAI embedding failed", e);
        }
    }


    private ArrayNode adaptOpenAIMessage(List<LLMMessage> messages) {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (LLMMessage message : messages) {
            ObjectNode node = mapper.createObjectNode();
            node.put("role", message.role());
            node.put("content", message.content());

            if (message.toolInfo() != null) {
                ObjectNode toolInfoNode = mapper.createObjectNode();
                toolInfoNode.put("id", message.toolInfo().id());
                toolInfoNode.put("type", "function");
                toolInfoNode.putIfAbsent(
                        "function",
                        mapper.createObjectNode()
                                .put("name", message.toolInfo().name())
                                .putIfAbsent("arguments", message.toolInfo().args())
                );

                node.putIfAbsent("tool_calls", toolInfoNode);
            }

            arrayNode.add(node);
        }

        return arrayNode;
    }
}
