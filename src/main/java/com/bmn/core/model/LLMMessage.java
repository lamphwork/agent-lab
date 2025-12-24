package com.bmn.core.model;

import java.io.Serializable;

public record LLMMessage(String role, String content, ToolInfo toolInfo) implements Serializable {
}
