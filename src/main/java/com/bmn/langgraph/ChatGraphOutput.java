package com.bmn.langgraph;

import lombok.*;

@Getter
@Setter(AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatGraphOutput {

    private String text;
    private String hint;
}
