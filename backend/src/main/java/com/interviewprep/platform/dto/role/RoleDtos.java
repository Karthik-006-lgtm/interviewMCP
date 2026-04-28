package com.interviewprep.platform.dto.role;

import java.util.List;

public final class RoleDtos {

    private RoleDtos() {
    }

    public record RoleResponse(
            Long id,
            String name,
            String summary,
            List<String> coreSkills,
            List<String> interviewFocusAreas
    ) {
    }
}
