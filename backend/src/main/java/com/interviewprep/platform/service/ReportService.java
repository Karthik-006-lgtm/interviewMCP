package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.report.ReportDtos.DashboardMetricsResponse;
import com.interviewprep.platform.dto.report.ReportDtos.PracticeReportResponse;
import com.interviewprep.platform.entity.PracticeReport;
import com.interviewprep.platform.entity.ResumeProfile;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.repository.InterviewAnswerRepository;
import com.interviewprep.platform.repository.PracticeReportRepository;
import com.interviewprep.platform.repository.ResumeProfileRepository;
import com.interviewprep.platform.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final PracticeReportRepository practiceReportRepository;
    private final ResumeProfileRepository resumeProfileRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final JsonStorageService jsonStorageService;

    public ReportService(
            PracticeReportRepository practiceReportRepository,
            ResumeProfileRepository resumeProfileRepository,
            InterviewAnswerRepository interviewAnswerRepository,
            UserRepository userRepository,
            CompanyService companyService,
            JsonStorageService jsonStorageService
    ) {
        this.practiceReportRepository = practiceReportRepository;
        this.resumeProfileRepository = resumeProfileRepository;
        this.interviewAnswerRepository = interviewAnswerRepository;
        this.userRepository = userRepository;
        this.companyService = companyService;
        this.jsonStorageService = jsonStorageService;
    }

    @Transactional(readOnly = true)
    public List<PracticeReportResponse> getReports(Long userId) {
        User user = loadUser(userId);
        return practiceReportRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboardMetrics(Long userId) {
        User user = loadUser(userId);
        List<PracticeReportResponse> reports = getReports(userId);
        ResumeProfile latestResume = resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
        int companyMatchCount = latestResume == null
                ? 0
                : companyService.matchCompanies(userId, jsonStorageService.readStringList(latestResume.getRecommendedRoles())).size();
        BigDecimal latestScore = reports.isEmpty() ? BigDecimal.ZERO : reports.getFirst().overallScore();
        BigDecimal averageScore = reports.isEmpty()
                ? BigDecimal.ZERO
                : reports.stream()
                .map(PracticeReportResponse::overallScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reports.size()), 2, RoundingMode.HALF_UP);
        BigDecimal progressDelta = reports.size() > 1
                ? reports.getFirst().overallScore().subtract(reports.get(1).overallScore()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new DashboardMetricsResponse(
                resumeProfileRepository.findAllByUserOrderByCreatedAtDesc(user).size(),
                companyMatchCount,
                Math.toIntExact(interviewAnswerRepository.countByQuestion_Session_User_Id(userId)),
                latestScore,
                averageScore,
                progressDelta,
                topWeakAreas(reports),
                reports.stream().limit(4).toList()
        );
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private PracticeReportResponse toResponse(PracticeReport report) {
        return new PracticeReportResponse(
                report.getId(),
                report.getSession().getId(),
                report.getTitle(),
                report.getExecutiveSummary(),
                jsonStorageService.readStringList(report.getWeakAreas()),
                jsonStorageService.readStringList(report.getRecommendedActions()),
                report.getImprovementAreas(),
                report.getNextSteps(),
                report.getProgressSummary(),
                jsonStorageService.readStringList(report.getWeeklyImprovementPlan()),
                jsonStorageService.readStringList(report.getPracticeTasks()),
                jsonStorageService.readStringList(report.getTargetedQuestions()),
                report.getOverallScore(),
                report.getCreatedAt()
        );
    }

    private List<String> topWeakAreas(List<PracticeReportResponse> reports) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (PracticeReportResponse report : reports.stream().limit(5).toList()) {
            for (String weakArea : report.weakAreas()) {
                counts.merge(weakArea, 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                .map(Map.Entry::getKey)
                .limit(4)
                .toList();
    }
}
