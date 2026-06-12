package com.interviewprep.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewRecordingService {

    private final Path recordingStorageDir;

    public InterviewRecordingService(
            @Value("${app.storage.recording-dir:./uploads/recordings}") String recordingDir
    ) {
        this.recordingStorageDir = Paths.get(recordingDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(recordingStorageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create recording storage directory", e);
        }
    }

    public String saveRecording(Long userId, MultipartFile video) throws IOException {
        // Create user-specific directory
        Path userDir = recordingStorageDir.resolve(String.valueOf(userId));
        Files.createDirectories(userDir);

        // Generate unique filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = video.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".webm";
        
        String fileName = "interview_" + timestamp + extension;
        Path targetPath = userDir.resolve(fileName);

        // Save file
        Files.copy(video.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public List<Map<String, Object>> listUserRecordings(Long userId) {
        Path userDir = recordingStorageDir.resolve(String.valueOf(userId));
        
        if (!Files.exists(userDir)) {
            return Collections.emptyList();
        }

        try {
            return Files.list(userDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".webm") || 
                                    path.getFileName().toString().endsWith(".mp4"))
                    .map(path -> {
                        Map<String, Object> recording = new HashMap<>();
                        try {
                            File file = path.toFile();
                            recording.put("fileName", file.getName());
                            recording.put("fileSize", file.length());
                            recording.put("createdAt", file.lastModified());
                            recording.put("url", "/api/interviews/recordings/view/" + file.getName());
                        } catch (Exception e) {
                            // Skip this file
                        }
                        return recording;
                    })
                    .filter(map -> !map.isEmpty())
                    .sorted((a, b) -> Long.compare((Long) b.get("createdAt"), (Long) a.get("createdAt")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public boolean deleteRecording(Long userId, String fileName) {
        Path userDir = recordingStorageDir.resolve(String.valueOf(userId));
        Path filePath = userDir.resolve(fileName);

        try {
            if (Files.exists(filePath) && filePath.startsWith(userDir)) {
                Files.delete(filePath);
                return true;
            }
        } catch (IOException e) {
            // Log error
        }
        return false;
    }

    public Path getRecordingPath(Long userId, String fileName) {
        Path userDir = recordingStorageDir.resolve(String.valueOf(userId));
        Path filePath = userDir.resolve(fileName);
        
        if (Files.exists(filePath) && filePath.startsWith(userDir)) {
            return filePath;
        }
        return null;
    }
}
