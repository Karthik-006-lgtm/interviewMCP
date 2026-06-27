import { ChangeEvent, ReactNode, useEffect, useMemo, useRef, useState } from "react";
import { NavLink, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const PROFILE_PHOTO_STORAGE_KEY = "interview-prep-profile-photo";

const links = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/upload", label: "Resume Studio" },
  { to: "/roles", label: "Role Strategy" },
  { to: "/companies", label: "Target Companies" },
  { to: "/interview", label: "Mock Interview" },
  { to: "/reports", label: "Reports" },
  { to: "/recordings", label: "Recordings" }
] as const;

interface DashboardLayoutProps {
  children: ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  const location = useLocation();
  const { user, logout } = useAuth();
  const navigationLinks = (user?.roles ?? []).includes("ADMIN")
    ? [...links, { to: "/admin", label: "Admin" as const }]
    : links;

  const [profilePhoto, setProfilePhoto] = useState<string | null>(null);
  const [isViewingPhoto, setIsViewingPhoto] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    const savedPhoto = localStorage.getItem(PROFILE_PHOTO_STORAGE_KEY);
    if (savedPhoto) {
      setProfilePhoto(savedPhoto);
    }
  }, []);

  const openFileDialog = () => {
    fileInputRef.current?.click();
  };

  const handlePhotoUpload = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file || !file.type.startsWith("image/")) {
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      localStorage.setItem(PROFILE_PHOTO_STORAGE_KEY, result);
      setProfilePhoto(result);
      setIsViewingPhoto(false);
    };
    reader.readAsDataURL(file);
    event.target.value = "";
  };

  const handleRemovePhoto = () => {
    localStorage.removeItem(PROFILE_PHOTO_STORAGE_KEY);
    setProfilePhoto(null);
    setIsViewingPhoto(false);
  };

  const handleProfileClick = () => {
    if (profilePhoto) {
      setIsViewingPhoto(true);
    } else {
      openFileDialog();
    }
  };

  const closePhotoViewer = () => {
    setIsViewingPhoto(false);
  };

  const pageInfo = useMemo(() => {
    const path = location.pathname;
    if (path === "/dashboard") {
      return {
        label: "Dashboard",
        heading: "Build sharper answers, stronger stories, and better hiring outcomes.",
        description:
          "See your latest resume insights, company matches, and discovery priorities in one clean view."
      };
    }

    if (path === "/upload") {
      return {
        label: "Resume Studio",
        heading: "Refine your resume with AI-aware feedback and tailored role positioning.",
        description:
          "Upload, update, and analyze your resume so every application is aligned to the roles you want."
      };
    }

    if (path === "/roles") {
      return {
        label: "Role Strategy",
        heading: "Choose target roles that match your skills, experience, and career goals.",
        description:
          "Explore the best role pathways and prepare the right stories for each hiring conversation."
      };
    }

    if (path === "/companies") {
      return {
        label: "Target Companies",
        heading: "Discover companies that are a strong fit for your profile.",
        description:
          "Browse employer matches, compare opportunities, and focus on the companies that matter most."
      };
    }

    if (path === "/companies/discover") {
      return {
        label: "Company Discovery",
        heading: "Find and enrich a new target employer profile.",
        description:
          "Search by company name, website, or domain to build the latest intelligence profile."
      };
    }

    if (path.startsWith("/companies/")) {
      return {
        label: "Company Details",
        heading: "Review the company, role fit, and what to highlight in your application.",
        description:
          "View the details for this company and tailor your strategy to the role and culture."
      };
    }

    if (path === "/interview") {
      return {
        label: "Mock Interview",
        heading: "Practice your answers, refine delivery, and build confidence for real interviews.",
        description:
          "Get instant interview feedback, review common questions, and strengthen your interview performance."
      };
    }

    if (path.startsWith("/interview/session")) {
      return {
        label: "Interview Session",
        heading: "Review your session performance and keep improving with every practice round.",
        description:
          "See your feedback, score trends, and the next actions that will boost your confidence."
      };
    }

    if (path === "/reports") {
      return {
        label: "Reports",
        heading: "Track your progress with clear reports and actionable interview insights.",
        description:
          "Review the latest feedback from your practice sessions and understand where to improve."
      };
    }

    if (path === "/recordings") {
      return {
        label: "Recordings",
        heading: "Review your recorded interview sessions and track your progress over time.",
        description:
          "Watch your past interviews to see how you've improved and identify areas for further growth."
      };
    }

    return {
      label: "Workspace",
      heading: "Your interview prep dashboard is ready.",
      description: "Choose a section to begin practicing, analyzing, or applying with confidence."
    };
  }, [location.pathname]);

  const initials = user?.fullName
    ? user.fullName
        .split(" ")
        .filter(Boolean)
        .map((part) => part[0])
        .slice(0, 2)
        .join("")
    : "IP";

  return (
    <div
      className="workspace-theme relative min-h-screen overflow-hidden"
      style={{
        backgroundImage:
          "radial-gradient(circle at 15% 18%, rgba(79, 140, 255, 0.22), transparent 28%), radial-gradient(circle at 82% 18%, rgba(139, 92, 246, 0.16), transparent 26%), linear-gradient(135deg, #060b16 0%, #0f172a 48%, #1e293b 100%)",
        backgroundAttachment: "fixed",
        backgroundRepeat: "no-repeat"
      }}
    >
      {/* Content container */}
      <div className="relative z-10 min-h-screen px-4 py-5 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl">
          <header className="glass-panel mb-6 overflow-hidden rounded-[2rem] p-4 sm:p-6 shadow-panel">
            <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
              <div className="space-y-4 max-w-3xl">
                <span className="inline-flex items-center rounded-full border border-slate-500/30 bg-slate-900/70 px-3 py-1 text-xs font-semibold uppercase tracking-[0.25em] text-slate-200">
                  {pageInfo.label}
                </span>
                <h1 className="font-display text-3xl text-white sm:text-4xl">{pageInfo.heading}</h1>
                <p className="max-w-2xl text-sm text-slate-300 sm:text-base">{pageInfo.description}</p>
              </div>

              <div className="rounded-[1.75rem] border border-slate-400/25 bg-slate-950/75 px-6 py-6 text-slate-100 shadow-[0_24px_80px_rgba(2,8,23,0.35)] ring-1 ring-white/10">
                <div className="grid gap-4 sm:grid-cols-[auto_1fr] sm:items-center">
                  <div className="flex items-center gap-4">
                    <div className="relative h-20 w-20 overflow-hidden rounded-3xl border border-gray-500/30 bg-white shadow-lg">
                      <div
                        role="button"
                        tabIndex={0}
                        onClick={handleProfileClick}
                        onKeyDown={(event) => {
                          if (event.key === "Enter" || event.key === " ") {
                            handleProfileClick();
                          }
                        }}
                        className="absolute inset-0 flex items-center justify-center bg-white/0 transition hover:bg-gray-100/50"
                        aria-label={profilePhoto ? "View profile photo" : "Upload profile photo"}
                      >
                        {profilePhoto ? (
                          <img
                            src={profilePhoto}
                            alt="Profile"
                            className="h-full w-full object-cover"
                          />
                        ) : (
                          <div className="flex h-full w-full items-center justify-center bg-slate-800/70 text-2xl font-semibold text-slate-100">
                            {initials}
                          </div>
                        )}
                      </div>
                      <button
                        type="button"
                        onClick={(event) => {
                          event.stopPropagation();
                          openFileDialog();
                        }}
                        className="absolute right-1 bottom-1 inline-flex h-6 w-6 items-center justify-center rounded-full bg-gradient-to-r from-[#8b6f47] to-[#6b5435] text-white shadow-sm transition hover:from-[#9d7d52] hover:to-[#7a5f3d]"
                        aria-label="Edit profile photo"
                      >
                        ✎
                      </button>
                      {profilePhoto ? (
                        <button
                          type="button"
                          onClick={(event) => {
                            event.stopPropagation();
                            handleRemovePhoto();
                          }}
                          className="absolute right-1 top-1 inline-flex h-5 w-5 items-center justify-center rounded-full bg-gradient-to-r from-[#8b6f47]/90 to-[#6b5435]/90 text-white text-[10px] transition hover:from-[#9d7d52] hover:to-[#7a5f3d]"
                          aria-label="Delete profile photo"
                        >
                          ×
                        </button>
                      ) : null}
                    </div>
                    <div className="min-w-0">
                      <p className="text-xs uppercase tracking-[0.3em] text-slate-400">Signed in</p>
                      <p className="mt-2 text-lg font-semibold text-white">{user?.fullName}</p>
                      <p className="truncate text-sm text-slate-300">{user?.email}</p>
                    </div>
                  </div>

                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handlePhotoUpload}
                  />
                </div>
                <button
                  type="button"
                  onClick={logout}
                  className="app-button-primary mt-5 inline-flex items-center justify-center rounded-full border border-transparent px-4 py-2 text-sm font-semibold transition focus:outline-none focus:ring-2 focus:ring-gray-600/30 active:border-gray-400"
                >
                  Sign out
                </button>
              </div>
            </div>
            <nav className="mt-6 flex flex-wrap gap-2">
              {navigationLinks.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `rounded-full px-4 py-2 text-sm font-medium transition ${
                      isActive
                        ? "bg-gradient-to-r from-[#4f8cff] to-[#8b5cf6] text-white font-semibold shadow-[0_10px_28px_rgba(79,140,255,0.28)] border border-transparent"
                        : "border border-slate-600/40 bg-slate-950/70 text-slate-200 hover:bg-slate-900/85 hover:border-slate-500/50"
                    }`
                  }
                >
                  {link.label}
                </NavLink>
              ))}
            </nav>
          </header>
          {isViewingPhoto && profilePhoto ? (
            <div
              className="fixed inset-0 z-50 flex items-center justify-center bg-[#948979]/90 p-4"
              onClick={closePhotoViewer}
            >
              <div
                className="relative max-h-full w-full max-w-3xl overflow-hidden rounded-[1.75rem] bg-white"
                onClick={(event) => event.stopPropagation()}
              >
                <button
                  type="button"
                  onClick={closePhotoViewer}
                  className="absolute right-4 top-4 inline-flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-r from-[#8b6f47]/80 to-[#6b5435]/80 text-white transition hover:from-[#9d7d52] hover:to-[#7a5f3d]"
                  aria-label="Close photo viewer"
                >
                  ×
                </button>
                <img src={profilePhoto} alt="Profile large view" className="h-auto max-h-[80vh] w-full object-contain" />
              </div>
            </div>
          ) : null}
          <main>{children}</main>
        </div>
      </div>
    </div>
  );
}

