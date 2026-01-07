package com.bmn.langgraph.node;

import com.bmn.langgraph.ChatGraphOutput;
import com.bmn.langgraph.GraphCallback;
import com.bmn.langgraph.context.ChatContext;
import com.bmn.langgraph.context.Step;
import com.bmn.model.Message;
import com.bmn.persistence.MessageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanningNode implements ProcessNode<ChatContext, ChatGraphOutput> {

    private final ChatClient llm;
    private final MessageRepository messageRepository;

    @Data
    public static class LLmResponse {
        private List<Step> steps;
        private String goals;
    }

    @Override
    public String execute(ChatContext state, GraphCallback<ChatGraphOutput> callback) {
        List<Message> latestMessages = messageRepository.getMessages(state.getId(), 5);
        String summary = latestMessages.stream()
                .map(message -> {
                    String sender = StringUtils.defaultIfBlank(message.getSendBy(), "User");
                    return sender + ": " + message.getText();
                }).collect(Collectors.joining("\n"));


        LLmResponse lLmResponse = llm.prompt()
                .system(systemPrompt)
                .user(userPrompt.formatted(summary, latestMessages.getLast().getText()))
                .call()
                .entity(LLmResponse.class);

        if (lLmResponse == null) {
            throw new RuntimeException("planning failed");
        }

        state.setPlan(lLmResponse.getSteps());
        state.setCurrentStepIndex(0);
        state.setGoal(lLmResponse.getGoals());
        state.setGoalDone(false);

        log.info("planning finished {}", lLmResponse);
        return null;
    }

    public static final String systemPrompt = """
            You are the Planner in an AI orchestration system.
            
            Your responsibility is to analyze the user's request and produce an execution plan.
            
            You must:
            - Identify the user's final goal
            - Decide whether the task can be executed
            - Break the task into ordered, concrete, and executable steps
            - Decide which tool (if any) is required for each step
            - Define clear completion criteria
            
            You must NOT:
            - Execute any steps
            - Respond conversationally to the user
            - Ask the user questions (use CLARIFY instead if needed)
            - Call tools or produce final results
            - Include explanations, opinions, or commentary
            
            Planning rules:
            - Each step must be specific, testable, and have an observable outcome
            - Steps must be minimal and strictly necessary
            - Prefer deterministic actions over open-ended reasoning
            - If required information is missing, return a CLARIFY plan
            - If the task is impossible or unsafe, return FAILED with a reason
            
            Output rules:
            - Output MUST be valid JSON
            - Do NOT include any text outside JSON
            
            """;

    public static final String userPrompt = """
            Conversation summary:
            ""\"
            %s
            ""\"
            
            User request:
            ""\"
            %s
            ""\"
            
            Constraints:
            - Max steps: 30
            """;
}
