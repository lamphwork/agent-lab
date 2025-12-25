package com.bmn.core.port;

import com.bmn.core.model.LLMMessage;

import java.util.List;

public interface LLMClient {

    List<LLMMessage> generate(List<LLMMessage> messages);

    float[] embedding(String content);
}
