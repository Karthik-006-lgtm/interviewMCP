from __future__ import annotations

import math
import re

try:
    import httpx
except ImportError:
    httpx = None

try:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.metrics.pairwise import cosine_similarity
except ImportError:
    TfidfVectorizer = None
    cosine_similarity = None

from app.core.config import settings
from app.models.schemas import AnswerScoreResponse, CoachAnswerResponse, GrammarCheckResponse
from app.services.speech_service import speech_service


class GrammarService:
    def score_answer(self, question: str, expected_points: str, answer_text: str, context: str) -> AnswerScoreResponse:
        reference = " ".join([question, expected_points, context]).strip()
        similarity = self._semantic_similarity(reference, answer_text)
        lowered_context = context.lower()

        expected_keywords = {
            token
            for token in re.split(r"[^a-zA-Z0-9+/.-]+", expected_points.lower())
            if token and len(token) > 3
        }
        answer_keywords = {
            token
            for token in re.split(r"[^a-zA-Z0-9+/.-]+", answer_text.lower())
            if token and len(token) > 3
        }
        filler_word_count = self._filler_word_count(answer_text)
        correctness = 48 + (len(expected_keywords & answer_keywords) / max(1, len(expected_keywords))) * 46
        relevance = 45 + similarity * 50
        confidence = self._confidence_score(answer_text)
        clarity = self._clarity_score(answer_text)
        completeness = self._completeness_score(answer_text, len(expected_keywords & answer_keywords), len(expected_keywords))
        structure = self._structure_score(answer_text)
        impact = self._impact_score(answer_text)
        hesitation = self._hesitation_score(answer_text, filler_word_count)
        emotion_signal = self._emotion_signal(confidence, hesitation, answer_text)
        weakness_signals = self._weakness_signals(
            correctness,
            relevance,
            clarity,
            completeness,
            structure,
            impact,
            hesitation,
            filler_word_count,
        )
        coaching_intensity = self._context_value(lowered_context, "coaching intensity")
        current_difficulty = self._context_value(lowered_context, "current difficulty")
        live_hints = self._live_coaching_hints(answer_text, weakness_signals, coaching_intensity)
        weekly_plan = self._weekly_improvement_plan(weakness_signals)
        practice_tasks = self._practice_tasks(weakness_signals, expected_keywords)
        targeted_questions = self._targeted_questions(question, weakness_signals)
        adaptive_note, next_difficulty = self._adaptive_difficulty_note(
            correctness,
            confidence,
            relevance,
            clarity,
            completeness,
            current_difficulty or "medium",
            lowered_context,
        )

        mentor_suggestions = self._mentor_suggestions(
            correctness,
            confidence,
            relevance,
            clarity,
            completeness,
        )
        return AnswerScoreResponse(
            correctnessScore=round(min(correctness, 98.0), 2),
            confidenceScore=round(min(confidence, 97.0), 2),
            relevanceScore=round(min(relevance, 98.0), 2),
            clarityScore=round(min(clarity, 98.0), 2),
            completenessScore=round(min(completeness, 98.0), 2),
            structureScore=round(min(structure, 98.0), 2),
            impactScore=round(min(impact, 98.0), 2),
            hesitationScore=round(min(hesitation, 98.0), 2),
            fillerWordCount=filler_word_count,
            emotionSignal=emotion_signal,
            mentorSuggestions=mentor_suggestions,
            liveCoachingHints=live_hints,
            weaknessSignals=weakness_signals,
            weeklyImprovementPlan=weekly_plan,
            practiceTasks=practice_tasks,
            targetedQuestions=targeted_questions,
            adaptiveDifficultyNote=adaptive_note,
            nextDifficultyLevel=next_difficulty,
        )

    def coach_answer(
        self,
        question: str,
        answer_draft: str,
        coaching_intensity: str,
        silence_detected: bool,
        target_role: str,
        company_name: str,
    ) -> CoachAnswerResponse:
        hints = self._live_coaching_hints(
            answer_draft,
            self._weakness_signals(
                self._confidence_score(answer_draft),
                self._semantic_similarity(question, answer_draft) * 100 if answer_draft.strip() else 52,
                self._clarity_score(answer_draft),
                self._completeness_score(answer_draft, 0, 1),
                self._structure_score(answer_draft),
                self._impact_score(answer_draft),
                self._hesitation_score(answer_draft, self._filler_word_count(answer_draft)),
                self._filler_word_count(answer_draft),
            ),
            coaching_intensity,
        )
        continuation_prompt = (
            f"Continue by tying your example back to the expectations of {company_name or 'the company'} and the {target_role or 'target'} role."
            if silence_detected
            else "Keep going by adding the action you took, the trade-off you considered, and the result."
        )
        suggested_keywords = self._extract_coach_keywords(question, answer_draft, target_role, company_name)
        return CoachAnswerResponse(
            hints=hints,
            suggestedKeywords=suggested_keywords,
            continuationPrompt=continuation_prompt,
            structureReminder="Use STAR: situation, task, action, result, then close with impact."
        )

    def grammar_check(self, answer_text: str) -> GrammarCheckResponse:
        language_tool_feedback = self._language_tool_feedback(answer_text)
        heuristic_feedback = self._heuristic_feedback(answer_text)
        grammar_feedback = language_tool_feedback or heuristic_feedback

        return GrammarCheckResponse(
            grammarFeedback=grammar_feedback,
            vocabularyFeedback=self._vocabulary_feedback(answer_text),
            toneFeedback=self._tone_feedback(answer_text),
            fluencyFeedback=self._fluency_feedback(answer_text),
            pronunciationFeedback=speech_service.pronunciation_feedback(),
            polishedAnswer=self._polish_answer(answer_text),
        )

    def _language_tool_feedback(self, answer_text: str) -> str:
        if not settings.language_tool_url or httpx is None:
            return ""

        try:
            response = httpx.post(
                settings.language_tool_url.rstrip("/") + "/v2/check",
                data={"text": answer_text, "language": "en-US"},
                timeout=4.0,
            )
            response.raise_for_status()
            matches = response.json().get("matches", [])[:4]
            if not matches:
                return ""
            messages = [match.get("message", "Grammar issue detected.") for match in matches]
            return " ".join(messages)
        except Exception:
            return ""

    def _heuristic_feedback(self, answer_text: str) -> str:
        notes: list[str] = []
        if re.search(r"\bi\b", answer_text):
            notes.append("Capitalize the pronoun 'I' to improve polish.")
        if "  " in answer_text:
            notes.append("Remove repeated spaces for cleaner written structure.")
        if not answer_text.strip().endswith((".", "!", "?")):
            notes.append("Close the answer with punctuation so it sounds more deliberate.")
        sentences = [sentence for sentence in re.split(r"[.!?]+", answer_text) if sentence.strip()]
        if any(len(sentence.split()) > 32 for sentence in sentences):
            notes.append("Shorten long sentences so the answer is easier to follow when spoken.")
        if len(answer_text.split()) < 25:
            notes.append("Add one more concrete detail or result so the response feels complete.")
        return " ".join(notes) if notes else "Grammar is solid overall; focus on sharper phrasing and cleaner transitions."

    def _tone_feedback(self, answer_text: str) -> str:
        lowered = answer_text.lower()
        if any(hedge in lowered for hedge in ["maybe", "kind of", "sort of", "i think"]):
            return "Your tone is thoughtful but slightly hesitant. Use more decisive language around your actions and impact."
        return "Tone is professional and direct. Keep balancing confidence with specific evidence."

    def _vocabulary_feedback(self, answer_text: str) -> str:
        unique_words = set(self._tokenize(answer_text))
        if len(unique_words) < 18:
            return "Vocabulary is understandable, but adding more precise technical and business language would make the answer sound stronger."
        return "Vocabulary range is solid. Keep pairing technical nouns with result-oriented verbs."

    def _fluency_feedback(self, answer_text: str) -> str:
        lowered = answer_text.lower()
        if any(filler in lowered for filler in ["um", "uh", "like", "you know"]):
            return "Fluency would improve by reducing fillers and pausing between key ideas."
        if len(re.split(r"[.!?]+", answer_text)) <= 2:
            return "Fluency is acceptable, but adding cleaner transitions between points would help spoken delivery."
        return "Fluency is steady overall. Keep your pace controlled and your transitions intentional."

    def _polish_answer(self, answer_text: str) -> str:
        polished = re.sub(r"\s+", " ", answer_text.strip())
        if not polished:
            return ""
        polished = polished[0].upper() + polished[1:]
        if not polished.endswith((".", "!", "?")):
            polished += "."
        return polished

    def _confidence_score(self, answer_text: str) -> float:
        word_count = len(answer_text.split())
        filler_penalty = self._filler_word_count(answer_text)
        base = 58 + min(word_count, 180) * 0.18
        adjusted = base - (filler_penalty * 4)
        return max(45.0, min(adjusted, 96.0))

    def _clarity_score(self, answer_text: str) -> float:
        sentences = [sentence.strip() for sentence in re.split(r"[.!?]+", answer_text) if sentence.strip()]
        if not sentences:
            return 40.0
        average_length = sum(len(sentence.split()) for sentence in sentences) / len(sentences)
        structure_bonus = 8 if any(word in answer_text.lower() for word in ["result", "impact", "because"]) else 0
        score = 82 - max(0, average_length - 18) * 1.4 + structure_bonus
        return max(45.0, min(score, 97.0))

    def _completeness_score(self, answer_text: str, keyword_hits: int, expected_count: int) -> float:
        coverage = keyword_hits / max(1, expected_count)
        length_factor = min(1.0, len(answer_text.split()) / 85)
        score = 44 + coverage * 34 + length_factor * 20
        return max(48.0, min(score, 97.0))

    def _structure_score(self, answer_text: str) -> float:
        lowered = answer_text.lower()
        markers = sum(1 for token in ["situation", "task", "action", "result", "impact", "because"] if token in lowered)
        sentences = [sentence.strip() for sentence in re.split(r"[.!?]+", answer_text) if sentence.strip()]
        score = 50 + (markers * 8) + min(18, len(sentences) * 3)
        return max(45.0, min(score, 98.0))

    def _impact_score(self, answer_text: str) -> float:
        lowered = answer_text.lower()
        metric_hits = len(re.findall(r"\b\d+[%x]?\b", lowered))
        impact_words = sum(1 for word in ["improved", "reduced", "grew", "saved", "delivered", "launched", "optimized"] if word in lowered)
        score = 46 + min(20, metric_hits * 9) + min(22, impact_words * 6)
        return max(42.0, min(score, 97.0))

    def _hesitation_score(self, answer_text: str, filler_word_count: int) -> float:
        lowered = answer_text.lower()
        hedge_hits = sum(lowered.count(term) for term in ["maybe", "i think", "kind of", "sort of", "probably"])
        score = 44 + min(32, filler_word_count * 5) + min(16, hedge_hits * 4)
        return max(42.0, min(score, 96.0))

    def _emotion_signal(self, confidence: float, hesitation: float, answer_text: str) -> str:
        lowered = answer_text.lower()
        if hesitation >= 74 or "sorry" in lowered:
            return "Nervous but coachable"
        if confidence >= 82:
            return "Confident and composed"
        if confidence >= 70:
            return "Steady with room to sound more assertive"
        return "Cautious and still warming up"

    def _weakness_signals(
        self,
        correctness: float,
        relevance: float,
        clarity: float,
        completeness: float,
        structure: float,
        impact: float,
        hesitation: float,
        filler_word_count: int,
    ) -> list[str]:
        weaknesses: list[str] = []
        if structure < 72:
            weaknesses.append("Structure is weak; STAR framing is incomplete.")
        if impact < 70:
            weaknesses.append("Impact is underplayed; outcomes and metrics are too soft.")
        if relevance < 74:
            weaknesses.append("Answer drift is visible; stay closer to the exact interviewer ask.")
        if completeness < 74:
            weaknesses.append("The conclusion is thin; add outcome, trade-off, and follow-up action.")
        if correctness < 74:
            weaknesses.append("Depth is limited; strengthen technical/business specifics.")
        if clarity < 74:
            weaknesses.append("Clarity drops in longer sentences; tighten sequencing.")
        if hesitation >= 72 or filler_word_count >= 3:
            weaknesses.append("Hesitation is noticeable; reduce fillers and hedging phrases.")
        return weaknesses[:5]

    def _live_coaching_hints(
        self,
        answer_text: str,
        weakness_signals: list[str],
        coaching_intensity: str,
    ) -> list[str]:
        lowered_intensity = (coaching_intensity or "balanced").lower()
        hints: list[str] = []
        if not answer_text.strip():
            hints.append("Start with the context in one sentence before jumping into details.")
        if any("Structure" in item for item in weakness_signals):
            hints.append("Use STAR explicitly: context, action, result.")
        if any("Impact" in item for item in weakness_signals):
            hints.append("Add one number or business result to prove impact.")
        if any("Hesitation" in item for item in weakness_signals):
            hints.append("Replace hedging with direct ownership language like 'I decided' or 'I led'.")
        if any("Answer drift" in item for item in weakness_signals):
            hints.append("Restate the question goal and tie each point back to it.")
        if len(hints) < 2:
            hints.append("Give one specific example and end with what changed because of your work.")

        if lowered_intensity == "advanced":
            return hints[:1]
        if lowered_intensity == "beginner":
            hints.extend([
                "Keep your answer under four clear steps.",
                "Close with the result and what you learned."
            ])
            return list(dict.fromkeys(hints))[:4]
        return list(dict.fromkeys(hints))[:2]

    def _weekly_improvement_plan(self, weakness_signals: list[str]) -> list[str]:
        plan = ["Day 1: Rewrite one answer using STAR with a stronger closing result."]
        if any("Hesitation" in item for item in weakness_signals):
            plan.append("Day 2: Record two one-minute answers and remove filler words on replay.")
        if any("Impact" in item for item in weakness_signals):
            plan.append("Day 3: Add metrics and business outcomes to three project stories.")
        if any("Structure" in item for item in weakness_signals):
            plan.append("Day 4: Practice opening, action, and result transitions for one technical answer.")
        plan.append("Day 5: Run a timed mock answer and finish with trade-offs plus next steps.")
        return plan[:5]

    def _practice_tasks(self, weakness_signals: list[str], expected_keywords: set[str]) -> list[str]:
        tasks = ["Practice one 90-second answer under time pressure."]
        if expected_keywords:
            tasks.append(f"Use these keywords naturally in your next answer: {', '.join(sorted(expected_keywords)[:4])}.")
        if any("Hesitation" in item for item in weakness_signals):
            tasks.append("Speak one answer without using 'um', 'uh', or 'maybe'.")
        if any("Impact" in item for item in weakness_signals):
            tasks.append("Add one measurable result to every practice answer.")
        return tasks[:4]

    def _targeted_questions(self, question: str, weakness_signals: list[str]) -> list[str]:
        prompts = [
            f"Answer again: {question} but keep it under 75 seconds.",
            f"Answer again: {question} and end with a quantified result.",
        ]
        if any("Structure" in item for item in weakness_signals):
            prompts.append(f"Re-answer this using STAR only: {question}")
        if any("Hesitation" in item for item in weakness_signals):
            prompts.append(f"Re-answer this with decisive language and no fillers: {question}")
        return prompts[:4]

    def _adaptive_difficulty_note(
        self,
        correctness: float,
        confidence: float,
        relevance: float,
        clarity: float,
        completeness: float,
        current_difficulty: str,
        context: str,
    ) -> tuple[str, str]:
        average = (correctness + confidence + relevance + clarity + completeness) / 5
        adaptive_enabled = "adaptive difficulty: true" in context
        if not adaptive_enabled:
            return ("Adaptive difficulty is disabled for this session.", current_difficulty)
        if average >= 84:
            return (
                "Performance is strong, so the next question should become more difficult and less guided.",
                "hard" if current_difficulty != "hard" else "expert",
            )
        if average <= 68:
            return (
                "Performance is under pressure, so the next question should become more guided with lower difficulty.",
                "easy",
            )
        return ("Stay at the current challenge level and keep refining structure plus impact.", "medium")

    def _extract_coach_keywords(
        self,
        question: str,
        answer_draft: str,
        target_role: str,
        company_name: str,
    ) -> list[str]:
        seed = f"{question} {target_role} {company_name} {answer_draft}"
        keywords = []
        for token in self._tokenize(seed):
            if token in {"question", "answer", "about"}:
                continue
            keywords.append(token)
        return list(dict.fromkeys(keyword.title() for keyword in keywords))[:5]

    def _context_value(self, context: str, label: str) -> str:
        pattern = rf"{re.escape(label)}\s*:\s*([^|]+)"
        match = re.search(pattern, context, re.IGNORECASE)
        return match.group(1).strip() if match else ""

    def _filler_word_count(self, answer_text: str) -> int:
        lowered = answer_text.lower()
        return sum(lowered.count(filler) for filler in ["um", "uh", "like", "you know", "maybe", "kind of"])

    def _mentor_suggestions(
        self,
        correctness: float,
        confidence: float,
        relevance: float,
        clarity: float,
        completeness: float,
    ) -> str:
        weakest = min(correctness, confidence, relevance, clarity, completeness)
        if weakest == correctness:
            return "Cover the core technical points more directly, then add one concrete example that proves execution depth."
        if weakest == relevance:
            return "Tie each part of your answer back to the question so the interviewer can follow your judgment and trade-offs."
        if weakest == clarity:
            return "Use shorter sentences and a cleaner structure so your logic is easier to follow in real time."
        if weakest == completeness:
            return "Finish with the outcome, the trade-off you considered, and what you would improve next."
        return "Slow down, remove hedging, and finish with the business result so your answer lands with more authority."

    def _semantic_similarity(self, reference: str, answer_text: str) -> float:
        if TfidfVectorizer is not None and cosine_similarity is not None:
            vectorizer = TfidfVectorizer(stop_words="english")
            matrix = vectorizer.fit_transform([reference, answer_text])
            return float(cosine_similarity(matrix[0:1], matrix[1:2])[0][0])

        reference_tokens = set(self._tokenize(reference))
        answer_tokens = set(self._tokenize(answer_text))
        if not reference_tokens or not answer_tokens:
            return 0.0
        return len(reference_tokens & answer_tokens) / math.sqrt(len(reference_tokens) * len(answer_tokens))

    def _tokenize(self, text: str) -> list[str]:
        return [
            token
            for token in re.split(r"[^a-zA-Z0-9+/.-]+", text.lower())
            if token and len(token) > 2
        ]


grammar_service = GrammarService()
