package com.bmn.core.nodes;

import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.ExecuteNode;
import com.bmn.core.port.LLMClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractLLMNode implements ExecuteNode {

    protected final LLMClient llm;

    @Override
    public Context execute(Context context, AgentCallback callback) {
        List<LLMMessage> inputMessages = generateMessages(context);
        List<LLMMessage> outputMessages = llm.generate(inputMessages);

        context.getHistories().addAll(outputMessages);
        return handleLLMResponse(outputMessages);
    }

    List<LLMMessage> generateMessages(Context context) {
        LLMMessage assistantTraces = context.getHistories()
                .stream()
                .filter(item -> "assistant".equals(item.role()))
                .toList()
                .getLast();

        return List.of(
                systemPrompt(context),
                assistantTraces,
                userPrompt(context)
        );
    }

    abstract LLMMessage systemPrompt(Context context);
    abstract LLMMessage userPrompt(Context context);
    abstract Context handleLLMResponse(List<LLMMessage> outputMessages);
}
