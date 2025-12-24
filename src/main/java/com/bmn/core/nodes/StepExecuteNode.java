package com.bmn.core.nodes;

import com.bmn.core.model.AgentOutput;
import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.model.TaskStep;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.ExecuteNode;
import com.bmn.core.port.LLMClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class StepExecuteNode implements ExecuteNode {

    private final LLMClient llm;

    @Override
    public Context execute(Context context, AgentCallback callback) {
        TaskStep step = context.getPlan().get(context.getCurrentStepIndex());
        callback.next(AgentOutput.hintMessage("Executing step: " + step.getTitle()));

        String prompt = """
                Role: bạn là executor, bạn sẽ thực hiện step %s (%s) với thông tin sau %s
                Định dạng trả về: bạn sẽ thực hiện step và phản hồi lại trạng thái thực hiện
                """.formatted(step.getTitle(), step.getDesc(), step.getMetadata().toPrettyString());


        List<LLMMessage> messages = List.of(
                LLMMessage.systemMessage(prompt),
                LLMMessage.userMessage(context.getLastUserReply(), null)
        );

        LLMMessage output = llm.generate(messages);
        callback.next(AgentOutput.textMessage(output.content()));

        context.setCurrentStepIndex(context.getCurrentStepIndex() + 1);

        return context;
    }
}
