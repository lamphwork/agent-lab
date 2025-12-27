package com.bmn.core.nodes;

import com.bmn.core.model.AgentOutput;
import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.model.TaskStep;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.LLMClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class StepExecuteNode extends AbstractLLMNode {

    private final ObjectMapper mapper = new ObjectMapper();

    public StepExecuteNode(LLMClient llm) {
        super(llm);
    }

    @Override
    LLMMessage systemPrompt(Context context) {
        String prompt = """
                Bạn là một tác nhân THỰC THI (execution agent).
                
                Nhiệm vụ của bạn là thực hiện CHÍNH XÁC MỘT step hiện tại trong kế hoạch.
                
                Bạn PHẢI trả về KẾT QUẢ CUỐI CÙNG của step.
                Không được trả về trạng thái "đang làm", "sẽ làm", hay "processing".
                
                Nếu step là thiết kế hoặc sinh code:
                - Hãy sinh nội dung hoàn chỉnh
                - Sau đó đánh dấu step là HOÀN THÀNH
                
                Bạn CHỈ được trả về MỘT trong các dạng sau (JSON):
                
                1. Step hoàn thành:
                {
                  "status": "done",
                  "content": "Kết quả hoàn chỉnh của step"
                }
                
                2. Cần hỏi người dùng:
                {
                  "status": "ask_user",
                  "content": "Câu hỏi cụ thể",
                  "required_fields": ["field1"]
                }
                
                3. Step thất bại:
                {
                  "status": "failed",
                  "content": "Mô tả lỗi rõ ràng"
                }
                
                KHÔNG được trả về bất kỳ trạng thái nào khác.
                
                """;

        return LLMMessage.systemMessage(prompt);
    }

    @Override
    LLMMessage userPrompt(Context context) {
        TaskStep currentStep = context.getPlan().get(context.getCurrentStepIndex());
        String contextPrompt = """
                Kế hoạch (plan):
                %s
                
                Step hiện tại:
                %s
                
                """.formatted(context.getPlan(), currentStep);

        return LLMMessage.userMessage(contextPrompt, null);
    }

    @Override
    Context handleLLMResponse(Context context, AgentCallback callback, List<LLMMessage> outputMessages) {
        TaskStep currentStep = context.getPlan().get(context.getCurrentStepIndex());

        for (LLMMessage message : outputMessages) {
            if (message.toolInfo() != null) {
                context.getToolsToExecute().add(message.toolInfo());
                continue;
            }

            try {
                JsonNode jsonNode = mapper.readTree(message.content());
                String status = jsonNode.get("status").asText();
                String content = jsonNode.get("content").asText();
                currentStep.setStatus(status);
                callback.next(AgentOutput.textMessage(content));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("can not parse execute result", e);
            }
        }

        return context;
    }
}
