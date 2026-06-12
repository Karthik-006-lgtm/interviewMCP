import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { companyApi, interviewApi, roleApi, resumeApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import { loadSelectedRoles, saveSelectedRoles } from "../../lib/rolePreferences";
import type { Company, ResumeAnalysis, RoleProfile } from "../../types";

const personalityOptions = [
  "Analytical and structured",
  "Collaborative and empathetic",
  "Fast-moving and decisive",
  "Calm under pressure"
];

const interviewerToneOptions = [
  "Friendly technical panel",
  "Strict HR screen",
  "Deep technical grilling",
  "Founder-style closeout"
];

const coachingIntensityOptions = ["Beginner", "Balanced", "Advanced"];
const realityModes = ["Standard", "Interrupted panel", "Panel voices", "Offline reality lag"];

export function InterviewPracticePage() {
  const navigate = useNavigate();
  const [resume, setResume] = useState<ResumeAnalysis | null>(null);
  const [roles, setRoles] = useState<RoleProfile[]>([]);
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [personalityProfile, setPersonalityProfile] = useState(personalityOptions[0]);
  const [technicalFocus, setTechnicalFocus] = useState("");
  const [matchedCompanies, setMatchedCompanies] = useState<Company[]>([]);
  const [selectedCompanyId, setSelectedCompanyId] = useState<string | undefined>(undefined);
  const [interviewerTone, setInterviewerTone] = useState(interviewerToneOptions[0]);
  const [coachingIntensity, setCoachingIntensity] = useState(coachingIntensityOptions[1]);
  const [liveCoachingEnabled, setLiveCoachingEnabled] = useState(true);
  const [adaptiveDifficultyEnabled, setAdaptiveDifficultyEnabled] = useState(true);
  const [realityMode, setRealityMode] = useState(realityModes[0]);
  const [cameraEnabled, setCameraEnabled] = useState(false);
  const [loading, setLoading] = useState(false);
  const [companyLoading, setCompanyLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([roleApi.list(), resumeApi.latest().catch(() => null)])
      .then(([availableRoles, latestResume]) => {
        setRoles(availableRoles);
        setResume(latestResume);
        const storedRoles = loadSelectedRoles();
        const initialRoles = storedRoles.length
          ? storedRoles
          : latestResume?.recommendedRoles.length
            ? latestResume.recommendedRoles
            : availableRoles.slice(0, 2).map((role) => role.name);
        setSelectedRoles(initialRoles);
        saveSelectedRoles(initialRoles);
      })
      .catch(() => setError("We couldn't load interview setup data right now. Please refresh and try again."));
  }, []);

  useEffect(() => {
    if (!selectedRoles.length) {
      setMatchedCompanies([]);
      setSelectedCompanyId(undefined);
      return;
    }
    setCompanyLoading(true);
    companyApi
      .search({ selectedRoles, minMatchScore: 50 })
      .then((companies) => {
        setMatchedCompanies(companies);
        setSelectedCompanyId((current) =>
          current && companies.some((company) => company.id === current) ? current : companies[0]?.id
        );
      })
      .catch(() => setMatchedCompanies([]))
      .finally(() => setCompanyLoading(false));
  }, [selectedRoles]);

  const toggleRole = (role: string) => {
    setSelectedRoles((current) => {
      const next = current.includes(role) ? current.filter((item) => item !== role) : [...current, role];
      saveSelectedRoles(next);
      return next;
    });
  };

  const handleCreateSession = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const session = await interviewApi.createSession({
        resumeId: resume?.id,
        companyId: selectedCompanyId,
        selectedRoles,
        personalityProfile,
        technicalFocus,
        interviewerTone,
        coachingIntensity,
        liveCoachingEnabled,
        adaptiveDifficultyEnabled,
        realityMode,
        cameraEnabled
      });
      navigate(`/interview/session/${session.sessionId}`);
    } catch {
      setError("Session generation failed. Make sure your backend and AI service are available, then try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <SectionCard title="Mock Interview Setup" subtitle="Generate dynamic questions from your resume, roles, skills, and working style.">
        <form onSubmit={handleCreateSession} className="space-y-6">
          {error ? <p className="text-sm text-rose-600">{error}</p> : null}
          <div>
            <p className="font-display text-lg text-white">Target roles</p>
            <div className="mt-4 grid gap-4 lg:grid-cols-2">
              {roles.map((role) => {
                const active = selectedRoles.includes(role.name);
                return (
                  <button
                    key={role.id}
                    type="button"
                    onClick={() => toggleRole(role.name)}
                    className={`rounded-[1.5rem] border p-5 text-left transition ${
                    active
                      ? "border-teal-600/45 bg-teal-600/12 text-gray-900 shadow-[0_20px_40px_rgba(110,207,164,0.18)]"
                      : "app-surface text-gray-700 hover:-translate-y-0.5 hover:border-teal-600/28"
                    }`}
                  >
                    <p className="font-display text-xl">{role.name}</p>
                    <p className={`mt-3 text-sm leading-7 ${active ? "text-gray-800" : "text-gray-600"}`}>
                      {role.summary}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <label className="block">
              <span className="mb-2 block text-sm font-medium text-gray-700">Personality profile</span>
              <select
                value={personalityProfile}
                onChange={(event) => setPersonalityProfile(event.target.value)}
                className="w-full rounded-2xl border px-4 py-3 outline-none transition"
              >
                {personalityOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
            <label className="block">
              <span className="mb-2 block text-sm font-medium text-gray-700">Technical focus</span>
              <input
                type="text"
                placeholder="Example: system design, Spring Boot, PostgreSQL"
                value={technicalFocus}
                onChange={(event) => setTechnicalFocus(event.target.value)}
                className="w-full rounded-2xl border px-4 py-3 outline-none transition"
              />
            </label>
          </div>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <label className="block">
              <span className="mb-2 block text-sm font-medium text-gray-700">Target company simulation</span>
              <select
                value={selectedCompanyId ?? ""}
                onChange={(event) => setSelectedCompanyId(event.target.value || undefined)}
                className="w-full rounded-2xl border px-4 py-3 outline-none transition"
              >
                <option value="">General interview mode</option>
                {matchedCompanies.map((company) => (
                  <option key={company.id} value={company.id}>
                    {company.name}  
                  </option>
                ))}
              </select>
              <p className="mt-2 text-xs text-gray-500">
                {companyLoading
                  ? "Loading role-matched companies..."
                  : selectedCompanyId
                    ? "The interviewer will adapt tone, prompts, and pressure to this company."
                    : "Leave empty to practice a general interview track."}
              </p>
            </label>

            <label className="block">
              <span className="mb-2 block text-sm font-medium text-gray-700">Interviewer tone</span>
              <select
                value={interviewerTone}
                onChange={(event) => setInterviewerTone(event.target.value)}
                className="w-full rounded-2xl border px-4 py-3 outline-none transition"
              >
                {interviewerToneOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>

            <label className="block">
              <span className="mb-2 block text-sm font-medium text-gray-700">Coaching intensity</span>
              <select
                value={coachingIntensity}
                onChange={(event) => setCoachingIntensity(event.target.value)}
                className="w-full rounded-2xl border px-4 py-3 outline-none transition"
              >
                {coachingIntensityOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <label className="block">
              <span className="mb-2 block text-sm font-medium text-gray-700">Reality mode</span>
              <select
                value={realityMode}
                onChange={(event) => setRealityMode(event.target.value)}
                className="w-full rounded-2xl border px-4 py-3 outline-none transition"
              >
                {realityModes.map((mode) => (
                  <option key={mode} value={mode}>
                    {mode}
                  </option>
                ))}
              </select>
            </label>

            <label className="app-surface flex items-center justify-between rounded-[1.5rem] px-4 py-4 text-sm text-gray-700">
              <span>Live AI coach</span>
              <input
                type="checkbox"
                checked={liveCoachingEnabled}
                onChange={(event) => setLiveCoachingEnabled(event.target.checked)}
                className="h-4 w-4"
              />
            </label>

            <label className="app-surface flex items-center justify-between rounded-[1.5rem] px-4 py-4 text-sm text-white/76">
              <span>Adaptive difficulty</span>
              <input
                type="checkbox"
                checked={adaptiveDifficultyEnabled}
                onChange={(event) => setAdaptiveDifficultyEnabled(event.target.checked)}
                className="h-4 w-4"
              />
            </label>

            <label className="app-surface flex items-center justify-between rounded-[1.5rem] px-4 py-4 text-sm text-white/76">
              <span>Camera emotion mode</span>
              <input
                type="checkbox"
                checked={cameraEnabled}
                onChange={(event) => setCameraEnabled(event.target.checked)}
                className="h-4 w-4"
              />
            </label>
          </div>

          {resume ? (
            <div className="grid gap-4 lg:grid-cols-[1fr_1fr]">
              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
                <p className="font-semibold text-gray-900">Resume signal</p>
                <p className="mt-2 leading-7">{resume.summary}</p>
              </div>
              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
                <p className="font-semibold text-gray-900">Focus areas to reinforce</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {(resume.missingSkills.length ? resume.missingSkills : resume.extractedSkills.slice(0, 4)).map((item) => (
                    <span key={item} className="app-chip-accent rounded-full px-3 py-2 text-xs font-medium">
                      {item}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          ) : null}

          <div className="grid gap-4 lg:grid-cols-3">
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
              <p className="font-semibold text-teal-700">Simulation engine</p>
              <p className="mt-2 leading-7">
                The interviewer will use {interviewerTone.toLowerCase()}, {realityMode.toLowerCase()}, and company context
                to change tone, follow-ups, and time pressure.
              </p>
            </div>
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
              <p className="font-semibold text-teal-700">Coaching mode</p>
              <p className="mt-2 leading-7">
                {liveCoachingEnabled
                  ? `Live hints are enabled with ${coachingIntensity.toLowerCase()} support.`
                  : "Live hints are disabled for a more realistic pressure round."}
              </p>
            </div>
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
              <p className="font-semibold text-teal-700">Difficulty pacing</p>
              <p className="mt-2 leading-7">
                {adaptiveDifficultyEnabled
                  ? "Difficulty will climb or soften in real time based on how you answer."
                  : "Difficulty will stay fixed across the whole practice run."}
              </p>
            </div>
          </div>

          {cameraEnabled ? (
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
              <p className="font-semibold text-teal-700">Camera emotion mode</p>
              <p className="mt-2 leading-7">
                The interview session will request camera access for a live preview and browser-side visual presence checks,
                while voice confidence and emotion scoring continue through the speech pipeline.
              </p>
            </div>
          ) : null}

          <button
            type="submit"
            disabled={loading || selectedRoles.length === 0}
            className="app-button-primary rounded-full px-6 py-3 font-medium transition disabled:opacity-70"
          >
            {loading ? "Generating session..." : "Start mock interview"}
          </button>
        </form>
      </SectionCard>
    </div>
  );
}
