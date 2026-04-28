from __future__ import annotations

import re
import tempfile
from pathlib import Path

try:
    import whisper  # type: ignore
except ImportError:
    whisper = None

from app.core.config import settings
from app.models.schemas import SpeechAnalysisResponse


class SpeechService:
    def __init__(self) -> None:
        self._whisper_model = None

    def analyze_audio(
        self,
        filename: str,
        audio_bytes: bytes,
        transcript_hint: str | None = None,
        duration_ms: int | None = None,
    ) -> SpeechAnalysisResponse:
        transcript = self._transcribe(filename, audio_bytes, transcript_hint)
        normalized_transcript = self._normalize_transcript(transcript)
        duration = duration_ms or self._estimate_duration_ms(normalized_transcript)
        filler_penalty = self._filler_penalty(normalized_transcript)
        words_per_minute = self._words_per_minute(normalized_transcript, duration)

        confidence = self._confidence_score(normalized_transcript, filler_penalty)
        fluency = self._fluency_score(words_per_minute, filler_penalty)
        clarity = self._clarity_score(normalized_transcript)

        return SpeechAnalysisResponse(
            transcript=normalized_transcript,
            confidenceScore=round(confidence, 2),
            fluencyScore=round(fluency, 2),
            clarityScore=round(clarity, 2),
            emotionSignal=self._emotion_signal(confidence, fluency, normalized_transcript),
            toneFeedback=self._tone_feedback(normalized_transcript),
            pronunciationFeedback=self.pronunciation_feedback(
                words_per_minute,
                filler_penalty,
                normalized_transcript,
            ),
            fluencyFeedback=self._fluency_feedback(
                normalized_transcript,
                words_per_minute,
                filler_penalty,
            ),
        )

    def pronunciation_feedback(
        self,
        words_per_minute: float | None = None,
        filler_penalty: int = 0,
        transcript: str = "",
    ) -> str:
        if not transcript:
            return (
                "Pronunciation scoring is available once a transcript is captured. "
                "Enable Whisper or browser speech recognition for deeper feedback."
            )
        if filler_penalty > 2:
            return (
                "Pronunciation is understandable, but filler-heavy delivery softens the impact. "
                "Pause between key ideas and articulate technical terms more distinctly."
            )
        if words_per_minute is not None and words_per_minute > 185:
            return (
                "Your pace may be a little fast for interview clarity. Slow down slightly "
                "so important technical details land cleanly."
            )
        if words_per_minute is not None and 0 < words_per_minute < 95:
            return (
                "Your pace is steady but slightly slow. Add a little more energy so the answer "
                "sounds more confident and decisive."
            )
        return (
            "Pronunciation appears clear enough for interview delivery. Keep emphasizing "
            "key outcomes, technologies, and ownership language."
        )

    def _transcribe(self, filename: str, audio_bytes: bytes, transcript_hint: str | None) -> str:
        if whisper is not None and audio_bytes:
            transcribed = self._transcribe_with_whisper(filename, audio_bytes)
            if transcribed:
                return transcribed
        return transcript_hint or ""

    def _transcribe_with_whisper(self, filename: str, audio_bytes: bytes) -> str:
        try:
            suffix = Path(filename or "audio.webm").suffix or ".webm"
            with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temporary_file:
                temporary_file.write(audio_bytes)
                temp_path = Path(temporary_file.name)

            model = self._load_whisper_model()
            result = model.transcribe(str(temp_path), fp16=False)
            return str(result.get("text", "")).strip()
        except Exception:
            return ""
        finally:
            if "temp_path" in locals():
                temp_path.unlink(missing_ok=True)

    def _load_whisper_model(self):
        if self._whisper_model is None:
            self._whisper_model = whisper.load_model(settings.whisper_model_name)
        return self._whisper_model

    def _normalize_transcript(self, transcript: str) -> str:
        cleaned = re.sub(r"\s+", " ", (transcript or "").strip())
        if not cleaned:
            return ""
        cleaned = cleaned[0].upper() + cleaned[1:]
        if not cleaned.endswith((".", "!", "?")):
            cleaned += "."
        return cleaned

    def _estimate_duration_ms(self, transcript: str) -> int:
        word_count = max(1, len(self._tokenize(transcript)))
        estimated_minutes = word_count / 145
        return max(20_000, int(estimated_minutes * 60_000))

    def _words_per_minute(self, transcript: str, duration_ms: int | None) -> float:
        if not transcript or not duration_ms or duration_ms <= 0:
            return 0.0
        return len(self._tokenize(transcript)) / (duration_ms / 60_000)

    def _confidence_score(self, transcript: str, filler_penalty: int) -> float:
        word_count = len(self._tokenize(transcript))
        base = 56 + min(word_count, 180) * 0.19
        adjusted = base - (filler_penalty * 3.5)
        return max(45.0, min(adjusted, 97.0))

    def _fluency_score(self, words_per_minute: float, filler_penalty: int) -> float:
        if words_per_minute == 0:
            return 45.0
        if 110 <= words_per_minute <= 170:
            band = 92.0
        elif 90 <= words_per_minute <= 185:
            band = 85.0
        else:
            band = 77.0
        return max(45.0, min(band - (filler_penalty * 3.0), 97.0))

    def _clarity_score(self, transcript: str) -> float:
        if not transcript:
            return 45.0
        sentences = [sentence.strip() for sentence in re.split(r"[.!?]+", transcript) if sentence.strip()]
        average_length = sum(len(sentence.split()) for sentence in sentences) / max(1, len(sentences))
        structure_bonus = 7 if any(word in transcript.lower() for word in ["because", "result", "impact"]) else 0
        score = 84 - max(0, average_length - 17) * 1.4 + structure_bonus
        return max(45.0, min(score, 97.0))

    def _tone_feedback(self, transcript: str) -> str:
        lowered = transcript.lower()
        if not lowered:
            return "Tone feedback will become more specific once a transcript is available."
        if any(hedge in lowered for hedge in ["maybe", "kind of", "sort of", "i think"]):
            return (
                "Tone is thoughtful but slightly hesitant. Replace hedging phrases with direct "
                "ownership statements and clearer decisions."
            )
        return "Tone is professional and composed. Keep that confidence while grounding points in specific evidence."

    def _fluency_feedback(self, transcript: str, words_per_minute: float, filler_penalty: int) -> str:
        if not transcript:
            return "Fluency analysis needs a transcript or speech-recognition hint to evaluate pacing and transitions."
        if filler_penalty > 2:
            return "Fluency would improve with fewer fillers and slightly longer pauses between major points."
        if words_per_minute > 185:
            return "Fluency is energetic, but the pace is high. Slow down slightly so each step is easier to follow."
        if 0 < words_per_minute < 95:
            return "Fluency is controlled but a bit slow. Try a more natural conversational pace for stronger presence."
        if len(re.split(r"[.!?]+", transcript)) <= 2:
            return "Fluency is acceptable, but adding cleaner transitions between ideas would strengthen delivery."
        return "Fluency is steady overall. Your pacing and transitions support interview clarity."

    def _emotion_signal(self, confidence: float, fluency: float, transcript: str) -> str:
        lowered = transcript.lower()
        if any(token in lowered for token in ["sorry", "maybe", "i think"]):
            return "Slightly nervous"
        if confidence >= 84 and fluency >= 82:
            return "Confident"
        if confidence >= 72:
            return "Composed"
        return "Still warming up"

    def _filler_penalty(self, transcript: str) -> int:
        lowered = transcript.lower()
        return sum(lowered.count(filler) for filler in [" um ", " uh ", " like ", " you know "])

    def _tokenize(self, text: str) -> list[str]:
        return [
            token
            for token in re.split(r"[^a-zA-Z0-9+/.-]+", text.lower())
            if token and len(token) > 1
        ]


speech_service = SpeechService()
