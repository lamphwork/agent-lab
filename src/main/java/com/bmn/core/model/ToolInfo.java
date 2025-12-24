package com.bmn.core.model;

import com.fasterxml.jackson.databind.JsonNode;

public record ToolInfo(String id, String name, JsonNode args) {
}
