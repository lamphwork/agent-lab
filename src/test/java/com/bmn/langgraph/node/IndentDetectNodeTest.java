package com.bmn.langgraph.node;

import com.bmn.langgraph.context.ChatContext;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IndentDetectNodeTest {

    @Autowired
    private ChatClient client;

    @Autowired
    private com.bmn.persistence.MessageRepository messageRepository;

    @Test
    public void testIndentDetectNode() {
        ChatContext context = ChatContext.builder().id("test").build();
        IndentDetectNode indentDetectNode = new IndentDetectNode(client, messageRepository);
        indentDetectNode.execute(context, data -> {
        });
    }
}