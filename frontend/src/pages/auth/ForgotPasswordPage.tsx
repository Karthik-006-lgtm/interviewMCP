import { Link } from "react-router-dom";
import { useState, type FormEvent } from "react";
import { Toast } from "../../components/ui/Toast";

function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.trim());
}

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" | "info" } | null>(null);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    if (!isValidEmail(email)) {
      setError("Please enter a valid email address.");
      return;
    }

    setSubmitted(true);
    setToast({
      message: "If an account exists with this email, password recovery instructions have been sent.",
      type: "success"
    });
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
      {toast ? <Toast message={toast.message} type={toast.type} duration={5000} onClose={() => setToast(null)} /> : null}

      <div className="relative z-10 flex min-h-screen items-center justify-center px-4 py-10">
        <div className="w-full max-w-md overflow-hidden rounded-[2rem] border border-slate-200 bg-slate-950 shadow-2xl">
          <div className="px-8 py-10 sm:px-10">
            <h2 className="font-display text-3xl text-white" style={{ color: "#FEC51E" }}>Reset password</h2>
            <p className="mt-2 text-sm text-white/80">
              Enter your email address and we'll help you recover access to your account.
            </p>

            {!submitted ? (
              <form onSubmit={handleSubmit} className="mt-8 space-y-4">
                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-white">Email</span>
                  <input
                    required
                    type="email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    placeholder="name@example.com"
                    className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3 text-slate-950 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-[#FEC51E] focus:ring-2 focus:ring-[#FEC51E]/30"
                  />
                </label>
                {error ? <p className="text-sm text-rose-400">{error}</p> : null}
                <button
                  type="submit"
                  className="w-full rounded-full px-5 py-3 font-medium transition duration-200 hover:bg-yellow-400 active:scale-[0.99] focus:outline-none focus:ring-4 focus:ring-[#FEC51E]/25"
                  style={{ backgroundColor: "#FEC51E", color: "#212222" }}
                >
                  Send recovery link
                </button>
              </form>
            ) : (
              <div className="mt-8 rounded-2xl bg-emerald-900/30 p-5 text-sm text-emerald-300">
                <p className="font-semibold">Check your inbox</p>
                <p className="mt-2 text-emerald-200/80">
                  If an account exists for <span className="font-medium text-white">{email}</span>, you will receive password recovery instructions shortly.
                </p>
              </div>
            )}

            <p className="mt-6 text-sm text-white/80">
              Remember your password?{" "}
              <Link className="font-semibold" style={{ color: "#FEC51E" }} to="/login">
                Sign in
              </Link>
            </p>
            <p className="mt-2 text-sm text-white/80">
              Don't have an account?{" "}
              <Link className="font-semibold" style={{ color: "#FEC51E" }} to="/register">
                Create one
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
