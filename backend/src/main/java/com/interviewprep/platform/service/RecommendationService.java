package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.recommendation.RecommendationDtos.RecommendationProfileResponse;
import com.interviewprep.platform.entity.ResumeProfile;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.repository.ResumeProfileRepository;
import com.interviewprep.platform.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecommendationService {

    private final ResumeProfileRepository resumeProfileRepository;
    private final UserRepository userRepository;
    private final JsonStorageService jsonStorageService;
    private final CompanyService companyService;

    public RecommendationService(
            ResumeProfileRepository resumeProfileRepository,
            UserRepository userRepository,
            JsonStorageService jsonStorageService,
            CompanyService companyService
    ) {
        this.resumeProfileRepository = resumeProfileRepository;
        this.userRepository = userRepository;
        this.jsonStorageService = jsonStorageService;
        this.companyService = companyService;
    }

    @Transactional(readOnly = true)
    public RecommendationProfileResponse getProfileRecommendations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ResumeProfile profile = resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No resume uploaded yet"));

        var recommendedRoles = jsonStorageService.readStringList(profile.getRecommendedRoles());
        return new RecommendationProfileResponse(
                profile.getId(),
                jsonStorageService.readStringList(profile.getStrengths()),
                jsonStorageService.readStringList(profile.getWeaknesses()),
                recommendedRoles,
                jsonStorageService.readStringList(profile.getMissingSkills()),
                jsonStorageService.readStringList(profile.getImprovementRoadmap()),
                jsonStorageService.readStringList(profile.getLearningSuggestions()),
                profile.getMentorGuidance(),
                companyService.matchCompanies(userId, recommendedRoles).size()
        );
    }
}
