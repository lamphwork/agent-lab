package com.bmn.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class Context {

    public enum State {
        INTENT_DETECT,
        GREETING,
        RESPONDER,
        PLANNING,
        EXECUTE_STEP,
        PAUSE_ASK_USER,
        DONE_GOAL,
        USER_CANCELED
    }

    private String id;
    private State state;
    private State resumState;

    // ===== History ======
    private List<LLMMessage> histories;

    // ===== Input =====
    private String userInput;
    private String lastUserReply;

    // ===== Intent =====
    private String intent; // greeting | ask | task

    // ===== Clarify =====
    private List<String> missingInfo;
    private String pendingQuestion; // câu hỏi đang chờ user trả lời

    // ===== Planning =====
    private List<TaskStep> plan;
    private int currentStepIndex;

    // ===== Execution =====
    private Map<String, Object> stepResult;

    // ===== Control =====
    private boolean userCanceled;

    public boolean shouldRun() {
        return state != State.DONE_GOAL
                && state != State.USER_CANCELED
                && state != State.PAUSE_ASK_USER;
    }

    public void resume() {
        this.state = this.resumState;
    }

    public void pauseToAskUser(State resumState) {
        this.state = State.PAUSE_ASK_USER;
        this.resumState = resumState;
    }

    public void putUserMessage(String userInput) {
        this.lastUserReply = userInput;
        this.histories.add(new LLMMessage("user", userInput, null));
    }

    public static Context newContext(String userInput) {
        return new Context(
                UUID.randomUUID().toString(),
                State.INTENT_DETECT,
                State.INTENT_DETECT,
                new LinkedList<>(),
                userInput,
                userInput,
                null,
                new LinkedList<>(),
                null,
                new LinkedList<>(),
                0,
                new LinkedHashMap<>(),
                false
        );
    }
}
