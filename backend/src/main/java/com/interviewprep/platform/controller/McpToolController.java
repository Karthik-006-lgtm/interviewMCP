package com.interviewprep.platform.controller;

import com.interviewprep.platform.dto.mcp.McpToolDtos.McpInvocationRequest;
import com.interviewprep.platform.dto.mcp.McpToolDtos.McpInvocationResponse;
import com.interviewprep.platform.dto.mcp.McpToolDtos.McpToolResponse;
import com.interviewprep.platform.service.McpToolCatalogService;
import com.interviewprep.platform.service.McpToolInvocationService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp/tools")
public class McpToolController {

    private final McpToolCatalogService mcpToolCatalogService;
    private final McpToolInvocationService mcpToolInvocationService;

    public McpToolController(
            McpToolCatalogService mcpToolCatalogService,
            McpToolInvocationService mcpToolInvocationService
    ) {
        this.mcpToolCatalogService = mcpToolCatalogService;
        this.mcpToolInvocationService = mcpToolInvocationService;
    }

    @GetMapping
    public List<McpToolResponse> listTools() {
        return mcpToolCatalogService.listTools();
    }

    @GetMapping("/{toolName}")
    public McpToolResponse getTool(@PathVariable String toolName) {
        return mcpToolCatalogService.getTool(toolName);
    }

    @PostMapping("/{toolName}/actions/{actionName}/invoke")
    public McpInvocationResponse invokeTool(
            @PathVariable String toolName,
            @PathVariable String actionName,
            @RequestBody(required = false) McpInvocationRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        return mcpToolInvocationService.invoke(
                toolName,
                actionName,
                request == null ? java.util.Map.of() : request.input(),
                authorizationHeader
        );
    }
}
