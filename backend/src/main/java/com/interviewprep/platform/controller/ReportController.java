package com.interviewprep.platform.controller;

import com.interviewprep.platform.dto.report.ReportDtos.DashboardMetricsResponse;
import com.interviewprep.platform.dto.report.ReportDtos.PracticeReportResponse;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.service.AuthService;
import com.interviewprep.platform.service.ReportService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    public ReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @GetMapping
    public List<PracticeReportResponse> getReports(Authentication authentication) {
        User user = authService.loadUser(authentication.getName());
        return reportService.getReports(user.getId());
    }

    @GetMapping("/dashboard")
    public DashboardMetricsResponse getDashboard(Authentication authentication) {
        User user = authService.loadUser(authentication.getName());
        return reportService.getDashboardMetrics(user.getId());
    }
}
