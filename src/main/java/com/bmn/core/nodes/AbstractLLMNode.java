package com.bmn.core.nodes;

import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.model.ToolInfo;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.ExecuteNode;
import com.bmn.core.port.LLMClient;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractLLMNode implements ExecuteNode {

    protected final LLMClient llm;

    @Override
    public Context execute(Context context, AgentCallback callback) {
        List<LLMMessage> inputMessages = generateMessages(context);
        List<LLMMessage> outputMessages = llm.generate(inputMessages);

        context.getHistories().addAll(outputMessages);
        return handleLLMResponse(context, callback, outputMessages);
    }

    List<LLMMessage> generateMessages(Context context) {
        List<LLMMessage> histories = context.getHistories();
        List<ToolInfo> toolInfoMap = context.getToolsToExecute();

        List<LLMMessage> traces = toolInfoMap.isEmpty() ?
                histories.stream()
                        .filter(item -> item.role().equals("assistant") && item.toolInfo() == null)
                        .toList() :
                histories.stream()
                        .skip(histories.size() - Math.min(toolInfoMap.size() * 2, histories.size() - 1))
                        .toList();

        List<LLMMessage> llmInput = new ArrayList<>();
        llmInput.add(systemPrompt(context));
        llmInput.addAll(traces);
        if (userPrompt(context) != null) {
            llmInput.add(userPrompt(context));
        }

        toolInfoMap.clear();
        return llmInput;
    }

    abstract LLMMessage systemPrompt(Context context);

    abstract LLMMessage userPrompt(Context context);

    abstract Context handleLLMResponse(Context context, AgentCallback callback, List<LLMMessage> outputMessages);
}
