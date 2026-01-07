package com.bmn.langgraph.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
public class Step {

    public enum Status {
        TODO, PROCESSING, DONE, ERROR
    }

    private String id;
    private String title;
    private String description;
    private Status status;
    private Map<String, Object> metadata;
    private String output;
}
