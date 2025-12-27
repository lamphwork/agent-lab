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
    private final Map<Context.State, Function<Context, Context.State>> edges;

    public Orchestrator(LLMClient llmClient, ContextLoader contextLoader) {
        this.llmClient = llmClient;
        this.contextLoader = contextLoader;

        this.nodes = new LinkedHashMap<>();
        this.edges = new LinkedHashMap<>();

        initNodes();
        initEdges();
    }

    private void initNodes() {
        nodes.put(Context.State.INTENT_DETECT, new DetectIntentNode(llmClient));
        nodes.put(Context.State.RESPONDER, new ResponserNode(llmClient));
        nodes.put(Context.State.PLANNING, new PlanningNode(llmClient));
        nodes.put(Context.State.EXECUTE_STEP, new StepExecuteNode(llmClient));
        nodes.put(Context.State.TOOL_CALL, new ToolExecuteNode());
    }

    private void initEdges() {
        edges.put(Context.State.INTENT_DETECT, context -> {
            if ("new_task".equals(context.getIntent())) {
                return Context.State.PLANNING;
            }
            if ("cancel".equals(context.getIntent())) {
                return Context.State.USER_CANCELED;
            }

            if (context.getResumState() == Context.State.INTENT_DETECT) {
                context.pauseToAskUser(Context.State.INTENT_DETECT);
                return Context.State.PAUSE_ASK_USER;
            }

            context.resume();
            return context.getResumState();
        });
        edges.put(Context.State.PLANNING, context -> Context.State.EXECUTE_STEP);
        edges.put(Context.State.EXECUTE_STEP, context -> {
            if (!context.getToolsToExecute().isEmpty()) {
                context.changeToToolCall(Context.State.EXECUTE_STEP);
                return context.getState();
            }

            TaskStep step = context.getPlan().get(context.getCurrentStepIndex());
            if ("ask_user".equals(step.getStatus())) {
                context.pauseToAskUser(Context.State.EXECUTE_STEP);
                return Context.State.PAUSE_ASK_USER;
            }
            if ("done".equals(step.getStatus())) {
                context.incrementStepIndex();
                if (context.getCurrentStepIndex() == context.getPlan().size() - 1) {
                    return Context.State.DONE_GOAL;
                }
            }

            return Context.State.EXECUTE_STEP;
        });
        edges.put(Context.State.TOOL_CALL, context -> {
            context.resume();
            return context.getState();
        });
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

            Context.State next = edges.get(context.getState()).apply(context);
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
