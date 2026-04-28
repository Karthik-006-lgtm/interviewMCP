package com.interviewprep.platform.dto.resume;

import java.time.Instant;
import java.util.List;

public final class ResumeDtos {

    private ResumeDtos() {
    }

    public record ResumeAnalysisResponse(
            Long id,
            String originalFileName,
            String candidateName,
            List<String> contactInfo,
            String summary,
            List<String> strengths,
            List<String> weaknesses,
            List<String> extractedSkills,
            List<String> education,
            List<String> experience,
            List<String> projects,
            List<String> certifications,
            List<String> missingSkills,
            List<String> strengthIndicators,
            List<String> weaknessIndicators,
            List<String> improvementRoadmap,
            List<String> learningSuggestions,
            String mentorGuidance,
            List<String> recommendedRoles,
            double readinessScore,
            Instant uploadedAt
    ) {
    }
}
