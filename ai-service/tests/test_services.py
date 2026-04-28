import unittest

from app.services.grammar_service import grammar_service
from app.services.nlp_service import nlp_service
from app.services.speech_service import speech_service


class NLPServiceTest(unittest.TestCase):
    def test_analyze_resume_extracts_sections_skills_and_roles(self) -> None:
        response = nlp_service.analyze_resume(
            "resume.pdf",
            (
                "Jane Doe\n"
                "jane@example.com | +1 555 123 4567 | linkedin.com/in/janedoe\n"
                "Professional Summary\n"
                "Full stack engineer delivering React and Spring Boot products with measurable performance gains.\n"
                "Skills\n"
                "React, TypeScript, Spring Boot, PostgreSQL, Docker\n"
                "Experience\n"
                "Built React and Spring Boot platforms, improved PostgreSQL performance by 30 percent.\n"
                "Projects\n"
                "Interview Prep MCP Platform using React, FastAPI, Docker, and PostgreSQL.\n"
                "Education\n"
                "B.Tech in Computer Science\n"
            ),
        )

        self.assertGreaterEqual(response.readinessScore, 52.0)
        self.assertTrue(response.extractedSkills)
        self.assertTrue(response.recommendedRoles)
        self.assertEqual("Jane Doe", response.candidateName)
        self.assertTrue(response.contactInfo)
        self.assertIn("Full stack engineer", response.summary)
        self.assertIn("React", response.extractedSkills)
        self.assertTrue(response.experience)
        self.assertTrue(response.projects)
        self.assertTrue(response.improvementRoadmap)

    def test_generate_questions_uses_roles_resume_and_personality(self) -> None:
        bundle = nlp_service.generate_questions(
            selected_roles=["Backend Engineer", "Java Developer"],
            personality_profile="Analytical and structured",
            technical_skills=["Java", "Spring Boot", "PostgreSQL"],
            resume_summary="Backend engineer with distributed systems experience.",
            experience=["Improved API latency by 35 percent across a Spring Boot service."],
            projects=["Built an observability dashboard for production incidents."],
            strengths=["Strong ownership and systems thinking."],
            weaknesses=["Need deeper stakeholder communication examples."],
            missing_skills=["Kubernetes"],
            company_name="Google",
            interviewer_tone="Deep technical grilling",
            reality_mode="Interrupted panel",
        )

        self.assertGreaterEqual(len(bundle.questions), 8)
        self.assertTrue(any(question.category == "TECHNICAL" for question in bundle.questions))
        self.assertTrue(any("Kubernetes" in question.prompt for question in bundle.questions))
        self.assertTrue(any("Analytical and structured".lower() in question.prompt.lower() for question in bundle.questions))
        self.assertTrue(any(question.interviewerCue for question in bundle.questions))
        self.assertTrue(any(question.timePressureSeconds > 0 for question in bundle.questions))


class GrammarServiceTest(unittest.TestCase):
    def test_score_and_grammar_feedback_return_structured_signals(self) -> None:
        score = grammar_service.score_answer(
            "How do you scale a backend service?",
            "architecture scalability observability database caching",
            "I would start with observability, caching, and database tuning before splitting services. The result is a safer scaling path with measurable impact.",
            "Roles: Backend Engineer | Coaching intensity: Balanced | Adaptive difficulty: true | Current difficulty: medium",
        )
        grammar = grammar_service.grammar_check("i improved api latency by reducing repeated queries")

        self.assertGreater(score.correctnessScore, 0)
        self.assertGreater(score.relevanceScore, 0)
        self.assertGreater(score.clarityScore, 0)
        self.assertGreater(score.completenessScore, 0)
        self.assertTrue(score.mentorSuggestions)
        self.assertTrue(score.liveCoachingHints)
        self.assertTrue(score.weeklyImprovementPlan)
        self.assertTrue(score.targetedQuestions)
        self.assertIn("Capitalize", grammar.grammarFeedback)
        self.assertTrue(grammar.vocabularyFeedback)
        self.assertTrue(grammar.fluencyFeedback)
        self.assertTrue(grammar.pronunciationFeedback)
        self.assertTrue(grammar.polishedAnswer.endswith("."))

    def test_live_coach_returns_hints_and_continuation_prompt(self) -> None:
        coaching = grammar_service.coach_answer(
            question="Tell me about a project challenge.",
            answer_draft="I improved the service but I am not sure how to explain the result.",
            coaching_intensity="Beginner",
            silence_detected=True,
            target_role="Backend Engineer",
            company_name="TCS",
        )

        self.assertTrue(coaching.hints)
        self.assertTrue(coaching.suggestedKeywords)
        self.assertIn("Continue", coaching.continuationPrompt)
        self.assertIn("STAR", coaching.structureReminder)


class SpeechServiceTest(unittest.TestCase):
    def test_speech_analysis_uses_transcript_hint_when_whisper_is_unavailable(self) -> None:
        analysis = speech_service.analyze_audio(
            "answer.webm",
            b"not-real-audio",
            transcript_hint="i improved the api by removing slow queries and the result was better latency",
            duration_ms=32000,
        )

        self.assertIn("improved", analysis.transcript.lower())
        self.assertGreater(analysis.confidenceScore, 0)
        self.assertGreater(analysis.fluencyScore, 0)
        self.assertGreater(analysis.clarityScore, 0)
        self.assertTrue(analysis.toneFeedback)
        self.assertTrue(analysis.pronunciationFeedback)
        self.assertTrue(analysis.fluencyFeedback)


if __name__ == "__main__":
    unittest.main()
