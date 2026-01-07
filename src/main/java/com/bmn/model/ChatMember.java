package com.bmn.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatMember {

    public enum MemberType {
        PERSON, AGENT
    }

    private String id;
    private MemberType memberType;
    private String refId;
}
