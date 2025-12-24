package com.bmn.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class AgentOutput {

    public enum OutputType {
        HINT, MESSAGE
    }

    private OutputType type;
    private String content;

    public static AgentOutput hintMessage(String content) {
        return new AgentOutput(OutputType.HINT, content);
    }

    public static AgentOutput textMessage(String content) {
        return new AgentOutput(OutputType.MESSAGE, content);
    }

}
