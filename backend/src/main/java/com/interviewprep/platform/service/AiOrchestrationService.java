package com.interviewprep.platform.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiOrchestrationService {

    private static final Map<String, List<String>> ROLE_KEYWORDS = Map.of(
            "Java Developer", List.of("java", "spring", "spring boot", "hibernate", "microservices", "sql"),
            "Python Developer", List.of("python", "fastapi", "django", "flask", "pandas", "automation"),
            "Full Stack Developer", List.of("react", "typescript", "java", "spring", "postgresql", "rest"),
            "Data Analyst", List.of("python", "sql", "analytics", "dashboard", "visualization", "statistics"),
            "DevOps Engineer", List.of("docker", "kubernetes", "aws", "terraform", "ci/cd", "linux"),
            "Frontend Engineer", List.of("react", "typescript", "javascript", "html", "css", "tailwind"),
            "Backend Engineer", List.of("java", "spring", "api", "postgresql", "microservices", "sql")
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d[\\d\\s().-]{8,}\\d)");
    private static final Pattern LINK_PATTERN = Pattern.compile("(https?://\\S+|linkedin\\.com/\\S+|github\\.com/\\S+)", Pattern.CASE_INSENSITIVE);

    private final WebClient aiWebClient;

    public AiOrchestrationService(WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    public ResumeAnalysis analyzeResume(String fileName, String resumeText) {
        ResumeAnalysis fallback = fallbackResumeAnalysis(fileName, resumeText);
        ResumeAnalysis response = aiWebClient.post()
                .uri("/analyze_resume")
                .bodyValue(new ResumeAnalysisRequest(fileName, resumeText))
                .retrieve()
                .bodyToMono(ResumeAnalysis.class)
                .onErrorReturn(fallback)
                .block();
        return response != null ? response : fallback;
    }

    public RoleRecommendation recommendRoles(String summary, List<String> skills) {
        RoleRecommendation fallback = fallbackRoleRecommendation(skills);
        RoleRecommendation response = aiWebClient.post()
                .uri("/recommend_role")
                .bodyValue(new RoleRecommendationRequest(summary, skills))
                .retrieve()
                .bodyToMono(RoleRecommendation.class)
                .onErrorReturn(fallback)
                .block();
        return response != null ? response : fallback;
    }

    public AnswerScore scoreAnswer(String question, String expectedPoints, String answerText, String context) {
        AnswerScore fallback = fallbackAnswerScore(expectedPoints, answerText);
        AnswerScore response = aiWebClient.post()
                .uri("/score_answer")
                .bodyValue(new AnswerScoreRequest(question, expectedPoints, answerText, context))
                .retrieve()
                .bodyToMono(AnswerScore.class)
                .onErrorReturn(fallback)
                .block();
        return response != null ? response : fallback;
    }

    public LiveCoaching liveCoach(
            String question,
            String answerDraft,
            String coachingIntensity,
            boolean silenceDetected,
            String targetRole,
            String companyName
    ) {
        LiveCoaching fallback = fallbackLiveCoaching(question, answerDraft, coachingIntensity, silenceDetected, targetRole, companyName);
        LiveCoaching response = aiWebClient.post()
                .uri("/coach_answer")
                .bodyValue(new CoachAnswerRequest(question, answerDraft, coachingIntensity, silenceDetected, targetRole, companyName))
                .retrieve()
                .bodyToMono(LiveCoaching.class)
                .onErrorReturn(fallback)
                .block();
        return response != null ? response : fallback;
    }

    public GrammarCheck grammarCheck(String answerText) {
        GrammarCheck fallback = fallbackGrammarCheck(answerText);
        GrammarCheck response = aiWebClient.post()
                .uri("/grammar_check")
                .bodyValue(new GrammarCheckRequest(answerText))
                .retrieve()
                .bodyToMono(GrammarCheck.class)
                .onErrorReturn(fallback)
                .block();
        return response != null ? response : fallback;
    }

    public GeneratedQuestionBundle generateInterviewQuestions(
            List<String> selectedRoles,
            String personalityProfile,
            List<String> technicalSkills,
            String resumeSummary,
            List<String> experience,
            List<String> projects,
            List<String> strengths,
            List<String> weaknesses,
            List<String> missingSkills,
            String companyName,
            String companyCulture,
            String companyHistory,
            List<String> companyFocusAreas,
            String interviewerTone,
            String coachingIntensity,
            String realityMode,
            boolean adaptiveDifficultyEnabled,
            boolean liveCoachingEnabled
    ) {
        GeneratedQuestionBundle fallback = fallbackInterviewQuestions(
                selectedRoles,
                personalityProfile,
                technicalSkills,
                resumeSummary,
                experience,
                projects,
                strengths,
                weaknesses,
                missingSkills,
                companyName,
                companyCulture,
                companyHistory,
                companyFocusAreas,
                interviewerTone,
                coachingIntensity,
                realityMode,
                adaptiveDifficultyEnabled,
                liveCoachingEnabled
        );
        GeneratedQuestionBundle response = aiWebClient.post()
                .uri("/generate_questions")
                .bodyValue(new GenerateQuestionsRequest(
                        selectedRoles,
                        personalityProfile,
                        technicalSkills,
                        resumeSummary,
                        experience,
                        projects,
                        strengths,
                        weaknesses,
                        missingSkills,
                        companyName,
                        companyCulture,
                        companyHistory,
                        companyFocusAreas,
                        interviewerTone,
                        coachingIntensity,
                        realityMode,
                        adaptiveDifficultyEnabled,
                        liveCoachingEnabled
                ))
                .retrieve()
                .bodyToMono(GeneratedQuestionBundle.class)
                .onErrorReturn(fallback)
                .block();
        return response != null ? response : fallback;
    }

    public SpeechAnalysis analyzeSpeech(Path audioPath, String transcriptHint, Long durationMs) {
        SpeechAnalysis fallback = fallbackSpeechAnalysis(transcriptHint, durationMs);
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(audioPath));
        builder.part("transcript_hint", transcriptHint == null ? "" : transcriptHint);
        if (durationMs != null) {
            builder.part("duration_ms", durationMs);
        }

        SpeechAnalysis response = aiWebClient.post()
                .uri("/transcribe_audio")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(SpeechAnalysis.class)
                .onErrorReturn(fallback)
                .block();
        return response != null ? response : fallback;
    }

    public SpeechAnalysis fallbackSpeechAnalysis(String transcriptHint, Long durationMs) {
        String transcript = StringUtils.hasText(transcriptHint) ? polishAnswer(transcriptHint) : "";
        int wordCount = countWords(transcript);
        double minutes = durationMs == null || durationMs <= 0 ? 1.0 : durationMs / 60000.0;
        double wordsPerMinute = wordCount == 0 ? 0.0 : wordCount / minutes;
        int fillerPenalty = List.of("um", "uh", "like", "you know").stream()
                .mapToInt(filler -> transcript.toLowerCase(Locale.ROOT).split("\\b" + Pattern.quote(filler) + "\\b", -1).length - 1)
                .sum();

        BigDecimal confidenceScore = clamp(score(0.48 + Math.min(0.35, (Math.max(wordCount, 20) / 120.0) * 0.35) - (fillerPenalty * 0.04)));
        BigDecimal fluencyScore = clamp(score(0.45 + fluencyBand(wordsPerMinute) - (fillerPenalty * 0.03)));
        BigDecimal clarityScore = clamp(score(0.45 + clarityBand(transcript)));

        return new SpeechAnalysis(
                transcript,
                confidenceScore,
                fluencyScore,
                clarityScore,
                emotionSignal(transcript, confidenceScore.doubleValue(), fillerPenalty),
                toneFeedback(transcript),
                pronunciationFeedback(wordsPerMinute, fillerPenalty, transcript),
                fluencyFeedback(transcript)
        );
    }

    private ResumeAnalysis fallbackResumeAnalysis(String fileName, String resumeText) {
        List<String> normalizedLines = resumeText.lines().map(String::trim).filter(StringUtils::hasText).toList();
        String summarySection = extractSummarySection(normalizedLines);
        List<String> skills = mergeUnique(extractSkillSection(normalizedLines), extractSkillCandidates(resumeText));
        RoleRecommendation roleRecommendation = fallbackRoleRecommendation(skills);
        List<String> education = extractSectionItems(resumeText, List.of("education", "academic background"));
        List<String> experience = extractSectionItems(resumeText, List.of("experience", "work experience", "professional experience"));
        List<String> projects = extractSectionItems(resumeText, List.of("projects", "project experience", "key projects"));
        List<String> certifications = extractSectionItems(resumeText, List.of("certifications", "certificates", "licenses"));
        List<String> contactInfo = extractContactInfo(resumeText);
        String candidateName = extractCandidateName(normalizedLines, fileName);
        List<String> missingSkills = inferMissingSkills(roleRecommendation.recommendedRoles(), skills);
        List<String> strengths = buildStrengths(resumeText, skills, projects, experience);
        List<String> weaknesses = buildWeaknesses(education, experience, projects, certifications, missingSkills);
        List<String> strengthIndicators = buildStrengthIndicators(resumeText, skills, experience);
        List<String> weaknessIndicators = buildWeaknessIndicators(education, experience, projects, certifications, missingSkills);
        List<String> improvementRoadmap = buildImprovementRoadmap(missingSkills, weaknessIndicators);
        List<String> learningSuggestions = buildLearningSuggestions(roleRecommendation.recommendedRoles(), missingSkills);
        return new ResumeAnalysis(
                candidateName,
                contactInfo,
                summarySection.isBlank() ? buildSummary(fileName, skills, experience, projects) : summarySection,
                strengths,
                weaknesses,
                skills,
                education,
                experience,
                projects,
                certifications,
                missingSkills,
                strengthIndicators,
                weaknessIndicators,
                improvementRoadmap,
                learningSuggestions,
                buildMentorGuidance(candidateName, roleRecommendation.recommendedRoles(), missingSkills),
                roleRecommendation.recommendedRoles(),
                scoreResume(skills, experience, projects, certifications, missingSkills, strengthIndicators)
        );
    }

    public GeneratedQuestionBundle fallbackInterviewQuestions(
            List<String> selectedRoles,
            String personalityProfile,
            List<String> technicalSkills,
            String resumeSummary,
            List<String> experience,
            List<String> projects,
            List<String> strengths,
            List<String> weaknesses,
            List<String> missingSkills,
            String companyName,
            String companyCulture,
            String companyHistory,
            List<String> companyFocusAreas,
            String interviewerTone,
            String coachingIntensity,
            String realityMode,
            boolean adaptiveDifficultyEnabled,
            boolean liveCoachingEnabled
    ) {
        String primaryRole = selectedRoles == null || selectedRoles.isEmpty() ? "Full Stack Developer" : selectedRoles.getFirst();
        String rolePhrase = selectedRoles == null || selectedRoles.isEmpty()
                ? primaryRole
                : String.join(", ", selectedRoles.stream().limit(2).toList());
        List<String> mergedSkills = mergeUnique(
                technicalSkills == null ? List.of() : technicalSkills,
                ROLE_KEYWORDS.getOrDefault(primaryRole, List.of()).stream().map(this::normalizeKeyword).toList(),
                companyFocusAreas == null ? List.of() : companyFocusAreas
        );
        String primarySkill = mergedSkills.isEmpty() ? "system design" : mergedSkills.getFirst();
        String secondarySkill = mergedSkills.size() > 1 ? mergedSkills.get(1) : "debugging";
        String experienceSignal = experience == null || experience.isEmpty()
                ? "a recent delivery challenge from your background"
                : experience.getFirst();
        String projectSignal = projects == null || projects.isEmpty()
                ? "a production-style project you have shipped"
                : projects.getFirst();
        String strengthSignal = strengths == null || strengths.isEmpty()
                ? "ownership and execution"
                : strengths.getFirst();
        String weaknessSignal = missingSkills != null && !missingSkills.isEmpty()
                ? missingSkills.getFirst()
                : weaknesses != null && !weaknesses.isEmpty()
                ? weaknesses.getFirst()
                : "stakeholder communication";
        String summarySignal = StringUtils.hasText(resumeSummary) ? resumeSummary : "your fit for " + rolePhrase;
        String companyPhrase = StringUtils.hasText(companyName) ? companyName : "this company";
        String cultureSignal = StringUtils.hasText(companyCulture) ? companyCulture : "high ownership and strong communication";
        String historySignal = StringUtils.hasText(companyHistory) ? companyHistory : "a growth-stage technology business";
        String cue = interviewerCue(interviewerTone, realityMode, coachingIntensity, liveCoachingEnabled);
        int pressureSeconds = timePressureSeconds(realityMode, interviewerTone);

        return new GeneratedQuestionBundle(List.of(
                new GeneratedQuestion(
                        "You are interviewing with " + companyPhrase + ". Walk me through " + experienceSignal
                                + " and explain how it proves your readiness for a " + primaryRole
                                + " role in a team shaped by " + cultureSignal + ".",
                        "EXPERIENCE",
                        "Context, ownership, decisions, measurable result, role alignment",
                        adaptiveDifficultyEnabled ? "hard" : "medium",
                        cue,
                        pressureSeconds
                ),
                new GeneratedQuestion(
                        "How would you design, implement, and test a production-ready solution using " + primarySkill
                                + " for a " + primaryRole + " interview at " + companyPhrase + "?",
                        "TECHNICAL",
                        "Architecture, implementation, testing strategy, trade-offs, scalability",
                        "hard",
                        cue,
                        pressureSeconds
                ),
                new GeneratedQuestion(
                        "What are the most important trade-offs when solving a real-world problem that depends on "
                                + secondarySkill + " at " + companyPhrase + ", and how would you explain them under follow-up pressure?",
                        "PROBLEM_SOLVING",
                        "Problem framing, constraints, options considered, final decision, risk management",
                        "hard",
                        interviewerCue("Strict technical panel", realityMode, coachingIntensity, false),
                        Math.max(60, pressureSeconds - 15)
                ),
                new GeneratedQuestion(
                        "Describe " + projectSignal + " in a way that helps both HR and hiring managers at " + companyPhrase
                                + " understand your impact.",
                        "HR",
                        "Career narrative, business value, collaboration, ownership, concise positioning",
                        "medium",
                        interviewerCue("Friendly HR panel", realityMode, coachingIntensity, liveCoachingEnabled),
                        pressureSeconds + 15
                ),
                new GeneratedQuestion(
                        "Imagine your new team at " + companyPhrase + " is weak in " + weaknessSignal
                                + ". How would you close the gap while still delivering on deadlines and interruptions?",
                        "REAL_WORLD_SCENARIO",
                        "Prioritization, learning plan, collaboration, delivery management, risk control",
                        "medium",
                        interviewerCue(interviewerTone, "Interrupted panel", coachingIntensity, liveCoachingEnabled),
                        Math.max(55, pressureSeconds - 10)
                ),
                new GeneratedQuestion(
                        "Given your " + personalityProfile.toLowerCase(Locale.ROOT) + " style, how do you handle disagreement, feedback, and shifting priorities at "
                                + companyPhrase + " without losing clarity?",
                        "COMMUNICATION",
                        "Self-awareness, communication style, conflict handling, adaptability, clarity",
                        "medium",
                        cue,
                        pressureSeconds
                ),
                new GeneratedQuestion(
                        "What should an interviewer at " + companyPhrase + " remember about your " + strengthSignal.toLowerCase(Locale.ROOT)
                                + " after hearing your answer to '" + summarySignal + "'?",
                        "BEHAVIORAL",
                        "Differentiation, confidence, evidence, concise story, memorable takeaway",
                        "medium",
                        cue,
                        pressureSeconds
                ),
                new GeneratedQuestion(
                        "If a " + primaryRole + " platform at " + companyPhrase + " needed to scale 10x next year, what architectural decisions would you make first given "
                                + historySignal + "?",
                        "SYSTEM_DESIGN",
                        "Bottlenecks, observability, resilience, data choices, scaling trade-offs",
                        "hard",
                        interviewerCue("Strict technical panel", realityMode, coachingIntensity, false),
                        Math.max(55, pressureSeconds - 20)
                ),
                new GeneratedQuestion(
                        "Based on your current profile summary, " + summarySignal + ", what is your strongest final-round value proposition for "
                                + rolePhrase + " roles at " + companyPhrase + "?",
                        "EXPERIENCE",
                        "Narrative clarity, role fit, achievements, seniority signal, closing pitch",
                        "medium",
                        interviewerCue("Founder-style closeout", realityMode, coachingIntensity, liveCoachingEnabled),
                        Math.max(45, pressureSeconds - 20)
                )
        ));
    }

    private RoleRecommendation fallbackRoleRecommendation(List<String> skills) {
        List<String> loweredSkills = skills.stream().map(skill -> skill.toLowerCase(Locale.ROOT)).toList();
        List<String> recommendations = ROLE_KEYWORDS.entrySet().stream()
                .filter(entry -> loweredSkills.stream().anyMatch(skill -> entry.getValue().contains(skill)))
                .sorted((left, right) -> Long.compare(
                        right.getValue().stream().filter(loweredSkills::contains).count(),
                        left.getValue().stream().filter(loweredSkills::contains).count()
                ))
                .map(Map.Entry::getKey)
                .limit(4)
                .toList();
        if (recommendations.isEmpty()) {
            recommendations = List.of("Full Stack Developer", "Java Developer", "Python Developer");
        }
        return new RoleRecommendation(recommendations);
    }

    private AnswerScore fallbackAnswerScore(String expectedPoints, String answerText) {
        List<String> expectedKeywords = tokenize(expectedPoints);
        List<String> answerKeywords = tokenize(answerText);
        long coverageHits = expectedKeywords.stream().filter(answerKeywords::contains).count();
        double coverage = expectedKeywords.isEmpty() ? 0.65 : (double) coverageHits / expectedKeywords.size();
        double lengthFactor = Math.min(1.0, answerText.length() / 420.0);
        double sentenceFactor = Math.min(1.0, Math.max(0.35, countSentences(answerText) / 4.0));
        double structureFactor = answerText.toLowerCase(Locale.ROOT).contains("result") ? 0.9 : 0.72;
        int fillerWordCount = fillerWordCount(answerText);
        BigDecimal correctness = score(0.46 + (coverage * 0.44));
        BigDecimal confidence = score(0.42 + (lengthFactor * 0.42));
        BigDecimal relevance = score(0.40 + (coverage * 0.48));
        BigDecimal clarity = score(0.38 + (sentenceFactor * 0.34) + (structureFactor * 0.16));
        BigDecimal completeness = score(0.35 + (coverage * 0.35) + (lengthFactor * 0.20));
        BigDecimal structure = score(0.40 + (structureFactor * 0.40));
        BigDecimal impact = score(0.36 + Math.min(0.32, countImpactSignals(answerText) * 0.08));
        BigDecimal hesitation = score(0.32 + Math.min(0.42, fillerWordCount * 0.08));
        List<String> weaknessSignals = weaknessSignals(correctness, relevance, clarity, completeness, structure, impact, hesitation, fillerWordCount);

        return new AnswerScore(
                correctness,
                confidence,
                relevance,
                clarity,
                completeness,
                structure,
                impact,
                hesitation,
                fillerWordCount,
                emotionSignal(answerText, confidence.doubleValue(), hesitation.doubleValue()),
                mentorSuggestion(correctness, confidence, relevance, clarity, completeness),
                liveHints(weaknessSignals, "balanced"),
                weaknessSignals,
                weeklyPlan(weaknessSignals),
                practiceTasks(weaknessSignals, expectedKeywords),
                targetedQuestions("Repeat the answer with stronger structure.", weaknessSignals),
                "Adaptive difficulty will increase when your score trend stays strong.",
                determineNextDifficulty(correctness, confidence, relevance, clarity, completeness, "medium", true)
        );
    }

    private GrammarCheck fallbackGrammarCheck(String answerText) {
        List<String> notices = new ArrayList<>();
        if (Pattern.compile("\\bi\\b").matcher(answerText).find()) {
            notices.add("Capitalize the pronoun 'I' to improve written polish.");
        }
        if (answerText.contains("  ")) {
            notices.add("Remove repeated spaces to make the answer easier to read.");
        }
        if (!answerText.trim().endsWith(".") && !answerText.trim().endsWith("!") && !answerText.trim().endsWith("?")) {
            notices.add("Close the answer with punctuation so it lands more confidently.");
        }
        if (countWords(answerText) < 25) {
            notices.add("Expand the answer with one more concrete example or result.");
        }
        String grammarFeedback = notices.isEmpty()
                ? "Grammar is solid overall. Focus next on smoother transitions and more precise verbs."
                : String.join(" ", notices);
        return new GrammarCheck(
                grammarFeedback,
                vocabularyFeedback(answerText),
                toneFeedback(answerText),
                fluencyFeedback(answerText),
                "Pronunciation feedback is strongest with audio, but this pass still highlights how clearly your answer is likely to land.",
                polishAnswer(answerText)
        );
    }

    private String extractCandidateName(List<String> lines, String fileName) {
        for (String line : lines.stream().limit(4).toList()) {
            if (!line.contains("@") && !line.toLowerCase(Locale.ROOT).contains("resume") && line.length() <= 60) {
                return line;
            }
        }
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    private List<String> extractContactInfo(String text) {
        LinkedHashSet<String> contactInfo = new LinkedHashSet<>();
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            contactInfo.add("Email: " + emailMatcher.group());
        }
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            contactInfo.add("Phone: " + phoneMatcher.group().trim());
        }
        Matcher linkMatcher = LINK_PATTERN.matcher(text);
        while (linkMatcher.find() && contactInfo.size() < 4) {
            contactInfo.add("Profile: " + linkMatcher.group());
        }
        return List.copyOf(contactInfo);
    }

    private List<String> extractSectionItems(String resumeText, List<String> headings) {
        List<String> items = new ArrayList<>();
        boolean active = false;
        for (String rawLine : resumeText.split("\\R")) {
            String line = rawLine.trim();
            if (!StringUtils.hasText(line)) {
                continue;
            }
            String normalizedLine = normalizeHeading(line);
            if (headings.contains(normalizedLine)) {
                active = true;
                continue;
            }
            if (active && isLikelyHeading(normalizedLine) && !headings.contains(normalizedLine)) {
                break;
            }
            if (active) {
                items.add(line);
            }
        }
        return items.stream().limit(6).toList();
    }

    private boolean isLikelyHeading(String value) {
        return value.length() <= 32 && value.chars().noneMatch(Character::isDigit);
    }

    private String extractSummarySection(List<String> lines) {
        return String.join(
                " ",
                extractSectionItems(
                        String.join(System.lineSeparator(), lines),
                        List.of("summary", "professional summary", "profile", "about", "career summary")
                ).stream().limit(4).toList()
        ).trim();
    }

    private List<String> extractSkillSection(List<String> lines) {
        List<String> rawItems = extractSectionItems(
                String.join(System.lineSeparator(), lines),
                List.of("skills", "technical skills", "core skills", "skills & tools", "tools")
        );
        List<String> extracted = new ArrayList<>();
        for (String item : rawItems) {
            extracted.addAll(Arrays.stream(item.split("[|,/]|(?:\\s+-\\s+)"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(this::normalizeKeyword)
                    .toList());
        }
        return extracted.stream().distinct().limit(12).toList();
    }

    private String normalizeHeading(String line) {
        return line.toLowerCase(Locale.ROOT).replace(":", "").replaceAll("[^a-z& ]+", "").trim();
    }

    @SafeVarargs
    private final List<String> mergeUnique(List<String>... groups) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (List<String> group : groups) {
            if (group == null) {
                continue;
            }
            group.stream().filter(StringUtils::hasText).map(String::trim).forEach(merged::add);
        }
        return merged.stream().limit(12).toList();
    }

    private List<String> inferMissingSkills(List<String> recommendedRoles, List<String> skills) {
        Set<String> lowerSkills = skills.stream().map(skill -> skill.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());
        LinkedHashSet<String> missingSkills = new LinkedHashSet<>();
        for (String role : recommendedRoles) {
            for (String keyword : ROLE_KEYWORDS.getOrDefault(role, List.of())) {
                if (!lowerSkills.contains(keyword)) {
                    missingSkills.add(normalizeKeyword(keyword));
                }
            }
        }
        return missingSkills.stream().limit(6).toList();
    }

    private List<String> buildStrengths(String resumeText, List<String> skills, List<String> projects, List<String> experience) {
        List<String> strengths = new ArrayList<>();
        if (!projects.isEmpty()) {
            strengths.add("Projects are present, which gives you concrete material for technical and scenario-based interviews.");
        }
        if (!experience.isEmpty()) {
            strengths.add("Work experience is documented, helping you frame credible ownership and delivery stories.");
        }
        if (skills.size() >= 5) {
            strengths.add("Your skill mix supports multiple role tracks and stronger company matching.");
        }
        if (Pattern.compile("\\b\\d+%|\\b\\d+\\b").matcher(resumeText).find()) {
            strengths.add("The resume includes measurable outcomes, which is a strong signal for interview storytelling.");
        }
        return strengths.stream().limit(4).toList();
    }

    private List<String> buildWeaknesses(List<String> education, List<String> experience, List<String> projects, List<String> certifications, List<String> missingSkills) {
        List<String> weaknesses = new ArrayList<>();
        if (experience.isEmpty()) {
            weaknesses.add("Add clearer experience bullets to show ownership, scope, and outcomes.");
        }
        if (projects.isEmpty()) {
            weaknesses.add("Include project details so interviewers can explore your technical decisions in depth.");
        }
        if (education.isEmpty() && certifications.isEmpty()) {
            weaknesses.add("Educational or certification context is missing, which weakens credibility for some hiring loops.");
        }
        if (!missingSkills.isEmpty()) {
            weaknesses.add("Some role-critical skills are missing or not visible in the current resume narrative.");
        }
        return weaknesses.stream().limit(4).toList();
    }

    private List<String> buildStrengthIndicators(String resumeText, List<String> skills, List<String> experience) {
        List<String> indicators = new ArrayList<>();
        if (Pattern.compile("\\bled\\b|\\bowned\\b|\\bmentored\\b", Pattern.CASE_INSENSITIVE).matcher(resumeText).find()) {
            indicators.add("Leadership and ownership language is present.");
        }
        if (!experience.isEmpty()) {
            indicators.add("Experience history gives enough signal to tailor behavioral questions.");
        }
        if (skills.size() >= 4) {
            indicators.add("Technical breadth supports role flexibility.");
        }
        return indicators.stream().limit(4).toList();
    }

    private List<String> buildWeaknessIndicators(List<String> education, List<String> experience, List<String> projects, List<String> certifications, List<String> missingSkills) {
        List<String> indicators = new ArrayList<>();
        if (education.isEmpty()) {
            indicators.add("Education section not clearly detected.");
        }
        if (projects.isEmpty()) {
            indicators.add("Projects section is missing or too thin.");
        }
        if (certifications.isEmpty()) {
            indicators.add("No certifications were detected.");
        }
        if (!missingSkills.isEmpty()) {
            indicators.add("Role-fit gaps were identified in the current skill set.");
        }
        if (experience.isEmpty()) {
            indicators.add("Experience details need stronger structure and specificity.");
        }
        return indicators.stream().limit(4).toList();
    }

    private List<String> buildImprovementRoadmap(List<String> missingSkills, List<String> weaknessIndicators) {
        LinkedHashSet<String> roadmap = new LinkedHashSet<>();
        if (!missingSkills.isEmpty()) {
            roadmap.add("Add evidence for " + String.join(", ", missingSkills.stream().limit(3).toList()) + " through projects, coursework, or quantified work examples.");
        }
        roadmap.add("Rewrite recent bullets in STAR form with clearer outcomes and technical trade-offs.");
        if (!weaknessIndicators.isEmpty()) {
            roadmap.add("Fill the missing sections called out in the analysis so recruiters can scan your profile faster.");
        }
        roadmap.add("Practice a concise 60-second value proposition aligned to your target roles.");
        return List.copyOf(roadmap);
    }

    private List<String> buildLearningSuggestions(List<String> recommendedRoles, List<String> missingSkills) {
        LinkedHashSet<String> learningSuggestions = new LinkedHashSet<>();
        if (!missingSkills.isEmpty()) {
            learningSuggestions.add("Build one showcase project focused on " + missingSkills.getFirst() + " and document the trade-offs you made.");
        }
        for (String role : recommendedRoles.stream().limit(2).toList()) {
            learningSuggestions.add("Review common interview themes for " + role + " roles and prepare two strong project stories for each theme.");
        }
        learningSuggestions.add("Record practice answers and tighten them until each one is direct, evidence-based, and measurable.");
        return List.copyOf(learningSuggestions);
    }

    private String buildSummary(String fileName, List<String> skills, List<String> experience, List<String> projects) {
        String skillSummary = skills.isEmpty() ? "general software delivery" : String.join(", ", skills.stream().limit(5).toList());
        return fileName + " suggests practical experience across " + skillSummary + ". "
                + "The profile includes " + experience.size() + " experience signals and " + projects.size()
                + " project signals that can be converted into targeted interview stories.";
    }

    private String buildMentorGuidance(String candidateName, List<String> recommendedRoles, List<String> missingSkills) {
        String rolePhrase = recommendedRoles.isEmpty() ? "your target roles" : String.join(", ", recommendedRoles.stream().limit(2).toList());
        if (!missingSkills.isEmpty()) {
            return candidateName + ", your strongest path is to target " + rolePhrase
                    + " while closing the most visible gaps in " + String.join(", ", missingSkills.stream().limit(3).toList()) + ".";
        }
        return candidateName + ", your current resume already supports " + rolePhrase
                + ". Focus next on sharper interview stories, quantified impact, and confident delivery.";
    }

    private double scoreResume(List<String> skills, List<String> experience, List<String> projects, List<String> certifications, List<String> missingSkills, List<String> strengthIndicators) {
        double score = 48
                + Math.min(20, skills.size() * 3)
                + Math.min(10, experience.size() * 2)
                + Math.min(8, projects.size() * 2)
                + Math.min(6, certifications.size() * 1.5)
                + Math.min(8, strengthIndicators.size() * 2)
                - Math.min(14, missingSkills.size() * 2.5);
        return Math.max(52.0, Math.min(96.0, Math.round(score * 100.0) / 100.0));
    }

    private String mentorSuggestion(BigDecimal correctness, BigDecimal confidence, BigDecimal relevance, BigDecimal clarity, BigDecimal completeness) {
        BigDecimal weakest = List.of(correctness, confidence, relevance, clarity, completeness).stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        if (weakest == correctness) {
            return "Cover the expected technical or business points more directly, then prove them with one concrete example.";
        }
        if (weakest == relevance) {
            return "Stay closer to the interviewer's question and explicitly connect each point back to the scenario.";
        }
        if (weakest == clarity) {
            return "Shorten the answer into clearer steps so the interviewer can follow your logic without extra prompting.";
        }
        if (weakest == completeness) {
            return "Finish the answer with outcome, trade-off, and what you would do next so it feels complete.";
        }
        return "Remove hedging, slow down, and end with measurable impact so your answer sounds more confident.";
    }

    private LiveCoaching fallbackLiveCoaching(
            String question,
            String answerDraft,
            String coachingIntensity,
            boolean silenceDetected,
            String targetRole,
            String companyName
    ) {
        List<String> weaknesses = weaknessSignals(
                score(0.55),
                score(0.55),
                score(answerDraft.length() > 160 ? 0.76 : 0.62),
                score(answerDraft.length() > 220 ? 0.74 : 0.58),
                score(answerDraft.toLowerCase(Locale.ROOT).contains("result") ? 0.78 : 0.60),
                score(countImpactSignals(answerDraft) > 0 ? 0.74 : 0.58),
                score(fillerWordCount(answerDraft) > 1 ? 0.74 : 0.56),
                fillerWordCount(answerDraft)
        );
        return new LiveCoaching(
                liveHints(weaknesses, coachingIntensity),
                extractCoachKeywords(question, answerDraft, targetRole, companyName),
                silenceDetected
                        ? "Continue by naming the action you took and the result it created."
                        : "Add the strongest example, the trade-off, and the final impact next.",
                "Use STAR: situation, task, action, result."
        );
    }

    private String interviewerCue(String interviewerTone, String realityMode, String coachingIntensity, boolean liveCoachingEnabled) {
        String tone = StringUtils.hasText(interviewerTone) ? interviewerTone : "Friendly technical panel";
        String reality = StringUtils.hasText(realityMode) ? realityMode : "Standard";
        String coaching = StringUtils.hasText(coachingIntensity) ? coachingIntensity : "Balanced";
        return tone + ". " + reality + ". " + (liveCoachingEnabled ? "Hints available." : "No live hints.")
                + " Coaching: " + coaching + ".";
    }

    private int timePressureSeconds(String realityMode, String interviewerTone) {
        String loweredMode = realityMode == null ? "" : realityMode.toLowerCase(Locale.ROOT);
        String loweredTone = interviewerTone == null ? "" : interviewerTone.toLowerCase(Locale.ROOT);
        int seconds = 105;
        if (loweredMode.contains("offline") || loweredMode.contains("lag")) {
            seconds = 75;
        }
        if (loweredMode.contains("panel") || loweredMode.contains("interrupt")) {
            seconds -= 10;
        }
        if (loweredTone.contains("strict") || loweredTone.contains("grill")) {
            seconds -= 15;
        }
        return Math.max(45, seconds);
    }

    private String emotionSignal(String answerText, double confidence, double hesitation) {
        String lowered = answerText == null ? "" : answerText.toLowerCase(Locale.ROOT);
        if (hesitation >= 74 || lowered.contains("sorry")) {
            return "Nervous but coachable";
        }
        if (confidence >= 82) {
            return "Confident and composed";
        }
        if (confidence >= 70) {
            return "Steady with room to sound more assertive";
        }
        return "Cautious and still warming up";
    }

    private List<String> weaknessSignals(
            BigDecimal correctness,
            BigDecimal relevance,
            BigDecimal clarity,
            BigDecimal completeness,
            BigDecimal structure,
            BigDecimal impact,
            BigDecimal hesitation,
            int fillerWordCount
    ) {
        List<String> weaknesses = new ArrayList<>();
        if (structure.doubleValue() < 72) {
            weaknesses.add("Structure is weak; STAR framing is incomplete.");
        }
        if (impact.doubleValue() < 70) {
            weaknesses.add("Impact is underplayed; outcomes and metrics are too soft.");
        }
        if (relevance.doubleValue() < 74) {
            weaknesses.add("Answer drift is visible; stay closer to the exact interviewer ask.");
        }
        if (completeness.doubleValue() < 74) {
            weaknesses.add("The conclusion is thin; add outcome, trade-off, and follow-up action.");
        }
        if (correctness.doubleValue() < 74) {
            weaknesses.add("Depth is limited; strengthen technical and business specifics.");
        }
        if (clarity.doubleValue() < 74) {
            weaknesses.add("Clarity drops in longer sentences; tighten sequencing.");
        }
        if (hesitation.doubleValue() >= 72 || fillerWordCount >= 3) {
            weaknesses.add("Hesitation is noticeable; reduce fillers and hedging phrases.");
        }
        return weaknesses.stream().limit(5).toList();
    }

    private List<String> liveHints(List<String> weaknessSignals, String coachingIntensity) {
        List<String> hints = new ArrayList<>();
        if (weaknessSignals.stream().anyMatch(item -> item.startsWith("Structure"))) {
            hints.add("Use STAR explicitly: context, action, result.");
        }
        if (weaknessSignals.stream().anyMatch(item -> item.startsWith("Impact"))) {
            hints.add("Add one metric or business result to prove impact.");
        }
        if (weaknessSignals.stream().anyMatch(item -> item.startsWith("Hesitation"))) {
            hints.add("Replace hedging with direct ownership language like 'I decided' or 'I led'.");
        }
        if (weaknessSignals.stream().anyMatch(item -> item.startsWith("Answer drift"))) {
            hints.add("Restate the question goal and tie each point back to it.");
        }
        if (hints.isEmpty()) {
            hints.add("Give one specific example and end with what changed because of your work.");
        }
        String loweredIntensity = coachingIntensity == null ? "balanced" : coachingIntensity.toLowerCase(Locale.ROOT);
        if ("advanced".equals(loweredIntensity)) {
            return hints.stream().limit(1).toList();
        }
        if ("beginner".equals(loweredIntensity)) {
            hints.add("Keep your answer under four clear steps.");
            hints.add("Close with the result and what you learned.");
            return hints.stream().distinct().limit(4).toList();
        }
        return hints.stream().distinct().limit(2).toList();
    }

    private List<String> weeklyPlan(List<String> weaknessSignals) {
        LinkedHashSet<String> plan = new LinkedHashSet<>();
        plan.add("Day 1: Rewrite one answer using STAR with a stronger closing result.");
        if (weaknessSignals.stream().anyMatch(item -> item.contains("Hesitation"))) {
            plan.add("Day 2: Record two one-minute answers and remove filler words on replay.");
        }
        if (weaknessSignals.stream().anyMatch(item -> item.contains("Impact"))) {
            plan.add("Day 3: Add metrics and business outcomes to three project stories.");
        }
        if (weaknessSignals.stream().anyMatch(item -> item.contains("Structure"))) {
            plan.add("Day 4: Practice opening, action, and result transitions for one technical answer.");
        }
        plan.add("Day 5: Run a timed mock answer and finish with trade-offs plus next steps.");
        return List.copyOf(plan);
    }

    private List<String> practiceTasks(List<String> weaknessSignals, java.util.Collection<String> expectedKeywords) {
        LinkedHashSet<String> tasks = new LinkedHashSet<>();
        tasks.add("Practice one 90-second answer under time pressure.");
        if (expectedKeywords != null && !expectedKeywords.isEmpty()) {
            tasks.add("Use these keywords naturally in your next answer: " + String.join(", ", expectedKeywords.stream().limit(4).toList()) + ".");
        }
        if (weaknessSignals.stream().anyMatch(item -> item.contains("Hesitation"))) {
            tasks.add("Speak one answer without using 'um', 'uh', or 'maybe'.");
        }
        if (weaknessSignals.stream().anyMatch(item -> item.contains("Impact"))) {
            tasks.add("Add one measurable result to every practice answer.");
        }
        return List.copyOf(tasks);
    }

    private List<String> targetedQuestions(String question, List<String> weaknessSignals) {
        LinkedHashSet<String> prompts = new LinkedHashSet<>();
        prompts.add(question + " Keep it under 75 seconds.");
        prompts.add(question + " End with a quantified result.");
        if (weaknessSignals.stream().anyMatch(item -> item.contains("Structure"))) {
            prompts.add("Re-answer using STAR only: " + question);
        }
        if (weaknessSignals.stream().anyMatch(item -> item.contains("Hesitation"))) {
            prompts.add("Re-answer with decisive language and no fillers: " + question);
        }
        return List.copyOf(prompts);
    }

    private String determineNextDifficulty(
            BigDecimal correctness,
            BigDecimal confidence,
            BigDecimal relevance,
            BigDecimal clarity,
            BigDecimal completeness,
            String currentDifficulty,
            boolean adaptiveEnabled
    ) {
        if (!adaptiveEnabled) {
            return currentDifficulty;
        }
        double average = List.of(correctness, confidence, relevance, clarity, completeness).stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(70.0);
        if (average >= 84) {
            return "hard".equalsIgnoreCase(currentDifficulty) ? "expert" : "hard";
        }
        if (average <= 68) {
            return "easy";
        }
        return "medium";
    }

    private List<String> extractCoachKeywords(String question, String answerDraft, String targetRole, String companyName) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        for (String token : tokenize(String.join(" ", List.of(question, answerDraft, targetRole, companyName)))) {
            if (token.length() > 3) {
                keywords.add(normalizeKeyword(token));
            }
        }
        return keywords.stream().limit(5).toList();
    }

    private int fillerWordCount(String answerText) {
        String lowered = answerText == null ? "" : answerText.toLowerCase(Locale.ROOT);
        return List.of("um", "uh", "like", "you know", "maybe", "kind of").stream()
                .mapToInt(filler -> lowered.split("\\b" + Pattern.quote(filler) + "\\b", -1).length - 1)
                .sum();
    }

    private int countImpactSignals(String answerText) {
        String lowered = answerText == null ? "" : answerText.toLowerCase(Locale.ROOT);
        int actionWords = (int) List.of("improved", "reduced", "grew", "saved", "delivered", "launched", "optimized").stream()
                .filter(lowered::contains)
                .count();
        int metricHits = lowered.split("\\b\\d+[%x]?\\b", -1).length - 1;
        return actionWords + metricHits;
    }

    private String vocabularyFeedback(String answerText) {
        Set<String> uniqueWords = new LinkedHashSet<>(tokenize(answerText));
        if (uniqueWords.size() < 18) {
            return "Vocabulary is understandable, but you can sound more senior by using more precise technical and business terms.";
        }
        return "Vocabulary range is solid. Keep pairing technical nouns with outcome-focused verbs.";
    }

    private String toneFeedback(String answerText) {
        String lowered = answerText.toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(lowered)) {
            return "Tone feedback will become more reliable once a transcript is available.";
        }
        if (lowered.contains("maybe") || lowered.contains("kind of") || lowered.contains("i think")) {
            return "Tone is thoughtful but hesitant. Use more decisive language around your actions and judgment.";
        }
        return "Tone is professional and direct. Maintain that confidence while adding specific evidence.";
    }

    private String fluencyFeedback(String answerText) {
        if (!StringUtils.hasText(answerText)) {
            return "Fluency analysis needs a transcript or browser speech recognition hint to score spoken delivery.";
        }
        int sentenceCount = countSentences(answerText);
        if (sentenceCount <= 1 || answerText.toLowerCase(Locale.ROOT).contains("um") || answerText.toLowerCase(Locale.ROOT).contains("uh")) {
            return "Fluency would improve with shorter spoken sentences and fewer fillers between key points.";
        }
        return "Fluency is steady overall. Keep transitions crisp so the answer sounds easy to follow.";
    }

    private String polishAnswer(String answerText) {
        String polished = answerText.trim().replaceAll("\\s+", " ");
        if (polished.isEmpty()) {
            return "";
        }
        polished = Character.toUpperCase(polished.charAt(0)) + polished.substring(1);
        if (!polished.endsWith(".") && !polished.endsWith("!") && !polished.endsWith("?")) {
            polished = polished + ".";
        }
        return polished;
    }

    private List<String> extractSkillCandidates(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        Set<String> vocabulary = Set.of(
                "java", "spring", "spring boot", "react", "typescript", "javascript", "postgresql",
                "python", "fastapi", "docker", "kubernetes", "aws", "tailwind", "rest",
                "microservices", "ci/cd", "git", "html", "css", "sql", "pandas", "terraform",
                "linux", "hibernate", "analytics", "statistics", "visualization", "flask", "django"
        );
        LinkedHashSet<String> matches = new LinkedHashSet<>();
        String lowered = text.toLowerCase(Locale.ROOT);
        for (String skill : vocabulary) {
            if (lowered.contains(skill)) {
                matches.add(normalizeKeyword(skill));
            }
        }
        return matches.stream().limit(12).toList();
    }

    private String normalizeKeyword(String keyword) {
        return switch (keyword) {
            case "ci/cd" -> "CI/CD";
            case "aws" -> "AWS";
            case "sql" -> "SQL";
            case "html" -> "HTML";
            case "css" -> "CSS";
            default -> keyword.substring(0, 1).toUpperCase(Locale.ROOT) + keyword.substring(1);
        };
    }

    private List<String> tokenize(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return Pattern.compile("[^a-zA-Z0-9+/.-]+")
                .splitAsStream(text.toLowerCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .toList();
    }

    private int countSentences(String text) {
        return Math.max(1, text.split("[.!?]+").length);
    }

    private int countWords(String text) {
        return tokenize(text).size();
    }

    private double fluencyBand(double wordsPerMinute) {
        if (wordsPerMinute == 0) {
            return 0.05;
        }
        if (wordsPerMinute >= 110 && wordsPerMinute <= 170) {
            return 0.34;
        }
        if (wordsPerMinute >= 90 && wordsPerMinute <= 190) {
            return 0.26;
        }
        return 0.18;
    }

    private double clarityBand(String transcript) {
        if (!StringUtils.hasText(transcript)) {
            return 0.08;
        }
        double sentenceFactor = Math.min(0.20, countSentences(transcript) / 10.0);
        double structureFactor = transcript.toLowerCase(Locale.ROOT).contains("result") ? 0.14 : 0.08;
        return sentenceFactor + structureFactor;
    }

    private String pronunciationFeedback(double wordsPerMinute, int fillerPenalty, String transcript) {
        if (!StringUtils.hasText(transcript)) {
            return "Audio was received, but a reliable transcript was not produced. Check browser speech recognition or enable Whisper in the AI service.";
        }
        if (fillerPenalty > 2) {
            return "Pronunciation is understandable, but filler-heavy delivery can make articulation sound less crisp. Slow down and separate key ideas.";
        }
        if (wordsPerMinute > 185) {
            return "Your delivery may be too fast for interview clarity. Slow the pace slightly so key technical points land cleanly.";
        }
        if (wordsPerMinute > 0 && wordsPerMinute < 95) {
            return "Your spoken pace is steady but a little slow. Add slightly more energy so the answer sounds more confident.";
        }
        return "Pronunciation and pacing appear clear enough for interview delivery. Keep emphasizing technical nouns and outcomes distinctly.";
    }

    private BigDecimal score(double fraction) {
        return BigDecimal.valueOf(fraction * 100).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal clamp(BigDecimal value) {
        if (value.compareTo(BigDecimal.valueOf(45)) < 0) {
            return BigDecimal.valueOf(45).setScale(2, RoundingMode.HALF_UP);
        }
        if (value.compareTo(BigDecimal.valueOf(98)) > 0) {
            return BigDecimal.valueOf(98).setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private record ResumeAnalysisRequest(String fileName, String resumeText) {
    }

    private record RoleRecommendationRequest(String summary, List<String> skills) {
    }

    private record AnswerScoreRequest(String question, String expectedPoints, String answerText, String context) {
    }

    private record GrammarCheckRequest(String answerText) {
    }

    private record CoachAnswerRequest(
            String question,
            String answerDraft,
            String coachingIntensity,
            boolean silenceDetected,
            String targetRole,
            String companyName
    ) {
    }

    private record GenerateQuestionsRequest(
            List<String> selectedRoles,
            String personalityProfile,
            List<String> technicalSkills,
            String resumeSummary,
            List<String> experience,
            List<String> projects,
            List<String> strengths,
            List<String> weaknesses,
            List<String> missingSkills,
            String companyName,
            String companyCulture,
            String companyHistory,
            List<String> companyFocusAreas,
            String interviewerTone,
            String coachingIntensity,
            String realityMode,
            boolean adaptiveDifficultyEnabled,
            boolean liveCoachingEnabled
    ) {
    }

    public record ResumeAnalysis(
            String candidateName,
            List<String> contactInfo,
            String summary,
            List<String> strengths,
            List<String> weaknesses,
            List<String> extractedSkills,
            List<String> education,
            List<String> experience,
            List<String> projects,
            List<String> certifications,
            List<String> missingSkills,
            List<String> strengthIndicators,
            List<String> weaknessIndicators,
            List<String> improvementRoadmap,
            List<String> learningSuggestions,
            String mentorGuidance,
            List<String> recommendedRoles,
            double readinessScore
    ) {
    }

    public record RoleRecommendation(List<String> recommendedRoles) {
    }

    public record AnswerScore(
            BigDecimal correctnessScore,
            BigDecimal confidenceScore,
            BigDecimal relevanceScore,
            BigDecimal clarityScore,
            BigDecimal completenessScore,
            BigDecimal structureScore,
            BigDecimal impactScore,
            BigDecimal hesitationScore,
            int fillerWordCount,
            String emotionSignal,
            String mentorSuggestions,
            List<String> liveCoachingHints,
            List<String> weaknessSignals,
            List<String> weeklyImprovementPlan,
            List<String> practiceTasks,
            List<String> targetedQuestions,
            String adaptiveDifficultyNote,
            String nextDifficultyLevel
    ) {
    }

    public record GrammarCheck(
            String grammarFeedback,
            String vocabularyFeedback,
            String toneFeedback,
            String fluencyFeedback,
            String pronunciationFeedback,
            String polishedAnswer
    ) {
    }

    public record GeneratedQuestion(
            String prompt,
            String category,
            String expectedPoints,
            String difficulty,
            String interviewerCue,
            Integer timePressureSeconds
    ) {
    }

    public record GeneratedQuestionBundle(List<GeneratedQuestion> questions) {
    }

    public record SpeechAnalysis(
            String transcript,
            BigDecimal confidenceScore,
            BigDecimal fluencyScore,
            BigDecimal clarityScore,
            String emotionSignal,
            String toneFeedback,
            String pronunciationFeedback,
            String fluencyFeedback
    ) {
    }

    public record LiveCoaching(
            List<String> hints,
            List<String> suggestedKeywords,
            String continuationPrompt,
            String structureReminder
    ) {
    }

}
