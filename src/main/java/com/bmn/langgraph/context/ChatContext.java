package com.bmn.langgraph.context;

import com.bmn.model.Message;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatContext extends State {

    private String id;
    private String intent;

    private Message lastUserMessage;

    private String goal;
    private boolean goalDone;
    private List<Step> plan;
    private int currentStepIndex;

    private boolean waitingUser;


    // human in the loop
    public void waitUser() {
        setWaitingUser(true);
    }

    public void resumChat() {
        setWaitingUser(false);
    }

}
