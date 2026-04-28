package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.company.CompanyDtos.CompanyResponse;
import com.interviewprep.platform.entity.Company;
import com.interviewprep.platform.entity.ResumeProfile;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.repository.CompanyRepository;
import com.interviewprep.platform.repository.ResumeProfileRepository;
import com.interviewprep.platform.repository.UserRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final ResumeProfileRepository resumeProfileRepository;
    private final UserRepository userRepository;
    private final JsonStorageService jsonStorageService;

    public CompanyService(
            CompanyRepository companyRepository,
            ResumeProfileRepository resumeProfileRepository,
            UserRepository userRepository,
            JsonStorageService jsonStorageService
    ) {
        this.companyRepository = companyRepository;
        this.resumeProfileRepository = resumeProfileRepository;
        this.userRepository = userRepository;
        this.jsonStorageService = jsonStorageService;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> matchCompanies(Long userId, List<String> selectedRoles) {
        User user = loadUser(userId);
        ResumeProfile latestResume = resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
        List<String> effectiveRoles = resolveRoles(selectedRoles, latestResume);
        if (effectiveRoles.isEmpty()) {
            return List.of();
        }

        List<String> normalizedRoles = effectiveRoles.stream()
                .map(role -> role.toLowerCase(Locale.ROOT).trim())
                .distinct()
                .toList();
        log.info("Matching companies for user {} against roles {}", userId, normalizedRoles);

        return companyRepository.findMatchingCompanies(normalizedRoles).stream()
                .map(company -> toResponse(company, latestResume, effectiveRoles))
                .sorted(Comparator.comparingDouble(CompanyResponse::matchScore).reversed().thenComparing(CompanyResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(Long userId, Long companyId) {
        User user = loadUser(userId);
        ResumeProfile latestResume = resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        return toResponse(company, latestResume, company.getSupportedRoles().stream().toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> searchCompanies(
            Long userId,
            List<String> selectedRoles,
            String query,
            Double minMatchScore,
            String companySize
    ) {
        double effectiveMinimum = minMatchScore == null ? 0.0 : minMatchScore;
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String normalizedCompanySize = companySize == null ? "all" : companySize.trim().toLowerCase(Locale.ROOT);

        return matchCompanies(userId, selectedRoles).stream()
                .filter(company -> company.matchScore() >= effectiveMinimum)
                .filter(company -> matchesQuery(company, normalizedQuery))
                .filter(company -> matchesCompanySize(company.employeeCount(), normalizedCompanySize))
                .toList();
    }

    private CompanyResponse toResponse(Company company, ResumeProfile resumeProfile, List<String> selectedRoles) {
        List<String> supportedRoles = company.getSupportedRoles().stream().sorted().toList();
        List<String> interviewFocusAreas = jsonStorageService.readStringList(company.getInterviewFocusAreas());
        double matchScore = computeMatchScore(company, resumeProfile, selectedRoles, interviewFocusAreas);

        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getWebsite(),
                StringUtils.hasText(company.getHrContact())
                        ? company.getHrContact()
                        : "Recruiter details will usually be shared after the initial shortlist.",
                StringUtils.hasText(company.getHiringManager())
                        ? company.getHiringManager()
                        : "Hiring manager to be confirmed during the interview loop.",
                StringUtils.hasText(company.getOwnerName())
                        ? company.getOwnerName()
                        : "Founder information can be shared during company research.",
                company.getEmployeeCount(),
                company.getCompanyHistory(),
                company.getCulture(),
                supportedRoles,
                interviewFocusAreas,
                buildWhyUserMatches(company, resumeProfile, selectedRoles, interviewFocusAreas),
                matchScore
        );
    }

    private double computeMatchScore(
            Company company,
            ResumeProfile resumeProfile,
            List<String> selectedRoles,
            List<String> interviewFocusAreas
    ) {
        Set<String> desiredRoles = selectedRoles.stream()
                .map(role -> role.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        Set<String> supportedRoles = company.getSupportedRoles().stream()
                .map(role -> role.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        long roleHits = desiredRoles.stream().filter(supportedRoles::contains).count();
        double roleFit = desiredRoles.isEmpty() ? 0.0 : (double) roleHits / desiredRoles.size();

        List<String> resumeSkills = resumeProfile == null
                ? List.of()
                : jsonStorageService.readStringList(resumeProfile.getExtractedSkills());
        Set<String> lowerResumeSkills = resumeSkills.stream()
                .map(skill -> skill.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        Set<String> lowerFocus = interviewFocusAreas.stream()
                .map(skill -> skill.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        long skillHits = lowerFocus.stream().filter(lowerResumeSkills::contains).count();
        double skillFit = lowerFocus.isEmpty() ? 0.45 : (double) skillHits / lowerFocus.size();

        double score = 50 + (roleFit * 30) + (skillFit * 18);
        return Math.max(48.0, Math.min(98.0, Math.round(score * 100.0) / 100.0));
    }

    private String buildWhyUserMatches(
            Company company,
            ResumeProfile resumeProfile,
            List<String> selectedRoles,
            List<String> interviewFocusAreas
    ) {
        List<String> resumeSkills = resumeProfile == null
                ? List.of()
                : jsonStorageService.readStringList(resumeProfile.getExtractedSkills());
        LinkedHashSet<String> overlappingSignals = new LinkedHashSet<>();

        Set<String> lowerSupportedRoles = company.getSupportedRoles().stream()
                .map(role -> role.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        selectedRoles.stream()
                .filter(role -> lowerSupportedRoles.contains(role.toLowerCase(Locale.ROOT)))
                .limit(2)
                .forEach(overlappingSignals::add);

        Set<String> lowerFocusAreas = interviewFocusAreas.stream()
                .map(area -> area.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        resumeSkills.stream()
                .filter(skill -> lowerFocusAreas.contains(skill.toLowerCase(Locale.ROOT)))
                .limit(3)
                .forEach(overlappingSignals::add);

        if (overlappingSignals.isEmpty()) {
            return "This company aligns with your selected track and gives you useful interview context around its culture, scale, and hiring themes.";
        }

        return "You match " + company.getName() + " because your profile overlaps with "
                + String.join(", ", overlappingSignals)
                + ", and the company culture emphasizes " + company.getCulture().toLowerCase(Locale.ROOT) + ".";
    }

    private List<String> resolveRoles(List<String> selectedRoles, ResumeProfile latestResume) {
        List<String> cleanedRoles = selectedRoles == null
                ? List.of()
                : selectedRoles.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();

        if (!cleanedRoles.isEmpty()) {
            return cleanedRoles;
        }
        if (latestResume == null) {
            return List.of();
        }
        return jsonStorageService.readStringList(latestResume.getRecommendedRoles());
    }

    private boolean matchesQuery(CompanyResponse company, String normalizedQuery) {
        if (!StringUtils.hasText(normalizedQuery)) {
            return true;
        }
        return company.name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || company.supportedRoles().stream().anyMatch(role -> role.toLowerCase(Locale.ROOT).contains(normalizedQuery))
                || company.interviewFocusAreas().stream().anyMatch(area -> area.toLowerCase(Locale.ROOT).contains(normalizedQuery));
    }

    private boolean matchesCompanySize(Integer employeeCount, String companySize) {
        if (!StringUtils.hasText(companySize) || "all".equals(companySize)) {
            return true;
        }
        return switch (companySize) {
            case "small" -> employeeCount < 500;
            case "mid" -> employeeCount >= 500 && employeeCount < 5000;
            case "enterprise" -> employeeCount >= 5000;
            default -> true;
        };
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
