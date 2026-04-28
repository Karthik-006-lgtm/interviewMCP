from fastapi import APIRouter, File, Form, UploadFile

from app.models.schemas import (
    AnswerScoreRequest,
    AnswerScoreResponse,
    CoachAnswerRequest,
    CoachAnswerResponse,
    GenerateQuestionsRequest,
    GenerateQuestionsResponse,
    GrammarCheckRequest,
    GrammarCheckResponse,
    RecommendRoleRequest,
    RecommendRoleResponse,
    ResumeAnalysisRequest,
    ResumeAnalysisResponse,
    SpeechAnalysisResponse,
)
from app.services.grammar_service import grammar_service
from app.services.nlp_service import nlp_service
from app.services.speech_service import speech_service

router = APIRouter()


@router.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@router.post("/analyze_resume", response_model=ResumeAnalysisResponse)
@router.post("/analyze-resume", response_model=ResumeAnalysisResponse)
def analyze_resume(payload: ResumeAnalysisRequest) -> ResumeAnalysisResponse:
    return nlp_service.analyze_resume(payload.fileName, payload.resumeText)


@router.post("/recommend_role", response_model=RecommendRoleResponse)
@router.post("/recommend-role", response_model=RecommendRoleResponse)
def recommend_role(payload: RecommendRoleRequest) -> RecommendRoleResponse:
    return nlp_service.recommend_roles(payload.summary, payload.skills)


@router.post("/score_answer", response_model=AnswerScoreResponse)
@router.post("/score-answer", response_model=AnswerScoreResponse)
def score_answer(payload: AnswerScoreRequest) -> AnswerScoreResponse:
    return grammar_service.score_answer(
        payload.question,
        payload.expectedPoints,
        payload.answerText,
        payload.context,
    )


@router.post("/coach_answer", response_model=CoachAnswerResponse)
@router.post("/coach-answer", response_model=CoachAnswerResponse)
def coach_answer(payload: CoachAnswerRequest) -> CoachAnswerResponse:
    return grammar_service.coach_answer(
        payload.question,
        payload.answerDraft,
        payload.coachingIntensity,
        payload.silenceDetected,
        payload.targetRole,
        payload.companyName,
    )


@router.post("/grammar_check", response_model=GrammarCheckResponse)
@router.post("/grammar-check", response_model=GrammarCheckResponse)
def grammar_check(payload: GrammarCheckRequest) -> GrammarCheckResponse:
    return grammar_service.grammar_check(payload.answerText)


@router.post("/generate_questions", response_model=GenerateQuestionsResponse)
@router.post("/generate-questions", response_model=GenerateQuestionsResponse)
def generate_questions(payload: GenerateQuestionsRequest) -> GenerateQuestionsResponse:
    return nlp_service.generate_questions(
        selected_roles=payload.selectedRoles,
        personality_profile=payload.personalityProfile,
        technical_skills=payload.technicalSkills,
        resume_summary=payload.resumeSummary,
        experience=payload.experience,
        projects=payload.projects,
        strengths=payload.strengths,
        weaknesses=payload.weaknesses,
        missing_skills=payload.missingSkills,
        company_name=payload.companyName,
        company_culture=payload.companyCulture,
        company_history=payload.companyHistory,
        company_focus_areas=payload.companyFocusAreas,
        interviewer_tone=payload.interviewerTone,
        coaching_intensity=payload.coachingIntensity,
        reality_mode=payload.realityMode,
        adaptive_difficulty_enabled=payload.adaptiveDifficultyEnabled,
        live_coaching_enabled=payload.liveCoachingEnabled,
    )


@router.post("/transcribe_audio", response_model=SpeechAnalysisResponse)
@router.post("/speech-analysis", response_model=SpeechAnalysisResponse)
async def transcribe_audio(
    file: UploadFile = File(...),
    transcript_hint: str | None = Form(default=None),
    duration_ms: int | None = Form(default=None),
) -> SpeechAnalysisResponse:
    return speech_service.analyze_audio(
        filename=file.filename or "voice-answer.webm",
        audio_bytes=await file.read(),
        transcript_hint=transcript_hint,
        duration_ms=duration_ms,
    )
