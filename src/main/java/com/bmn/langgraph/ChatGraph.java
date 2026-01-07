package com.bmn.langgraph;

import com.bmn.langgraph.context.ChatContext;
import com.bmn.langgraph.context.Step;
import com.bmn.langgraph.node.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ChatGraph extends StateGraph<ChatContext, ChatGraphOutput> {

    public ChatGraph(final IndentDetectNode indentDetectNode,
                     final PlanningNode planningNode,
                     final Responder responder,
                     final ExecuteNode executeNode,
                     final PlanVerifier verifier) {
        super(Map.of(
                "indentDetect", indentDetectNode,
                "planning", planningNode,
                "responsor", responder,
                "executor", executeNode,
                "planVerifier", verifier
        ));
    }

    @Override
    String getNextNode(ChatContext context) {
        String currentNode = StringUtils.trim(context.getCurrentNode());
        if (StringUtils.isEmpty(currentNode)) {
            return "indentDetect";
        }

        return switch (currentNode) {
            case "indentDetect" -> {
                String intent = context.getIntent();
                if (context.isWaitingUser() || StringUtils.isBlank(intent)) {
                    context.resumChat();
                    yield "indentDetect";
                }

                if (List.of("CHAT", "CLARIFY").contains(intent)) {
                    yield "responsor";
                }

                if ("TASK".equals(intent)) {
                    yield "planning";
                }
                yield "responsor";
            }
            case "planning" -> "executor";
            case "executor" -> {
                List<Step> plan = context.getPlan();
                boolean allTaskDone = plan.stream().allMatch(step -> step.getStatus().equals(Step.Status.DONE));
                boolean hasTaskError = plan.stream().anyMatch(step -> step.getStatus().equals(Step.Status.ERROR));
                if (allTaskDone || hasTaskError) {
                    yield "planVerifier";
                }
                yield "executor";
            }
            case "responsor" -> "indentDetect";
            case "planVerifier" -> context.isGoalDone() ? "" : "planning";
            default -> null;
        };
    }

    @Override
    boolean shouldStop(ChatContext context) {
        return context.isWaitingUser() || context.isGoalDone();
    }
}
