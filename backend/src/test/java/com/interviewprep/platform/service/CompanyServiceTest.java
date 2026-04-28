package com.interviewprep.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewprep.platform.entity.Company;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.repository.CompanyRepository;
import com.interviewprep.platform.repository.ResumeProfileRepository;
import com.interviewprep.platform.repository.UserRepository;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ResumeProfileRepository resumeProfileRepository;

    @Mock
    private UserRepository userRepository;

    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyService = new CompanyService(
                companyRepository,
                resumeProfileRepository,
                userRepository,
                new JsonStorageService(new ObjectMapper())
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void matchCompaniesNormalizesRolesAndSortsResults() {
        User user = user(7L);
        Company zeta = company(2L, "Zeta Systems");
        Company alpha = company(1L, "Alpha Labs");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        when(companyRepository.findMatchingCompanies(anyCollection())).thenReturn(List.of(zeta, alpha));

        var response = companyService.matchCompanies(7L, List.of("Backend Engineer", "Full Stack Developer"));

        ArgumentCaptor<Collection<String>> rolesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(companyRepository).findMatchingCompanies(rolesCaptor.capture());
        assertEquals(List.of("backend engineer", "full stack developer"), List.copyOf(rolesCaptor.getValue()));
        assertEquals(List.of("Alpha Labs", "Zeta Systems"), response.stream().map(item -> item.name()).toList());
    }

    @Test
    void matchCompaniesReturnsEmptyWhenRepositoryReturnsNoMatches() {
        User user = user(8L);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        when(resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        when(companyRepository.findMatchingCompanies(anyCollection())).thenReturn(List.of());

        var response = companyService.matchCompanies(8L, List.of("DevOps Engineer"));

        assertEquals(List.of(), response);
    }

    @Test
    void searchCompaniesAppliesServerSideFilters() {
        User user = user(9L);
        Company alpha = company(1L, "Alpha Labs");
        Company harbor = company(2L, "Harbor Stack");
        harbor.setEmployeeCount(6200);

        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        when(companyRepository.findMatchingCompanies(anyCollection())).thenReturn(List.of(alpha, harbor));

        var response = companyService.searchCompanies(
                9L,
                List.of("Backend Engineer"),
                "harbor",
                70.0,
                "enterprise"
        );

        assertEquals(List.of("Harbor Stack"), response.stream().map(item -> item.name()).toList());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        return user;
    }

    private Company company(Long id, String name) {
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        company.setWebsite("https://" + name.toLowerCase().replace(" ", "") + ".example.com");
        company.setHrContact("HR Contact");
        company.setHiringManager("Hiring Manager");
        company.setOwnerName("Owner Name");
        company.setEmployeeCount(1000);
        company.setCompanyHistory("Company history");
        company.setCulture("Collaborative and delivery-focused.");
        company.setInterviewFocusAreas("[\"Java\",\"Spring\"]");
        company.setSupportedRoles(new LinkedHashSet<>(List.of("Backend Engineer", "Full Stack Developer")));
        return company;
    }
}
