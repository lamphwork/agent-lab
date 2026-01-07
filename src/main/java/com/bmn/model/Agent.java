package com.bmn.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Agent {

    private String id;
    private String name;
    private String instructions;
}
