package com.interviewprep.platform.entity;

import com.interviewprep.platform.entity.enums.InterviewQuestionCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InterviewQuestionCategory category;

    @Column(columnDefinition = "TEXT")
    private String expectedAnswerPoints;

    @Column(nullable = false, length = 40)
    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String interviewerCue;

    @Column
    private Integer timePressureSeconds;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewSession getSession() {
        return session;
    }

    public void setSession(InterviewSession session) {
        this.session = session;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public InterviewQuestionCategory getCategory() {
        return category;
    }

    public void setCategory(InterviewQuestionCategory category) {
        this.category = category;
    }

    public String getExpectedAnswerPoints() {
        return expectedAnswerPoints;
    }

    public void setExpectedAnswerPoints(String expectedAnswerPoints) {
        this.expectedAnswerPoints = expectedAnswerPoints;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getInterviewerCue() {
        return interviewerCue;
    }

    public void setInterviewerCue(String interviewerCue) {
        this.interviewerCue = interviewerCue;
    }

    public Integer getTimePressureSeconds() {
        return timePressureSeconds;
    }

    public void setTimePressureSeconds(Integer timePressureSeconds) {
        this.timePressureSeconds = timePressureSeconds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
