import type { AnswerEvaluation, InterviewQuestion } from "../types";

interface SimulationMilestone {
  id: string;
  message: string;
}

const speakerKeywords: Record<string, string[]> = {
  hr: ["zira", "aria", "samantha", "female", "heera"],
  technical: ["david", "guy", "microsoft", "male", "alex"],
  manager: ["mark", "daniel", "google", "male", "james"],
  founder: ["serena", "jenny", "female", "sara", "ava"]
};

export function resolveActiveSpeaker(
  question: InterviewQuestion,
  realityMode: string,
  remainingSeconds?: number
) {
  const loweredMode = realityMode.toLowerCase();
  if (!loweredMode.includes("panel")) {
    return question.category === "HR" ? "HR interviewer" : "Lead interviewer";
  }

  const speakers = [
    "HR interviewer",
    "Technical panelist",
    "Hiring manager",
    "Founder observer"
  ];
  const total = question.timePressureSeconds || 90;
  const elapsed = remainingSeconds == null ? 0 : Math.max(0, total - remainingSeconds);
  const speakerIndex = Math.min(speakers.length - 1, Math.floor(elapsed / 20));
  return speakers[speakerIndex];
}

export function buildSimulationMilestone(
  question: InterviewQuestion,
  remainingSeconds: number,
  realityMode: string,
  targetCompanyName?: string | null,
  interviewerTone?: string
): SimulationMilestone | null {
  const loweredMode = realityMode.toLowerCase();
  const loweredTone = (interviewerTone || "").toLowerCase();
  const company = targetCompanyName || "the company";

  if (remainingSeconds === 0) {
    return {
      id: "time-up",
      message: `Time is up. Close with impact and your fit for ${company}.`
    };
  }

  if (remainingSeconds === 45 && loweredTone.includes("strict")) {
    return {
      id: "strict-follow-up",
      message: "Follow-up pressure: justify your trade-off instead of repeating the first answer."
    };
  }

  if (remainingSeconds === 40 && loweredMode.includes("lag")) {
    return {
      id: "lag-warning",
      message: "Offline reality mode: unstable connection simulated. Keep your answer tighter than usual."
    };
  }

  if (remainingSeconds === 30 && loweredMode.includes("interrupt")) {
    return {
      id: "interruption",
      message: "Interruption: the panel cuts in and asks for the decision you made, not the full backstory."
    };
  }

  if (remainingSeconds === 25 && loweredMode.includes("panel")) {
    return {
      id: "panel-voice",
      message: `${company} panel shift: a second interviewer asks how your answer changes under production pressure.`
    };
  }

  if (remainingSeconds === 15) {
    return {
      id: "closing",
      message: "Final push: end with a result, business impact, and one lesson learned."
    };
  }

  return null;
}

export function buildFollowUpPrompts(
  question: InterviewQuestion,
  evaluation: AnswerEvaluation,
  targetCompanyName?: string | null
) {
  const company = targetCompanyName || "the company";
  const prompts = [
    `Re-answer this for ${company} in under 60 seconds with a sharper close.`,
    "Give the same answer again, but lead with your decision before the background."
  ];

  if (evaluation.structureScore < 72) {
    prompts.push("Use STAR explicitly and spend only one sentence on the setup.");
  }
  if (evaluation.impactScore < 70) {
    prompts.push("Add a metric, outcome, or business consequence to prove impact.");
  }
  if (evaluation.hesitationScore >= 72) {
    prompts.push("Remove fillers and replace tentative phrasing with direct ownership language.");
  }
  if (question.category === "TECHNICAL" || question.category === "SYSTEM_DESIGN") {
    prompts.push("State one trade-off you rejected and why your final decision was still correct.");
  }

  return prompts.slice(0, 4);
}

function pickVoice(role: "hr" | "technical" | "manager" | "founder") {
  if (typeof window === "undefined" || !("speechSynthesis" in window)) {
    return null;
  }
  const voices = window.speechSynthesis.getVoices();
  if (!voices.length) {
    return null;
  }

  const keywords = speakerKeywords[role];
  return (
    voices.find((voice) =>
      keywords.some((keyword) => voice.name.toLowerCase().includes(keyword))
    ) ?? voices[0]
  );
}

function resolveVoiceRole(speaker: string) {
  const lowered = speaker.toLowerCase();
  if (lowered.includes("hr")) {
    return "hr" as const;
  }
  if (lowered.includes("manager")) {
    return "manager" as const;
  }
  if (lowered.includes("founder")) {
    return "founder" as const;
  }
  return "technical" as const;
}

export function speakSimulationCue(message: string, speaker: string, realityMode: string) {
  if (typeof window === "undefined" || !("speechSynthesis" in window) || !("SpeechSynthesisUtterance" in window)) {
    return;
  }

  const utterance = new SpeechSynthesisUtterance(message);
  const voiceRole = resolveVoiceRole(speaker);
  const voice = pickVoice(voiceRole);
  if (voice) {
    utterance.voice = voice;
  }

  const loweredMode = realityMode.toLowerCase();
  utterance.rate = loweredMode.includes("lag") ? 0.9 : voiceRole === "technical" ? 1.03 : 0.98;
  utterance.pitch =
    voiceRole === "founder" ? 1.15 : voiceRole === "hr" ? 1.05 : voiceRole === "manager" ? 0.95 : 0.9;
  utterance.volume = 0.88;

  window.speechSynthesis.cancel();
  window.speechSynthesis.speak(utterance);
}
