package com.interviewprep.platform.dto.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ReportDtos {

    private ReportDtos() {
    }

    public record PracticeReportResponse(
            Long id,
            Long sessionId,
            String title,
            String executiveSummary,
            List<String> weakAreas,
            List<String> recommendedActions,
            String improvementAreas,
            String nextSteps,
            String progressSummary,
            List<String> weeklyImprovementPlan,
            List<String> practiceTasks,
            List<String> targetedQuestions,
            BigDecimal overallScore,
            Instant createdAt
    ) {
    }

    public record DashboardMetricsResponse(
            int resumeCount,
            int companyMatchCount,
            int completedAnswers,
            BigDecimal latestScore,
            BigDecimal averageScore,
            BigDecimal progressDelta,
            List<String> topWeakAreas,
            List<PracticeReportResponse> recentReports
    ) {
    }
}
