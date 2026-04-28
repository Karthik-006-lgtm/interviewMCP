package com.interviewprep.platform.dto.company;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public final class CompanyDtos {

    private CompanyDtos() {
    }

    public record CompanyMatchRequest(
            @NotEmpty List<String> selectedRoles
    ) {
    }

    public record CompanyResponse(
            Long id,
            String name,
            String website,
            String hrContact,
            String hiringManager,
            String ownerName,
            Integer employeeCount,
            String companyHistory,
            String culture,
            List<String> supportedRoles,
            List<String> interviewFocusAreas,
            String whyUserMatches,
            double matchScore
    ) {
    }
}
