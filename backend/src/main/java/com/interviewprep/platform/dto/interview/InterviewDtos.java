package com.interviewprep.platform.dto.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class InterviewDtos {

    private InterviewDtos() {
    }

    public record InterviewSessionRequest(
            Long resumeId,
            Long companyId,
            @NotEmpty List<String> selectedRoles,
            @NotBlank @Size(max = 160) String personalityProfile,
            String technicalFocus,
            @Size(max = 80) String interviewerTone,
            @Size(max = 40) String coachingIntensity,
            Boolean liveCoachingEnabled,
            Boolean adaptiveDifficultyEnabled,
            @Size(max = 60) String realityMode,
            Boolean cameraEnabled
    ) {
    }

    public record InterviewQuestionResponse(
            Long id,
            String prompt,
            String category,
            String difficulty,
            String interviewerCue,
            Integer timePressureSeconds
    ) {
    }

    public record InterviewSessionResponse(
            Long sessionId,
            List<String> selectedRoles,
            String personalityProfile,
            String technicalSkills,
            String targetCompanyName,
            String targetCompanyWebsite,
            String interviewerTone,
            String coachingIntensity,
            boolean liveCoachingEnabled,
            boolean adaptiveDifficultyEnabled,
            String realityMode,
            boolean cameraEnabled,
            String currentDifficultyLevel,
            List<InterviewQuestionResponse> questions,
            BigDecimal overallScore,
            Instant createdAt
    ) {
    }

    public record AnswerSubmissionRequest(
            @Size(max = 8000) String answerText,
            String audioReference,
            @Size(max = 200) String visualPresenceSignal,
            @Size(max = 200) String visualEyeContactSignal,
            @Size(max = 200) String visualConfidenceSignal,
            @Size(max = 200) String visualNervousnessSignal
    ) {
    }

    public record LiveCoachingRequest(
            @Size(max = 8000) String answerDraft,
            Boolean silenceDetected
    ) {
    }

    public record LiveCoachingResponse(
            List<String> hints,
            List<String> suggestedKeywords,
            String continuationPrompt,
            String structureReminder
    ) {
    }

    public record AudioProcessingResponse(
            String audioReference,
            String transcript,
            BigDecimal confidenceScore,
            BigDecimal fluencyScore,
            BigDecimal clarityScore,
            String emotionSignal,
            String toneFeedback,
            String pronunciationFeedback,
            String fluencyFeedback,
            Long durationMs,
            Instant createdAt
    ) {
    }

    public record AnswerEvaluationResponse(
            Long answerId,
            BigDecimal correctnessScore,
            BigDecimal confidenceScore,
            BigDecimal relevanceScore,
            BigDecimal clarityScore,
            BigDecimal completenessScore,
            BigDecimal structureScore,
            BigDecimal impactScore,
            BigDecimal hesitationScore,
            int fillerWordCount,
            String emotionSignal,
            String grammarFeedback,
            String vocabularyFeedback,
            String mentorSuggestions,
            String polishedAnswer,
            String pronunciationFeedback,
            String toneFeedback,
            String fluencyFeedback,
            List<String> liveCoachingHints,
            List<String> weaknessSignals,
            List<String> weeklyImprovementPlan,
            List<String> practiceTasks,
            List<String> targetedQuestions,
            String adaptiveDifficultyNote,
            String nextDifficultyLevel,
            Instant createdAt
    ) {
    }
}
