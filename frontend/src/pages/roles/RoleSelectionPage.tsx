import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { recommendationApi, roleApi, resumeApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import { loadSelectedRoles, saveSelectedRoles } from "../../lib/rolePreferences";
import type { RecommendationProfile, ResumeAnalysis, RoleProfile } from "../../types";

export function RoleSelectionPage() {
  const [roles, setRoles] = useState<RoleProfile[]>([]);
  const [resume, setResume] = useState<ResumeAnalysis | null>(null);
  const [recommendations, setRecommendations] = useState<RecommendationProfile | null>(null);
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [selectionMode, setSelectionMode] = useState<"single" | "multi">("multi");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const storedRoles = loadSelectedRoles();
    if (storedRoles.length) {
      setSelectedRoles(storedRoles);
    }

    Promise.all([roleApi.list(), resumeApi.latest().catch(() => null), recommendationApi.profile().catch(() => null)])
      .then(([availableRoles, latestResume, profileRecommendations]) => {
        setRoles(availableRoles);
        setResume(latestResume);
        setRecommendations(profileRecommendations);

        if (!storedRoles.length) {
          const suggestedRoles =
            profileRecommendations?.recommendedRoles.length
              ? profileRecommendations.recommendedRoles
              : latestResume?.recommendedRoles.length
                ? latestResume.recommendedRoles
                : availableRoles.slice(0, 2).map((role) => role.name);
          setSelectedRoles(suggestedRoles);
          saveSelectedRoles(suggestedRoles);
        }
      })
      .catch(() => setError("We couldn't load role intelligence right now. Try refreshing once the backend is available."))
      .finally(() => setLoading(false));
  }, []);

  const toggleRole = (roleName: string) => {
    const nextRoles =
      selectionMode === "single"
        ? [roleName]
        : selectedRoles.includes(roleName)
          ? selectedRoles.filter((role) => role !== roleName)
          : [...selectedRoles, roleName];

    setSelectedRoles(nextRoles);
    saveSelectedRoles(nextRoles);
  };

  const suggestedRoles = useMemo(
    () => recommendations?.recommendedRoles ?? resume?.recommendedRoles ?? [],
    [recommendations, resume]
  );

  if (loading) {
    return <div className="glass-panel rounded-[1.75rem] p-6 text-gray-700">Loading role recommendations...</div>;
  }

  return (
    <div className="space-y-6">
      <SectionCard
        title="Role Selection"
        subtitle="Choose a single role or a multi-role interview strategy, then carry those choices into company matching and mock interviews."
        action={
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setSelectionMode("single")}
              className={`rounded-full px-4 py-2 text-sm font-medium transition ${
                selectionMode === "single" ? "app-button-primary" : "app-button-soft"
              }`}
            >
              Single select
            </button>
            <button
              type="button"
              onClick={() => setSelectionMode("multi")}
              className={`rounded-full px-4 py-2 text-sm font-medium transition ${
                selectionMode === "multi" ? "app-button-primary" : "app-button-soft"
              }`}
            >
              Multi select
            </button>
          </div>
        }
      >
        {error ? <p className="mb-4 text-sm text-rose-600">{error}</p> : null}
        {suggestedRoles.length ? (
          <div className="app-surface mb-5 rounded-[1.5rem] p-5">
            <p className="font-display text-lg text-gray-900">Suggested roles from your profile</p>
            <div className="mt-4 flex flex-wrap gap-2">
              {suggestedRoles.map((role) => (
                <span key={role} className="app-chip rounded-full px-3 py-2 text-sm font-medium">
                  {role}
                </span>
              ))}
            </div>
          </div>
        ) : null}

        <div className="grid gap-4 lg:grid-cols-2">
          {roles.map((role) => {
            const active = selectedRoles.includes(role.name);
            const suggested = suggestedRoles.includes(role.name);
            return (
              <button
                key={role.id}
                type="button"
                onClick={() => toggleRole(role.name)}
                className={`rounded-[1.5rem] border p-5 text-left transition ${
                  active
                    ? "border-teal-600/45 bg-teal-600/12 text-gray-900 shadow-[0_24px_45px_rgba(110,207,164,0.18)]"
                    : "app-surface text-gray-700 hover:-translate-y-0.5 hover:border-teal-600/30"
                }`}
              >
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <p className="font-display text-xl">{role.name}</p>
                  <div className="flex gap-2">
                    {suggested ? (
                      <span className={`rounded-full px-3 py-1 text-xs font-semibold ${active ? "bg-white/20 text-gray-900" : "app-chip"}`}>
                        Suggested
                      </span>
                    ) : null}
                    {active ? (
                      <span className="rounded-full bg-white/20 px-3 py-1 text-xs font-semibold text-gray-900">Selected</span>
                    ) : null}
                  </div>
                </div>
                <p className={`mt-3 text-sm leading-7 ${active ? "text-gray-800" : "text-gray-600"}`}>{role.summary}</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {role.coreSkills.map((skill) => (
                    <span
                      key={skill}
                      className={`rounded-full px-3 py-2 text-xs font-medium ${
                        active ? "bg-white/20 text-gray-900" : "app-chip"
                      }`}
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </button>
            );
          })}
        </div>
      </SectionCard>

      <SectionCard title="Next Step" subtitle="Use your saved roles to filter companies and generate a more focused interview plan.">
        <div className="flex flex-wrap items-center gap-3">
          <Link to="/companies" className="app-button-primary rounded-full px-5 py-3 text-sm font-medium transition">
            Explore company matches
          </Link>
          <Link to="/interview" className="app-button-secondary rounded-full px-5 py-3 text-sm font-medium transition">
            Start interview practice
          </Link>
          <p className="text-sm text-gray-600">
            Current selection: {selectedRoles.length ? selectedRoles.join(", ") : "No roles selected yet"}
          </p>
        </div>
      </SectionCard>
    </div>
  );
}

