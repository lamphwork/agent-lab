package com.bmn.core.nodes;

import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.model.TaskStep;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.ExecuteNode;
import com.bmn.core.port.LLMClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PlanningNode implements ExecuteNode {

    private final LLMClient llm;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Context execute(Context context, AgentCallback callback) {
        List<LLMMessage> messages = getLlmMessages();

        LLMMessage output = llm.generate(messages);
        List<TaskStep> steps = new LinkedList<>();

        try {
            JsonNode jsonNode = objectMapper.readTree(output.content());
            if (!jsonNode.isArray()) {
                throw new RuntimeException("planner response invalid array");
            }

            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                ObjectNode objectNode = (ObjectNode) arrayNode.get(i);
                TaskStep task = new  TaskStep(
                        UUID.randomUUID().toString(),
                        objectNode.get("title").asText(),
                        objectNode.get("desc").asText(),
                        (ObjectNode) objectNode.get("metadata")
                );
                steps.add(task);
            }

            context.setCurrentStepIndex(0);
            context.setPlan(steps);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("can not read planner response", e);
        }

        return context;
    }

    private static List<LLMMessage> getLlmMessages() {
        String prompt = """
        Role: Bạn là một planner cho AI agent.
        
        Nhiệm vụ:
        - Phân rã yêu cầu của người dùng thành các bước THỰC THI ĐƯỢC bởi AI agent.
        - KHÔNG đưa ra các bước quản lý dự án chung chung (như họp, thu thập yêu cầu, quản lý team).
        - Mỗi step phải có thể được executor xử lý trực tiếp (thiết kế, phân tích, viết code, đề xuất kiến trúc, ...).
        
        Output:
        - Chỉ trả về JSON thuần (raw JSON).
        - KHÔNG dùng markdown.
        - KHÔNG dùng ``` hoặc bất kỳ ký hiệu định dạng nào.
        - Output phải là một mảng JSON.
        
        Schema:
        [
          {
            "title": "short technical action",
            "desc": "mô tả rõ việc cần làm",
            "metadata": {
              "type": "design | analysis | implementation",
              "dependency": []
            }
          }
        ]
        
        Yêu cầu quan trọng:
        - Không giải thích thêm.
        - Không thêm text ngoài JSON.
        """;

        return List.of(
                new LLMMessage("system", prompt, null)
        );
    }
}
