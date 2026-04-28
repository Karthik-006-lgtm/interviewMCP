package com.interviewprep.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "reports")
public class PracticeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String improvementAreas;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String weakAreas;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedActions;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String nextSteps;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String progressSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String weeklyImprovementPlan;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String practiceTasks;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String targetedQuestions;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal overallScore;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public InterviewSession getSession() {
        return session;
    }

    public void setSession(InterviewSession session) {
        this.session = session;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public String getImprovementAreas() {
        return improvementAreas;
    }

    public void setImprovementAreas(String improvementAreas) {
        this.improvementAreas = improvementAreas;
    }

    public String getWeakAreas() {
        return weakAreas;
    }

    public void setWeakAreas(String weakAreas) {
        this.weakAreas = weakAreas;
    }

    public String getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(String recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public String getNextSteps() {
        return nextSteps;
    }

    public void setNextSteps(String nextSteps) {
        this.nextSteps = nextSteps;
    }

    public String getProgressSummary() {
        return progressSummary;
    }

    public void setProgressSummary(String progressSummary) {
        this.progressSummary = progressSummary;
    }

    public String getWeeklyImprovementPlan() {
        return weeklyImprovementPlan;
    }

    public void setWeeklyImprovementPlan(String weeklyImprovementPlan) {
        this.weeklyImprovementPlan = weeklyImprovementPlan;
    }

    public String getPracticeTasks() {
        return practiceTasks;
    }

    public void setPracticeTasks(String practiceTasks) {
        this.practiceTasks = practiceTasks;
    }

    public String getTargetedQuestions() {
        return targetedQuestions;
    }

    public void setTargetedQuestions(String targetedQuestions) {
        this.targetedQuestions = targetedQuestions;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
