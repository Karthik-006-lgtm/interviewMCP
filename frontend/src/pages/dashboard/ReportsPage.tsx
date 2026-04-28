import { useEffect, useState } from "react";
import { reportApi } from "../../api/client";
import { SectionCard } from "../../components/ui/SectionCard";
import type { PracticeReport } from "../../types";

export function ReportsPage() {
  const [reports, setReports] = useState<PracticeReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    reportApi
      .list()
      .then(setReports)
      .catch(() => setError("We couldn't load reports right now."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <SectionCard title="Reports Dashboard" subtitle="Track score trends, weak areas, and the actions that will improve your next interview round.">
      {error ? <p className="mb-4 text-sm text-rose-600">{error}</p> : null}
      {loading ? (
        <p className="text-sm text-white/68">Loading reports...</p>
      ) : reports.length ? (
        <div className="grid gap-4 lg:grid-cols-2">
          {reports.map((report) => (
            <article key={report.id} className="app-surface rounded-[1.6rem] p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h3 className="font-display text-xl text-white">{report.title}</h3>
                  <p className="text-sm text-white/52">
                    Session #{report.sessionId} | {new Date(report.createdAt).toLocaleString()}
                  </p>
                </div>
                <span className="app-chip-accent rounded-full px-3 py-2 text-sm font-semibold">
                  {Math.round(report.overallScore)}%
                </span>
              </div>
              <div className="mt-5 space-y-4 text-sm text-white/76">
                <div>
                  <p className="font-semibold text-cyan-200">Executive summary</p>
                  <p className="mt-1">{report.executiveSummary}</p>
                </div>
                <div>
                  <p className="font-semibold text-cyan-200">Weak areas</p>
                  <ul className="mt-2 space-y-2">
                    {report.weakAreas.map((item) => (
                      <li key={item}>- {item}</li>
                    ))}
                  </ul>
                </div>
                <div>
                  <p className="font-semibold text-cyan-200">Recommended actions</p>
                  <ul className="mt-2 space-y-2">
                    {report.recommendedActions.map((item) => (
                      <li key={item}>- {item}</li>
                    ))}
                  </ul>
                </div>
                <div>
                  <p className="font-semibold text-cyan-200">Progress summary</p>
                  <p className="mt-1">{report.progressSummary}</p>
                </div>
                <div>
                  <p className="font-semibold text-cyan-200">Next steps</p>
                  <p className="mt-1">{report.nextSteps}</p>
                </div>
                <div>
                  <p className="font-semibold text-cyan-200">Weekly improvement plan</p>
                  <ul className="mt-2 space-y-2">
                    {report.weeklyImprovementPlan.map((item) => (
                      <li key={item}>- {item}</li>
                    ))}
                  </ul>
                </div>
                <div>
                  <p className="font-semibold text-cyan-200">Practice tasks</p>
                  <ul className="mt-2 space-y-2">
                    {report.practiceTasks.map((item) => (
                      <li key={item}>- {item}</li>
                    ))}
                  </ul>
                </div>
                <div>
                  <p className="font-semibold text-cyan-200">Targeted questions</p>
                  <ul className="mt-2 space-y-2">
                    {report.targetedQuestions.map((item) => (
                      <li key={item}>- {item}</li>
                    ))}
                  </ul>
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <p className="app-surface rounded-[1.5rem] p-5 text-sm text-white/68">
          Reports appear here after you answer interview questions.
        </p>
      )}
    </SectionCard>
  );
}

