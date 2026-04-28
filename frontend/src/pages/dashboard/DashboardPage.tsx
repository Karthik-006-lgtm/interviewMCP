import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { recommendationApi, reportApi, resumeApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import { StatCard } from "../../components/ui/StatCard";
import type { DashboardMetrics, RecommendationProfile, ResumeAnalysis } from "../../types";

export function DashboardPage() {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [resume, setResume] = useState<ResumeAnalysis | null>(null);
  const [recommendations, setRecommendations] = useState<RecommendationProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      reportApi.dashboard(),
      resumeApi.latest().catch(() => null),
      recommendationApi.profile().catch(() => null)
    ])
      .then(([dashboardMetrics, latestResume, recommendationProfile]) => {
        setMetrics(dashboardMetrics);
        setResume(latestResume);
        setRecommendations(recommendationProfile);
      })
      .catch(() => setError("We couldn't load dashboard analytics right now."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-white/75">Loading dashboard insights...</div>;
  }

  return (
    <div className="space-y-6">
      {error ? <div className="glass-panel rounded-[1.5rem] p-4 text-sm text-rose-600">{error}</div> : null}
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Resumes"
          value={String(metrics?.resumeCount ?? 0)}
          accent="text-cyan-200"
          helper="Profile versions available for AI analysis."
        />
        <StatCard
          label="Company Matches"
          value={String(metrics?.companyMatchCount ?? 0)}
          accent="text-cyan-300"
          helper="Companies currently aligned to your target roles."
        />
        <StatCard
          label="Average Score"
          value={`${Math.round(metrics?.averageScore ?? 0)}%`}
          accent="text-amber-300"
          helper="Average score across recent practice reports."
        />
        <StatCard
          label="Trend"
          value={`${metrics?.progressDelta && metrics.progressDelta > 0 ? "+" : ""}${Math.round(metrics?.progressDelta ?? 0)}%`}
          accent="text-fuchsia-300"
          helper="Latest score change compared with your prior report."
        />
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <SectionCard
          title="Candidate Intelligence"
          subtitle="Your current profile, role fit, and mentor guidance."
          action={
            <Link className="app-button-primary rounded-full px-4 py-2 text-sm font-medium transition" to="/upload">
              Update resume
            </Link>
          }
        >
          {resume ? (
            <div className="space-y-5">
              <div className="grid gap-4 lg:grid-cols-[0.9fr_1.1fr]">
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">{resume.candidateName || "Latest candidate profile"}</p>
                  <p className="app-muted mt-2 text-sm">{resume.originalFileName}</p>
                  <p className="mt-5 text-4xl font-bold text-cyan-300">{Math.round(resume.readinessScore)}%</p>
                  <p className="app-muted mt-3 text-sm">Readiness score</p>
                </div>
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">Profile summary</p>
                  <p className="app-muted mt-3 text-sm leading-7">{resume.summary}</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {resume.contactInfo.map((item) => (
                      <span key={item} className="app-chip rounded-full px-3 py-2 text-xs font-medium">
                        {item}
                      </span>
                    ))}
                  </div>
                </div>
              </div>

              <div className="grid gap-4 lg:grid-cols-2">
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">Recommended roles</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {resume.recommendedRoles.map((role) => (
                      <span key={role} className="app-chip-secondary rounded-full px-3 py-2 text-sm font-medium">{role}</span>
                    ))}
                  </div>
                </div>
                <div className="app-surface rounded-[1.5rem] p-5">
                  <p className="font-display text-lg text-white">Missing skills</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {(recommendations?.missingSkills.length ? recommendations.missingSkills : resume.missingSkills).map((skill) => (
                      <span key={skill} className="app-chip-accent rounded-full px-3 py-2 text-sm font-medium">{skill}</span>
                    ))}
                  </div>
                </div>
              </div>

              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/88">
                <p className="font-display text-lg text-white">Mentor guidance</p>
                <p className="mt-3 leading-7">{recommendations?.mentorGuidance || resume.mentorGuidance}</p>
              </div>
            </div>
          ) : (
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/82">
              Upload your first resume to unlock section extraction, role recommendations, company matching, and interview guidance.
            </div>
          )}
        </SectionCard>

        <SectionCard title="Priority Roadmap" subtitle="What to improve next based on your latest analysis.">
          {recommendations ? (
            <div className="space-y-4">
              <div className="app-surface rounded-[1.5rem] p-5">
                <p className="font-display text-lg text-white">Improvement roadmap</p>
                <ul className="app-muted mt-4 space-y-2 text-sm">
                  {recommendations.improvementRoadmap.map((item) => (
                    <li key={item}>- {item}</li>
                  ))}
                </ul>
              </div>
              <div className="app-surface rounded-[1.5rem] p-5">
                <p className="font-display text-lg text-white">Learning suggestions</p>
                <ul className="app-muted mt-4 space-y-2 text-sm">
                  {recommendations.learningSuggestions.map((item) => (
                    <li key={item}>- {item}</li>
                  ))}
                </ul>
              </div>
              <div className="app-surface rounded-[1.5rem] p-5">
                <p className="font-display text-lg text-white">Why this matters</p>
                <p className="app-muted mt-3 text-sm leading-7">
                  These recommendations currently map to {recommendations.matchingCompanyCount} role-aligned company
                  match{recommendations.matchingCompanyCount === 1 ? "" : "es"} in the platform.
                </p>
              </div>
            </div>
          ) : (
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/82">
              Upload a resume to generate your improvement roadmap and tailored learning plan.
            </div>
          )}
        </SectionCard>
      </div>

      <div className="grid gap-6 xl:grid-cols-[0.85fr_1.15fr]">
        <SectionCard title="Top Weak Areas" subtitle="Cross-report patterns you should actively improve.">
          {metrics?.topWeakAreas.length ? (
            <ul className="space-y-3 text-sm text-white/82">
              {metrics.topWeakAreas.map((item) => (
                <li key={item} className="app-surface rounded-[1.25rem] p-4">
                  {item}
                </li>
              ))}
            </ul>
          ) : (
            <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/82">
              Complete more practice sessions to see your recurring weak areas here.
            </div>
          )}
        </SectionCard>

        <SectionCard title="Recent Reports" subtitle="Your latest interview feedback and score movement.">
          <div className="space-y-4">
            {metrics?.recentReports.length ? (
              metrics.recentReports.map((report) => (
                <div key={report.id} className="app-surface rounded-[1.5rem] p-5 text-white/82">
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <p className="font-display text-lg text-white">{report.title}</p>
                      <p className="text-sm text-white/58">
                        Session #{report.sessionId} - {new Date(report.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <span className="app-chip-accent rounded-full px-3 py-2 text-sm font-semibold">
                      {Math.round(report.overallScore)}%
                    </span>
                  </div>
                  <p className="mt-4 text-sm text-white/82">{report.executiveSummary}</p>
                  <p className="mt-3 text-sm text-white/82">{report.progressSummary}</p>
                </div>
              ))
            ) : (
              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/82">
                Submit interview answers to generate detailed reports and progress tracking.
              </div>
            )}
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

