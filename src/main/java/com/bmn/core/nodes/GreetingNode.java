package com.bmn.core.nodes;

import com.bmn.core.model.AgentOutput;
import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.ExecuteNode;
import com.bmn.core.port.LLMClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GreetingNode implements ExecuteNode {

    private final LLMClient llm;

    @Override
    public Context execute(Context context, AgentCallback callback) {
        String prompt = """
                Role: Bạn là greater, bạn sẽ đưa ra lời chào và hỏi người dùng có cần giúp gì không
                """;

        List<LLMMessage> messages = List.of(
                new LLMMessage("system", prompt, null)
        );

        List<LLMMessage> output = llm.generate(messages);

        for (LLMMessage message : output) {
            context.setPendingQuestion(message.content());
            context.pauseToAskUser(Context.State.GREETING);
            callback.next(AgentOutput.textMessage(message.content()));
        }

        return context;
    }
}
