package com.interviewprep.platform.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record AdminDashboardResponse(
            long totalUsers,
            long totalResumes,
            long totalCompanies,
            long totalInterviews,
            long totalReports,
            long totalRoles,
            long revokedTokenCount,
            List<AdminUserSummary> recentUsers,
            List<AdminResumeSummary> recentResumes,
            List<AdminInterviewSummary> recentInterviews,
            List<String> availableMcpTools
    ) {
    }

    public record AdminUserSummary(
            Long id,
            String fullName,
            String email,
            List<String> roles,
            Instant createdAt
    ) {
    }

    public record AdminResumeSummary(
            Long id,
            String candidateName,
            String userEmail,
            String originalFileName,
            List<String> recommendedRoles,
            Instant uploadedAt
    ) {
    }

    public record AdminInterviewSummary(
            Long sessionId,
            String userEmail,
            List<String> selectedRoles,
            BigDecimal overallScore,
            Instant createdAt
    ) {
    }
}
