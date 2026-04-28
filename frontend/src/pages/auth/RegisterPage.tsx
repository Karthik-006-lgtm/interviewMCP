import { Link, useNavigate } from "react-router-dom";
import { useState, type FormEvent } from "react";
import { useAuth } from "../../hooks/useAuth";

export function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      await register(form);
      navigate("/dashboard");
    } catch {
      setError("Registration failed. Please confirm the backend is running and try again.");
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
        <div className="auth-card w-full max-w-4xl overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-2xl">
          <div className="grid lg:grid-cols-[0.95fr_1.05fr]">
            <div
              className="px-8 py-10 sm:px-10"
              style={{ backgroundColor: "#FEC51E", color: "#212222" }}
            >
              <p className="font-display text-sm uppercase tracking-[0.35em]" style={{ color: "#212222" }}>Career Accelerator</p>
              <h1 className="mt-4 font-display text-4xl" style={{ color: "#212222" }}>Create your workspace and start practicing smarter.</h1>
              <p className="mt-5" style={{ color: "rgba(33, 34, 34, 0.9)" }}>
                Bring your resume, target roles, and speaking practice into one system built for consistent progress.
              </p>
            </div>
            <div className="bg-slate-950 px-8 py-10 text-white sm:px-10">
              <h2 className="font-display text-3xl" style={{ color: "#FEC51E" }}>Create account</h2>
              <form onSubmit={handleSubmit} className="mt-8 space-y-4">
                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-white">Full name</span>
                  <input
                    required
                    type="text"
                    value={form.fullName}
                    onChange={(event) => setForm((current) => ({ ...current, fullName: event.target.value }))}
                    className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3 text-slate-950 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-[#FEC51E] focus:ring-2 focus:ring-[#FEC51E]/30"
                  />
                </label>
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
                    minLength={8}
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
                  className="w-full rounded-full px-5 py-3 font-medium transition duration-200 hover:bg-yellow-400 active:scale-[0.99] active:bg-slate-950 active:text-white focus:outline-none focus:ring-4 focus:ring-[#FEC51E]/25 disabled:opacity-70"
                  style={{ backgroundColor: "#FEC51E", color: "#212222" }}
                >
                  {loading ? "Creating account..." : "Create account"}
                </button>
              </form>
              <p className="mt-6 text-sm text-white/80">
                Already have an account?{" "}
                <Link className="font-semibold" style={{ color: "#FEC51E" }} to="/login">
                  Sign in
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

