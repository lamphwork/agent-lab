package com.bmn.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter(AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    private String id;
    private String groupId;
    private String text;
    private Instant sendTime;
    private String sendBy;
}
