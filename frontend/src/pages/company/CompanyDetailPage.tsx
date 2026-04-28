import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { companyApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import type { Company } from "../../types";

export function CompanyDetailPage() {
  const { companyId } = useParams();
  const [company, setCompany] = useState<Company | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!companyId) {
      return;
    }
    setLoading(true);
    setError("");
    companyApi
      .getById(companyId)
      .then(setCompany)
      .catch(() => setError("We couldn't load this company profile right now."))
      .finally(() => setLoading(false));
  }, [companyId]);

  if (loading) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-white/68">Loading company profile...</div>;
  }

  if (!company) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-rose-600">{error || "Company profile not found."}</div>;
  }

  return (
    <div className="space-y-6">
      <SectionCard title={company.name} subtitle={company.website}>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-white/48">Recruiter / HR</p>
            <p className="mt-3 text-lg font-semibold text-white">{company.hrContact}</p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-white/48">Hiring Manager</p>
            <p className="mt-3 text-lg font-semibold text-white">{company.hiringManager}</p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-white/48">Owner / Founder</p>
            <p className="mt-3 text-lg font-semibold text-white">{company.ownerName}</p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-white/48">Employees</p>
            <p className="mt-3 text-lg font-semibold text-white">{company.employeeCount.toLocaleString()}</p>
          </div>
        </div>
      </SectionCard>

      <div className="grid gap-6 xl:grid-cols-[1fr_1fr]">
        <SectionCard title="Why You Match" subtitle="The current fit rationale generated from your roles and resume profile.">
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-sm leading-7 text-white/78">{company.whyUserMatches}</p>
            <div className="app-chip-accent mt-4 inline-flex rounded-full px-3 py-2 text-sm font-semibold">
              Match score: {Math.round(company.matchScore)}%
            </div>
          </div>
        </SectionCard>

        <SectionCard title="Culture" subtitle="Use this context to tailor your stories during recruiter and manager rounds.">
          <p className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-white/78">{company.culture}</p>
        </SectionCard>
      </div>

      <SectionCard title="Supported Roles" subtitle="Use these role tracks to align your examples and value proposition.">
        <div className="flex flex-wrap gap-2">
          {company.supportedRoles.map((role) => (
            <span key={role} className="app-chip rounded-full px-3 py-2 text-sm font-medium">
              {role}
            </span>
          ))}
        </div>
      </SectionCard>

      <SectionCard title="Interview Focus Areas" subtitle="Topics this company is likely to probe during the hiring loop.">
        <div className="flex flex-wrap gap-2">
          {company.interviewFocusAreas.map((area) => (
            <span key={area} className="app-chip-secondary rounded-full px-3 py-2 text-sm font-medium">
              {area}
            </span>
          ))}
        </div>
      </SectionCard>

      <SectionCard title="Company History" subtitle="A compact narrative you can use before recruiter and manager rounds.">
        <p className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-white/78">{company.companyHistory}</p>
      </SectionCard>
    </div>
  );
}

