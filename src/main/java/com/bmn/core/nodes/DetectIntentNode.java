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

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class DetectIntentNode implements ExecuteNode {

    private final LLMClient llm;

    @Override
    public Context execute(Context context, AgentCallback callback) {
        String prompt = """
        Role: Bạn là một intent detector. Công việc của bạn là tương tác với người dùng để phân loại thông tin thành một trong các type sau:
            - reply: người dùng trả lời câu hỏi trong hội thoại trước đấy
            - new_task: một đoạn hội thoại mới, không liên quan đến hội thoại trước đấy
            - cancel: người dùng huỷ công việc hiện tại
        Cách phân biệt loại thông tin:
            - Luôn dựa vào lịch sử trò chuyện để xác định
            - Nếu là tin nhắn đầu tiên và yêu cầu cụ thể thực hiện việc nào đó -> new_task
            - Nếu có trò chuyện trước đấy, và thông tin người dùng nhập vào có liên quan, hoặc trả lời câu hỏi trước đấy -> reply
            - Nếu người nói dừng/ huỷ / cancel hoặc tương tự -> cancel
        Result format: Bạn sẽ trả về cho tôi loại thông tin dạng json, chỉ có 1 field là intentType
        Ví dụ: {"intentType": "reply"}
        Lưu ý: Bắt buộc trả về định dạng json
        """;

        List<LLMMessage> messages = new LinkedList<>(List.of(new LLMMessage("system", prompt, null)));
        messages.addAll(context.getHistories());
        messages.add(new LLMMessage("user", "Input từ người dùng: " + context.getLastUserReply(), null));

        LLMMessage output = llm.generate(messages);

        try {
            JsonNode jsonNode = new ObjectMapper().readTree(output.content());
            String intentType = jsonNode.get("intentType").asText();

            context.setIntent(intentType);

            callback.next(AgentOutput.hintMessage(intentType));

            return context;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("can not parse llm output in DetectIntentNode", e);
        }
    }
}
