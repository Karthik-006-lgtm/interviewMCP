package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.role.RoleDtos.RoleResponse;
import com.interviewprep.platform.entity.RoleProfile;
import com.interviewprep.platform.repository.RoleProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleProfileRepository roleProfileRepository;
    private final JsonStorageService jsonStorageService;

    public RoleService(RoleProfileRepository roleProfileRepository, JsonStorageService jsonStorageService) {
        this.roleProfileRepository = roleProfileRepository;
        this.jsonStorageService = jsonStorageService;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleProfileRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private RoleResponse toResponse(RoleProfile roleProfile) {
        return new RoleResponse(
                roleProfile.getId(),
                roleProfile.getName(),
                roleProfile.getSummary(),
                jsonStorageService.readStringList(roleProfile.getCoreSkills()),
                jsonStorageService.readStringList(roleProfile.getInterviewFocusAreas())
        );
    }
}
