from __future__ import annotations

try:
    from pydantic import BaseModel, Field

    class ResumeAnalysisRequest(BaseModel):
        fileName: str
        resumeText: str = Field(min_length=20)


    class ResumeAnalysisResponse(BaseModel):
        candidateName: str
        contactInfo: list[str]
        summary: str
        strengths: list[str]
        weaknesses: list[str]
        extractedSkills: list[str]
        education: list[str]
        experience: list[str]
        projects: list[str]
        certifications: list[str]
        missingSkills: list[str]
        strengthIndicators: list[str]
        weaknessIndicators: list[str]
        improvementRoadmap: list[str]
        learningSuggestions: list[str]
        mentorGuidance: str
        recommendedRoles: list[str]
        readinessScore: float


    class RecommendRoleRequest(BaseModel):
        summary: str
        skills: list[str]


    class RecommendRoleResponse(BaseModel):
        recommendedRoles: list[str]


    class AnswerScoreRequest(BaseModel):
        question: str
        expectedPoints: str
        answerText: str = Field(min_length=10)
        context: str = ""


    class AnswerScoreResponse(BaseModel):
        correctnessScore: float
        confidenceScore: float
        relevanceScore: float
        clarityScore: float
        completenessScore: float
        structureScore: float
        impactScore: float
        hesitationScore: float
        fillerWordCount: int
        emotionSignal: str
        mentorSuggestions: str
        liveCoachingHints: list[str]
        weaknessSignals: list[str]
        weeklyImprovementPlan: list[str]
        practiceTasks: list[str]
        targetedQuestions: list[str]
        adaptiveDifficultyNote: str
        nextDifficultyLevel: str


    class GrammarCheckRequest(BaseModel):
        answerText: str = Field(min_length=5)


    class GrammarCheckResponse(BaseModel):
        grammarFeedback: str
        vocabularyFeedback: str
        toneFeedback: str
        fluencyFeedback: str
        pronunciationFeedback: str
        polishedAnswer: str


    class GenerateQuestionsRequest(BaseModel):
        selectedRoles: list[str]
        personalityProfile: str
        technicalSkills: list[str] = Field(default_factory=list)
        resumeSummary: str = ""
        experience: list[str] = Field(default_factory=list)
        projects: list[str] = Field(default_factory=list)
        strengths: list[str] = Field(default_factory=list)
        weaknesses: list[str] = Field(default_factory=list)
        missingSkills: list[str] = Field(default_factory=list)
        companyName: str = ""
        companyCulture: str = ""
        companyHistory: str = ""
        companyFocusAreas: list[str] = Field(default_factory=list)
        interviewerTone: str = "Friendly technical panel"
        coachingIntensity: str = "Balanced"
        realityMode: str = "Standard"
        adaptiveDifficultyEnabled: bool = True
        liveCoachingEnabled: bool = True


    class GeneratedQuestion(BaseModel):
        prompt: str
        category: str
        expectedPoints: str
        difficulty: str
        interviewerCue: str
        timePressureSeconds: int


    class GenerateQuestionsResponse(BaseModel):
        questions: list[GeneratedQuestion]


    class SpeechAnalysisResponse(BaseModel):
        transcript: str
        confidenceScore: float
        fluencyScore: float
        clarityScore: float
        emotionSignal: str
        toneFeedback: str
        pronunciationFeedback: str
        fluencyFeedback: str


    class CoachAnswerRequest(BaseModel):
        question: str
        answerDraft: str = ""
        coachingIntensity: str = "Balanced"
        silenceDetected: bool = False
        targetRole: str = ""
        companyName: str = ""


    class CoachAnswerResponse(BaseModel):
        hints: list[str]
        suggestedKeywords: list[str]
        continuationPrompt: str
        structureReminder: str

except ImportError:
    from dataclasses import dataclass

    @dataclass(slots=True)
    class ResumeAnalysisRequest:
        fileName: str
        resumeText: str


    @dataclass(slots=True)
    class ResumeAnalysisResponse:
        candidateName: str
        contactInfo: list[str]
        summary: str
        strengths: list[str]
        weaknesses: list[str]
        extractedSkills: list[str]
        education: list[str]
        experience: list[str]
        projects: list[str]
        certifications: list[str]
        missingSkills: list[str]
        strengthIndicators: list[str]
        weaknessIndicators: list[str]
        improvementRoadmap: list[str]
        learningSuggestions: list[str]
        mentorGuidance: str
        recommendedRoles: list[str]
        readinessScore: float


    @dataclass(slots=True)
    class RecommendRoleRequest:
        summary: str
        skills: list[str]


    @dataclass(slots=True)
    class RecommendRoleResponse:
        recommendedRoles: list[str]


    @dataclass(slots=True)
    class AnswerScoreRequest:
        question: str
        expectedPoints: str
        answerText: str
        context: str = ""


    @dataclass(slots=True)
    class AnswerScoreResponse:
        correctnessScore: float
        confidenceScore: float
        relevanceScore: float
        clarityScore: float
        completenessScore: float
        structureScore: float
        impactScore: float
        hesitationScore: float
        fillerWordCount: int
        emotionSignal: str
        mentorSuggestions: str
        liveCoachingHints: list[str]
        weaknessSignals: list[str]
        weeklyImprovementPlan: list[str]
        practiceTasks: list[str]
        targetedQuestions: list[str]
        adaptiveDifficultyNote: str
        nextDifficultyLevel: str


    @dataclass(slots=True)
    class GrammarCheckRequest:
        answerText: str


    @dataclass(slots=True)
    class GrammarCheckResponse:
        grammarFeedback: str
        vocabularyFeedback: str
        toneFeedback: str
        fluencyFeedback: str
        pronunciationFeedback: str
        polishedAnswer: str


    @dataclass(slots=True)
    class GenerateQuestionsRequest:
        selectedRoles: list[str]
        personalityProfile: str
        technicalSkills: list[str]
        resumeSummary: str = ""
        experience: list[str] = None
        projects: list[str] = None
        strengths: list[str] = None
        weaknesses: list[str] = None
        missingSkills: list[str] = None
        companyName: str = ""
        companyCulture: str = ""
        companyHistory: str = ""
        companyFocusAreas: list[str] = None
        interviewerTone: str = "Friendly technical panel"
        coachingIntensity: str = "Balanced"
        realityMode: str = "Standard"
        adaptiveDifficultyEnabled: bool = True
        liveCoachingEnabled: bool = True


    @dataclass(slots=True)
    class GeneratedQuestion:
        prompt: str
        category: str
        expectedPoints: str
        difficulty: str
        interviewerCue: str
        timePressureSeconds: int


    @dataclass(slots=True)
    class GenerateQuestionsResponse:
        questions: list[GeneratedQuestion]


    @dataclass(slots=True)
    class SpeechAnalysisResponse:
        transcript: str
        confidenceScore: float
        fluencyScore: float
        clarityScore: float
        emotionSignal: str
        toneFeedback: str
        pronunciationFeedback: str
        fluencyFeedback: str


    @dataclass(slots=True)
    class CoachAnswerRequest:
        question: str
        answerDraft: str = ""
        coachingIntensity: str = "Balanced"
        silenceDetected: bool = False
        targetRole: str = ""
        companyName: str = ""


    @dataclass(slots=True)
    class CoachAnswerResponse:
        hints: list[str]
        suggestedKeywords: list[str]
        continuationPrompt: str
        structureReminder: str
