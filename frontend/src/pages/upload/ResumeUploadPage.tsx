import { useEffect, useState, type FormEvent } from "react";
import { resumeApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import type { ResumeAnalysis } from "../../types";

function SectionList({ title, items }: { title: string; items: string[] }) {
  return (
    <div className="app-surface rounded-[1.5rem] p-5">
      <p className="font-display text-lg text-white">{title}</p>
      {items.length ? (
        <ul className="app-muted mt-4 space-y-2 text-sm">
          {items.map((item) => (
            <li key={item}>- {item}</li>
          ))}
        </ul>
      ) : (
        <p className="app-muted mt-4 text-sm">No strong signal detected yet.</p>
      )}
    </div>
  );
}

export function ResumeUploadPage() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [analysis, setAnalysis] = useState<ResumeAnalysis | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [messageTone, setMessageTone] = useState<"neutral" | "success" | "error">("neutral");

  useEffect(() => {
    resumeApi
      .latest()
      .then(setAnalysis)
      .catch(() => undefined);
  }, []);

  const handleUpload = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedFile) {
      setMessage("Choose a PDF or DOCX resume before uploading.");
      setMessageTone("error");
      return;
    }
    if (!/\.(pdf|docx)$/i.test(selectedFile.name)) {
      setMessage("Only PDF and DOCX resumes are supported.");
      setMessageTone("error");
      return;
    }
    if (selectedFile.size > 8 * 1024 * 1024) {
      setMessage("Please upload a resume smaller than 8 MB.");
      setMessageTone("error");
      return;
    }

    setLoading(true);
    setMessage("");
    setMessageTone("neutral");

    try {
      const response = await resumeApi.upload(selectedFile);
      setAnalysis(response);
      setMessage("Resume uploaded and analyzed successfully.");
      setMessageTone("success");
    } catch {
      setMessage("Upload failed. Please verify the backend and AI service are running, then try again.");
      setMessageTone("error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <SectionCard title="Resume Studio" subtitle="Upload your latest PDF or DOCX and convert it into a complete interview-prep profile.">
        <form onSubmit={handleUpload} className="grid gap-4 lg:grid-cols-[1fr_auto]">
          <label className="rounded-[1.5rem] border border-dashed border-cyan-300/35 bg-white/5 p-6">
            <span className="font-display text-xl text-white">Select resume file</span>
            <p className="app-muted mt-2 text-sm">Supported formats: PDF, DOCX</p>
            <input
              type="file"
              accept=".pdf,.docx"
              className="mt-4 block w-full text-sm text-white/85"
              onChange={(event) => {
                const nextFile = event.target.files?.[0] ?? null;
                setSelectedFile(nextFile);
                if (nextFile) {
                  setMessage(`${nextFile.name} is ready to analyze.`);
                  setMessageTone("neutral");
                }
              }}
            />
          </label>
          <button
            type="submit"
            disabled={loading}
            className="app-button-primary h-fit rounded-full px-6 py-3 font-medium transition disabled:opacity-70"
          >
            {loading ? "Analyzing..." : "Upload and analyze"}
          </button>
        </form>
        {message ? (
          <p
            className={`mt-4 text-sm ${
              messageTone === "error"
                ? "text-rose-600"
                : messageTone === "success"
                  ? "text-cyan-300"
                  : "text-white/70"
            }`}
          >
            {message}
          </p>
        ) : null}
      </SectionCard>

      {analysis ? (
        <>
          <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
            <SectionCard title="Candidate Snapshot" subtitle={analysis.originalFileName}>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">{analysis.candidateName || "Detected profile"}</p>
                  <p className="mt-4 text-4xl font-bold text-cyan-300">{Math.round(analysis.readinessScore)}%</p>
                  <p className="app-muted mt-4 text-sm">{analysis.summary}</p>
                </div>
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">Contact info</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {analysis.contactInfo.map((item) => (
                      <span key={item} className="app-chip rounded-full px-3 py-2 text-sm font-medium">
                        {item}
                      </span>
                    ))}
                  </div>
                  <p className="mt-5 font-display text-lg text-white">Extracted skills</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {analysis.extractedSkills.map((skill) => (
                      <span key={skill} className="app-chip rounded-full px-3 py-2 text-sm font-medium">
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            </SectionCard>

            <SectionCard title="Interview Strategy" subtitle="Your role fit, skill gaps, and mentor direction.">
              <div className="space-y-4">
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">Recommended roles</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {analysis.recommendedRoles.map((role) => (
                      <span key={role} className="app-chip-secondary rounded-full px-3 py-2 text-sm font-medium">
                        {role}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">Missing skills</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {analysis.missingSkills.length ? (
                      analysis.missingSkills.map((skill) => (
                        <span key={skill} className="app-chip-accent rounded-full px-3 py-2 text-sm font-medium">
                          {skill}
                        </span>
                      ))
                    ) : (
                      <span className="app-muted text-sm">No major role-fit skill gaps detected.</span>
                    )}
                  </div>
                </div>
                <div className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-white/82">
                  <p className="font-display text-lg text-white">Mentor guidance</p>
                  <p className="mt-3">{analysis.mentorGuidance}</p>
                </div>
              </div>
            </SectionCard>
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <SectionList title="Strengths" items={analysis.strengths} />
            <SectionList title="Weaknesses" items={analysis.weaknesses} />
            <SectionList title="Strength indicators" items={analysis.strengthIndicators} />
            <SectionList title="Weakness indicators" items={analysis.weaknessIndicators} />
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <SectionList title="Experience" items={analysis.experience} />
            <SectionList title="Projects" items={analysis.projects} />
            <SectionList title="Education" items={analysis.education} />
            <SectionList title="Certifications" items={analysis.certifications} />
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <SectionList title="Improvement roadmap" items={analysis.improvementRoadmap} />
            <SectionList title="Learning suggestions" items={analysis.learningSuggestions} />
          </div>
        </>
      ) : null}
    </div>
  );
}

