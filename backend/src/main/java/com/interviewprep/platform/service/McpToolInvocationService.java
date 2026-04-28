package com.interviewprep.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewprep.platform.dto.mcp.McpToolDtos.McpInvocationResponse;
import com.interviewprep.platform.dto.mcp.McpToolDtos.McpToolResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class McpToolInvocationService {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}/]+)}");

    private final McpToolCatalogService mcpToolCatalogService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Value("${app.mcp.workspace-root}")
    private String workspaceRoot;

    public McpToolInvocationService(McpToolCatalogService mcpToolCatalogService, ObjectMapper objectMapper) {
        this.mcpToolCatalogService = mcpToolCatalogService;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    public McpInvocationResponse invoke(
            String toolName,
            String actionName,
            Map<String, Object> input,
            String authorizationHeader
    ) {
        McpToolResponse tool = mcpToolCatalogService.getTool(toolName);
        Map<String, Object> action = tool.actions().stream()
                .filter(item -> actionName.equals(item.get("name")))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MCP action not found"));

        String method = String.valueOf(action.getOrDefault("method", "GET"));
        String path = String.valueOf(action.getOrDefault("path", "/"));
        String contentType = action.get("contentType") == null ? null : String.valueOf(action.get("contentType"));
        Map<String, Object> workingInput = new LinkedHashMap<>(input == null ? Map.of() : input);
        String resolvedPath = resolvePath(path, workingInput);
        String targetUrl = buildUrl(tool.baseUrl(), resolvedPath, workingInput, method);

        var requestSpec = webClient.method(HttpMethod.valueOf(method.toUpperCase()))
                .uri(targetUrl);

        if (StringUtils.hasText(tool.auth()) && tool.auth().toLowerCase().contains("bearer")
                && StringUtils.hasText(authorizationHeader)) {
            requestSpec.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        if (MediaType.MULTIPART_FORM_DATA_VALUE.equalsIgnoreCase(contentType)) {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            appendMultipart(builder, workingInput);
            requestSpec.contentType(MediaType.MULTIPART_FORM_DATA);
            return exchange(toolName, actionName, requestSpec.body(BodyInserters.fromMultipartData(builder.build())));
        }

        if (!HttpMethod.GET.matches(method) && !HttpMethod.DELETE.matches(method)) {
            requestSpec.contentType(MediaType.APPLICATION_JSON);
            return exchange(toolName, actionName, requestSpec.bodyValue(workingInput));
        }

        return exchange(toolName, actionName, requestSpec);
    }

    private McpInvocationResponse exchange(String toolName, String actionName, WebClient.RequestHeadersSpec<?> requestSpec) {
        ExchangeResult result = requestSpec.exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new ExchangeResult(response.statusCode().value(), body)))
                .block();

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "MCP invocation returned no response");
        }
        if (result.status() >= 400) {
            throw new ResponseStatusException(HttpStatus.valueOf(result.status()), extractErrorMessage(result.body()));
        }

        return new McpInvocationResponse(toolName, actionName, result.status(), parseResponseBody(result.body()));
    }

    private String resolvePath(String path, Map<String, Object> input) {
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(path);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variable = matcher.group(1);
            Object value = input.remove(variable);
            if (value == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing MCP path variable: " + variable);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String buildUrl(String baseUrl, String resolvedPath, Map<String, Object> input, String method) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(resolvedPath);
        if (HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            input.forEach((key, value) -> appendQueryParam(builder, key, value));
        }
        return builder.build(true).toUriString();
    }

    private void appendQueryParam(UriComponentsBuilder builder, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> builder.queryParam(key, item));
            return;
        }
        builder.queryParam(key, value);
    }

    private void appendMultipart(MultipartBodyBuilder builder, Map<String, Object> input) {
        input.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if ("file".equals(key)) {
                builder.part(key, new FileSystemResource(resolveWorkspaceFile(String.valueOf(value))));
                return;
            }
            if (value instanceof Collection<?> collection) {
                collection.forEach(item -> builder.part(key, String.valueOf(item)));
                return;
            }
            builder.part(key, String.valueOf(value));
        });
    }

    private Path resolveWorkspaceFile(String rawPath) {
        Path root = resolveWorkspaceRoot();
        Path candidate = Path.of(rawPath);
        Path resolved = candidate.isAbsolute() ? candidate.normalize() : root.resolve(candidate).normalize();
        if (!resolved.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "MCP file access is restricted to the configured workspace");
        }
        if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MCP file input not found: " + rawPath);
        }
        return resolved;
    }

    private Path resolveWorkspaceRoot() {
        Path configured = Path.of(workspaceRoot);
        return configured.isAbsolute()
                ? configured.normalize()
                : Path.of("").toAbsolutePath().resolve(configured).normalize();
    }

    private Object parseResponseBody(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            return objectMapper.readValue(body, Object.class);
        } catch (IOException exception) {
            return body;
        }
    }

    private String extractErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return "MCP invocation failed";
        }
        try {
            Object parsed = objectMapper.readValue(body, Object.class);
            return objectMapper.writeValueAsString(parsed);
        } catch (JsonProcessingException exception) {
            return body;
        }
    }

    private record ExchangeResult(int status, String body) {
    }
}
