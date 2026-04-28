package com.interviewprep.platform.controller;

import com.interviewprep.platform.dto.recommendation.RecommendationDtos.RecommendationProfileResponse;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.service.AuthService;
import com.interviewprep.platform.service.RecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AuthService authService;

    public RecommendationController(RecommendationService recommendationService, AuthService authService) {
        this.recommendationService = recommendationService;
        this.authService = authService;
    }

    @GetMapping("/profile")
    public RecommendationProfileResponse getProfileRecommendations(Authentication authentication) {
        User user = authService.loadUser(authentication.getName());
        return recommendationService.getProfileRecommendations(user.getId());
    }
}
