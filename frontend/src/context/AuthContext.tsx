import {
  createContext,
  useEffect,
  useState,
  type PropsWithChildren
} from "react";
import { authApi } from "../api/client";
import type { UserProfile } from "../types";

interface AuthContextValue {
  user: UserProfile | null;
  loading: boolean;
  login: (payload: { email: string; password: string }) => Promise<void>;
  register: (payload: { fullName: string; email: string; password: string }) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const TOKEN_KEY = "interview-prep-token";

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      setLoading(false);
      return;
    }

    authApi
      .me()
      .then((profile) => setUser(profile))
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setLoading(false));
  }, []);

  const login = async (payload: { email: string; password: string }) => {
    const response = await authApi.login(payload);
    localStorage.setItem(TOKEN_KEY, response.token);
    setUser(response.user);
  };

  const register = async (payload: { fullName: string; email: string; password: string }) => {
    const response = await authApi.register(payload);
    // Don't auto-login — let user go through login page for confirmation flow
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
    return response;
  };

  const logout = () => {
    authApi
      .logout()
      .catch(() => undefined)
      .finally(() => {
        localStorage.removeItem(TOKEN_KEY);
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

