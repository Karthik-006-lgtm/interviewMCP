package com.interviewprep.platform.controller;

import com.interviewprep.platform.dto.interview.InterviewDtos.AnswerEvaluationResponse;
import com.interviewprep.platform.dto.interview.InterviewDtos.AudioProcessingResponse;
import com.interviewprep.platform.dto.interview.InterviewDtos.AnswerSubmissionRequest;
import com.interviewprep.platform.dto.interview.InterviewDtos.InterviewSessionRequest;
import com.interviewprep.platform.dto.interview.InterviewDtos.InterviewSessionResponse;
import com.interviewprep.platform.dto.interview.InterviewDtos.LiveCoachingRequest;
import com.interviewprep.platform.dto.interview.InterviewDtos.LiveCoachingResponse;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.service.AuthService;
import com.interviewprep.platform.service.InterviewService;
import com.interviewprep.platform.service.SpeechProcessingService;
import java.io.IOException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final SpeechProcessingService speechProcessingService;
    private final AuthService authService;

    public InterviewController(
            InterviewService interviewService,
            SpeechProcessingService speechProcessingService,
            AuthService authService
    ) {
        this.interviewService = interviewService;
        this.speechProcessingService = speechProcessingService;
        this.authService = authService;
    }

    @SuppressWarnings("null")
    @PostMapping("/sessions")
    public InterviewSessionResponse createSession(
            Authentication authentication,
            @Valid @RequestBody InterviewSessionRequest request
    ) {
        User user = authService.loadUser(authentication.getName());
        return interviewService.createSession(user.getId(), request);
    }

    @SuppressWarnings("null")
    @GetMapping("/sessions/{sessionId}")
    public InterviewSessionResponse getSession(
            Authentication authentication,
            @PathVariable Long sessionId
    ) {
        User user = authService.loadUser(authentication.getName());
        return interviewService.getSession(user.getId(), sessionId);
    }

    @SuppressWarnings("null")
    @PostMapping("/questions/{questionId}/answer")
    public AnswerEvaluationResponse submitAnswer(
            Authentication authentication,
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerSubmissionRequest request
    ) {
        User user = authService.loadUser(authentication.getName());
        return interviewService.submitAnswer(user.getId(), questionId, request);
    }

    @SuppressWarnings("null")
    @PostMapping("/questions/{questionId}/coach")
    public LiveCoachingResponse coachAnswer(
            Authentication authentication,
            @PathVariable Long questionId,
            @Valid @RequestBody LiveCoachingRequest request
    ) {
        User user = authService.loadUser(authentication.getName());
        return interviewService.coachAnswer(user.getId(), questionId, request);
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AudioProcessingResponse uploadAudio(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "transcriptHint", required = false) String transcriptHint,
            @RequestParam(value = "durationMs", required = false) Long durationMs
    ) throws IOException {
        User user = authService.loadUser(authentication.getName());
        return speechProcessingService.uploadAndAnalyzeAudio(user.getId(), file, transcriptHint, durationMs);
    }
}
