import { Link } from "react-router-dom";

export function ForbiddenPage() {
  return (
    <div
      className="workspace-theme flex min-h-screen items-center justify-center px-4"
      style={{
        backgroundImage:
          "radial-gradient(circle at 15% 18%, rgba(255, 75, 199, 0.2), transparent 28%), radial-gradient(circle at 82% 18%, rgba(30, 214, 255, 0.18), transparent 26%), linear-gradient(to right, #18081F 50%, #0A1736 50%)"
      }}
    >
      <div className="glass-panel max-w-xl rounded-[2rem] p-10 text-center">
        <p className="font-display text-sm uppercase tracking-[0.35em] text-cyan-300">Access Restricted</p>
        <h1 className="mt-4 font-display text-4xl text-white">This area is reserved for platform administrators.</h1>
        <p className="mt-4 text-sm leading-7 text-white/68">
          Your account is signed in correctly, but it does not currently carry the permissions needed for this route.
        </p>
        <Link
          to="/dashboard"
          className="app-button-primary mt-6 inline-flex rounded-full px-5 py-3 text-sm font-medium transition"
        >
          Return to dashboard
        </Link>
      </div>
    </div>
  );
}

