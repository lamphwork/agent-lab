package com.bmn.controller;

import com.bmn.langgraph.ChatGraph;
import com.bmn.langgraph.ChatGraphOutput;
import com.bmn.langgraph.context.ChatContext;
import com.bmn.model.Message;
import com.bmn.persistence.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatGraph chatGraph;
    private final MessageRepository messageRepository;
    private final Map<String, ChatContext> inMemContext = new ConcurrentHashMap<>();

    @PostMapping(produces = { MediaType.TEXT_EVENT_STREAM_VALUE })
    public Flux<ServerSentEvent<ChatGraphOutput>> chat(@RequestBody Message message) {
        messageRepository.pushMessage(message);
        return Flux.create(synchronousSink -> {
            ChatContext chatContext = inMemContext.get(message.getGroupId());
            if (chatContext == null) {
                chatContext = ChatContext.builder()
                        .id(message.getGroupId())
                        .build();
                inMemContext.put(message.getGroupId(), chatContext);
            }

            chatContext.resumChat();
            chatGraph.process(chatContext, data -> {
                if (StringUtils.isNotBlank(data.getText())) {
                    messageRepository.pushMessage(Message.builder().sendBy("AGENT").build());
                }
                synchronousSink.next(
                        ServerSentEvent.<ChatGraphOutput>builder()
                                .id(UUID.randomUUID().toString())
                                .data(data)
                                .event("hint")
                                .build());
            });
            synchronousSink.complete();
        });
    }
}
