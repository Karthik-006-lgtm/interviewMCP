package com.interviewprep.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewprep.platform.dto.mcp.McpToolDtos.McpToolResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class McpToolCatalogService {

    private static final Logger log = LoggerFactory.getLogger(McpToolCatalogService.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    @Value("${app.mcp.tools-dir}")
    private String toolsDir;

    public McpToolCatalogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<McpToolResponse> listTools() {
        Path root = resolveToolsDir();
        if (!Files.exists(root)) {
            log.warn("MCP tools directory {} does not exist", root);
            return List.of();
        }

        try (var directories = Files.list(root)) {
            return directories
                    .filter(Files::isDirectory)
                    .map(this::readManifestOrNull)
                    .filter(java.util.Objects::nonNull)
                    .sorted(Comparator.comparing(McpToolResponse::name))
                    .toList();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read MCP tool catalog");
        }
    }

    public McpToolResponse getTool(String toolName) {
        Path manifestPath = resolveToolsDir().resolve(toolName).resolve("tool.json");
        if (!Files.exists(manifestPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MCP tool not found");
        }
        return readManifest(manifestPath);
    }

    private McpToolResponse readManifestOrNull(Path directory) {
        Path manifestPath = directory.resolve("tool.json");
        if (!Files.exists(manifestPath)) {
            return null;
        }
        return readManifest(manifestPath);
    }

    private McpToolResponse readManifest(Path manifestPath) {
        try {
            Map<String, Object> manifest = objectMapper.readValue(manifestPath.toFile(), MAP_TYPE);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actions = (List<Map<String, Object>>) manifest.getOrDefault("actions", List.of());
            return new McpToolResponse(
                    String.valueOf(manifest.getOrDefault("name", manifestPath.getParent().getFileName().toString())),
                    String.valueOf(manifest.getOrDefault("description", "")),
                    String.valueOf(manifest.getOrDefault("transport", "http")),
                    manifest.get("baseUrl") == null ? null : String.valueOf(manifest.get("baseUrl")),
                    manifest.get("auth") == null ? null : String.valueOf(manifest.get("auth")),
                    actions
            );
        } catch (IOException exception) {
            log.error("Failed to read MCP tool manifest {}", manifestPath, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read MCP tool manifest");
        }
    }

    private Path resolveToolsDir() {
        Path configured = Path.of(toolsDir);
        return configured.isAbsolute() ? configured.normalize() : Path.of("").toAbsolutePath().resolve(configured).normalize();
    }
}
