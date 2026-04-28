package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.interview.InterviewDtos.AnswerEvaluationResponse;
import com.interviewprep.platform.dto.interview.InterviewDtos.AnswerSubmissionRequest;
import com.interviewprep.platform.dto.interview.InterviewDtos.InterviewQuestionResponse;
import com.interviewprep.platform.dto.interview.InterviewDtos.LiveCoachingResponse;
import com.interviewprep.platform.dto.interview.InterviewDtos.LiveCoachingRequest;
import com.interviewprep.platform.dto.interview.InterviewDtos.InterviewSessionRequest;
import com.interviewprep.platform.dto.interview.InterviewDtos.InterviewSessionResponse;
import com.interviewprep.platform.entity.Company;
import com.interviewprep.platform.entity.InterviewAnswer;
import com.interviewprep.platform.entity.InterviewQuestion;
import com.interviewprep.platform.entity.InterviewSession;
import com.interviewprep.platform.entity.PracticeReport;
import com.interviewprep.platform.entity.ResumeProfile;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.entity.enums.InterviewQuestionCategory;
import com.interviewprep.platform.repository.InterviewAnswerRepository;
import com.interviewprep.platform.repository.InterviewQuestionRepository;
import com.interviewprep.platform.repository.InterviewSessionRepository;
import com.interviewprep.platform.repository.PracticeReportRepository;
import com.interviewprep.platform.repository.ResumeProfileRepository;
import com.interviewprep.platform.repository.CompanyRepository;
import com.interviewprep.platform.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final PracticeReportRepository practiceReportRepository;
    private final ResumeProfileRepository resumeProfileRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AiOrchestrationService aiOrchestrationService;
    private final JsonStorageService jsonStorageService;
    private final SpeechProcessingService speechProcessingService;

    public InterviewService(
            InterviewSessionRepository interviewSessionRepository,
            InterviewQuestionRepository interviewQuestionRepository,
            InterviewAnswerRepository interviewAnswerRepository,
            PracticeReportRepository practiceReportRepository,
            ResumeProfileRepository resumeProfileRepository,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            AiOrchestrationService aiOrchestrationService,
            JsonStorageService jsonStorageService,
            SpeechProcessingService speechProcessingService
    ) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.interviewAnswerRepository = interviewAnswerRepository;
        this.practiceReportRepository = practiceReportRepository;
        this.resumeProfileRepository = resumeProfileRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.aiOrchestrationService = aiOrchestrationService;
        this.jsonStorageService = jsonStorageService;
        this.speechProcessingService = speechProcessingService;
    }

    @Transactional
    public InterviewSessionResponse createSession(Long userId, InterviewSessionRequest request) {
        User user = loadUser(userId);
        ResumeProfile resumeProfile = request.resumeId() != null
                ? resumeProfileRepository.findById(request.resumeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"))
                : resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);

        if (request.resumeId() != null && resumeProfile != null && !resumeProfile.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot use this resume");
        }

        List<String> technicalSkills = resumeProfile != null
                ? jsonStorageService.readStringList(resumeProfile.getExtractedSkills())
                : List.of();
        Company company = request.companyId() == null
                ? null
                : companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        if (StringUtils.hasText(request.technicalFocus())) {
            technicalSkills = mergeSkills(technicalSkills, request.technicalFocus());
        }

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setResumeProfile(resumeProfile);
        session.setSelectedRoles(jsonStorageService.write(request.selectedRoles()));
        session.setPersonalityProfile(request.personalityProfile());
        session.setTechnicalSkills(jsonStorageService.write(technicalSkills));
        session.setTargetCompanyId(company != null ? company.getId() : null);
        session.setTargetCompanyName(company != null ? company.getName() : null);
        session.setTargetCompanyWebsite(company != null ? company.getWebsite() : null);
        session.setInterviewerTone(StringUtils.hasText(request.interviewerTone()) ? request.interviewerTone().trim() : "Friendly technical panel");
        session.setCoachingIntensity(StringUtils.hasText(request.coachingIntensity()) ? request.coachingIntensity().trim() : "Balanced");
        session.setLiveCoachingEnabled(request.liveCoachingEnabled() == null || request.liveCoachingEnabled());
        session.setAdaptiveDifficultyEnabled(request.adaptiveDifficultyEnabled() == null || request.adaptiveDifficultyEnabled());
        session.setRealityMode(StringUtils.hasText(request.realityMode()) ? request.realityMode().trim() : "Standard");
        session.setCameraEnabled(request.cameraEnabled() != null && request.cameraEnabled());
        session.setCurrentDifficultyLevel("medium");
        InterviewSession savedSession = interviewSessionRepository.save(session);

        List<InterviewQuestion> savedQuestions = buildQuestions(
                savedSession,
                resumeProfile,
                company,
                request.selectedRoles(),
                request.personalityProfile(),
                technicalSkills
        );
        interviewQuestionRepository.saveAll(savedQuestions);

        return toSessionResponse(savedSession, savedQuestions);
    }

    @Transactional(readOnly = true)
    public InterviewSessionResponse getSession(Long userId, Long sessionId) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview session not found"));
        if (!session.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this interview session");
        }
        List<InterviewQuestion> questions = interviewQuestionRepository.findAllBySessionOrderByIdAsc(session);
        return toSessionResponse(session, questions);
    }

    @Transactional
    public AnswerEvaluationResponse submitAnswer(Long userId, Long questionId, AnswerSubmissionRequest request) {
        InterviewQuestion question = interviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        if (!question.getSession().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot answer this question");
        }

        InterviewSession session = question.getSession();
        String interviewContext = "Roles: " + session.getSelectedRoles()
                + " | Skills: " + session.getTechnicalSkills()
                + " | Personality: " + session.getPersonalityProfile()
                + " | Company: " + safeValue(session.getTargetCompanyName())
                + " | Interviewer tone: " + safeValue(session.getInterviewerTone())
                + " | Coaching intensity: " + safeValue(session.getCoachingIntensity())
                + " | Adaptive difficulty: " + session.isAdaptiveDifficultyEnabled()
                + " | Current difficulty: " + safeValue(question.getDifficulty())
                + " | Visual presence: " + safeValue(request.visualPresenceSignal())
                + " | Visual eye contact: " + safeValue(request.visualEyeContactSignal())
                + " | Visual confidence: " + safeValue(request.visualConfidenceSignal())
                + " | Visual nervousness: " + safeValue(request.visualNervousnessSignal());

        AiOrchestrationService.AnswerScore answerScore = aiOrchestrationService.scoreAnswer(
                question.getPrompt(),
                question.getExpectedAnswerPoints(),
                request.answerText(),
                interviewContext
        );
        AiOrchestrationService.SpeechAnalysis speechAnalysis = speechProcessingService.analyzeStoredAudio(
                userId,
                request.audioReference(),
                request.answerText(),
                null
        );

        String effectiveAnswerText = StringUtils.hasText(request.answerText())
                ? request.answerText().trim()
                : StringUtils.hasText(speechAnalysis.transcript())
                        ? speechAnalysis.transcript().trim()
                        : "";
        if (!StringUtils.hasText(effectiveAnswerText)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please provide a typed answer or record an audio answer before submitting.");
        }

        AiOrchestrationService.GrammarCheck grammarCheck = aiOrchestrationService.grammarCheck(effectiveAnswerText);

        InterviewAnswer answer = new InterviewAnswer();
        answer.setQuestion(question);
        answer.setTranscript(effectiveAnswerText);
        answer.setCorrectnessScore(answerScore.correctnessScore());
        answer.setConfidenceScore(mergeScore(answerScore.confidenceScore(), speechAnalysis.confidenceScore()));
        answer.setRelevanceScore(answerScore.relevanceScore());
        answer.setClarityScore(mergeScore(answerScore.clarityScore(), speechAnalysis.clarityScore()));
        answer.setCompletenessScore(answerScore.completenessScore());
        answer.setStructureScore(answerScore.structureScore());
        answer.setImpactScore(answerScore.impactScore());
        answer.setHesitationScore(answerScore.hesitationScore());
        answer.setFillerWordCount(answerScore.fillerWordCount());
        answer.setEmotionSignal(StringUtils.hasText(speechAnalysis.emotionSignal()) ? speechAnalysis.emotionSignal() : answerScore.emotionSignal());
        answer.setGrammarFeedback(grammarCheck.grammarFeedback());
        answer.setVocabularyFeedback(grammarCheck.vocabularyFeedback());
        answer.setToneFeedback(preferFeedback(speechAnalysis.toneFeedback(), grammarCheck.toneFeedback()));
        answer.setFluencyFeedback(preferFeedback(speechAnalysis.fluencyFeedback(), grammarCheck.fluencyFeedback()));
        answer.setPronunciationFeedback(preferFeedback(speechAnalysis.pronunciationFeedback(), grammarCheck.pronunciationFeedback()));
        answer.setMentorSuggestions(answerScore.mentorSuggestions());
        answer.setPolishedAnswer(grammarCheck.polishedAnswer());
        answer.setLiveCoachingHints(jsonStorageService.write(answerScore.liveCoachingHints()));
        answer.setWeaknessSignals(jsonStorageService.write(answerScore.weaknessSignals()));
        answer.setWeeklyImprovementPlan(jsonStorageService.write(answerScore.weeklyImprovementPlan()));
        answer.setPracticeTasks(jsonStorageService.write(answerScore.practiceTasks()));
        answer.setTargetedQuestions(jsonStorageService.write(answerScore.targetedQuestions()));
        answer.setAdaptiveDifficultyNote(answerScore.adaptiveDifficultyNote());
        answer.setNextDifficultyLevel(answerScore.nextDifficultyLevel());
        answer.setAudioPath(request.audioReference());
        InterviewAnswer savedAnswer = interviewAnswerRepository.save(answer);
        log.info("Saved answer {} for user {} and question {}", savedAnswer.getId(), userId, questionId);

        applyAdaptiveDifficulty(session, question.getId(), answerScore.nextDifficultyLevel());
        refreshSessionScoreAndReport(session);

        return new AnswerEvaluationResponse(
                savedAnswer.getId(),
                savedAnswer.getCorrectnessScore(),
                savedAnswer.getConfidenceScore(),
                savedAnswer.getRelevanceScore(),
                savedAnswer.getClarityScore(),
                savedAnswer.getCompletenessScore(),
                savedAnswer.getStructureScore(),
                savedAnswer.getImpactScore(),
                savedAnswer.getHesitationScore(),
                savedAnswer.getFillerWordCount(),
                savedAnswer.getEmotionSignal(),
                savedAnswer.getGrammarFeedback(),
                savedAnswer.getVocabularyFeedback(),
                savedAnswer.getMentorSuggestions(),
                savedAnswer.getPolishedAnswer(),
                savedAnswer.getPronunciationFeedback(),
                savedAnswer.getToneFeedback(),
                savedAnswer.getFluencyFeedback(),
                jsonStorageService.readStringList(savedAnswer.getLiveCoachingHints()),
                jsonStorageService.readStringList(savedAnswer.getWeaknessSignals()),
                jsonStorageService.readStringList(savedAnswer.getWeeklyImprovementPlan()),
                jsonStorageService.readStringList(savedAnswer.getPracticeTasks()),
                jsonStorageService.readStringList(savedAnswer.getTargetedQuestions()),
                savedAnswer.getAdaptiveDifficultyNote(),
                savedAnswer.getNextDifficultyLevel(),
                savedAnswer.getCreatedAt() != null ? savedAnswer.getCreatedAt() : Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public LiveCoachingResponse coachAnswer(Long userId, Long questionId, LiveCoachingRequest request) {
        InterviewQuestion question = interviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        if (!question.getSession().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this question");
        }
        InterviewSession session = question.getSession();
        List<String> selectedRoles = jsonStorageService.readStringList(session.getSelectedRoles());
        AiOrchestrationService.LiveCoaching coaching = aiOrchestrationService.liveCoach(
                question.getPrompt(),
                request.answerDraft() == null ? "" : request.answerDraft(),
                safeValue(session.getCoachingIntensity()),
                request.silenceDetected() != null && request.silenceDetected(),
                selectedRoles.isEmpty() ? "" : selectedRoles.getFirst(),
                safeValue(session.getTargetCompanyName())
        );
        return new LiveCoachingResponse(
                coaching.hints(),
                coaching.suggestedKeywords(),
                coaching.continuationPrompt(),
                coaching.structureReminder()
        );
    }

    private InterviewSessionResponse toSessionResponse(InterviewSession session, List<InterviewQuestion> questions) {
        return new InterviewSessionResponse(
                session.getId(),
                jsonStorageService.readStringList(session.getSelectedRoles()),
                session.getPersonalityProfile(),
                String.join(", ", jsonStorageService.readStringList(session.getTechnicalSkills())),
                session.getTargetCompanyName(),
                session.getTargetCompanyWebsite(),
                session.getInterviewerTone(),
                session.getCoachingIntensity(),
                session.isLiveCoachingEnabled(),
                session.isAdaptiveDifficultyEnabled(),
                session.getRealityMode(),
                session.isCameraEnabled(),
                session.getCurrentDifficultyLevel(),
                questions.stream()
                        .map(question -> new InterviewQuestionResponse(
                                question.getId(),
                                question.getPrompt(),
                                question.getCategory().name(),
                                question.getDifficulty(),
                                question.getInterviewerCue(),
                                question.getTimePressureSeconds()))
                        .toList(),
                session.getOverallScore(),
                session.getCreatedAt() != null ? session.getCreatedAt() : Instant.now()
        );
    }

    private List<InterviewQuestion> buildQuestions(
            InterviewSession session,
            ResumeProfile resumeProfile,
            Company company,
            List<String> selectedRoles,
            String personalityProfile,
            List<String> technicalSkills
    ) {
        String summary = resumeProfile != null && StringUtils.hasText(resumeProfile.getSummary())
                ? resumeProfile.getSummary()
                : "your recent projects and accomplishments";
        List<String> experience = resumeProfile == null ? List.of() : jsonStorageService.readStringList(resumeProfile.getExperience());
        List<String> projects = resumeProfile == null ? List.of() : jsonStorageService.readStringList(resumeProfile.getProjects());
        List<String> strengths = resumeProfile == null ? List.of() : jsonStorageService.readStringList(resumeProfile.getStrengths());
        List<String> weaknesses = resumeProfile == null ? List.of() : jsonStorageService.readStringList(resumeProfile.getWeaknesses());
        List<String> missingSkills = resumeProfile == null ? List.of() : jsonStorageService.readStringList(resumeProfile.getMissingSkills());
        List<String> companyFocusAreas = company == null ? List.of() : jsonStorageService.readStringList(company.getInterviewFocusAreas());

        AiOrchestrationService.GeneratedQuestionBundle generated = aiOrchestrationService.generateInterviewQuestions(
                selectedRoles,
                personalityProfile,
                technicalSkills,
                summary,
                experience,
                projects,
                strengths,
                weaknesses,
                missingSkills,
                company != null ? company.getName() : "",
                company != null ? company.getCulture() : "",
                company != null ? company.getCompanyHistory() : "",
                companyFocusAreas,
                safeValue(session.getInterviewerTone()),
                safeValue(session.getCoachingIntensity()),
                safeValue(session.getRealityMode()),
                session.isAdaptiveDifficultyEnabled(),
                session.isLiveCoachingEnabled()
        );

        return generated.questions().stream()
                .map(item -> question(
                        session,
                        item.prompt(),
                        parseCategory(item.category()),
                        item.expectedPoints(),
                        item.difficulty(),
                        item.interviewerCue(),
                        item.timePressureSeconds()))
                .toList();
    }

    private InterviewQuestion question(
            InterviewSession session,
            String prompt,
            InterviewQuestionCategory category,
            String expectedPoints,
            String difficulty,
            String interviewerCue,
            Integer timePressureSeconds
    ) {
        InterviewQuestion question = new InterviewQuestion();
        question.setSession(session);
        question.setPrompt(prompt);
        question.setCategory(category);
        question.setExpectedAnswerPoints(expectedPoints);
        question.setDifficulty(difficulty);
        question.setInterviewerCue(interviewerCue);
        question.setTimePressureSeconds(timePressureSeconds);
        return question;
    }

    private InterviewQuestionCategory parseCategory(String rawCategory) {
        if (!StringUtils.hasText(rawCategory)) {
            return InterviewQuestionCategory.TECHNICAL;
        }
        try {
            return InterviewQuestionCategory.valueOf(rawCategory.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            log.warn("Unknown interview question category '{}', defaulting to TECHNICAL", rawCategory);
            return InterviewQuestionCategory.TECHNICAL;
        }
    }

    private void refreshSessionScoreAndReport(InterviewSession session) {
        List<InterviewAnswer> answers = interviewAnswerRepository.findAllByQuestion_Session_IdOrderByCreatedAtDesc(session.getId());
        if (answers.isEmpty()) {
            return;
        }

        BigDecimal averageScore = answers.stream()
                .map(answer -> answer.getCorrectnessScore()
                        .add(answer.getConfidenceScore())
                        .add(answer.getRelevanceScore())
                        .add(answer.getClarityScore())
                        .add(answer.getCompletenessScore())
                        .divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(answers.size()), 2, RoundingMode.HALF_UP);

        session.setOverallScore(averageScore);
        interviewSessionRepository.save(session);

        List<String> weakAreas = buildWeakAreas(answers);
        List<String> recommendedActions = buildRecommendedActions(weakAreas);
        List<String> weeklyPlan = mergeReportLists(answers, InterviewAnswer::getWeeklyImprovementPlan);
        List<String> practiceTasks = mergeReportLists(answers, InterviewAnswer::getPracticeTasks);
        List<String> targetedQuestions = mergeReportLists(answers, InterviewAnswer::getTargetedQuestions);
        String executiveSummary = averageScore.compareTo(BigDecimal.valueOf(78)) >= 0
                ? "Interview readiness is trending strong. Your answers are covering the expected content with improving clarity and completeness."
                : "Interview readiness is developing, but the score pattern shows clear opportunities to improve structure, precision, and follow-through.";
        String improvementAreas = String.join(" ", weakAreas);
        String nextSteps = String.join(" ", recommendedActions);
        String progressSummary = "You have submitted " + answers.size() + " answer(s) in this session with a current average score of "
                + averageScore.setScale(0, RoundingMode.HALF_UP) + "%.";

        PracticeReport report = practiceReportRepository.findBySession(session).orElseGet(PracticeReport::new);
        report.setUser(session.getUser());
        report.setSession(session);
        report.setTitle("Interview Readiness Report");
        report.setExecutiveSummary(executiveSummary);
        report.setImprovementAreas(improvementAreas);
        report.setWeakAreas(jsonStorageService.write(weakAreas));
        report.setRecommendedActions(jsonStorageService.write(recommendedActions));
        report.setNextSteps(nextSteps);
        report.setProgressSummary(progressSummary);
        report.setWeeklyImprovementPlan(jsonStorageService.write(weeklyPlan));
        report.setPracticeTasks(jsonStorageService.write(practiceTasks));
        report.setTargetedQuestions(jsonStorageService.write(targetedQuestions));
        report.setOverallScore(averageScore);
        practiceReportRepository.save(report);
    }

    private void applyAdaptiveDifficulty(InterviewSession session, Long answeredQuestionId, String nextDifficultyLevel) {
        if (!session.isAdaptiveDifficultyEnabled() || !StringUtils.hasText(nextDifficultyLevel)) {
            return;
        }
        session.setCurrentDifficultyLevel(nextDifficultyLevel);
        interviewSessionRepository.save(session);
        Set<Long> answeredQuestionIds = interviewAnswerRepository.findAllByQuestion_Session_IdOrderByCreatedAtDesc(session.getId()).stream()
                .map(answer -> answer.getQuestion().getId())
                .collect(java.util.stream.Collectors.toSet());
        for (InterviewQuestion question : interviewQuestionRepository.findAllBySessionOrderByIdAsc(session)) {
            if (question.getId().equals(answeredQuestionId) || answeredQuestionIds.contains(question.getId())) {
                continue;
            }
            question.setDifficulty(nextDifficultyLevel);
            interviewQuestionRepository.save(question);
            break;
        }
    }

    private List<String> mergeReportLists(List<InterviewAnswer> answers, java.util.function.Function<InterviewAnswer, String> mapper) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (InterviewAnswer answer : answers) {
            merged.addAll(jsonStorageService.readStringList(mapper.apply(answer)));
        }
        return merged.stream().limit(6).toList();
    }

    private List<String> buildWeakAreas(List<InterviewAnswer> answers) {
        LinkedHashSet<String> weakAreas = new LinkedHashSet<>();
        double correctness = averageMetric(answers, answers.stream().map(InterviewAnswer::getCorrectnessScore).toList());
        double confidence = averageMetric(answers, answers.stream().map(InterviewAnswer::getConfidenceScore).toList());
        double relevance = averageMetric(answers, answers.stream().map(InterviewAnswer::getRelevanceScore).toList());
        double clarity = averageMetric(answers, answers.stream().map(InterviewAnswer::getClarityScore).toList());
        double completeness = averageMetric(answers, answers.stream().map(InterviewAnswer::getCompletenessScore).toList());

        if (correctness < 76) {
            weakAreas.add("Correctness needs deeper technical and business coverage.");
        }
        if (confidence < 74) {
            weakAreas.add("Confidence can improve through sharper language and stronger closing statements.");
        }
        if (relevance < 75) {
            weakAreas.add("Answers should stay closer to the exact question and scenario.");
        }
        if (clarity < 75) {
            weakAreas.add("Clarity is inconsistent; simplify the structure and reduce overly long explanations.");
        }
        if (completeness < 75) {
            weakAreas.add("Completeness is low; finish answers with outcomes, trade-offs, and next steps.");
        }
        answers.stream()
                .map(InterviewAnswer::getGrammarFeedback)
                .filter(StringUtils::hasText)
                .findFirst()
                .ifPresent(feedback -> weakAreas.add("Language polish still needs attention: " + feedback));

        return List.copyOf(weakAreas);
    }

    private List<String> buildRecommendedActions(List<String> weakAreas) {
        LinkedHashSet<String> actions = new LinkedHashSet<>();
        for (String weakArea : weakAreas) {
            if (weakArea.toLowerCase().contains("correctness")) {
                actions.add("Review the expected technical talking points before your next practice round and add one concrete example to each answer.");
            }
            if (weakArea.toLowerCase().contains("confidence")) {
                actions.add("Record yourself answering out loud and remove hedging words like 'maybe' or 'I think' where possible.");
            }
            if (weakArea.toLowerCase().contains("relevance")) {
                actions.add("Start each answer by restating the problem so your response stays tightly aligned to the interviewer's ask.");
            }
            if (weakArea.toLowerCase().contains("clarity")) {
                actions.add("Use a simple structure like Situation, Action, Result, then stop once the point is made.");
            }
            if (weakArea.toLowerCase().contains("completeness")) {
                actions.add("End every answer with the result, trade-off, and what you would improve next.");
            }
        }
        if (actions.isEmpty()) {
            actions.add("Keep practicing under time pressure and focus on maintaining the same answer quality across all question categories.");
        }
        return List.copyOf(actions);
    }

    private double averageMetric(List<InterviewAnswer> answers, List<BigDecimal> metrics) {
        if (answers.isEmpty() || metrics.isEmpty()) {
            return 0;
        }
        return metrics.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(metrics.size()), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private BigDecimal mergeScore(BigDecimal primary, BigDecimal secondary) {
        if (secondary == null) {
            return primary;
        }
        return primary.add(secondary).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private String preferFeedback(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private String safeValue(String value) {
        return StringUtils.hasText(value) ? value : "";
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private List<String> mergeSkills(List<String> existing, String technicalFocus) {
        LinkedHashSet<String> skills = new LinkedHashSet<>(existing);
        for (String item : technicalFocus.split(",")) {
            String trimmed = item.trim();
            if (StringUtils.hasText(trimmed)) {
                skills.add(trimmed);
            }
        }
        return List.copyOf(skills);
    }
}
