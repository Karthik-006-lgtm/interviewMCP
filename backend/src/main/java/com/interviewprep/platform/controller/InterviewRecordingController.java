package com.interviewprep.platform.controller;

import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.service.AuthService;
import com.interviewprep.platform.service.InterviewRecordingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews/recordings")
public class InterviewRecordingController {

    private final InterviewRecordingService recordingService;
    private final AuthService authService;

    public InterviewRecordingController(
            InterviewRecordingService recordingService,
            AuthService authService
    ) {
        this.recordingService = recordingService;
        this.authService = authService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadRecording(
            Authentication authentication,
            @RequestParam("video") MultipartFile video
    ) throws IOException {
        User user = authService.loadUser(authentication.getName());
        
        String fileName = recordingService.saveRecording(user.getId(), video);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("fileName", fileName);
        response.put("message", "Recording saved successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listRecordings(
            Authentication authentication
    ) {
        User user = authService.loadUser(authentication.getName());
        List<Map<String, Object>> recordings = recordingService.listUserRecordings(user.getId());
        return ResponseEntity.ok(recordings);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Map<String, String>> deleteRecording(
            Authentication authentication,
            @PathVariable String fileName
    ) {
        User user = authService.loadUser(authentication.getName());
        boolean deleted = recordingService.deleteRecording(user.getId(), fileName);
        
        Map<String, String> response = new HashMap<>();
        if (deleted) {
            response.put("message", "Recording deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Recording not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> viewRecording(
            Authentication authentication,
            @PathVariable String fileName
    ) {
        User user = authService.loadUser(authentication.getName());
        java.nio.file.Path filePath = recordingService.getRecordingPath(user.getId(), fileName);
        
        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(filePath);
        
        return ResponseEntity.ok()
                .header("Content-Type", "video/webm")
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
