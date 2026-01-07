package com.bmn.model;

import lombok.*;

import java.util.List;

@Getter
@Setter(AccessLevel.PROTECTED)
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    private String id;
    private String name;
    private List<ChatMember> members;

}
