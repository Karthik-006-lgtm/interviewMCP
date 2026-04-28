package com.interviewprep.platform.dto.mcp;

import java.util.List;
import java.util.Map;

public final class McpToolDtos {

    private McpToolDtos() {
    }

    public record McpToolResponse(
            String name,
            String description,
            String transport,
            String baseUrl,
            String auth,
            List<Map<String, Object>> actions
    ) {
    }

    public record McpInvocationRequest(
            Map<String, Object> input
    ) {
    }

    public record McpInvocationResponse(
            String toolName,
            String actionName,
            int status,
            Object data
    ) {
    }
}
