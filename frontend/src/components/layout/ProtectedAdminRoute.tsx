import type { PropsWithChildren } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

export function ProtectedAdminRoute({ children }: PropsWithChildren) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div
        className="workspace-theme flex min-h-screen items-center justify-center"
        style={{
          backgroundImage:
            "radial-gradient(circle at 15% 18%, rgba(255, 75, 199, 0.2), transparent 28%), radial-gradient(circle at 82% 18%, rgba(30, 214, 255, 0.18), transparent 26%), linear-gradient(to right, #18081F 50%, #0A1736 50%)"
        }}
      >
        <div className="glass-panel rounded-[1.75rem] px-8 py-6 text-center">
          <p className="font-display text-xl text-white">Loading admin workspace...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <Navigate to="/forbidden" replace />;
  }

  return <>{children}</>;
}

