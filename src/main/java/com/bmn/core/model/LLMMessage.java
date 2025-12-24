package com.bmn.core.model;

import java.io.Serializable;

public record LLMMessage(String role, String content, ToolInfo toolInfo) implements Serializable {

    public static LLMMessage systemMessage(String content) {
        return  new LLMMessage("system", content, null);
    }

    public static LLMMessage userMessage(String content, ToolInfo toolInfo) {
        return  new LLMMessage("user", content, toolInfo);
    }

    public static LLMMessage assistantMessage(String content, ToolInfo toolInfo) {
        return  new LLMMessage("assistant", content, toolInfo);
    }
}
