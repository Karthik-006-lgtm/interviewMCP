package com.interviewprep.platform.controller;

import com.interviewprep.platform.dto.resume.ResumeDtos.ResumeAnalysisResponse;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.service.AuthService;
import com.interviewprep.platform.service.ResumeService;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final AuthService authService;

    public ResumeController(ResumeService resumeService, AuthService authService) {
        this.resumeService = resumeService;
        this.authService = authService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeAnalysisResponse uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        User user = authService.loadUser(authentication.getName());
        return resumeService.uploadResume(user.getId(), file);
    }

    @GetMapping("/latest")
    public ResumeAnalysisResponse getLatestResume(Authentication authentication) {
        User user = authService.loadUser(authentication.getName());
        return resumeService.getLatestResume(user.getId());
    }
}
