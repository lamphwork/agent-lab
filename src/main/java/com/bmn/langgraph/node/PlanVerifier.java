package com.bmn.langgraph.node;

import com.bmn.langgraph.ChatGraphOutput;
import com.bmn.langgraph.GraphCallback;
import com.bmn.langgraph.context.ChatContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanVerifier implements ProcessNode<ChatContext, ChatGraphOutput> {

    private final ChatClient llmClient;

    public record VerifyResult(boolean isDone, String reason) {
    }

    @Override
    public String execute(ChatContext state, GraphCallback<ChatGraphOutput> callback) {
        String executionHistory = state.getPlan().stream()
                .map(step -> String.format("Step: %s\nStatus: %s\nOutput: %s",
                        step.getTitle(), step.getStatus(), step.getOutput()))
                .collect(Collectors.joining("\n---\n"));

        VerifyResult result = llmClient.prompt()
                .system(systemPrompt)
                .user(userPrompt.formatted(state.getGoal(), executionHistory))
                .call()
                .entity(VerifyResult.class);

        log.info("Verification result: {}", result);

        if (result != null && result.isDone()) {
            state.setGoalDone(true); // Mark goal as done to stop the loop
            callback.call(ChatGraphOutput.builder()
                    .text("Task completed: " + result.reason())
                    .build());
        } else {
            // If not done, we might need replanning or just stop?
            // Current ChatGraph logic loops back to planning if not done.
            // We should probably rely on the planner to see the history.
            log.warn("Goal not reached: {}", result != null ? result.reason() : "Unknown");
        }

        return null;
    }

    private static final String systemPrompt = """
            You are the Verifier in an AI orchestration system.

            Your job is to verify if the user's goal has been accomplished based on the execution history.

            Return a JSON object:
            {
                "isDone": boolean,
                "reason": string
            }

            - isDone: true if the goal is fully satisfied. false if more steps are needed or if it failed.
            - reason: brief explanation.
            """;

    private static final String userPrompt = """
            User Goal:
            %s

            Execution History:
            %s
            """;
}
