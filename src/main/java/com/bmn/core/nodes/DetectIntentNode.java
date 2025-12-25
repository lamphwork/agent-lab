package com.bmn.core.nodes;

import com.bmn.core.model.Agent;
import com.bmn.core.model.AgentOutput;
import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.ExecuteNode;
import com.bmn.core.port.LLMClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class DetectIntentNode implements ExecuteNode {

    private final LLMClient llm;

    @Override
    public Context execute(Context context, AgentCallback callback) {
        callback.next(AgentOutput.hintMessage("Detect Intent started"));
        String prompt = """
        Role: Bạn là một intent detector. Công việc của bạn là tương tác với người dùng để xác định ý định của người dùng:
        - reply: Không có yêu cầu cụ thể, user chỉ đơn thuần cung cấp thông tin hoặc trả lời một câu hỏi trước đấy
        - new_task: Một yêu cầu mới, cần lên kế hoạch lại
        - cancel: người dùng huỷ công việc hiện tại

        Cách phân biệt loại thông tin:
        - Luôn dựa vào lịch sử trò chuyện để xác định
        - Nếu có yêu cầu cụ thể cần thực hiện -> new_task
        - Nếu không có yêu cầu cụ thể, người dùng chỉ đang trả lời và bạn cần làm rõ thêm -> reply
        - Nếu người nói dừng/ huỷ / cancel hoặc tương tự -> cancel
        Result format: Bạn sẽ trả về cho tôi loại thông tin dạng json, bao gồm field intentType, content
        Ví dụ: {"intentType": "reply", "content": "Nội dung phản hồi với người dùng"}
        Lưu ý: Bắt buộc trả về định dạng json
        
        """;

        List<LLMMessage> messages = new LinkedList<>(List.of(new LLMMessage("system", prompt, null)));
        messages.addAll(context.getHistories());
        messages.add(new LLMMessage("user", "Input từ người dùng: " + context.getLastUserReply(), null));

        List<LLMMessage> output = llm.generate(messages);
        try {
            for (LLMMessage message : output) {
                JsonNode jsonNode = new ObjectMapper().readTree(message.content());
                String intentType = jsonNode.get("intentType").asText();
                String content = jsonNode.get("content").asText();

                context.setIntent(intentType);
                callback.next(AgentOutput.hintMessage(intentType));
                callback.next(AgentOutput.textMessage(content));
            }

            return context;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("can not parse llm output in DetectIntentNode", e);
        }
    }
}
