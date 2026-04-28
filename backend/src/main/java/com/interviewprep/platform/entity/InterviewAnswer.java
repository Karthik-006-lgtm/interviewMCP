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
@Table(name = "answers")
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String transcript;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal correctnessScore;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal confidenceScore;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal relevanceScore;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal clarityScore;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal completenessScore;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal structureScore = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal impactScore = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal hesitationScore = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer fillerWordCount = 0;

    @Column(length = 120)
    private String emotionSignal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String grammarFeedback;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String vocabularyFeedback;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String toneFeedback;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fluencyFeedback;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pronunciationFeedback;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mentorSuggestions;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String polishedAnswer;

    @Column(columnDefinition = "TEXT")
    private String liveCoachingHints;

    @Column(columnDefinition = "TEXT")
    private String weaknessSignals;

    @Column(columnDefinition = "TEXT")
    private String weeklyImprovementPlan;

    @Column(columnDefinition = "TEXT")
    private String practiceTasks;

    @Column(columnDefinition = "TEXT")
    private String targetedQuestions;

    @Column(columnDefinition = "TEXT")
    private String adaptiveDifficultyNote;

    @Column(length = 40)
    private String nextDifficultyLevel;

    @Column(length = 255)
    private String audioPath;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewQuestion getQuestion() {
        return question;
    }

    public void setQuestion(InterviewQuestion question) {
        this.question = question;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public BigDecimal getCorrectnessScore() {
        return correctnessScore;
    }

    public void setCorrectnessScore(BigDecimal correctnessScore) {
        this.correctnessScore = correctnessScore;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public BigDecimal getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(BigDecimal relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public BigDecimal getClarityScore() {
        return clarityScore;
    }

    public void setClarityScore(BigDecimal clarityScore) {
        this.clarityScore = clarityScore;
    }

    public BigDecimal getCompletenessScore() {
        return completenessScore;
    }

    public void setCompletenessScore(BigDecimal completenessScore) {
        this.completenessScore = completenessScore;
    }

    public BigDecimal getStructureScore() {
        return structureScore;
    }

    public void setStructureScore(BigDecimal structureScore) {
        this.structureScore = structureScore;
    }

    public BigDecimal getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(BigDecimal impactScore) {
        this.impactScore = impactScore;
    }

    public BigDecimal getHesitationScore() {
        return hesitationScore;
    }

    public void setHesitationScore(BigDecimal hesitationScore) {
        this.hesitationScore = hesitationScore;
    }

    public Integer getFillerWordCount() {
        return fillerWordCount;
    }

    public void setFillerWordCount(Integer fillerWordCount) {
        this.fillerWordCount = fillerWordCount;
    }

    public String getEmotionSignal() {
        return emotionSignal;
    }

    public void setEmotionSignal(String emotionSignal) {
        this.emotionSignal = emotionSignal;
    }

    public String getGrammarFeedback() {
        return grammarFeedback;
    }

    public void setGrammarFeedback(String grammarFeedback) {
        this.grammarFeedback = grammarFeedback;
    }

    public String getVocabularyFeedback() {
        return vocabularyFeedback;
    }

    public void setVocabularyFeedback(String vocabularyFeedback) {
        this.vocabularyFeedback = vocabularyFeedback;
    }

    public String getToneFeedback() {
        return toneFeedback;
    }

    public void setToneFeedback(String toneFeedback) {
        this.toneFeedback = toneFeedback;
    }

    public String getFluencyFeedback() {
        return fluencyFeedback;
    }

    public void setFluencyFeedback(String fluencyFeedback) {
        this.fluencyFeedback = fluencyFeedback;
    }

    public String getPronunciationFeedback() {
        return pronunciationFeedback;
    }

    public void setPronunciationFeedback(String pronunciationFeedback) {
        this.pronunciationFeedback = pronunciationFeedback;
    }

    public String getMentorSuggestions() {
        return mentorSuggestions;
    }

    public void setMentorSuggestions(String mentorSuggestions) {
        this.mentorSuggestions = mentorSuggestions;
    }

    public String getPolishedAnswer() {
        return polishedAnswer;
    }

    public void setPolishedAnswer(String polishedAnswer) {
        this.polishedAnswer = polishedAnswer;
    }

    public String getLiveCoachingHints() {
        return liveCoachingHints;
    }

    public void setLiveCoachingHints(String liveCoachingHints) {
        this.liveCoachingHints = liveCoachingHints;
    }

    public String getWeaknessSignals() {
        return weaknessSignals;
    }

    public void setWeaknessSignals(String weaknessSignals) {
        this.weaknessSignals = weaknessSignals;
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

    public String getAdaptiveDifficultyNote() {
        return adaptiveDifficultyNote;
    }

    public void setAdaptiveDifficultyNote(String adaptiveDifficultyNote) {
        this.adaptiveDifficultyNote = adaptiveDifficultyNote;
    }

    public String getNextDifficultyLevel() {
        return nextDifficultyLevel;
    }

    public void setNextDifficultyLevel(String nextDifficultyLevel) {
        this.nextDifficultyLevel = nextDifficultyLevel;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
