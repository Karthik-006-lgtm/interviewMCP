import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { interviewApi } from "../../api/client";
import { CameraPresencePanel } from "../../components/interview/CameraPresencePanel";
import { SectionCard } from "../../components/ui/SectionCard";
import { VoiceRecorder } from "../../components/ui/VoiceRecorder";
import {
  buildFollowUpPrompts,
  buildSimulationMilestone,
  resolveActiveSpeaker,
  speakSimulationCue
} from "../../lib/interviewSimulation";
import type { AnswerEvaluation, AudioProcessingResult, InterviewSession, LiveCoaching } from "../../types";

export function InterviewSessionPage() {
  const { sessionId } = useParams();
  const [session, setSession] = useState<InterviewSession | null>(null);
  const [loading, setLoading] = useState(true);
  const [pageError, setPageError] = useState("");
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [audioReferences, setAudioReferences] = useState<Record<string, string>>({});
  const [audioAnalyses, setAudioAnalyses] = useState<Record<string, AudioProcessingResult>>({});
  const [audioProcessing, setAudioProcessing] = useState<Record<string, boolean>>({});
  const [evaluations, setEvaluations] = useState<Record<string, AnswerEvaluation>>({});
  const [coachingHints, setCoachingHints] = useState<Record<string, LiveCoaching>>({});
  const [coachingLoading, setCoachingLoading] = useState<Record<string, boolean>>({});
  const [questionErrors, setQuestionErrors] = useState<Record<string, string>>({});
  const [countdowns, setCountdowns] = useState<Record<string, number>>({});
  const [simulationFeed, setSimulationFeed] = useState<Record<string, string[]>>({});
  const [triggeredMilestones, setTriggeredMilestones] = useState<Record<string, string[]>>({});
  const [spokenMessages, setSpokenMessages] = useState<Record<string, string[]>>({});
  const [visualSignals, setVisualSignals] = useState({
    presence: "Voice-based emotion analysis only.",
    eyeContact: "Enable camera mode to add visual presence checks.",
    confidence: "Visual confidence scoring is inactive.",
    nervousness: "Visual nervousness scoring is inactive."
  });
  const [submittingQuestionId, setSubmittingQuestionId] = useState<number | null>(null);
  const [cameraActiveForQuestion, setCameraActiveForQuestion] = useState<string | null>(null);

  useEffect(() => {
    if (!sessionId) {
      return;
    }
    setLoading(true);
    setPageError("");
    interviewApi
      .getSession(sessionId)
      .then(setSession)
      .catch(() => setPageError("We couldn't load this interview session. Try refreshing the page."))
      .finally(() => setLoading(false));
  }, [sessionId]);

  useEffect(() => {
    const handle = window.setInterval(() => {
      setCountdowns((current) => {
        let changed = false;
        const next: Record<string, number> = {};

        Object.entries(current).forEach(([questionKey, seconds]) => {
          if (seconds <= 0) {
            next[questionKey] = 0;
            return;
          }
          next[questionKey] = seconds - 1;
          changed = true;
        });

        return changed ? next : current;
      });
    }, 1000);

    return () => window.clearInterval(handle);
  }, []);

  useEffect(() => {
    if (!session) {
      return;
    }

    const additions = session.questions
      .map((question) => {
        const questionKey = String(question.id);
        const milestone = buildSimulationMilestone(
          question,
          countdowns[questionKey],
          session.realityMode,
          session.targetCompanyName,
          session.interviewerTone
        );
        if (!milestone) {
          return null;
        }
        if ((triggeredMilestones[questionKey] ?? []).includes(milestone.id)) {
          return null;
        }
        return { questionKey, ...milestone };
      })
      .filter((item): item is { questionKey: string; id: string; message: string } => item !== null);

    if (!additions.length) {
      return;
    }

    setTriggeredMilestones((current) => {
      const next = { ...current };
      additions.forEach(({ questionKey, id }) => {
        next[questionKey] = [...(next[questionKey] ?? []), id];
      });
      return next;
    });
    setSimulationFeed((current) => {
      const next = { ...current };
      additions.forEach(({ questionKey, message }) => {
        next[questionKey] = [message, ...(next[questionKey] ?? [])].slice(0, 4);
      });
      return next;
    });
  }, [countdowns, session, triggeredMilestones]);

  useEffect(() => {
    if (!session) {
      return;
    }
    const supportsVoices =
      typeof window !== "undefined" &&
      "speechSynthesis" in window &&
      (session.realityMode.toLowerCase().includes("panel") ||
        session.realityMode.toLowerCase().includes("interrupt") ||
        session.realityMode.toLowerCase().includes("lag"));
    if (!supportsVoices) {
      return;
    }

    const nextSpoken: Record<string, string[]> = {};
    let changed = false;

    for (const question of session.questions) {
      const questionKey = String(question.id);
      const latestMessage = simulationFeed[questionKey]?.[0];
      if (!latestMessage) {
        continue;
      }
      const alreadySpoken = spokenMessages[questionKey] ?? [];
      nextSpoken[questionKey] = alreadySpoken;
      if (alreadySpoken.includes(latestMessage)) {
        continue;
      }
      speakSimulationCue(latestMessage, resolveActiveSpeaker(question, session.realityMode, countdowns[questionKey]), session.realityMode);
      nextSpoken[questionKey] = [latestMessage, ...alreadySpoken].slice(0, 6);
      changed = true;
      break;
    }

    if (changed) {
      setSpokenMessages((current) => ({ ...current, ...nextSpoken }));
    }
  }, [countdowns, session, simulationFeed, spokenMessages]);

  const lagDelayMs = useMemo(
    () => (session?.realityMode.toLowerCase().includes("lag") ? 1400 : 0),
    [session?.realityMode]
  );

  const appendSimulationMessage = (questionKey: string, message: string) => {
    setSimulationFeed((current) => ({
      ...current,
      [questionKey]: [message, ...(current[questionKey] ?? [])].slice(0, 4)
    }));
  };

  const startTimedRound = (questionId: number, durationSeconds: number) => {
    const questionKey = String(questionId);
    setCountdowns((current) => ({
      ...current,
      [questionKey]: durationSeconds
    }));
    setTriggeredMilestones((current) => ({
      ...current,
      [questionKey]: []
    }));
    appendSimulationMessage(questionKey, `Timed round started: ${durationSeconds}s on the clock.`);
  };

  const resetTimedRound = (questionId: number) => {
    const questionKey = String(questionId);
    setCountdowns((current) => {
      const next = { ...current };
      delete next[questionKey];
      return next;
    });
    setTriggeredMilestones((current) => {
      const next = { ...current };
      delete next[questionKey];
      return next;
    });
    setSimulationFeed((current) => ({
      ...current,
      [questionKey]: ["Simulation round reset. Start again when you're ready."]
    }));
  };

  const submitAnswer = async (questionId: number) => {
    const questionKey = String(questionId);
    if (audioProcessing[questionKey]) {
      setQuestionErrors((current) => ({
        ...current,
        [questionKey]: "Audio is still being analyzed. Wait for the speech scorecard before submitting."
      }));
      return;
    }

    const typedAnswer = answers[questionKey] ?? "";
    const transcriptAnswer = audioAnalyses[questionKey]?.transcript ?? "";
    const audioReference = audioReferences[questionKey];
    const answerText = typedAnswer.trim() || transcriptAnswer.trim();
    const cameraModeEnabled = session?.cameraEnabled ?? false;
    if (!answerText) {
      setQuestionErrors((current) => ({
        ...current,
        [questionKey]: audioReference
          ? "No transcript was captured from your recording. Please type your answer in the text box above, then submit."
          : "Type an answer or record an audio answer before submitting."
      }));
      return;
    }

    if (!typedAnswer.trim() && transcriptAnswer.trim()) {
      setAnswers((current) => ({ ...current, [questionKey]: transcriptAnswer }));
    }

    setSubmittingQuestionId(questionId);
    setCameraActiveForQuestion(null);
    setQuestionErrors((current) => ({ ...current, [questionKey]: "" }));
    try {
      if (lagDelayMs > 0) {
        appendSimulationMessage(questionKey, "Offline reality mode: sending your answer through a simulated unstable connection...");
        await new Promise((resolve) => window.setTimeout(resolve, lagDelayMs));
      }
      const evaluation = await interviewApi.submitAnswer(questionId, {
        answerText,
        audioReference: audioReference,
        visualPresenceSignal: cameraModeEnabled ? visualSignals.presence : undefined,
        visualEyeContactSignal: cameraModeEnabled ? visualSignals.eyeContact : undefined,
        visualConfidenceSignal: cameraModeEnabled ? visualSignals.confidence : undefined,
        visualNervousnessSignal: cameraModeEnabled ? visualSignals.nervousness : undefined
      });
      setEvaluations((current) => ({ ...current, [questionKey]: evaluation }));
      appendSimulationMessage(questionKey, "Answer scored. Review the breakdown and targeted follow-up prompts.");
      if (sessionId) {
        setSession(await interviewApi.getSession(sessionId));
      }
    } catch {
      setQuestionErrors((current) => ({
        ...current,
        [questionKey]: "Answer submission failed. Please try again."
      }));
    } finally {
      setSubmittingQuestionId(null);
    }
  };

  const requestCoach = async (questionId: number, silenceDetected = false) => {
    const questionKey = String(questionId);
    setCoachingLoading((current) => ({ ...current, [questionKey]: true }));
    setQuestionErrors((current) => ({ ...current, [questionKey]: "" }));
    try {
      if (lagDelayMs > 0) {
        appendSimulationMessage(questionKey, "Coach request is delayed to simulate network instability...");
        await new Promise((resolve) => window.setTimeout(resolve, 800));
      }
      const coaching = await interviewApi.coachAnswer(questionId, {
        answerDraft: answers[questionKey] ?? audioAnalyses[questionKey]?.transcript ?? "",
        silenceDetected
      });
      setCoachingHints((current) => ({ ...current, [questionKey]: coaching }));
      appendSimulationMessage(
        questionKey,
        silenceDetected
          ? "AI coach detected silence and suggested a continuation."
          : "AI coach returned a live prompt for your next response pass."
      );
    } catch {
      setQuestionErrors((current) => ({
        ...current,
        [questionKey]: "AI coach is unavailable right now. Try again in a moment."
      }));
    } finally {
      setCoachingLoading((current) => ({ ...current, [questionKey]: false }));
    }
  };

  if (loading) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-white/68">Loading interview session...</div>;
  }

  if (!session) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-rose-600">{pageError || "Interview session not found."}</div>;
  }

  return (
    <div className="space-y-6">
      <SectionCard
        title="Interview Session"
        subtitle={`Roles: ${session.selectedRoles.join(", ")} | Personality: ${session.personalityProfile}`}
      >
        {pageError ? <p className="mb-4 text-sm text-rose-600">{pageError}</p> : null}
        <div className="grid gap-4 lg:grid-cols-[1fr_0.45fr] xl:grid-cols-[1fr_0.45fr_0.55fr]">
          <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/78">
            <p>Technical focus: {session.technicalSkills || "Generated from your latest resume"}.</p>
            <p className="mt-3">Target company: {session.targetCompanyName || "General interview mode"}.</p>
            <p className="mt-3">Interviewer tone: {session.interviewerTone}.</p>
            <p className="mt-3">Reality mode: {session.realityMode}.</p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-sm font-medium text-white/65">Session score</p>
            <p className="mt-3 font-display text-4xl text-cyan-300">
              {session.overallScore !== null ? `${Math.round(session.overallScore)}%` : "Pending"}
            </p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/78">
            <p className="font-semibold text-cyan-200">Simulation state</p>
            <p className="mt-3">Coaching intensity: {session.coachingIntensity}</p>
            <p className="mt-2">Live coach: {session.liveCoachingEnabled ? "Enabled" : "Disabled"}</p>
            <p className="mt-2">Adaptive difficulty: {session.adaptiveDifficultyEnabled ? "Enabled" : "Disabled"}</p>
            <p className="mt-2">Current difficulty: {session.currentDifficultyLevel}</p>
            <p className="mt-2">Camera emotion mode: {session.cameraEnabled ? "Enabled" : "Disabled"}</p>
          </div>
        </div>
        {session.cameraEnabled ? (
          <div className="mt-4 grid gap-4 xl:grid-cols-[1.05fr_0.95fr]">
            <CameraPresencePanel enabled={session.cameraEnabled} active={cameraActiveForQuestion !== null} onSignalChange={setVisualSignals} />
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/78">
              <p className="font-semibold text-cyan-200">Simulation engine</p>
              <ul className="mt-3 space-y-2 leading-7">
                <li>- Company-aware questioning is active for {session.targetCompanyName || "general practice"}.</li>
                <li>- Time pressure, interruptions, and speaker rotation adapt to the selected reality mode.</li>
                <li>- Live coaching and adaptive difficulty stay independent, so existing answer scoring still works.</li>
              </ul>
              <div className="mt-5 rounded-[1rem] bg-white/5 p-4">
                <p className="font-semibold text-white">Visual analysis read</p>
                <p className="mt-2">Presence: {visualSignals.presence}</p>
                <p className="mt-2">Eye contact: {visualSignals.eyeContact}</p>
                <p className="mt-2">Confidence: {visualSignals.confidence}</p>
                <p className="mt-2">Nervousness: {visualSignals.nervousness}</p>
              </div>
            </div>
          </div>
        ) : null}
      </SectionCard>

      <div className="space-y-5">
        {session.questions.map((question, index) => {
          const evaluation = evaluations[String(question.id)];
          const audioAnalysis = audioAnalyses[String(question.id)];
          const questionKey = String(question.id);
          const isAudioProcessing = audioProcessing[questionKey];
          const questionError = questionErrors[questionKey];
          const coaching = coachingHints[questionKey];
          const isCoaching = coachingLoading[questionKey];
          const countdown = countdowns[questionKey];
          const simulationMessages = simulationFeed[questionKey] ?? [];
          const activeSpeaker = resolveActiveSpeaker(question, session.realityMode, countdown);
          const followUpPrompts = evaluation
            ? buildFollowUpPrompts(question, evaluation, session.targetCompanyName)
            : [];
          return (
            <SectionCard
              key={question.id}
              title={`Question ${index + 1}`}
              subtitle={`${question.category} | ${question.difficulty}`}
            >
              <div className="mb-4 flex flex-wrap gap-2">
                {question.interviewerCue ? (
                  <span className="app-chip-secondary rounded-full px-3 py-2 text-xs font-medium">
                    {question.interviewerCue}
                  </span>
                ) : null}
                {question.timePressureSeconds ? (
                  <span className="app-chip-accent rounded-full px-3 py-2 text-xs font-medium">
                    Time pressure: {question.timePressureSeconds}s
                  </span>
                ) : null}
                <span className="app-chip rounded-full px-3 py-2 text-xs font-medium">{activeSpeaker}</span>
                {typeof countdown === "number" ? (
                  <span className="app-chip-secondary rounded-full px-3 py-2 text-xs font-medium">
                    Live timer: {countdown}s
                  </span>
                ) : null}
              </div>
              <p className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-white/82">{question.prompt}</p>
              <div className="mt-4 flex flex-wrap gap-3">
                <button
                  type="button"
                  onClick={() => startTimedRound(question.id, question.timePressureSeconds || 90)}
                  className="app-button-secondary rounded-full px-4 py-2 text-sm font-medium transition"
                >
                  Start timed round
                </button>
                <button
                  type="button"
                  onClick={() => resetTimedRound(question.id)}
                  className="app-button-soft rounded-full px-4 py-2 text-sm font-medium transition"
                >
                  Reset round
                </button>
              </div>

              {simulationMessages.length ? (
                <div className="app-surface-strong mt-4 rounded-[1.5rem] p-5 text-sm text-white/78">
                  <p className="font-display text-lg text-white">Simulation console</p>
                  <ul className="mt-3 space-y-2">
                    {simulationMessages.map((message) => (
                      <li key={message}>- {message}</li>
                    ))}
                  </ul>
                </div>
              ) : null}

              <textarea
                value={answers[String(question.id)] ?? ""}
                onChange={(event) =>
                  setAnswers((current) => ({ ...current, [String(question.id)]: event.target.value }))
                }
                rows={6}
                placeholder="Write or dictate your answer here..."
                className="mt-5 w-full rounded-[1.5rem] border px-4 py-4 outline-none transition"
              />
              <div className="mt-4">
                <VoiceRecorder
                  onRecordingStart={() => {
                    if (session.cameraEnabled) {
                      setCameraActiveForQuestion(questionKey);
                    }
                  }}
                  onRecordingStop={() => {
                    setCameraActiveForQuestion(null);
                  }}
                  onProcessingChange={(processing) =>
                    setAudioProcessing((current) => ({
                      ...current,
                      [questionKey]: processing
                    }))
                  }
                  onRecorded={(result) => {
                    setAudioReferences((current) => ({
                      ...current,
                      [questionKey]: result.audioReference
                    }));
                    setAudioAnalyses((current) => ({
                      ...current,
                      [questionKey]: result
                    }));
                    setQuestionErrors((current) => ({
                      ...current,
                      [questionKey]: ""
                    }));
                    if (result.transcript.trim()) {
                      setAnswers((current) => ({
                        ...current,
                        [questionKey]: current[questionKey]?.trim() ? current[questionKey] : result.transcript
                      }));
                    }
                  }}
                />
              </div>

              <div className="mt-4 flex flex-wrap gap-3">
                {session.liveCoachingEnabled ? (
                  <>
                    <button
                      type="button"
                      onClick={() => requestCoach(question.id)}
                      disabled={isCoaching}
                      className="app-button-secondary rounded-full px-4 py-2 text-sm font-medium transition disabled:opacity-70"
                    >
                      {isCoaching ? "Coaching..." : "Get live hint"}
                    </button>
                    <button
                      type="button"
                      onClick={() => requestCoach(question.id, true)}
                      disabled={isCoaching}
                      className="app-button-soft rounded-full px-4 py-2 text-sm font-medium transition disabled:opacity-70"
                    >
                      I went silent
                    </button>
                  </>
                ) : null}
              </div>

              {questionError ? <p className="mt-4 text-sm text-rose-600">{questionError}</p> : null}

              {coaching ? (
                <div className="app-surface-strong mt-5 rounded-[1.5rem] p-5 text-sm text-white/78">
                  <p className="font-display text-lg text-white">Real-time AI coach</p>
                  <p className="mt-3 text-cyan-200">{coaching.structureReminder}</p>
                  <div className="mt-4 grid gap-4 xl:grid-cols-[1fr_0.9fr]">
                    <div>
                      <p className="text-xs uppercase tracking-[0.25em] text-white/50">Hints</p>
                      <ul className="mt-3 space-y-2">
                        {coaching.hints.map((hint) => (
                          <li key={hint}>- {hint}</li>
                        ))}
                      </ul>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-[0.25em] text-white/50">Suggested keywords</p>
                      <div className="mt-3 flex flex-wrap gap-2">
                        {coaching.suggestedKeywords.map((keyword) => (
                          <span key={keyword} className="app-chip rounded-full px-3 py-2 text-xs font-medium">
                            {keyword}
                          </span>
                        ))}
                      </div>
                      <p className="mt-4 text-sm text-white/72">{coaching.continuationPrompt}</p>
                    </div>
                  </div>
                </div>
              ) : null}

              <button
                type="button"
                onClick={() => submitAnswer(question.id)}
                disabled={submittingQuestionId === question.id || isAudioProcessing}
                className="app-button-primary mt-5 rounded-full px-5 py-3 text-sm font-medium transition disabled:opacity-70"
              >
                {submittingQuestionId === question.id
                  ? "Scoring answer..."
                  : isAudioProcessing
                    ? "Analyzing voice answer..."
                    : "Submit answer"}
              </button>

              {evaluation ? (
                <div className="mt-5 grid gap-4 xl:grid-cols-3">
                  <div className="app-surface rounded-[1.5rem] p-5">
                    <p className="font-display text-lg text-white">Scorecard</p>
                    <div className="mt-4 space-y-2 text-sm text-white/76">
                      <p>Correctness: {Math.round(evaluation.correctnessScore)}%</p>
                      <p>Confidence: {Math.round(evaluation.confidenceScore)}%</p>
                      <p>Relevance: {Math.round(evaluation.relevanceScore)}%</p>
                      <p>Clarity: {Math.round(evaluation.clarityScore)}%</p>
                      <p>Completeness: {Math.round(evaluation.completenessScore)}%</p>
                      <p>Structure: {Math.round(evaluation.structureScore)}%</p>
                      <p>Impact: {Math.round(evaluation.impactScore)}%</p>
                      <p>Hesitation: {Math.round(evaluation.hesitationScore)}%</p>
                      <p>Filler words: {evaluation.fillerWordCount}</p>
                      <p>Emotion: {evaluation.emotionSignal}</p>
                    </div>
                  </div>
                  <div className="app-surface rounded-[1.5rem] p-5">
                    <p className="font-display text-lg text-white">Language feedback</p>
                    <div className="mt-4 space-y-3 text-sm text-white/76">
                      <p>
                        <span className="font-semibold text-cyan-200">Grammar:</span> {evaluation.grammarFeedback}
                      </p>
                      <p>
                        <span className="font-semibold text-cyan-200">Vocabulary:</span> {evaluation.vocabularyFeedback}
                      </p>
                      <p>
                        <span className="font-semibold text-cyan-200">Fluency:</span> {evaluation.fluencyFeedback}
                      </p>
                      <p>
                        <span className="font-semibold text-cyan-200">Tone:</span> {evaluation.toneFeedback}
                      </p>
                      <p>
                        <span className="font-semibold text-cyan-200">Pronunciation:</span> {evaluation.pronunciationFeedback}
                      </p>
                    </div>
                  </div>
                  <div className="app-surface rounded-[1.5rem] p-5">
                    <p className="font-display text-lg text-white">Mentor coach</p>
                    <div className="mt-4 space-y-3 text-sm text-white/76">
                      <p>{evaluation.mentorSuggestions}</p>
                      <div className="rounded-[1rem] bg-white/5 p-4">
                        <p className="font-semibold text-cyan-200">Adaptive difficulty</p>
                        <p className="mt-2 leading-7">{evaluation.adaptiveDifficultyNote}</p>
                        <p className="mt-2 text-sm text-white/70">Next question level: {evaluation.nextDifficultyLevel}</p>
                      </div>
                      <div className="rounded-[1rem] bg-white/5 p-4">
                        <p className="font-semibold text-cyan-200">Polished version</p>
                        <p className="mt-2 leading-7">{evaluation.polishedAnswer}</p>
                      </div>
                      <div className="rounded-[1rem] bg-white/5 p-4">
                        <p className="font-semibold text-cyan-200">Follow-up grilling</p>
                        <ul className="mt-2 space-y-2">
                          {followUpPrompts.map((item) => (
                            <li key={item}>- {item}</li>
                          ))}
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              ) : null}

              {evaluation ? (
                <div className="mt-5 grid gap-4 xl:grid-cols-3">
                  <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/76">
                    <p className="font-display text-lg text-white">Weakness detection</p>
                    <ul className="mt-3 space-y-2">
                      {evaluation.weaknessSignals.map((signal) => (
                        <li key={signal}>- {signal}</li>
                      ))}
                    </ul>
                  </div>
                  <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/76">
                    <p className="font-display text-lg text-white">Weekly roadmap</p>
                    <ul className="mt-3 space-y-2">
                      {evaluation.weeklyImprovementPlan.map((item) => (
                        <li key={item}>- {item}</li>
                      ))}
                    </ul>
                  </div>
                  <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/76">
                    <p className="font-display text-lg text-white">Practice tasks</p>
                    <ul className="mt-3 space-y-2">
                      {evaluation.practiceTasks.map((item) => (
                        <li key={item}>- {item}</li>
                      ))}
                    </ul>
                    <p className="mt-4 font-semibold text-cyan-200">Targeted repeats</p>
                    <ul className="mt-2 space-y-2">
                      {evaluation.targetedQuestions.map((item) => (
                        <li key={item}>- {item}</li>
                      ))}
                    </ul>
                  </div>
                </div>
              ) : null}

              {audioAnalysis ? (
                <div className="mt-5 app-surface rounded-[1.5rem] p-5 text-sm text-white/76">
                  <p className="font-display text-lg text-white">Speech and emotion read</p>
                  <div className="mt-3 grid gap-3 md:grid-cols-4">
                    <p>Confidence: {Math.round(audioAnalysis.confidenceScore)}%</p>
                    <p>Fluency: {Math.round(audioAnalysis.fluencyScore)}%</p>
                    <p>Clarity: {Math.round(audioAnalysis.clarityScore)}%</p>
                    <p>Emotion: {audioAnalysis.emotionSignal}</p>
                  </div>
                </div>
              ) : null}
            </SectionCard>
          );
        })}
      </div>
    </div>
  );
}

