package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.resume.ResumeDtos.ResumeAnalysisResponse;
import com.interviewprep.platform.entity.ResumeProfile;
import com.interviewprep.platform.entity.Skill;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.repository.ResumeProfileRepository;
import com.interviewprep.platform.repository.SkillRepository;
import com.interviewprep.platform.repository.UserRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);
    private static final long MAX_RESUME_BYTES = 8 * 1024 * 1024;
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/octet-stream"
    );

    private final ResumeProfileRepository resumeProfileRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final AiOrchestrationService aiOrchestrationService;
    private final JsonStorageService jsonStorageService;

    public ResumeService(
            ResumeProfileRepository resumeProfileRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            AiOrchestrationService aiOrchestrationService,
            JsonStorageService jsonStorageService
    ) {
        this.resumeProfileRepository = resumeProfileRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.aiOrchestrationService = aiOrchestrationService;
        this.jsonStorageService = jsonStorageService;
    }

    @Value("${app.storage.resume-dir}")
    private String resumeStorageDir;

    @SuppressWarnings("null")
    @Transactional
    public ResumeAnalysisResponse uploadResume(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Resume file cannot be empty");
        }
        if (file.getSize() > MAX_RESUME_BYTES) {
            throw new IllegalArgumentException("Resume file exceeds the 8 MB limit");
        }

        String originalFileName = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        if (!List.of("pdf", "docx").contains(extension)) {
            throw new IllegalArgumentException("Only PDF and DOCX files are supported");
        }
        if (StringUtils.hasText(file.getContentType()) && !SUPPORTED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported resume content type: " + file.getContentType());
        }

        String extractedText;
        try {
            extractedText = extractText(file.getBytes(), extension);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalArgumentException("The uploaded resume could not be read as a valid PDF or DOCX file");
        }
        if (!StringUtils.hasText(extractedText)) {
            throw new IllegalArgumentException("Unable to extract text from the uploaded resume");
        }

        Files.createDirectories(Path.of(resumeStorageDir));
        String storedFileName = UUID.randomUUID() + "-" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = Path.of(resumeStorageDir).resolve(storedFileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        AiOrchestrationService.ResumeAnalysis analysis = aiOrchestrationService.analyzeResume(originalFileName, extractedText);
        List<String> recommendedRoles = analysis.recommendedRoles();
        if (recommendedRoles == null || recommendedRoles.isEmpty()) {
            recommendedRoles = aiOrchestrationService.recommendRoles(analysis.summary(), analysis.extractedSkills()).recommendedRoles();
        }

        ResumeProfile profile = new ResumeProfile();
        profile.setUser(user);
        profile.setOriginalFileName(originalFileName);
        profile.setStoragePath(target.toString());
        profile.setExtractedText(extractedText);
        profile.setCandidateName(analysis.candidateName());
        profile.setContactInfo(jsonStorageService.write(defaultList(analysis.contactInfo())));
        profile.setSummary(analysis.summary());
        profile.setStrengths(jsonStorageService.write(defaultList(analysis.strengths())));
        profile.setWeaknesses(jsonStorageService.write(defaultList(analysis.weaknesses())));
        profile.setExtractedSkills(jsonStorageService.write(defaultList(analysis.extractedSkills())));
        profile.setSkills(resolveSkills(defaultList(analysis.extractedSkills())));
        profile.setEducation(jsonStorageService.write(defaultList(analysis.education())));
        profile.setExperience(jsonStorageService.write(defaultList(analysis.experience())));
        profile.setProjects(jsonStorageService.write(defaultList(analysis.projects())));
        profile.setCertifications(jsonStorageService.write(defaultList(analysis.certifications())));
        profile.setMissingSkills(jsonStorageService.write(defaultList(analysis.missingSkills())));
        profile.setStrengthIndicators(jsonStorageService.write(defaultList(analysis.strengthIndicators())));
        profile.setWeaknessIndicators(jsonStorageService.write(defaultList(analysis.weaknessIndicators())));
        profile.setImprovementRoadmap(jsonStorageService.write(defaultList(analysis.improvementRoadmap())));
        profile.setLearningSuggestions(jsonStorageService.write(defaultList(analysis.learningSuggestions())));
        profile.setMentorGuidance(analysis.mentorGuidance());
        profile.setRecommendedRoles(jsonStorageService.write(defaultList(recommendedRoles)));

        ResumeProfile saved = resumeProfileRepository.save(profile);
        log.info("Stored resume {} for user {} with analysis score {}", saved.getId(), userId, analysis.readinessScore());
        return toResponse(saved, analysis.readinessScore());
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getLatestResume(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ResumeProfile profile = resumeProfileRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No resume uploaded yet"));
        return toResponse(profile, calculateReadiness(profile));
    }

    private ResumeAnalysisResponse toResponse(ResumeProfile profile, double readinessScore) {
        return new ResumeAnalysisResponse(
                profile.getId(),
                profile.getOriginalFileName(),
                profile.getCandidateName(),
                jsonStorageService.readStringList(profile.getContactInfo()),
                profile.getSummary(),
                jsonStorageService.readStringList(profile.getStrengths()),
                jsonStorageService.readStringList(profile.getWeaknesses()),
                jsonStorageService.readStringList(profile.getExtractedSkills()),
                jsonStorageService.readStringList(profile.getEducation()),
                jsonStorageService.readStringList(profile.getExperience()),
                jsonStorageService.readStringList(profile.getProjects()),
                jsonStorageService.readStringList(profile.getCertifications()),
                jsonStorageService.readStringList(profile.getMissingSkills()),
                jsonStorageService.readStringList(profile.getStrengthIndicators()),
                jsonStorageService.readStringList(profile.getWeaknessIndicators()),
                jsonStorageService.readStringList(profile.getImprovementRoadmap()),
                jsonStorageService.readStringList(profile.getLearningSuggestions()),
                profile.getMentorGuidance(),
                jsonStorageService.readStringList(profile.getRecommendedRoles()),
                readinessScore,
                profile.getCreatedAt() != null ? profile.getCreatedAt() : Instant.now()
        );
    }

    private double calculateReadiness(ResumeProfile profile) {
        int skillWeight = jsonStorageService.readStringList(profile.getExtractedSkills()).size() * 3;
        int experienceWeight = jsonStorageService.readStringList(profile.getExperience()).size() * 2;
        int projectWeight = jsonStorageService.readStringList(profile.getProjects()).size() * 2;
        int certificationWeight = jsonStorageService.readStringList(profile.getCertifications()).size();
        int missingPenalty = jsonStorageService.readStringList(profile.getMissingSkills()).size() * 2;
        double score = 54 + skillWeight + experienceWeight + projectWeight + certificationWeight - missingPenalty;
        return Math.max(52.0, Math.min(96.0, score));
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private Set<Skill> resolveSkills(List<String> skillNames) {
        return skillNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(this::loadOrCreateSkill)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private Skill loadOrCreateSkill(String skillName) {
        return skillRepository.findByNameIgnoreCase(skillName)
                .orElseGet(() -> {
                    Skill skill = new Skill();
                    skill.setName(skillName);
                    return skillRepository.save(skill);
                });
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private String extractText(byte[] fileBytes, String extension) throws IOException {
        return switch (extension) {
            case "pdf" -> extractPdfText(fileBytes);
            case "docx" -> extractDocxText(fileBytes);
            default -> "";
        };
    }

    private String extractPdfText(byte[] fileBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            return new PDFTextStripper().getText(document).trim();
        }
    }

    private String extractDocxText(byte[] fileBytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(fileBytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText().trim();
        }
    }
}
