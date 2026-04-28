interface StatCardProps {
  label: string;
  value: string;
  accent: string;
  helper: string;
}

export function StatCard({ label, value, accent, helper }: StatCardProps) {
  return (
    <div className="glass-panel rounded-[1.75rem] p-5 text-white transition duration-300 hover:-translate-y-0.5 hover:shadow-2xl">
      <p className="font-display text-xs uppercase tracking-[0.3em] text-white/55">{label}</p>
      <p className={`mt-4 font-display text-4xl ${accent}`}>{value}</p>
      <p className="mt-3 text-sm text-white/70">{helper}</p>
    </div>
  );
}

