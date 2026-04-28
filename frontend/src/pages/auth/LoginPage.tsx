import { Link, useNavigate } from "react-router-dom";
import { useState, type FormEvent } from "react";
import { useAuth } from "../../hooks/useAuth";

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      await login(form);
      navigate("/dashboard");
    } catch {
      setError("Unable to sign in. Please verify your credentials and backend service.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="relative min-h-screen overflow-hidden"
      style={{
        backgroundImage: "linear-gradient(to right, #FEC51E 50%, #212222 50%)",
        backgroundAttachment: "fixed",
        backgroundRepeat: "no-repeat"
      }}
    >
      <div className="relative z-10 flex min-h-screen items-center justify-center px-4 py-10">
        <div className="auth-card w-full max-w-5xl overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-2xl">
          <div className="grid lg:grid-cols-2">
            <div
              className="flex min-h-[24rem] flex-col justify-center px-8 py-10 sm:px-10"
              style={{ backgroundColor: "#FEC51E", color: "#212222" }}
            >
              <div className="mx-auto max-w-lg text-center sm:text-left">
                <h1 className="font-display text-5xl font-semibold tracking-tight sm:text-6xl" style={{ color: "#212222" }}>
                  Interview Prep
                </h1>
                <p className="mt-4 text-lg leading-snug sm:text-xl" style={{ color: "rgba(33, 34, 34, 0.9)" }}>
                  Practice like your best mentor is sitting beside you.
                </p>
              </div>
            </div>
            <div className="border-slate-300/70 bg-slate-950 px-8 py-10 text-white sm:px-10 lg:border-l">
              <h2 className="font-display text-3xl" style={{ color: "#FEC51E" }}>Welcome back</h2>
              <p className="mt-2 text-sm text-white/80">Sign in to continue your interview preparation journey.</p>
              <form onSubmit={handleSubmit} className="mt-8 space-y-4">
                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-white">Email</span>
                  <input
                    required
                    type="email"
                    value={form.email}
                    onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
                    className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3 text-slate-950 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-[#FEC51E] focus:ring-2 focus:ring-[#FEC51E]/30"
                  />
                </label>
                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-white">Password</span>
                  <input
                    required
                    type="password"
                    value={form.password}
                    onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
                    className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3 text-slate-950 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-[#FEC51E] focus:ring-2 focus:ring-[#FEC51E]/30"
                  />
                </label>
                {error ? <p className="text-sm text-rose-400">{error}</p> : null}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full rounded-full border border-[#FEC51E]/35 bg-slate-900 px-5 py-3 font-medium text-white transition-all duration-300 ease-out hover:bg-[#FEC51E] hover:text-[#212222] hover:shadow-lg hover:shadow-[#FEC51E]/20 focus:bg-[#FEC51E] focus:text-[#212222] focus:outline-none focus:ring-4 focus:ring-[#FEC51E]/25 active:scale-[0.99] active:bg-[#FEC51E] active:text-[#212222] disabled:opacity-70"
                >
                  {loading ? "Signing in..." : "Sign in"}
                </button>
              </form>
              <p className="mt-6 text-sm text-white/80">
                New here?{" "}
                <Link className="font-semibold" style={{ color: "#FEC51E" }} to="/register">
                  Create an account
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

