export interface UserProfile {
  id: string;
  email: string;
  first_name: string;
  last_name: string;
  fullName: string;
  is_active: boolean;
  email_verified: boolean;
  created_at: string;
  createdAt: string;
  last_login?: string;
  roles?: string[];
}

export interface AuthResponse {
  user: UserProfile;
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

export interface ResumeAnalysis {
  id: number;
  originalFileName: string;
  candidateName: string;
  contactInfo: string[];
  summary: string;
  strengths: string[];
  weaknesses: string[];
  extractedSkills: string[];
  education: string[];
  experience: string[];
  projects: string[];
  certifications: string[];
  missingSkills: string[];
  strengthIndicators: string[];
  weaknessIndicators: string[];
  improvementRoadmap: string[];
  learningSuggestions: string[];
  mentorGuidance: string;
  recommendedRoles: string[];
  readinessScore: number;
  uploadedAt: string;
}

export interface Company {
  id: string;
  name: string;
  domain?: string;
  description?: string;
  website?: string;
  industry?: string;
  size?: string;
  foundedYear?: number;
  headquarters?: string;
  careersUrl?: string;
  revenueRange?: string;
  fundingStage?: string;
  hrContact?: string;
  hiringManager?: string;
  ownerName?: string;
  employeeCount?: number;
  companyHistory?: string;
  culture?: string;
  supportedRoles: string[];
  interviewFocusAreas: string[];
  whyUserMatches?: string;
  matchScore?: number;
}

export interface CompanyProfile extends Company {
  leadership?: Array<{ name: string; title?: string; url?: string }>;
  products?: string[];
  techStack?: string[];
  officialLinks?: Array<{ platform: string; url: string; isPrimary?: boolean }>;
  opportunities?: Array<{
    title: string;
    role: string;
    location?: string;
    employmentType?: string;
    experienceLevel?: string;
    description?: string;
    requirements?: string[];
    responsibilities?: string[];
    skillsRequired?: string[];
    isActive?: boolean;
    applicationDeadline?: string;
    postedDate?: string;
    source?: string;
    sourceUrl?: string;
    timing?: string;
  }>;
  intelligenceSources?: string[];
  lastEnrichedAt?: string;
}

export interface RoleProfile {
  id: number;
  name: string;
  summary: string;
  coreSkills: string[];
  interviewFocusAreas: string[];
}

export interface RecommendationProfile {
  resumeId: number;
  strengths: string[];
  weaknesses: string[];
  recommendedRoles: string[];
  missingSkills: string[];
  improvementRoadmap: string[];
  learningSuggestions: string[];
  mentorGuidance: string;
  matchingCompanyCount: number;
}

export interface InterviewQuestion {
  id: string;
  prompt: string;
  category: string;
  difficulty: string;
  interviewerCue: string;
  timePressureSeconds: number;
}

export interface InterviewSession {
  sessionId: string;
  selectedRoles: string[];
  personalityProfile: string;
  technicalSkills: string;
  targetCompanyName: string | null;
  targetCompanyWebsite: string | null;
  interviewerTone: string;
  coachingIntensity: string;
  liveCoachingEnabled: boolean;
  adaptiveDifficultyEnabled: boolean;
  realityMode: string;
  cameraEnabled: boolean;
  currentDifficultyLevel: string;
  questions: InterviewQuestion[];
  overallScore: number | null;
  createdAt: string;
}

export interface AnswerEvaluation {
  answerId: string;
  correctnessScore: number;
  confidenceScore: number;
  relevanceScore: number;
  clarityScore: number;
  completenessScore: number;
  structureScore: number;
  impactScore: number;
  hesitationScore: number;
  fillerWordCount: number;
  emotionSignal: string;
  grammarFeedback: string;
  vocabularyFeedback: string;
  mentorSuggestions: string;
  polishedAnswer: string;
  pronunciationFeedback: string;
  toneFeedback: string;
  fluencyFeedback: string;
  liveCoachingHints: string[];
  weaknessSignals: string[];
  weeklyImprovementPlan: string[];
  practiceTasks: string[];
  targetedQuestions: string[];
  adaptiveDifficultyNote: string;
  nextDifficultyLevel: string;
  createdAt: string;
}

export interface LiveCoaching {
  hints: string[];
  suggestedKeywords: string[];
  continuationPrompt: string;
  structureReminder: string;
}

export interface AudioProcessingResult {
  audioReference: string;
  transcript: string;
  confidenceScore: number;
  fluencyScore: number;
  clarityScore: number;
  emotionSignal: string;
  toneFeedback: string;
  pronunciationFeedback: string;
  fluencyFeedback: string;
  durationMs: number | null;
  createdAt: string;
}

export interface PracticeReport {
  id: number;
  sessionId: number;
  title: string;
  executiveSummary: string;
  weakAreas: string[];
  recommendedActions: string[];
  improvementAreas: string;
  nextSteps: string;
  progressSummary: string;
  weeklyImprovementPlan: string[];
  practiceTasks: string[];
  targetedQuestions: string[];
  overallScore: number;
  createdAt: string;
}

export interface DashboardMetrics {
  resumeCount: number;
  companyMatchCount: number;
  completedAnswers: number;
  latestScore: number;
  averageScore: number;
  progressDelta: number;
  topWeakAreas: string[];
  recentReports: PracticeReport[];
}

export interface AdminUserSummary {
  id: number;
  fullName: string;
  email: string;
  roles: string[];
  createdAt: string;
}

export interface AdminResumeSummary {
  id: number;
  candidateName: string | null;
  userEmail: string;
  originalFileName: string;
  recommendedRoles: string[];
  uploadedAt: string;
}

export interface AdminInterviewSummary {
  sessionId: number;
  userEmail: string;
  selectedRoles: string[];
  overallScore: number | null;
  createdAt: string;
}

export interface AdminDashboard {
  totalUsers: number;
  totalResumes: number;
  totalCompanies: number;
  totalInterviews: number;
  totalReports: number;
  totalRoles: number;
  revokedTokenCount: number;
  recentUsers: AdminUserSummary[];
  recentResumes: AdminResumeSummary[];
  recentInterviews: AdminInterviewSummary[];
  availableMcpTools: string[];
}

