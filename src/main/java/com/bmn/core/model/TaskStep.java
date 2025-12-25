package com.bmn.core.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TaskStep {

    private String id;
    private String title;
    private String desc;
    private ObjectNode metadata;
    private String status;
}
