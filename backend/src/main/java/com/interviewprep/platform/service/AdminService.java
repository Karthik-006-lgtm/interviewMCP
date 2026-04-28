package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.admin.AdminDtos.AdminDashboardResponse;
import com.interviewprep.platform.dto.admin.AdminDtos.AdminInterviewSummary;
import com.interviewprep.platform.dto.admin.AdminDtos.AdminResumeSummary;
import com.interviewprep.platform.dto.admin.AdminDtos.AdminUserSummary;
import com.interviewprep.platform.repository.CompanyRepository;
import com.interviewprep.platform.repository.InterviewSessionRepository;
import com.interviewprep.platform.repository.PracticeReportRepository;
import com.interviewprep.platform.repository.ResumeProfileRepository;
import com.interviewprep.platform.repository.RevokedTokenRepository;
import com.interviewprep.platform.repository.RoleProfileRepository;
import com.interviewprep.platform.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ResumeProfileRepository resumeProfileRepository;
    private final CompanyRepository companyRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final PracticeReportRepository practiceReportRepository;
    private final RoleProfileRepository roleProfileRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JsonStorageService jsonStorageService;
    private final McpToolCatalogService mcpToolCatalogService;

    public AdminService(
            UserRepository userRepository,
            ResumeProfileRepository resumeProfileRepository,
            CompanyRepository companyRepository,
            InterviewSessionRepository interviewSessionRepository,
            PracticeReportRepository practiceReportRepository,
            RoleProfileRepository roleProfileRepository,
            RevokedTokenRepository revokedTokenRepository,
            JsonStorageService jsonStorageService,
            McpToolCatalogService mcpToolCatalogService
    ) {
        this.userRepository = userRepository;
        this.resumeProfileRepository = resumeProfileRepository;
        this.companyRepository = companyRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.practiceReportRepository = practiceReportRepository;
        this.roleProfileRepository = roleProfileRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.jsonStorageService = jsonStorageService;
        this.mcpToolCatalogService = mcpToolCatalogService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse getDashboard() {
        List<AdminUserSummary> recentUsers = userRepository.findAll(PageRequest.of(
                        0,
                        6,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(user -> new AdminUserSummary(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRoles().stream().map(Enum::name).sorted().toList(),
                        user.getCreatedAt()))
                .toList();

        List<AdminResumeSummary> recentResumes = resumeProfileRepository.findAll(PageRequest.of(
                        0,
                        6,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(resume -> new AdminResumeSummary(
                        resume.getId(),
                        resume.getCandidateName(),
                        resume.getUser().getEmail(),
                        resume.getOriginalFileName(),
                        jsonStorageService.readStringList(resume.getRecommendedRoles()),
                        resume.getCreatedAt()))
                .toList();

        List<AdminInterviewSummary> recentInterviews = interviewSessionRepository.findAll(PageRequest.of(
                        0,
                        6,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(session -> new AdminInterviewSummary(
                        session.getId(),
                        session.getUser().getEmail(),
                        jsonStorageService.readStringList(session.getSelectedRoles()),
                        session.getOverallScore(),
                        session.getCreatedAt()))
                .toList();

        List<String> availableMcpTools = mcpToolCatalogService.listTools().stream()
                .map(tool -> tool.name())
                .sorted(Comparator.naturalOrder())
                .toList();

        return new AdminDashboardResponse(
                userRepository.count(),
                resumeProfileRepository.count(),
                companyRepository.count(),
                interviewSessionRepository.count(),
                practiceReportRepository.count(),
                roleProfileRepository.count(),
                revokedTokenRepository.count(),
                recentUsers,
                recentResumes,
                recentInterviews,
                availableMcpTools
        );
    }
}
