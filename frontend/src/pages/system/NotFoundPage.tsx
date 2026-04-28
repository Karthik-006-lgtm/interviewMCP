import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div
      className="workspace-theme flex min-h-screen items-center justify-center px-4 py-10"
      style={{
        backgroundImage:
          "radial-gradient(circle at 15% 18%, rgba(255, 75, 199, 0.2), transparent 28%), radial-gradient(circle at 82% 18%, rgba(30, 214, 255, 0.18), transparent 26%), linear-gradient(to right, #18081F 50%, #0A1736 50%)"
      }}
    >
      <div className="glass-panel w-full max-w-2xl rounded-[2rem] p-10 text-center">
        <p className="font-display text-sm uppercase tracking-[0.35em] text-cyan-300">404</p>
        <h1 className="mt-4 font-display text-4xl text-white">That page doesn&apos;t exist in this workspace.</h1>
        <p className="mt-4 text-sm leading-7 text-white/68">
          The route may be incorrect, expired, or no longer available. Head back to your dashboard and continue from a known screen.
        </p>
        <div className="mt-8 flex flex-wrap justify-center gap-3">
          <Link to="/dashboard" className="app-button-primary rounded-full px-5 py-3 text-sm font-medium transition">
            Go to dashboard
          </Link>
          <Link to="/login" className="app-button-soft rounded-full px-5 py-3 text-sm font-medium transition">
            Go to login
          </Link>
        </div>
      </div>
    </div>
  );
}

