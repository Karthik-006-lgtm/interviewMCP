import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/layout/AppShell";
import { ProtectedAdminRoute } from "./components/layout/ProtectedAdminRoute";
import { ProtectedRoute } from "./components/layout/ProtectedRoute";
import { AdminDashboardPage } from "./pages/admin/AdminDashboardPage";
import { CompanyDetailPage } from "./pages/company/CompanyDetailPage";
import { CompanyMatchesPage } from "./pages/company/CompanyMatchesPage";
import { LoginPage } from "./pages/auth/LoginPage";
import { RegisterPage } from "./pages/auth/RegisterPage";
import { ForgotPasswordPage } from "./pages/auth/ForgotPasswordPage";
import { DashboardPage } from "./pages/dashboard/DashboardPage";
import { ReportsPage } from "./pages/dashboard/ReportsPage";
import { InterviewPracticePage } from "./pages/interview/InterviewPracticePage";
import { InterviewSessionPage } from "./pages/interview/InterviewSessionPage";
import { RoleSelectionPage } from "./pages/roles/RoleSelectionPage";
import { ForbiddenPage } from "./pages/system/ForbiddenPage";
import { NotFoundPage } from "./pages/system/NotFoundPage";
import { ResumeUploadPage } from "./pages/upload/ResumeUploadPage";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/upload" element={<ResumeUploadPage />} />
        <Route path="/roles" element={<RoleSelectionPage />} />
        <Route path="/companies" element={<CompanyMatchesPage />} />
        <Route path="/companies/:companyId" element={<CompanyDetailPage />} />
        <Route path="/interview" element={<InterviewPracticePage />} />
        <Route path="/interview/session/:sessionId" element={<InterviewSessionPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route
          path="/admin"
          element={
            <ProtectedAdminRoute>
              <AdminDashboardPage />
            </ProtectedAdminRoute>
          }
        />
      </Route>
      <Route path="/forbidden" element={<ForbiddenPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

