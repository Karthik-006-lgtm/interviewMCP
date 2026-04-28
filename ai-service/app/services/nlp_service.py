from __future__ import annotations

import math
import re
from collections import Counter
from typing import Any

try:
    import spacy
except ImportError:
    spacy = None

try:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.metrics.pairwise import cosine_similarity
except ImportError:
    TfidfVectorizer = None
    cosine_similarity = None

from app.models.schemas import (
    GenerateQuestionsResponse,
    GeneratedQuestion,
    RecommendRoleResponse,
    ResumeAnalysisResponse,
)


ROLE_SKILL_MAP: dict[str, list[str]] = {
    "Java Developer": ["java", "spring", "spring boot", "sql", "microservices", "rest"],
    "Python Developer": ["python", "fastapi", "flask", "django", "pandas", "automation"],
    "Full Stack Developer": ["react", "typescript", "java", "spring", "postgresql", "rest"],
    "Data Analyst": ["python", "sql", "analytics", "dashboard", "visualization", "statistics"],
    "DevOps Engineer": ["docker", "kubernetes", "aws", "terraform", "ci/cd", "linux"],
    "Frontend Engineer": ["react", "typescript", "javascript", "css", "html", "tailwind"],
    "Backend Engineer": ["java", "spring", "api", "postgresql", "sql", "microservices"],
}

SKILL_VOCABULARY = {
    "java",
    "spring",
    "spring boot",
    "react",
    "typescript",
    "javascript",
    "postgresql",
    "sql",
    "python",
    "fastapi",
    "docker",
    "kubernetes",
    "aws",
    "tailwind",
    "rest",
    "microservices",
    "ci/cd",
    "git",
    "html",
    "css",
    "pandas",
    "terraform",
    "linux",
    "analytics",
    "visualization",
    "statistics",
    "flask",
    "django",
}


class NLPService:
    def __init__(self) -> None:
        self.nlp: Any | None = None
        if spacy is None:
            return

        try:
            self.nlp = spacy.load("en_core_web_sm")
        except OSError:
            self.nlp = spacy.blank("en")
            if "sentencizer" not in self.nlp.pipe_names:
                self.nlp.add_pipe("sentencizer")

    def analyze_resume(self, file_name: str, resume_text: str) -> ResumeAnalysisResponse:
        normalized = self._normalize_text(resume_text)
        lines = self._extract_lines(resume_text)
        summary_section = self._extract_summary(lines)
        section_skills = self._extract_skill_section(lines)
        skills = self._merge_unique(section_skills, self.extract_skills(normalized))
        recommended_roles = self.recommend_roles("", skills).recommendedRoles
        education = self._extract_section(lines, {"education", "academic background"})
        experience = self._extract_section(lines, {"experience", "work experience", "professional experience"})
        projects = self._extract_section(lines, {"projects", "key projects", "project experience"})
        certifications = self._extract_section(lines, {"certifications", "certificates", "licenses"})
        missing_skills = self._infer_missing_skills(recommended_roles, skills)
        strength_indicators = self._build_strength_indicators(normalized, skills, experience)
        weakness_indicators = self._build_weakness_indicators(education, experience, projects, certifications, missing_skills)
        summary = self._build_summary(file_name, skills, experience, projects)
        strengths = self._build_strengths(normalized, skills, projects, experience)
        weaknesses = self._build_weaknesses(education, experience, projects, certifications, missing_skills)
        improvement_roadmap = self._build_improvement_roadmap(missing_skills, weakness_indicators)
        learning_suggestions = self._build_learning_suggestions(recommended_roles, missing_skills)
        readiness_score = self._score_resume(skills, experience, projects, certifications, missing_skills, strength_indicators)

        return ResumeAnalysisResponse(
            candidateName=self._extract_candidate_name(lines, file_name),
            contactInfo=self._extract_contact_info(resume_text),
            summary=summary_section or summary,
            strengths=strengths,
            weaknesses=weaknesses,
            extractedSkills=skills,
            education=education,
            experience=experience,
            projects=projects,
            certifications=certifications,
            missingSkills=missing_skills,
            strengthIndicators=strength_indicators,
            weaknessIndicators=weakness_indicators,
            improvementRoadmap=improvement_roadmap,
            learningSuggestions=learning_suggestions,
            mentorGuidance=self._build_mentor_guidance(recommended_roles, missing_skills),
            recommendedRoles=recommended_roles,
            readinessScore=readiness_score,
        )

    def recommend_roles(self, summary: str, skills: list[str]) -> RecommendRoleResponse:
        candidate_document = " ".join([summary, *skills]).strip()
        if not candidate_document:
            return RecommendRoleResponse(recommendedRoles=list(ROLE_SKILL_MAP.keys())[:4])

        if TfidfVectorizer is not None and cosine_similarity is not None:
            role_documents = [" ".join(keywords) for keywords in ROLE_SKILL_MAP.values()]
            vectorizer = TfidfVectorizer(ngram_range=(1, 2))
            matrix = vectorizer.fit_transform([*role_documents, candidate_document])
            similarities = cosine_similarity(matrix[-1], matrix[:-1]).flatten()
            ranked_roles = [
                role
                for role, _ in sorted(
                    zip(ROLE_SKILL_MAP.keys(), similarities, strict=True),
                    key=lambda item: item[1],
                    reverse=True,
                )
            ]
        else:
            candidate_tokens = set(self._tokenize(candidate_document))
            ranked_roles = sorted(
                ROLE_SKILL_MAP.keys(),
                key=lambda role: self._keyword_similarity(
                    candidate_tokens, set(self._tokenize(" ".join(ROLE_SKILL_MAP[role])))
                ),
                reverse=True,
            )
        return RecommendRoleResponse(recommendedRoles=ranked_roles[:4])

    def generate_questions(
        self,
        selected_roles: list[str],
        personality_profile: str,
        technical_skills: list[str] | None = None,
        resume_summary: str = "",
        experience: list[str] | None = None,
        projects: list[str] | None = None,
        strengths: list[str] | None = None,
        weaknesses: list[str] | None = None,
        missing_skills: list[str] | None = None,
        company_name: str = "",
        company_culture: str = "",
        company_history: str = "",
        company_focus_areas: list[str] | None = None,
        interviewer_tone: str = "Friendly technical panel",
        coaching_intensity: str = "Balanced",
        reality_mode: str = "Standard",
        adaptive_difficulty_enabled: bool = True,
        live_coaching_enabled: bool = True,
    ) -> GenerateQuestionsResponse:
        selected_roles = selected_roles or ["Full Stack Developer"]
        company_focus_areas = company_focus_areas or []
        technical_skills = self._merge_unique(
            technical_skills or [],
            self._role_skills_for(selected_roles[0]),
            company_focus_areas,
        )
        experience = experience or []
        projects = projects or []
        strengths = strengths or []
        weaknesses = weaknesses or []
        missing_skills = missing_skills or []

        primary_role = selected_roles[0]
        role_phrase = ", ".join(selected_roles[:2])
        primary_skill = technical_skills[0] if technical_skills else "system design"
        secondary_skill = technical_skills[1] if len(technical_skills) > 1 else "debugging"
        experience_signal = experience[0] if experience else "a recent delivery challenge from your background"
        project_signal = projects[0] if projects else "a production-style project you have shipped"
        strength_signal = strengths[0] if strengths else "ownership and execution"
        weakness_signal = (missing_skills or weaknesses or ["stakeholder communication"])[0]
        summary_signal = resume_summary or f"your fit for {role_phrase}"
        company_phrase = company_name or "this company"
        tone = interviewer_tone.strip() or "Friendly technical panel"
        culture_signal = company_culture or "high ownership and clear communication"
        history_signal = company_history or "a growth-stage technology business"
        pressure_seconds = self._time_pressure_seconds(reality_mode, tone)
        cue = self._interviewer_cue(tone, reality_mode, coaching_intensity, live_coaching_enabled)
        opening_difficulty = "hard" if adaptive_difficulty_enabled else "medium"

        questions = [
            GeneratedQuestion(
                prompt=(
                    f"You are interviewing with {company_phrase}. Walk me through {experience_signal} and explain how it proves your readiness for a {primary_role} role in a team shaped by {culture_signal}."
                ),
                category="EXPERIENCE",
                expectedPoints="Context, ownership, decisions, measurable result, role alignment",
                difficulty=opening_difficulty,
                interviewerCue=cue,
                timePressureSeconds=pressure_seconds,
            ),
            GeneratedQuestion(
                prompt=(
                    f"Design, implement, and test a production-ready solution using {primary_skill} for a {primary_role} interview at {company_phrase}. Make your trade-offs explicit."
                ),
                category="TECHNICAL",
                expectedPoints="Architecture, implementation, testing strategy, trade-offs, scalability",
                difficulty="hard",
                interviewerCue=cue,
                timePressureSeconds=pressure_seconds,
            ),
            GeneratedQuestion(
                prompt=(
                    f"What are the most important trade-offs when solving a real-world problem that depends on {secondary_skill} at {company_phrase}, and how would you defend those trade-offs under follow-up pressure?"
                ),
                category="PROBLEM_SOLVING",
                expectedPoints="Problem framing, constraints, options considered, final decision, risk management",
                difficulty="hard",
                interviewerCue=self._interviewer_cue(tone, reality_mode, coaching_intensity, False),
                timePressureSeconds=max(60, pressure_seconds - 15),
            ),
            GeneratedQuestion(
                prompt=(
                    f"Describe {project_signal} in a way that helps both HR and hiring managers at {company_phrase} understand your impact and team fit."
                ),
                category="HR",
                expectedPoints="Career narrative, business value, collaboration, ownership, concise positioning",
                difficulty="medium",
                interviewerCue=self._interviewer_cue("Friendly HR panel", reality_mode, coaching_intensity, live_coaching_enabled),
                timePressureSeconds=pressure_seconds + 15,
            ),
            GeneratedQuestion(
                prompt=(
                    f"Imagine your new team at {company_phrase} is weak in {weakness_signal}. How would you close the gap while still delivering on deadlines and handling interruptions?"
                ),
                category="REAL_WORLD_SCENARIO",
                expectedPoints="Prioritization, learning plan, collaboration, delivery management, risk control",
                difficulty="medium",
                interviewerCue=self._interviewer_cue(tone, "Interrupted panel" if reality_mode.lower() != "standard" else reality_mode, coaching_intensity, live_coaching_enabled),
                timePressureSeconds=max(55, pressure_seconds - 10),
            ),
            GeneratedQuestion(
                prompt=(
                    f"Given your {personality_profile.lower()} style, how do you handle disagreement, feedback, and shifting priorities at {company_phrase} without losing clarity?"
                ),
                category="COMMUNICATION",
                expectedPoints="Self-awareness, communication style, conflict handling, adaptability, clarity",
                difficulty="medium",
                interviewerCue=cue,
                timePressureSeconds=pressure_seconds,
            ),
            GeneratedQuestion(
                prompt=(
                    f"What should an interviewer at {company_phrase} remember about your {strength_signal.lower()} after hearing your answer to '{summary_signal}'?"
                ),
                category="BEHAVIORAL",
                expectedPoints="Differentiation, confidence, evidence, concise story, memorable takeaway",
                difficulty="medium",
                interviewerCue=cue,
                timePressureSeconds=pressure_seconds,
            ),
            GeneratedQuestion(
                prompt=(
                    f"If a {primary_role} platform at {company_phrase} needed to scale 10x next year, what architectural decisions would you make first given {history_signal}?"
                ),
                category="SYSTEM_DESIGN",
                expectedPoints="Bottlenecks, observability, resilience, data choices, scaling trade-offs",
                difficulty="hard",
                interviewerCue=self._interviewer_cue("Strict technical panel", reality_mode, coaching_intensity, False),
                timePressureSeconds=max(60, pressure_seconds - 20),
            ),
            GeneratedQuestion(
                prompt=(
                    f"Based on your current profile summary, {summary_signal}, what is your strongest final-round value proposition for {role_phrase} roles at {company_phrase}?"
                ),
                category="EXPERIENCE",
                expectedPoints="Narrative clarity, role fit, achievements, seniority signal, closing pitch",
                difficulty="medium",
                interviewerCue=self._interviewer_cue("Founder-style closeout", reality_mode, coaching_intensity, live_coaching_enabled),
                timePressureSeconds=max(45, pressure_seconds - 20),
            ),
        ]
        return GenerateQuestionsResponse(questions=questions)

    def _interviewer_cue(
        self,
        tone: str,
        reality_mode: str,
        coaching_intensity: str,
        live_coaching_enabled: bool,
    ) -> str:
        tone_phrase = tone or "Friendly technical panel"
        reality_phrase = reality_mode or "Standard"
        coaching_phrase = coaching_intensity or "Balanced"
        coach_note = "Live hints available." if live_coaching_enabled else "No coaching assist."
        return f"{tone_phrase}. {reality_phrase}. {coach_note} Coaching level: {coaching_phrase}."

    def _time_pressure_seconds(self, reality_mode: str, tone: str) -> int:
        lowered_mode = (reality_mode or "").lower()
        lowered_tone = (tone or "").lower()
        seconds = 105
        if "offline" in lowered_mode or "lag" in lowered_mode:
            seconds = 75
        if "interruption" in lowered_mode or "panel" in lowered_mode:
            seconds -= 10
        if "strict" in lowered_tone or "grill" in lowered_tone:
            seconds -= 15
        return max(45, seconds)

    def extract_skills(self, text: str) -> list[str]:
        lowered = text.lower()
        matches = []
        for skill in SKILL_VOCABULARY:
            if skill in lowered:
                matches.append(self._normalize_skill(skill))
        if not matches:
            tokens = [
                token
                for token in re.split(r"[^a-zA-Z0-9+/.-]+", lowered)
                if token and len(token) > 2
            ]
            common = Counter(tokens).most_common(8)
            matches = [self._normalize_skill(token) for token, _ in common]
        return list(dict.fromkeys(matches))[:12]

    def _normalize_text(self, value: str) -> str:
        return re.sub(r"\s+", " ", value).strip()

    def _extract_lines(self, text: str) -> list[str]:
        return [line.strip() for line in text.splitlines() if line.strip()]

    def _extract_summary(self, lines: list[str]) -> str:
        summary_items = self._extract_section(
            lines,
            {"summary", "professional summary", "profile", "about", "career summary"},
            limit=4,
        )
        return " ".join(summary_items).strip()

    def _extract_skill_section(self, lines: list[str]) -> list[str]:
        skill_lines = self._extract_section(
            lines,
            {"skills", "technical skills", "core skills", "skills & tools", "tools"},
            limit=8,
        )
        extracted: list[str] = []
        for line in skill_lines:
            extracted.extend(self._extract_skill_line_items(line))
        return self._merge_unique(extracted)

    def _build_summary(self, file_name: str, skills: list[str], experience: list[str], projects: list[str]) -> str:
        skill_phrase = ", ".join(skills[:5]) if skills else "software delivery"
        return (
            f"{file_name} reflects hands-on experience in {skill_phrase}. "
            f"The profile currently shows {len(experience)} experience signals and {len(projects)} project signals that can power tailored interviews."
        )

    def _build_strengths(
        self, text: str, skills: list[str], projects: list[str], experience: list[str]
    ) -> list[str]:
        strengths = []
        if projects:
            strengths.append("Projects are visible, giving you strong material for technical and scenario-based interview answers.")
        if experience:
            strengths.append("Experience history is detectable, which helps build more credible ownership stories.")
        if len(skills) >= 5:
            strengths.append("The resume demonstrates a broad enough toolkit to support multiple target role paths.")
        if re.search(r"\b\d+%|\b\d+\b", text):
            strengths.append("Measurable outcomes are present, which strengthens impact-oriented storytelling.")
        return strengths[:4]

    def _build_weaknesses(
        self,
        education: list[str],
        experience: list[str],
        projects: list[str],
        certifications: list[str],
        missing_skills: list[str],
    ) -> list[str]:
        weaknesses = []
        if not experience:
            weaknesses.append("Add clearer experience bullets that show ownership, execution, and outcomes.")
        if not projects:
            weaknesses.append("Include more detailed projects so the interview engine can generate better real-world questions.")
        if not education and not certifications:
            weaknesses.append("Educational or certification context is missing, which weakens profile completeness.")
        if missing_skills:
            weaknesses.append("Some role-critical skills are not visible yet in the resume narrative.")
        return weaknesses[:4]

    def _build_strength_indicators(
        self, text: str, skills: list[str], experience: list[str]
    ) -> list[str]:
        indicators = []
        if re.search(r"\bled\b|\bowned\b|\bmentored\b", text):
            indicators.append("Leadership and ownership language is present.")
        if experience:
            indicators.append("Experience depth supports behavioral question generation.")
        if len(skills) >= 4:
            indicators.append("Technical breadth supports multiple role recommendations.")
        return indicators[:4]

    def _build_weakness_indicators(
        self,
        education: list[str],
        experience: list[str],
        projects: list[str],
        certifications: list[str],
        missing_skills: list[str],
    ) -> list[str]:
        indicators = []
        if not education:
            indicators.append("Education section not clearly detected.")
        if not experience:
            indicators.append("Experience section needs stronger structure.")
        if not projects:
            indicators.append("Projects section is missing or too thin.")
        if not certifications:
            indicators.append("No certifications were detected.")
        if missing_skills:
            indicators.append("Role-fit skill gaps were identified.")
        return indicators[:4]

    def _build_improvement_roadmap(
        self, missing_skills: list[str], weakness_indicators: list[str]
    ) -> list[str]:
        roadmap = []
        if missing_skills:
            roadmap.append(
                f"Add visible evidence for {', '.join(missing_skills[:3])} through projects, coursework, or quantified work examples."
            )
        roadmap.append("Rewrite recent bullets in STAR form with clearer outcomes and trade-offs.")
        if weakness_indicators:
            roadmap.append("Fill the missing sections detected by the analyzer so recruiters can scan your profile faster.")
        roadmap.append("Prepare a 60-second value proposition aligned to your strongest target role.")
        return roadmap[:4]

    def _build_learning_suggestions(
        self, recommended_roles: list[str], missing_skills: list[str]
    ) -> list[str]:
        suggestions = []
        if missing_skills:
            suggestions.append(
                f"Build one focused project that demonstrates {missing_skills[0]} in a production-style workflow."
            )
        for role in recommended_roles[:2]:
            suggestions.append(
                f"Review common interview themes for {role} roles and prepare two role-specific examples."
            )
        suggestions.append("Record practice answers and tighten them until they are concise, evidence-based, and measurable.")
        return suggestions[:4]

    def _build_mentor_guidance(self, recommended_roles: list[str], missing_skills: list[str]) -> str:
        role_phrase = ", ".join(recommended_roles[:2]) if recommended_roles else "your target roles"
        if missing_skills:
            return (
                f"Your strongest path is to target {role_phrase} while closing the most visible gaps in "
                f"{', '.join(missing_skills[:3])}."
            )
        return (
            f"Your current profile already supports {role_phrase}. Focus next on sharper impact stories and more confident delivery."
        )

    def _score_resume(
        self,
        skills: list[str],
        experience: list[str],
        projects: list[str],
        certifications: list[str],
        missing_skills: list[str],
        strength_indicators: list[str],
    ) -> float:
        score = (
            48
            + min(20, len(skills) * 3)
            + min(10, len(experience) * 2)
            + min(8, len(projects) * 2)
            + min(6, len(certifications) * 1.5)
            + min(8, len(strength_indicators) * 2)
            - min(14, len(missing_skills) * 2.5)
        )
        return round(max(52.0, min(96.0, score)), 2)

    def _extract_candidate_name(self, lines: list[str], file_name: str) -> str:
        for line in lines[:4]:
            lowered = line.lower()
            if "@" not in line and "resume" not in lowered and len(line) <= 60:
                return line
        return file_name.rsplit(".", 1)[0]

    def _extract_contact_info(self, text: str) -> list[str]:
        results: list[str] = []
        email = re.search(r"[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}", text, re.IGNORECASE)
        phone = re.search(r"(\+?\d[\d\s().-]{8,}\d)", text)
        links = re.findall(r"(https?://\S+|linkedin\.com/\S+|github\.com/\S+)", text, re.IGNORECASE)
        if email:
            results.append(f"Email: {email.group(0)}")
        if phone:
            results.append(f"Phone: {phone.group(1).strip()}")
        for link in links[:2]:
            results.append(f"Profile: {link}")
        return results

    def _extract_section(self, lines: list[str], headings: set[str], limit: int = 6) -> list[str]:
        items: list[str] = []
        active = False
        for line in lines:
            normalized = self._normalize_heading(line)
            if normalized in headings:
                active = True
                continue
            if active and self._is_likely_heading(normalized) and normalized not in headings:
                break
            if active:
                items.append(line)
        return items[:limit]

    def _is_likely_heading(self, text: str) -> bool:
        return len(text) <= 32 and not any(character.isdigit() for character in text)

    def _normalize_heading(self, text: str) -> str:
        return re.sub(r"[^a-z& ]+", "", text.lower()).replace("&", " & ").replace("  ", " ").strip()

    def _extract_skill_line_items(self, line: str) -> list[str]:
        normalized_line = re.sub(r"^[\-\u2022]+", "", line).strip()
        candidates = re.split(r"[|,/]|(?:\s+-\s+)", normalized_line)
        extracted = []
        for candidate in candidates:
            token = candidate.strip()
            if not token:
                continue
            lower = token.lower()
            if lower in SKILL_VOCABULARY:
                extracted.append(self._normalize_skill(lower))
            elif len(token.split()) <= 3 and any(char.isalpha() for char in token):
                extracted.append(token)
        return extracted

    def _merge_unique(self, *groups: list[str]) -> list[str]:
        merged: list[str] = []
        seen: set[str] = set()
        for group in groups:
            for item in group:
                normalized = item.strip()
                if not normalized:
                    continue
                key = normalized.lower()
                if key in seen:
                    continue
                seen.add(key)
                merged.append(normalized)
        return merged[:12]

    def _role_skills_for(self, role: str) -> list[str]:
        return [self._normalize_skill(skill) for skill in ROLE_SKILL_MAP.get(role, [])]

    def _infer_missing_skills(self, roles: list[str], skills: list[str]) -> list[str]:
        lower_skills = {skill.lower() for skill in skills}
        missing: list[str] = []
        for role in roles:
            for keyword in ROLE_SKILL_MAP.get(role, []):
                if keyword not in lower_skills:
                    missing.append(self._normalize_skill(keyword))
        return list(dict.fromkeys(missing))[:6]

    def _normalize_skill(self, skill: str) -> str:
        skill = skill.lower()
        if skill == "ci/cd":
            return "CI/CD"
        if skill in {"aws", "sql", "html", "css"}:
            return skill.upper()
        return skill.title()

    def _tokenize(self, text: str) -> list[str]:
        return [
            token
            for token in re.split(r"[^a-zA-Z0-9+/.-]+", text.lower())
            if token and len(token) > 1
        ]

    def _keyword_similarity(self, left: set[str], right: set[str]) -> float:
        if not left or not right:
            return 0.0
        return len(left & right) / math.sqrt(len(left) * len(right))


nlp_service = NLPService()
