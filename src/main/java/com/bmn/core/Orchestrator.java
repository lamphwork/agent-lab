package com.bmn.core;

import com.bmn.core.model.*;
import com.bmn.core.nodes.*;
import com.bmn.core.port.*;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Getter
public class Orchestrator {

    private final LLMClient llmClient;
    private final ContextLoader contextLoader;
    private final Map<Context.State, ExecuteNode> nodes;
    private final StateRouter router;

    public Orchestrator(LLMClient llmClient, ContextLoader contextLoader, StateRouter stateRouter) {
        this.llmClient = llmClient;
        this.contextLoader = contextLoader;
        this.router = stateRouter;

        this.nodes = new LinkedHashMap<>();

        initNodes();
    }

    private void initNodes() {
        nodes.put(Context.State.INTENT_DETECT, new DetectIntentNode(llmClient));
        nodes.put(Context.State.GREETING, new GreetingNode(llmClient));
        nodes.put(Context.State.RESPONDER, new ResponserNode(llmClient));
        nodes.put(Context.State.PLANNING, new PlanningNode(llmClient));
        nodes.put(Context.State.EXECUTE_STEP, new StepExecuteNode(llmClient));
    }

    public void process(Agent agent, AgentInput input, AgentCallback callback) {
        Context context = contextLoader.load(input.getContextId());
        if (context == null) {
            context = Context.newContext(input.getContent());
        } else {
            context.resume();
        }
        context.putUserMessage(input.getContent());


        AgentCallback proxiedCb = proxiedCallback(context, callback);
        while (context.shouldRun()) {
            ExecuteNode executor = nodes.get(context.getState());
            if (executor == null) {
                throw new RuntimeException("No executor found for state " + context.getState());
            }
            context = executor.execute(context, proxiedCb);

            Context.State next = router.route(context);
            context.setState(next);

            contextLoader.save(context);
        }
    }

    AgentCallback proxiedCallback(Context context, AgentCallback originCallback) {
        return output -> {
            if (output.getType() == AgentOutput.OutputType.MESSAGE) {
                LLMMessage message = new LLMMessage("assistant", output.getContent(), null);
                context.getHistories().add(message);
            }
            originCallback.next(output);
        };
    }

}
