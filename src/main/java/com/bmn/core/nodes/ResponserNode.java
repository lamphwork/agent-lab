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
public class ResponserNode implements ExecuteNode {

    private final LLMClient llmClient;

    @Override
    public Context execute(Context context, AgentCallback callback) {
        String lastUserInput = context.getLastUserReply();

        List<LLMMessage> messages = List.of(
                new LLMMessage("user", lastUserInput, null)
        );

        LLMMessage output = llmClient.generate(messages);
        String answer = output.content();
        context.setState(Context.State.DONE_GOAL);

        callback.next(AgentOutput.textMessage(answer));

        return context;
    }
}
