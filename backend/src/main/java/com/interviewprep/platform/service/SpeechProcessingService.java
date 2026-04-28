package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.interview.InterviewDtos.AudioProcessingResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SpeechProcessingService {

    private static final Logger log = LoggerFactory.getLogger(SpeechProcessingService.class);
    private static final Set<String> SUPPORTED_AUDIO_EXTENSIONS = Set.of("webm", "wav", "mp3", "m4a", "ogg");
    private static final Set<String> SUPPORTED_AUDIO_CONTENT_TYPES = Set.of(
            "audio/webm",
            "audio/wav",
            "audio/x-wav",
            "audio/mpeg",
            "audio/mp3",
            "audio/mp4",
            "audio/ogg"
    );
    private static final long MAX_AUDIO_BYTES = 15 * 1024 * 1024;

    private final AiOrchestrationService aiOrchestrationService;

    @Value("${app.storage.audio-dir}")
    private String audioStorageDir;

    public SpeechProcessingService(AiOrchestrationService aiOrchestrationService) {
        this.aiOrchestrationService = aiOrchestrationService;
    }

    public AudioProcessingResponse uploadAndAnalyzeAudio(
            Long userId,
            MultipartFile file,
            String transcriptHint,
            Long durationMs
    ) throws IOException {
        validateAudio(file);

        Path userAudioDir = Path.of(audioStorageDir).resolve(String.valueOf(userId));
        Files.createDirectories(userAudioDir);

        String originalFileName = file.getOriginalFilename() == null ? "voice-answer.webm" : file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + "." + extension;
        Path storedFilePath = userAudioDir.resolve(storedFileName).normalize();

        Files.copy(file.getInputStream(), storedFilePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Stored interview audio for user {} at {}", userId, storedFilePath);

        AiOrchestrationService.SpeechAnalysis analysis = aiOrchestrationService.analyzeSpeech(
                storedFilePath,
                transcriptHint,
                durationMs
        );

        return new AudioProcessingResponse(
                userId + "/" + storedFileName,
                analysis.transcript(),
                analysis.confidenceScore(),
                analysis.fluencyScore(),
                analysis.clarityScore(),
                analysis.emotionSignal(),
                analysis.toneFeedback(),
                analysis.pronunciationFeedback(),
                analysis.fluencyFeedback(),
                durationMs,
                Instant.now()
        );
    }

    public AiOrchestrationService.SpeechAnalysis analyzeStoredAudio(
            Long userId,
            String audioReference,
            String transcriptHint,
            Long durationMs
    ) {
        if (!StringUtils.hasText(audioReference)) {
            return aiOrchestrationService.fallbackSpeechAnalysis(transcriptHint, durationMs);
        }
        Path resolved = resolveAudioPath(userId, audioReference);
        return aiOrchestrationService.analyzeSpeech(resolved, transcriptHint, durationMs);
    }

    private Path resolveAudioPath(Long userId, String audioReference) {
        String normalizedReference = audioReference.replace("\\", "/");
        String expectedPrefix = userId + "/";
        if (!normalizedReference.startsWith(expectedPrefix)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this audio recording");
        }

        Path baseDir = Path.of(audioStorageDir).resolve(String.valueOf(userId)).normalize();
        Path resolvedPath = baseDir.resolve(normalizedReference.substring(expectedPrefix.length())).normalize();

        if (!resolvedPath.startsWith(baseDir) || !Files.exists(resolvedPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Audio recording not found");
        }
        return resolvedPath;
    }

    private void validateAudio(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Audio file cannot be empty");
        }
        if (file.getSize() > MAX_AUDIO_BYTES) {
            throw new IllegalArgumentException("Audio file exceeds the 15 MB limit");
        }
        String originalFileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        if (!SUPPORTED_AUDIO_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only WEBM, WAV, MP3, M4A, and OGG audio files are supported");
        }
        if (StringUtils.hasText(file.getContentType())) {
            String contentType = file.getContentType().toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
            if (!SUPPORTED_AUDIO_CONTENT_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Unsupported audio content type: " + file.getContentType());
            }
        }
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "webm";
        }
        String extension = fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
        return SUPPORTED_AUDIO_EXTENSIONS.contains(extension) ? extension : "webm";
    }
}
