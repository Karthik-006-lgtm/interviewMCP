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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "interviews")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_profile_id")
    private ResumeProfile resumeProfile;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String selectedRoles;

    @Column(nullable = false, length = 160)
    private String personalityProfile;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String technicalSkills;

    @Column
    private Long targetCompanyId;

    @Column(length = 160)
    private String targetCompanyName;

    @Column(length = 255)
    private String targetCompanyWebsite;

    @Column(length = 80)
    private String interviewerTone;

    @Column(length = 40)
    private String coachingIntensity;

    @Column(nullable = false)
    private boolean liveCoachingEnabled;

    @Column(nullable = false)
    private boolean adaptiveDifficultyEnabled;

    @Column(length = 60)
    private String realityMode;

    @Column(nullable = false)
    private boolean cameraEnabled;

    @Column(length = 40)
    private String currentDifficultyLevel;

    @Column(precision = 5, scale = 2)
    private BigDecimal overallScore;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

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

    public ResumeProfile getResumeProfile() {
        return resumeProfile;
    }

    public void setResumeProfile(ResumeProfile resumeProfile) {
        this.resumeProfile = resumeProfile;
    }

    public String getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(String selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public String getPersonalityProfile() {
        return personalityProfile;
    }

    public void setPersonalityProfile(String personalityProfile) {
        this.personalityProfile = personalityProfile;
    }

    public String getTechnicalSkills() {
        return technicalSkills;
    }

    public void setTechnicalSkills(String technicalSkills) {
        this.technicalSkills = technicalSkills;
    }

    public Long getTargetCompanyId() {
        return targetCompanyId;
    }

    public void setTargetCompanyId(Long targetCompanyId) {
        this.targetCompanyId = targetCompanyId;
    }

    public String getTargetCompanyName() {
        return targetCompanyName;
    }

    public void setTargetCompanyName(String targetCompanyName) {
        this.targetCompanyName = targetCompanyName;
    }

    public String getTargetCompanyWebsite() {
        return targetCompanyWebsite;
    }

    public void setTargetCompanyWebsite(String targetCompanyWebsite) {
        this.targetCompanyWebsite = targetCompanyWebsite;
    }

    public String getInterviewerTone() {
        return interviewerTone;
    }

    public void setInterviewerTone(String interviewerTone) {
        this.interviewerTone = interviewerTone;
    }

    public String getCoachingIntensity() {
        return coachingIntensity;
    }

    public void setCoachingIntensity(String coachingIntensity) {
        this.coachingIntensity = coachingIntensity;
    }

    public boolean isLiveCoachingEnabled() {
        return liveCoachingEnabled;
    }

    public void setLiveCoachingEnabled(boolean liveCoachingEnabled) {
        this.liveCoachingEnabled = liveCoachingEnabled;
    }

    public boolean isAdaptiveDifficultyEnabled() {
        return adaptiveDifficultyEnabled;
    }

    public void setAdaptiveDifficultyEnabled(boolean adaptiveDifficultyEnabled) {
        this.adaptiveDifficultyEnabled = adaptiveDifficultyEnabled;
    }

    public String getRealityMode() {
        return realityMode;
    }

    public void setRealityMode(String realityMode) {
        this.realityMode = realityMode;
    }

    public boolean isCameraEnabled() {
        return cameraEnabled;
    }

    public void setCameraEnabled(boolean cameraEnabled) {
        this.cameraEnabled = cameraEnabled;
    }

    public String getCurrentDifficultyLevel() {
        return currentDifficultyLevel;
    }

    public void setCurrentDifficultyLevel(String currentDifficultyLevel) {
        this.currentDifficultyLevel = currentDifficultyLevel;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
