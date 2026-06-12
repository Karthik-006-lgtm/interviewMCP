import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { companyApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import type { CompanyProfile } from "../../types";

export function CompanyDetailPage() {
  const { companyId } = useParams();
  const [company, setCompany] = useState<CompanyProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshing, setRefreshing] = useState(false);
  const [refreshError, setRefreshError] = useState("");

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

  const refreshCompany = async () => {
    if (!companyId) {
      return;
    }
    setRefreshError("");
    setRefreshing(true);
    try {
      const updated = await companyApi.refresh(companyId);
      setCompany(updated);
    } catch {
      setRefreshError("Unable to refresh the company profile right now. Please try again in a moment.");
    } finally {
      setRefreshing(false);
    }
  };

  if (loading) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-gray-600">Loading company profile...</div>;
  }

  if (!company) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-rose-600">{error || "Company profile not found."}</div>;
  }

  return (
    <div className="space-y-6">
      <SectionCard title={company.name} subtitle={company.website ?? company.industry ?? "Company intelligence profile"}>
        <div className="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4 xl:flex-1">
            <div className="app-surface rounded-[1.5rem] p-5">
              <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Industry</p>
              <p className="mt-3 text-lg font-semibold text-gray-900">{company.industry || "Unknown"}</p>
            </div>
            <div className="app-surface rounded-[1.5rem] p-5">
              <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Size</p>
              <p className="mt-3 text-lg font-semibold text-gray-900">{company.size || "Not available"}</p>
            </div>
            <div className="app-surface rounded-[1.5rem] p-5">
              <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Founded</p>
              <p className="mt-3 text-lg font-semibold text-gray-900">{company.foundedYear ?? "—"}</p>
            </div>
            <div className="app-surface rounded-[1.5rem] p-5">
              <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Headquarters</p>
              <p className="mt-3 text-lg font-semibold text-gray-900">{company.headquarters || "—"}</p>
            </div>
          </div>
          <div className="flex flex-col items-start gap-3 xl:items-end">
            <div className="rounded-[1.5rem] border border-gray-300 bg-white/80 p-4 text-sm text-gray-700">
              {company.lastEnrichedAt ? (
                <p>Last refreshed: {new Date(company.lastEnrichedAt).toLocaleString()}</p>
              ) : (
                <p>Profile has not been refreshed from public sources yet.</p>
              )}
            </div>
            <button
              type="button"
              onClick={refreshCompany}
              disabled={refreshing}
              className="app-button-secondary rounded-full px-4 py-2 text-sm font-medium transition"
            >
              {refreshing ? "Refreshing…" : "Refresh company data"}
            </button>
            {refreshError ? <p className="text-sm text-rose-400">{refreshError}</p> : null}
          </div>
        </div>

        <div className="mt-5 grid gap-4 lg:grid-cols-2">
          <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
            <p className="font-semibold text-teal-700">Website</p>
            {company.website ? (
              <a href={company.website} target="_blank" rel="noreferrer" className="text-teal-600 hover:underline">
                {company.website}
              </a>
            ) : (
              <p className="mt-2">Not available</p>
            )}
          </div>
          <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
            <p className="font-semibold text-teal-700">Careers page</p>
            {company.careersUrl ? (
              <a href={company.careersUrl} target="_blank" rel="noreferrer" className="text-teal-600 hover:underline">
                {company.careersUrl}
              </a>
            ) : (
              <p className="mt-2">Ready for enrichment</p>
            )}
          </div>
        </div>
      </SectionCard>

      <SectionCard title="Key Contacts" subtitle="Useful names for recruiter and hiring manager outreach.">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Recruiter / HR</p>
            <p className="mt-3 text-lg font-semibold text-gray-900">{company.hrContact || "Not listed"}</p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Hiring Manager</p>
            <p className="mt-3 text-lg font-semibold text-gray-900">{company.hiringManager || "Not listed"}</p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Owner / Founder</p>
            <p className="mt-3 text-lg font-semibold text-gray-900">{company.ownerName || "Not listed"}</p>
          </div>
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-xs uppercase tracking-[0.25em] text-gray-500">Employees</p>
            <p className="mt-3 text-lg font-semibold text-gray-900">
              {company.employeeCount ? company.employeeCount.toLocaleString() : "Unknown"}
            </p>
          </div>
        </div>
      </SectionCard>

      {company.description ? (
        <SectionCard title="Company Description" subtitle="Use this summary to ground your outreach and interview preparation.">
          <p className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-gray-700">{company.description}</p>
        </SectionCard>
      ) : null}

      <div className="grid gap-6 xl:grid-cols-[1fr_1fr]">
        <SectionCard title="Why You Match" subtitle="The current fit rationale generated from your roles and resume profile.">
          <div className="app-surface rounded-[1.5rem] p-5">
            <p className="text-sm leading-7 text-gray-700">{company.whyUserMatches || "The platform has not generated a fit rationale yet."}</p>
            <div className="app-chip-accent mt-4 inline-flex rounded-full px-3 py-2 text-sm font-semibold">
              Match score: {company.matchScore !== undefined ? `${Math.round(company.matchScore)}%` : "N/A"}
            </div>
          </div>
        </SectionCard>

        <SectionCard title="Culture" subtitle="Use this context to tailor your stories during recruiter and manager rounds.">
          <p className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-gray-700">{company.culture || "Culture information is not available for this company."}</p>
        </SectionCard>
      </div>
      <SectionCard title="Supported Roles" subtitle="Use these role tracks to align your examples and value proposition.">
        <div className="flex flex-wrap gap-2">
          {(company.supportedRoles || []).map((role) => (
            <span key={role} className="app-chip rounded-full px-3 py-2 text-sm font-medium">
              {role}
            </span>
          ))}
        </div>
      </SectionCard>
      <SectionCard title="Interview Focus Areas" subtitle="Topics this company is likely to probe during the hiring loop.">
        <div className="flex flex-wrap gap-2">
          {(company.interviewFocusAreas || []).map((area) => (
            <span key={area} className="app-chip-secondary rounded-full px-3 py-2 text-sm font-medium">
              {area}
            </span>
          ))}
        </div>
      </SectionCard>

      <SectionCard title="Company History" subtitle="A compact narrative you can use before recruiter and manager rounds.">
        <p className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-gray-700">{company.companyHistory || "History details are not available."}</p>
      </SectionCard>

      {company.products?.length || company.techStack?.length || company.leadership?.length || company.officialLinks?.length ? (
        <SectionCard title="Intelligence Snapshot" subtitle="Key signals from public profiles and hiring insights.">
          <div className="grid gap-4 lg:grid-cols-2">
            {company.products?.length ? (
              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
                <p className="font-semibold text-teal-700">Products</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {company.products.map((product) => (
                    <span key={product} className="app-chip rounded-full px-3 py-2 text-xs font-medium">
                      {product}
                    </span>
                  ))}
                </div>
              </div>
            ) : null}
            {company.techStack?.length ? (
              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
                <p className="font-semibold text-teal-700">Tech stack</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {company.techStack.map((tech) => (
                    <span key={tech} className="app-chip rounded-full px-3 py-2 text-xs font-medium">
                      {tech}
                    </span>
                  ))}
                </div>
              </div>
            ) : null}
            {company.leadership?.length ? (
              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
                <p className="font-semibold text-teal-700">Leadership</p>
                <ul className="mt-3 space-y-2">
                  {company.leadership.map((leader) => (
                    <li key={`${leader.name}-${leader.title}`}>
                      <span className="font-semibold text-gray-900">{leader.name}</span>
                      {leader.title ? ` — ${leader.title}` : ""}
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
            {company.officialLinks?.length ? (
              <div className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
                <p className="font-semibold text-teal-700">Official links</p>
                <ul className="mt-3 space-y-2">
                  {company.officialLinks.map((link) => (
                    <li key={link.url}>
                      <a href={link.url} target="_blank" rel="noreferrer" className="text-teal-600 hover:underline">
                        {link.platform}
                      </a>
                      {link.isPrimary ? <span className="ml-2 text-xs uppercase tracking-[0.2em] text-gray-500">Primary</span> : null}
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
          </div>
        </SectionCard>
      ) : null}

      {company.opportunities?.length ? (
        <SectionCard title="Active Opportunities" subtitle="Hiring opportunities discovered for this company.">
          <div className="space-y-4">
            {company.opportunities.map((job) => (
              <div key={`${job.title}-${job.role}`} className="app-surface rounded-[1.5rem] p-5 text-sm text-gray-700">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="font-semibold text-gray-900">{job.title}</p>
                    <p className="mt-1 text-xs text-gray-500">{job.role} • {job.location || "Remote/Unknown"}</p>
                  </div>
                  <span className="app-chip rounded-full px-3 py-1 text-xs font-semibold">{job.employmentType || "Full-time"}</span>
                </div>
                {job.description ? <p className="mt-3 leading-7">{job.description}</p> : null}
                <div className="mt-3 flex flex-wrap gap-2">
                  {job.skillsRequired?.map((skill) => (
                    <span key={skill} className="app-chip rounded-full px-3 py-2 text-xs font-medium">
                      {skill}
                    </span>
                  ))}
                </div>
                {job.sourceUrl ? (
                  <a href={job.sourceUrl} target="_blank" rel="noreferrer" className="mt-3 inline-block text-teal-600 hover:underline">
                    View job posting
                  </a>
                ) : null}
              </div>
            ))}
          </div>
        </SectionCard>
      ) : null}

      {company.intelligenceSources?.length ? (
        <SectionCard title="Intelligence Sources" subtitle="Sources used to build this company profile.">
          <div className="flex flex-wrap gap-2">
            {company.intelligenceSources.map((source) => (
              <span key={source} className="app-chip-secondary rounded-full px-3 py-2 text-sm font-medium">
                {source}
              </span>
            ))}
          </div>
        </SectionCard>
      ) : null}
    </div>
  );
}

