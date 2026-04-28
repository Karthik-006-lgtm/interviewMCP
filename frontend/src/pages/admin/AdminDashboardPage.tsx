import { useEffect, useState } from "react";
import { adminApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import type { AdminDashboard } from "../../types";

export function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState<AdminDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi
      .dashboard()
      .then(setDashboard)
      .catch(() => setError("Admin telemetry is unavailable right now. Check backend access and try again."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-white/68">Loading admin telemetry...</div>;
  }

  if (error || !dashboard) {
    return (
      <div className="glass-panel rounded-[1.75rem] p-6 text-sm text-rose-600">
        {error || "Admin data could not be loaded."}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <SectionCard
        title="Admin Control Tower"
        subtitle="Monitor candidate activity, orchestration volume, and the MCP tool surface from one secured view."
      >
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <MetricCard label="Users" value={dashboard.totalUsers} />
          <MetricCard label="Resumes" value={dashboard.totalResumes} />
          <MetricCard label="Interviews" value={dashboard.totalInterviews} />
          <MetricCard label="Reports" value={dashboard.totalReports} />
          <MetricCard label="Companies" value={dashboard.totalCompanies} />
          <MetricCard label="Roles" value={dashboard.totalRoles} />
          <MetricCard label="Revoked Tokens" value={dashboard.revokedTokenCount} />
          <MetricCard label="MCP Tools" value={dashboard.availableMcpTools.length} />
        </div>
      </SectionCard>

      <div className="grid gap-6 xl:grid-cols-2">
        <SectionCard title="Recent Users" subtitle="Newest accounts and granted roles.">
          <div className="space-y-3">
            {dashboard.recentUsers.map((user) => (
              <div key={user.id} className="app-surface rounded-[1.25rem] p-4 text-sm text-white/76">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <p className="font-semibold text-white">{user.fullName}</p>
                  <span className="text-xs uppercase tracking-[0.25em] text-white/45">
                    {new Date(user.createdAt).toLocaleString()}
                  </span>
                </div>
                <p className="mt-2">{user.email}</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {user.roles.map((role) => (
                    <span key={role} className="app-chip rounded-full px-3 py-1 text-xs font-medium">
                      {role}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard title="Recent Resume Uploads" subtitle="Structured resume ingestion health at a glance.">
          <div className="space-y-3">
            {dashboard.recentResumes.map((resume) => (
              <div key={resume.id} className="app-surface rounded-[1.25rem] p-4 text-sm text-white/76">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <p className="font-semibold text-white">{resume.candidateName || resume.userEmail}</p>
                  <span className="text-xs uppercase tracking-[0.25em] text-white/45">
                    {new Date(resume.uploadedAt).toLocaleString()}
                  </span>
                </div>
                <p className="mt-2">{resume.originalFileName}</p>
                <p className="mt-1 text-white/48">{resume.userEmail}</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {resume.recommendedRoles.map((role) => (
                    <span key={role} className="app-chip rounded-full px-3 py-1 text-xs font-medium">
                      {role}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </SectionCard>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <SectionCard title="Recent Interview Sessions" subtitle="Track live practice volume and score coverage.">
          <div className="space-y-3">
            {dashboard.recentInterviews.map((session) => (
              <div key={session.sessionId} className="app-surface rounded-[1.25rem] p-4 text-sm text-white/76">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <p className="font-semibold text-white">{session.userEmail}</p>
                  <span className="app-chip-accent rounded-full px-3 py-1 text-xs font-semibold">
                    {typeof session.overallScore === "number" ? `${Math.round(session.overallScore)}% scored` : "In progress"}
                  </span>
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  {session.selectedRoles.map((role) => (
                    <span key={role} className="app-chip-secondary rounded-full px-3 py-1 text-xs font-medium">
                      {role}
                    </span>
                  ))}
                </div>
                <p className="mt-3 text-xs uppercase tracking-[0.25em] text-white/45">
                  {new Date(session.createdAt).toLocaleString()}
                </p>
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard title="MCP Runtime Surface" subtitle="Tool manifests currently available through the backend invocation bridge.">
          <div className="flex flex-wrap gap-2">
            {dashboard.availableMcpTools.map((tool) => (
              <span key={tool} className="app-chip-secondary rounded-full px-3 py-2 text-xs font-medium">
                {tool}
              </span>
            ))}
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

function MetricCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="app-surface rounded-[1.5rem] p-5">
      <p className="text-xs font-semibold uppercase tracking-[0.25em] text-white/45">{label}</p>
      <p className="mt-3 font-display text-3xl text-cyan-300">{value}</p>
    </div>
  );
}

