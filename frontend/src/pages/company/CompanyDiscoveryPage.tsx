import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { companyApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";

export function CompanyDiscoveryPage() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [website, setWebsite] = useState("");
  const [domain, setDomain] = useState("");
  const [refresh, setRefresh] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    if (!name.trim()) {
      setError("Company name is required.");
      return;
    }

    setLoading(true);
    try {
      const profile = await companyApi.discover({
        name: name.trim(),
        website: website.trim() || undefined,
        domain: domain.trim() || undefined,
        refresh,
      });
      navigate(`/companies/${profile.id}`);
    } catch (err) {
      setError("We couldn't discover that company right now. Please check the name or URL and try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <SectionCard
        title="Discover a company"
        subtitle="Bring a new target company into your intelligence workflow and build a live profile from public sources."
        action={
          <button
            type="submit"
            form="company-discovery-form"
            disabled={loading}
            className="app-button-primary rounded-full px-4 py-2 text-sm font-medium transition"
          >
            {loading ? "Discovering…" : "Discover company"}
          </button>
        }
      >
        <form id="company-discovery-form" className="space-y-5" onSubmit={handleSubmit}>
          {error ? <p className="text-sm text-rose-500">{error}</p> : null}
          <label className="space-y-2 text-sm text-gray-700">
            <span className="font-medium text-gray-900">Company name</span>
            <input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="e.g. Acme Labs"
              className="w-full rounded-[1rem] border border-gray-300 bg-white px-4 py-3 text-gray-900 outline-none transition"
              required
            />
          </label>

          <div className="grid gap-4 lg:grid-cols-2">
            <label className="space-y-2 text-sm text-gray-700">
              <span className="font-medium text-gray-900">Website</span>
              <input
                value={website}
                onChange={(event) => setWebsite(event.target.value)}
                placeholder="https://example.com"
                className="w-full rounded-[1rem] border border-gray-300 bg-white px-4 py-3 text-gray-900 outline-none transition"
              />
            </label>
            <label className="space-y-2 text-sm text-gray-700">
              <span className="font-medium text-gray-900">Domain</span>
              <input
                value={domain}
                onChange={(event) => setDomain(event.target.value)}
                placeholder="example.com"
                className="w-full rounded-[1rem] border border-gray-300 bg-white px-4 py-3 text-gray-900 outline-none transition"
              />
            </label>
          </div>

          <label className="inline-flex items-center gap-3 text-sm text-gray-700">
            <input
              type="checkbox"
              checked={refresh}
              onChange={(event) => setRefresh(event.target.checked)}
              className="h-4 w-4 rounded border-gray-400 bg-white text-teal-600 focus:ring-teal-600"
            />
            <span>Force refresh from public company sources even if this company already exists.</span>
          </label>

          <p className="text-sm text-gray-600">
            Use a company name plus website or domain to fetch company insights, careers pages, leadership signals, and opportunity data.
          </p>
        </form>
      </SectionCard>

      <SectionCard title="Discovery workflow" subtitle="Know when to add a new company to your target list.">
        <div className="app-surface rounded-[1.5rem] p-5 text-sm leading-7 text-gray-700">
          <p className="mb-3">
            Discover companies by name, website, or domain to create a persisted profile you can review, refresh, and prepare against.
          </p>
          <p className="mb-3">
            If the company already exists in your dashboard, the refresh option ensures the latest public intelligence is loaded before you begin outreach.
          </p>
          <p className="text-gray-600">
            After discovery, you'll be taken to the company detail page, where you can review the profile, contacts, culture, and interview focus areas.
          </p>
        </div>
      </SectionCard>
    </div>
  );
}
