package com.interviewprep.platform.dto.recommendation;

import java.util.List;

public final class RecommendationDtos {

    private RecommendationDtos() {
    }

    public record RecommendationProfileResponse(
            Long resumeId,
            List<String> strengths,
            List<String> weaknesses,
            List<String> recommendedRoles,
            List<String> missingSkills,
            List<String> improvementRoadmap,
            List<String> learningSuggestions,
            String mentorGuidance,
            int matchingCompanyCount
    ) {
    }
}
