import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { companyApi, roleApi, resumeApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import { loadSelectedRoles, saveSelectedRoles } from "../../lib/rolePreferences";
import type { Company, RoleProfile } from "../../types";

export function CompanyMatchesPage() {
  const [availableRoles, setAvailableRoles] = useState<RoleProfile[]>([]);
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [minMatchScore, setMinMatchScore] = useState(60);
  const [companySize, setCompanySize] = useState("all");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    setError("");
    Promise.all([roleApi.list(), resumeApi.latest().catch(() => null)])
      .then(([roles, resume]) => {
        setAvailableRoles(roles);
        const storedRoles = loadSelectedRoles();
        const initialRoles = storedRoles.length
          ? storedRoles
          : resume?.recommendedRoles.length
            ? resume.recommendedRoles
            : roles.slice(0, 2).map((role) => role.name);
        setSelectedRoles(initialRoles);
        saveSelectedRoles(initialRoles);
        if (initialRoles.length) {
          return companyApi.search({
            selectedRoles: initialRoles,
            query: searchTerm,
            minMatchScore,
            companySize
          });
        }
        return [];
      })
      .then(setCompanies)
      .catch(() => setError("We couldn't load company matches right now. Try refreshing in a moment."))
      .finally(() => setLoading(false));
  }, []);

  const toggleRole = (role: string) => {
    setSelectedRoles((current) => {
      const next = current.includes(role) ? current.filter((item) => item !== role) : [...current, role];
      saveSelectedRoles(next);
      return next;
    });
  };

  const refreshMatches = async () => {
    setLoading(true);
    setError("");
    try {
      setCompanies(
        selectedRoles.length
          ? await companyApi.search({
              selectedRoles,
              query: searchTerm,
              minMatchScore,
              companySize
            })
          : []
      );
    } catch {
      setError("Refreshing company matches failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <SectionCard
        title="Role Selection"
        subtitle="Choose one or multiple roles to filter company matches down to the right interview targets."
        action={
          <button
            type="button"
            onClick={refreshMatches}
            className="app-button-primary rounded-full px-4 py-2 text-sm font-medium transition"
          >
            Apply roles
          </button>
        }
      >
        <div className="grid gap-4 lg:grid-cols-2">
          {availableRoles.map((role) => {
            const active = selectedRoles.includes(role.name);
            return (
              <button
                key={role.id}
                type="button"
                onClick={() => toggleRole(role.name)}
                className={`rounded-[1.5rem] border p-5 text-left transition ${
                  active
                    ? "border-cyan-300/45 bg-cyan-300/12 text-white shadow-[0_20px_40px_rgba(30,214,255,0.12)]"
                    : "app-surface text-white hover:-translate-y-0.5 hover:border-cyan-300/28"
                }`}
              >
                <div className="flex items-center justify-between gap-3">
                  <p className="font-display text-xl text-white">{role.name}</p>
                  {active ? <span className="rounded-full bg-cyan-300 px-3 py-1 text-xs font-semibold text-slate-950">Selected</span> : null}
                </div>
                <p className="mt-3 text-sm leading-7 text-white/70">{role.summary}</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {role.coreSkills.map((skill) => (
                    <span key={skill} className="app-chip rounded-full px-3 py-2 text-xs font-medium">
                      {skill}
                    </span>
                  ))}
                </div>
              </button>
            );
          })}
        </div>
      </SectionCard>

      <SectionCard
        title="Matched Companies"
        subtitle="Use backend-powered search and filters to narrow the list to the companies you want to target first."
        action={
          <button
            type="button"
            onClick={refreshMatches}
            className="app-button-secondary rounded-full px-4 py-2 text-sm font-medium transition"
          >
            Apply filters
          </button>
        }
      >
        <div className="app-surface mb-5 grid gap-4 rounded-[1.5rem] p-5 lg:grid-cols-[1.2fr_0.8fr_0.8fr]">
          <label className="space-y-2 text-sm text-white/68">
            <span className="font-medium text-white">Search companies or roles</span>
            <input
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Search by company name or role"
              className="w-full rounded-[1rem] border px-4 py-3 outline-none transition"
            />
          </label>
          <label className="space-y-2 text-sm text-white/68">
            <span className="font-medium text-white">Minimum fit score</span>
            <select
              value={String(minMatchScore)}
              onChange={(event) => setMinMatchScore(Number(event.target.value))}
              className="w-full rounded-[1rem] border px-4 py-3 outline-none transition"
            >
              <option value="50">50%+</option>
              <option value="60">60%+</option>
              <option value="70">70%+</option>
              <option value="80">80%+</option>
            </select>
          </label>
          <label className="space-y-2 text-sm text-white/68">
            <span className="font-medium text-white">Company size</span>
            <select
              value={companySize}
              onChange={(event) => setCompanySize(event.target.value)}
              className="w-full rounded-[1rem] border px-4 py-3 outline-none transition"
            >
              <option value="all">All sizes</option>
              <option value="small">Small (&lt; 500)</option>
              <option value="mid">Mid-market (500 - 4,999)</option>
              <option value="enterprise">Enterprise (5,000+)</option>
            </select>
          </label>
        </div>

        {error ? <p className="mb-4 text-sm text-rose-600">{error}</p> : null}
        {loading ? (
          <p className="text-sm text-white/68">Loading company matches...</p>
        ) : companies.length ? (
          <div className="grid gap-4 lg:grid-cols-2">
            {companies.map((company) => (
              <article key={company.id} className="app-surface rounded-[1.75rem] p-5">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-display text-2xl text-white">{company.name}</h3>
                    <p className="mt-1 text-sm text-white/55">{company.website}</p>
                  </div>
                  <span className="app-chip-accent rounded-full px-3 py-2 text-sm font-semibold">
                    {Math.round(company.matchScore)}% fit
                  </span>
                </div>
                <div className="mt-4 flex flex-wrap gap-2">
                  {company.supportedRoles.map((role) => (
                    <span key={role} className="app-chip rounded-full px-3 py-2 text-xs font-medium">
                      {role}
                    </span>
                  ))}
                </div>
                <p className="mt-4 text-sm leading-7 text-white/78">{company.whyUserMatches}</p>
                <p className="mt-4 text-sm text-white/62">{company.culture}</p>
                <Link
                  to={`/companies/${company.id}`}
                  className="app-button-primary mt-5 inline-flex rounded-full px-4 py-2 text-sm font-medium transition"
                >
                  View company profile
                </Link>
              </article>
            ))}
          </div>
        ) : (
          <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/68">
            No companies match the current role selection and filter set yet. Try adjusting your filters or adding another adjacent role.
          </div>
        )}
      </SectionCard>
    </div>
  );
}

