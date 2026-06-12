import {
  createContext,
  useEffect,
  useState,
  type PropsWithChildren
} from "react";
import { authApi, ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY } from "../api/client";
import type { UserProfile } from "../types";

interface AuthContextValue {
  user: UserProfile | null;
  loading: boolean;
  login: (payload: { email: string; password: string }) => Promise<void>;
  register: (payload: { fullName: string; email: string; password: string }) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function normalizeUser(profile: UserProfile): UserProfile {
  return {
    ...profile,
    fullName: profile.fullName || `${profile.first_name ?? ""} ${profile.last_name ?? ""}`.trim(),
    createdAt: profile.createdAt || profile.created_at || new Date().toISOString(),
    roles: profile.roles ?? [],
  };
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (!token) {
      setLoading(false);
      return;
    }

    authApi
      .me()
      .then((profile) => setUser(normalizeUser(profile)))
      .catch(() => {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (payload: { email: string; password: string }) => {
    const response = await authApi.login(payload);
    localStorage.setItem(ACCESS_TOKEN_KEY, response.access_token);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refresh_token);
    setUser(normalizeUser(response.user));
  };

  const register = async (payload: { fullName: string; email: string; password: string }) => {
    await authApi.register(payload);
    // Don't auto-login — let user go through login page for confirmation flow
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    setUser(null);
  };

  const logout = () => {
    authApi
      .logout()
      .catch(() => undefined)
      .finally(() => {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        setUser(null);
      });
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

