import axios from "axios";
import type {
  AdminDashboard,
  AudioProcessingResult,
  AnswerEvaluation,
  AuthResponse,
  Company,
  CompanyProfile,
  DashboardMetrics,
  InterviewSession,
  LiveCoaching,
  PracticeReport,
  RecommendationProfile,
  ResumeAnalysis,
  RoleProfile,
  TokenResponse,
  UserProfile
} from "../types";

const ACCESS_TOKEN_KEY = "interview-prep-token";
const REFRESH_TOKEN_KEY = "interview-prep-refresh-token";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api"
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authApi = {
  register: async (payload: { fullName: string; email: string; password: string }) => {
    const { data } = await api.post<AuthResponse>("/auth/register", {
      fullName: payload.fullName.trim(),
      email: payload.email.trim(),
      password: payload.password,
    });

    return data;
  },
  login: async (payload: { email: string; password: string }) => {
    const { data } = await api.post<AuthResponse>("/auth/login", payload);
    return data;
  },
  logout: async () => {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY) ?? "";
    await api.post("/auth/logout", { refresh_token: refreshToken });
  },
  refreshToken: async (refreshToken: string) => {
    const { data } = await api.post<TokenResponse>("/auth/refresh-token", {
      refresh_token: refreshToken,
    });
    return data;
  },
  me: async () => {
    const { data } = await api.get<UserProfile>("/auth/me");
    return data;
  }
};

export { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, api };

export const resumeApi = {
  upload: async (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    const { data } = await api.post<ResumeAnalysis>("/resumes", formData);
    return data;
  },
  latest: async () => {
    const { data } = await api.get<ResumeAnalysis>("/resumes/latest");
    return data;
  }
};

export const roleApi = {
  list: async () => {
    const { data } = await api.get<RoleProfile[]>("/roles");
    return data;
  }
};

export const recommendationApi = {
  profile: async () => {
    const { data } = await api.get<RecommendationProfile>("/recommendations/profile");
    return data;
  }
};

export const companyApi = {
  match: async (selectedRoles: string[]) => {
    const { data } = await api.post<Company[]>("/companies/match", { selectedRoles });
    return data;
  },
  search: async (payload: {
    selectedRoles: string[];
    query?: string;
    minMatchScore?: number;
    companySize?: string;
  }) => {
    const params = new URLSearchParams();
    payload.selectedRoles.forEach((role) => params.append("selectedRoles", role));
    if (payload.query?.trim()) {
      params.append("query", payload.query.trim());
    }
    if (typeof payload.minMatchScore === "number") {
      params.append("minMatchScore", String(payload.minMatchScore));
    }
    if (payload.companySize?.trim()) {
      params.append("companySize", payload.companySize.trim());
    }

    const { data } = await api.get<Company[]>(`/companies/search?${params.toString()}`);
    return data;
  },
  getById: async (companyId: string) => {
    const { data } = await api.get<CompanyProfile>(`/companies/${companyId}`);
    return data;
  },
  discover: async (payload: { name: string; website?: string; domain?: string; refresh?: boolean }) => {
    const { data } = await api.post<CompanyProfile>(`/companies/discover`, payload);
    return data;
  },
  refresh: async (companyId: string) => {
    const { data } = await api.post<CompanyProfile>(`/companies/${companyId}/refresh`);
    return data;
  }
};

export const interviewApi = {
  createSession: async (payload: {
    resumeId?: number;
    companyId?: string;
    selectedRoles: string[];
    personalityProfile: string;
    technicalFocus?: string;
    interviewerTone?: string;
    coachingIntensity?: string;
    liveCoachingEnabled?: boolean;
    adaptiveDifficultyEnabled?: boolean;
    realityMode?: string;
    cameraEnabled?: boolean;
  }) => {
    const { data } = await api.post<InterviewSession>("/interviews/sessions", payload);
    return data;
  },
  getSession: async (sessionId: string) => {
    const { data } = await api.get<InterviewSession>(`/interviews/sessions/${sessionId}`);
    return data;
  },
  submitAnswer: async (
    sessionId: string,
    questionId: string,
    payload: {
      answerText: string;
      audioReference?: string;
      visualPresenceSignal?: string;
      visualEyeContactSignal?: string;
      visualConfidenceSignal?: string;
      visualNervousnessSignal?: string;
    }
  ) => {
    const { data } = await api.post<AnswerEvaluation>(`/interviews/sessions/${sessionId}/questions/${questionId}/answer`, {
      questionId: String(questionId),
      ...payload,
    });
    return data;
  },
  coachAnswer: async (
    sessionId: string,
    questionId: string,
    payload: { answerDraft?: string; silenceDetected?: boolean }
  ) => {
    const { data } = await api.post<LiveCoaching>(`/interviews/questions/${questionId}/coach`, {
      answerDraft: payload.answerDraft ?? "",
      silenceDetected: payload.silenceDetected ?? false,
    });
    return data;
  },
  uploadAudio: async (payload: { file: Blob; fileName: string; transcriptHint?: string; durationMs?: number }) => {
    const formData = new FormData();
    formData.append("file", payload.file, payload.fileName);
    if (payload.transcriptHint?.trim()) {
      formData.append("transcriptHint", payload.transcriptHint.trim());
    }
    if (typeof payload.durationMs === "number") {
      formData.append("durationMs", String(payload.durationMs));
    }

    const { data } = await api.post<AudioProcessingResult>("/interviews/audio", formData);
    return data;
  }
};

export const reportApi = {
  dashboard: async () => {
    const { data } = await api.get<DashboardMetrics>("/reports/dashboard");
    return data;
  },
  list: async () => {
    const { data } = await api.get<PracticeReport[]>("/reports");
    return data;
  }
};

export const adminApi = {
  dashboard: async () => {
    const { data } = await api.get<AdminDashboard>("/admin/dashboard");
    return data;
  }
};

