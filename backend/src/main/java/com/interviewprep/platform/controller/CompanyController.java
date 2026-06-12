package com.interviewprep.platform.controller;

import com.interviewprep.platform.dto.company.CompanyDtos.CompanyMatchRequest;
import com.interviewprep.platform.dto.company.CompanyDtos.CompanyResponse;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.service.AuthService;
import com.interviewprep.platform.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final AuthService authService;

    public CompanyController(CompanyService companyService, AuthService authService) {
        this.companyService = companyService;
        this.authService = authService;
    }

    @SuppressWarnings("null")
    @PostMapping("/match")
    public List<CompanyResponse> matchCompanies(
            Authentication authentication,
            @Valid @RequestBody CompanyMatchRequest request
    ) {
        User user = authService.loadUser(authentication.getName());
        return companyService.matchCompanies(user.getId(), request.selectedRoles());
    }

    @SuppressWarnings("null")
    @GetMapping("/search")
    public List<CompanyResponse> searchCompanies(
            Authentication authentication,
            @RequestParam List<String> selectedRoles,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minMatchScore,
            @RequestParam(required = false) String companySize
    ) {
        User user = authService.loadUser(authentication.getName());
        return companyService.searchCompanies(user.getId(), selectedRoles, query, minMatchScore, companySize);
    }

    @SuppressWarnings("null")
    @GetMapping("/{companyId}")
    public CompanyResponse getCompany(Authentication authentication, @PathVariable Long companyId) {
        User user = authService.loadUser(authentication.getName());
        return companyService.getCompany(user.getId(), companyId);
    }
}
