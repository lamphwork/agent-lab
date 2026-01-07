package com.bmn.langgraph.node;

import com.bmn.langgraph.ChatGraphOutput;
import com.bmn.langgraph.GraphCallback;
import com.bmn.langgraph.context.ChatContext;
import com.bmn.model.Message;
import com.bmn.persistence.MessageRepository;
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
public class IndentDetectNode implements ProcessNode<ChatContext, ChatGraphOutput> {

    private final ChatClient llm;
    private final MessageRepository messageRepository;


    public record IndentDetectResult(String intent, String reason) {

    }

    @Override
    public String execute(ChatContext state, GraphCallback<ChatGraphOutput> callback) {
        List<Message> lastestMessages = messageRepository.getMessages(state.getId(), 5);
        String summary = lastestMessages.stream()
                .map(message -> {
                    String sender = StringUtils.defaultIfBlank(message.getSendBy(), "User");
                    return sender + ": " + message.getText();
                }).collect(Collectors.joining("\n"));

        IndentDetectResult result = llm.prompt()
                .system(systemPrompt)
                .user(userPrompt.formatted(summary, lastestMessages.getLast().getText()))
                .call()
                .entity(IndentDetectResult.class);
        log.info("indent: {}", result);
        if (result == null) {
            throw new RuntimeException("Intent detect result is null");
        }

        state.setIntent(result.intent);
        callback.call(ChatGraphOutput.builder().hint(result.intent).build());

        return "";
    }

    private static final String systemPrompt = """
            You are an intent detector for an AI orchestration system.
            
            Your job is to decide whether the user's message requires:
            - CHAT: normal conversation, explanation, analysis, or opinion
            - TASK: planning and executing concrete work
            - CLARIFY: not enough information to decide
            - CANCEL: cancel or stop the current task
            
            Decision rules:
            - If the user asks for knowledge, explanation, reasoning, comparison, or discussion → CHAT
            - If the user asks to create, build, implement, generate, write, run, or perform a concrete action → TASK
            - If the request is vague or ambiguous → CLARIFY
            - If the user asks to stop, cancel, or abort → CANCEL
            
            Focus only on deciding whether planning and execution are needed.
            Do NOT explain the content.
            Do NOT create plans or solutions.
            
            Return JSON only, strictly following this schema:
            {
              "intent": "CHAT | TASK | CLARIFY | CANCEL",
              "confidence": number,
              "reason": string
            }
            
            """;

    private static final String userPrompt = """
            Conversation (most recent first):
            %s
            
            User message:
            ""\"
            %s
            ""\"
            """;
}
