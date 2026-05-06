# HireSense AI - Phase 1: System Architecture

## Executive Summary

HireSense AI is a production-grade enterprise SaaS platform for:
- AI-powered interview preparation
- Company intelligence and hiring insights
- Personalized mock interview platform
- Real-time voice interviews
- Resume intelligence and skill analysis

## 1. HIGH-LEVEL SYSTEM ARCHITECTURE

### Core Components

```
┌─────────────────────────────────────────────────────────────┐
│                     USER LAYER                              │
├─────────────────────────────────────────────────────────────┤
│  Web Application (Next.js 15 + TypeScript)                 │
│  Mobile App (React Native - Future)                        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Rate Limiting | Auth | Load Balancing | CORS              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  MICROSERVICES LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │  Auth Service    │  │  User Service    │               │
│  │  (FastAPI)       │  │  (FastAPI)       │               │
│  └──────────────────┘  └──────────────────┘               │
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │ Interview Engine │  │  Company Intel   │               │
│  │  (FastAPI)       │  │  (FastAPI)       │               │
│  └──────────────────┘  └──────────────────┘               │
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │  Resume Engine   │  │  Voice Service   │               │
│  │  (FastAPI)       │  │  (FastAPI)       │               │
│  └──────────────────┘  └──────────────────┘               │
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │  Analytics Svc   │  │  Opportunity Eng │               │
│  │  (FastAPI)       │  │  (FastAPI)       │               │
│  └──────────────────┘  └──────────────────┘               │
└─────────────────────────────────────────────────────────────┘
         ↓              ↓              ↓              ↓
┌─────────────────────────────────────────────────────────────┐
│                   DATA LAYER                                │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │  PostgreSQL      │  │  Redis Cache     │               │
│  │  (Primary DB)    │  │  (Session/Queue) │               │
│  └──────────────────┘  └──────────────────┘               │
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │  Pinecone/       │  │  AWS S3          │               │
│  │  Weaviate (VDB)  │  │  (Storage)       │               │
│  └──────────────────┘  └──────────────────┘               │
└─────────────────────────────────────────────────────────────┘
         ↓              ↓              ↓
┌─────────────────────────────────────────────────────────────┐
│                  EXTERNAL SERVICES                          │
├─────────────────────────────────────────────────────────────┤
│  OAuth Providers | LLM APIs | Web Scraper | Speech APIs    │
└─────────────────────────────────────────────────────────────┘
```

## 2. TECHNOLOGY STACK

### Frontend
- **Framework**: Next.js 15 with TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: ShadCN UI
- **State Management**: TanStack Query (React Query) + Zustand
- **Forms**: React Hook Form + Zod
- **Real-time**: Socket.io
- **Testing**: Jest + React Testing Library
- **Build**: Turbopack

### Backend / AI Services
- **Framework**: FastAPI (Python)
- **API Server**: Uvicorn + Gunicorn
- **Database ORM**: SQLAlchemy
- **Async**: asyncio + aiohttp
- **Task Queue**: Celery + Redis
- **Testing**: pytest + pytest-asyncio

### Data & Storage
- **Primary Database**: PostgreSQL 15+
- **Cache**: Redis 7+
- **Vector Database**: Pinecone / Weaviate
- **Object Storage**: AWS S3
- **Full-text Search**: Elasticsearch

### AI & ML
- **LLM**: OpenAI GPT-4 / Claude 3
- **Embeddings**: OpenAI / Sentence Transformers
- **RAG Framework**: LangChain / LlamaIndex
- **Speech-to-Text**: Deepgram / OpenAI Whisper
- **Text-to-Speech**: ElevenLabs / AWS Polly

### Infrastructure & DevOps
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Service Mesh**: Istio (optional)
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack / Datadog
- **CI/CD**: GitHub Actions
- **IaC**: Terraform / CloudFormation
- **CDN**: CloudFront

## 3. SERVICE COMMUNICATION DIAGRAM

### Internal Service Communication

```
Frontend (Next.js)
    ↓↓↓↓↓↓↓
API Gateway (Kong / Nginx)
    ↓
┌─────────────────────────────────────────┐
│                                         │
├─ Auth Service (JWT Validation)          │
│  ├→ User Service                        │
│  ├→ Company Intelligence Service        │
│  ├→ Interview Engine                    │
│  ├→ Resume Engine                       │
│  ├→ Voice Service                       │
│  ├→ Analytics Service                   │
│  └→ Opportunity Engine                  │
│                                         │
├─ MCP Tools (Model Context Protocol)    │
│  ├→ Company Intelligence MCP Tool       │
│  ├→ Resume Intelligence MCP Tool        │
│  ├→ Interview Generation MCP Tool       │
│  ├→ Voice Intelligence MCP Tool         │
│  └→ Opportunity Tracking MCP Tool       │
│                                         │
├─ Task Queue (Celery)                    │
│  ├→ Resume Processing                   │
│  ├→ Company Data Crawling                │
│  ├→ Vector Embedding                    │
│  ├→ PDF Generation                      │
│  └→ Email Notifications                 │
│                                         │
└─ Async Jobs (Background Processing)     │
```

### External Service Integration

```
OpenAI API (GPT-4, Embeddings)
      ↑
      │
Interview Engine & RAG
      ↑
      │
External Data Sources:
├→ LinkedIn API
├→ GitHub API
├→ Web Scraper (Puppeteer)
├→ Company Websites
├→ Engineering Blogs
├→ News APIs
├→ Job Portals (LinkedIn, Indeed)
└→ Glassdoor Data
```

## 4. MCP TOOL ARCHITECTURE

### Tool 1: Company Intelligence MCP Tool

**Functions**:
- `fetch_company_data(company_name: str)` → CompanyProfile
- `get_company_tech_stack(company_id: str)` → TechStack
- `fetch_current_jobs(company_id: str)` → List[JobPosting]
- `get_company_hiring_trends(company_id: str, months: int)` → HiringTrends
- `fetch_social_links(company_id: str)` → SocialLinks
- `discover_opportunities(company_id: str, filters: Dict)` → List[Opportunity]
- `crawl_company_website(url: str)` → WebsiteData

**Input Validation**:
- Company name: non-empty string, max 255 chars
- Company ID: valid UUID format
- URL: valid HTTP/HTTPS URL

**Output Schema**:
```json
{
  "company_id": "uuid",
  "name": "string",
  "logo": "url",
  "hq": "string",
  "industry": "string",
  "employees": "number",
  "revenue": "string",
  "founded_year": "number",
  "tech_stack": ["string"],
  "current_jobs": "number",
  "social_links": {
    "website": "url",
    "linkedin": "url",
    "github": "url",
    "twitter": "url"
  },
  "hiring_intelligence": {
    "avg_rounds": "number",
    "common_skills": ["string"],
    "recent_hires": "number"
  }
}
```

### Tool 2: Resume Intelligence MCP Tool

**Functions**:
- `parse_resume(file_content: bytes, format: str)` → ParsedResume
- `extract_skills(parsed_resume: ParsedResume)` → List[Skill]
- `identify_skill_gaps(skills: List[Skill], target_role: str)` → List[SkillGap]
- `generate_skill_graph(skills: List[Skill])` → SkillGraph
- `extract_experience(parsed_resume: ParsedResume)` → List[Experience]

### Tool 3: Interview Generation MCP Tool

**Functions**:
- `generate_questions(context: InterviewContext)` → List[Question]
- `generate_followup(question: Question, answer: str)` → Question
- `score_technical_answer(question: Question, answer: str)` → TechnicalScore
- `score_behavioral_answer(question: Question, answer: str)` → BehavioralScore

### Tool 4: Voice Intelligence MCP Tool

**Functions**:
- `speech_to_text(audio_stream: bytes)` → TranscriptionResult
- `text_to_speech(text: str, voice: str)` → AudioStream
- `analyze_fluency(audio_stream: bytes)` → FluencyMetrics
- `analyze_confidence(audio_stream: bytes, transcript: str)` → ConfidenceMetrics

### Tool 5: Opportunity Tracking MCP Tool

**Functions**:
- `discover_internships(filters: Dict)` → List[Opportunity]
- `discover_hackathons(filters: Dict)` → List[Opportunity]
- `discover_events(filters: Dict)` → List[Opportunity]
- `track_opportunity(user_id: str, opportunity_id: str)` → Tracking
- `update_opportunity_status(opportunity_id: str, status: str)` → UpdateResult

## 5. RAG PIPELINE ARCHITECTURE

### Data Collection & Ingestion

```
┌─────────────────────────────────────────────────────────────┐
│                    DATA SOURCES                             │
├─────────────────────────────────────────────────────────────┤
│  • Company websites & careers pages                         │
│  • LinkedIn profiles & company pages                        │
│  • GitHub repositories & organizations                      │
│  • Engineering blogs & technical articles                   │
│  • News articles (TechCrunch, ArsTechnica, etc.)           │
│  • Glassdoor reviews & salary data                          │
│  • Job postings (LinkedIn, Indeed, etc.)                    │
│  • Interview experiences (LeetCode, Blind, etc.)           │
│  • Developer communities (Reddit, HackerNews)              │
│  • Conference talks & research papers                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                      CRAWLING                               │
├─────────────────────────────────────────────────────────────┤
│  Puppeteer / Scrapy for web scraping                        │
│  API integration (LinkedIn, GitHub, News APIs)             │
│  Scheduled daily/weekly crawls                             │
│  Rate limiting & robots.txt compliance                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                    EXTRACTION                               │
├─────────────────────────────────────────────────────────────┤
│  HTML parsing (BeautifulSoup, lxml)                         │
│  Structured data extraction                                 │
│  Metadata extraction                                        │
│  Link graph construction                                    │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                   CLEANING & FILTERING                      │
├─────────────────────────────────────────────────────────────┤
│  Remove boilerplate (ads, nav, footer)                      │
│  Deduplication                                              │
│  Language detection & filtering                            │
│  Quality scoring                                            │
│  PII removal                                                │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                      CHUNKING                               │
├─────────────────────────────────────────────────────────────┤
│  Semantic chunking (max 1024 tokens)                        │
│  Preserve context & metadata                               │
│  Sliding window chunks (overlap: 10%)                       │
│  Chunk quality scoring                                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                    EMBEDDING                                │
├─────────────────────────────────────────────────────────────┤
│  Model: OpenAI text-embedding-3-large                       │
│  Batch processing for efficiency                            │
│  Vector dimension: 3072                                     │
│  Normalization & indexing                                   │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                      STORAGE                                │
├─────────────────────────────────────────────────────────────┤
│  Vector DB: Pinecone (namespaced by company/topic)          │
│  Metadata: PostgreSQL (chunks, sources, dates)              │
│  Raw content: S3 (for re-indexing)                          │
│  Cache: Redis (recent queries)                              │
└─────────────────────────────────────────────────────────────┘
```

### Retrieval & Generation Pipeline

```
User Query (Interview Prep Question)
          ↓
┌─────────────────────────────────────────────────────────────┐
│              QUERY UNDERSTANDING                            │
├─────────────────────────────────────────────────────────────┤
│  Extract: company, role, skill, question_type              │
│  Expand query with synonyms & related terms                 │
│  Add user context (experience, weak areas)                  │
│  Determine retrieval strategy                               │
└─────────────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────────┐
│            HYBRID RETRIEVAL                                 │
├─────────────────────────────────────────────────────────────┤
│  1. Vector Search (Semantic similarity)                     │
│     - Query embedding via OpenAI                            │
│     - Pinecone vector search                                │
│     - Top 50 candidates                                     │
│                                                             │
│  2. BM25 Search (Keyword matching)                          │
│     - Elasticsearch keyword search                          │
│     - Top 50 candidates                                     │
│                                                             │
│  3. Metadata Filter                                         │
│     - Filter by company, role, skill                        │
│     - Filter by recency & quality score                     │
│                                                             │
│  4. Fusion & Re-ranking                                     │
│     - Reciprocal rank fusion (RRF)                          │
│     - Cross-encoder re-ranking                              │
│     - Contextual relevance scoring                          │
│     - Final: Top 10 chunks                                  │
└─────────────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────────┐
│          CONTEXT AUGMENTATION                               │
├─────────────────────────────────────────────────────────────┤
│  Add user's resume context                                  │
│  Add user's skill gaps                                      │
│  Add company hiring patterns                                │
│  Add recent industry trends                                 │
│  Add similar user performance                               │
│  Build comprehensive system prompt                          │
└─────────────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────────┐
│         GENERATION (LLM)                                    │
├─────────────────────────────────────────────────────────────┤
│  Model: GPT-4 Turbo / Claude 3 Opus                         │
│  Temperature: 0.7 (balanced creativity)                     │
│  Max tokens: 2000                                           │
│  Constraints:                                               │
│    - Must cite sources                                      │
│    - Must be technically accurate                           │
│    - Must be company-specific                               │
│    - Must match user's experience level                     │
└─────────────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────────┐
│        OUTPUT & VALIDATION                                  │
├─────────────────────────────────────────────────────────────┤
│  Generated Question                                         │
│  Difficulty Score (1-10)                                    │
│  Related Skills                                             │
│  Expected Time (minutes)                                    │
│  Source References                                          │
│  Follow-up Topics                                           │
└─────────────────────────────────────────────────────────────┘
```

### Update Strategy

```
Daily Updates:
├─ Crawl company websites & careers pages (5:00 AM UTC)
├─ Fetch LinkedIn job postings (6:00 AM UTC)
├─ Fetch GitHub org updates (7:00 AM UTC)
├─ Fetch news articles (8:00 AM UTC)
├─ Process & embed new content (9:00 AM - 12:00 PM UTC)

Weekly Updates:
├─ Deep crawl engineering blogs
├─ Re-index Glassdoor data
├─ Update hiring trends analysis
├─ Cleanup & deduplication
└─ Vector DB optimization

Monthly Updates:
├─ Full recrawl of core sources
├─ Model updates (embeddings, re-ranker)
├─ Analytics & quality assessment
└─ Archive old/stale data

Triggers:
├─ Company raises funding → update immediately
├─ Company posts new blog → index within 1 hour
├─ Major news event → manual trigger
└─ User feedback → re-index & improve
```

## 6. DATABASE ER DIAGRAM

### Core Entities

```
USERS
├─ id (UUID, PK)
├─ email (UNIQUE, NOT NULL)
├─ password_hash (NOT NULL)
├─ first_name
├─ last_name
├─ profile_picture_url
├─ created_at
├─ updated_at
├─ last_login
├─ is_active
├─ email_verified
└─ phone_verified

PROFILES
├─ id (UUID, PK)
├─ user_id (FK → USERS)
├─ experience_level (ENUM: fresher, junior, mid, senior)
├─ bio
├─ headline
├─ target_companies (ARRAY)
├─ target_roles (ARRAY)
├─ preferred_domains (ARRAY)
├─ target_salary_min
├─ target_salary_max
├─ interview_timeline (ENUM: immediate, 1month, 3months, 6months)
└─ updated_at

RESUMES
├─ id (UUID, PK)
├─ user_id (FK → USERS)
├─ filename
├─ file_url (S3)
├─ parsed_content (JSONB)
├─ extracted_skills (ARRAY)
├─ extracted_education (JSONB)
├─ extracted_experience (JSONB)
├─ extracted_certifications (ARRAY)
├─ uploaded_at
├─ last_parsed_at
└─ version

SKILLS
├─ id (UUID, PK)
├─ user_id (FK → USERS)
├─ skill_name
├─ proficiency_level (ENUM: beginner, intermediate, advanced, expert)
├─ years_of_experience
├─ is_primary
├─ skill_category (ENUM: technical, soft, domain, language)
├─ endorsed_count
├─ verified
└─ created_at

SKILL_GAPS
├─ id (UUID, PK)
├─ user_id (FK → USERS)
├─ skill_name
├─ target_role_id (FK → ROLES)
├─ gap_level (1-10)
├─ estimated_learning_days
├─ learning_resources (JSONB)
└─ identified_at

COMPANIES
├─ id (UUID, PK)
├─ name (UNIQUE, NOT NULL)
├─ logo_url
├─ hq_location
├─ industry
├─ employee_count
├─ revenue_usd
├─ founded_year
├─ website
├─ careers_page_url
├─ tech_stack (ARRAY)
├─ products (ARRAY)
├─ engineering_blogs (ARRAY)
├─ github_org
├─ data_updated_at
└─ hiring_is_active

COMPANY_SOCIAL_LINKS
├─ id (UUID, PK)
├─ company_id (FK → COMPANIES)
├─ platform (ENUM: linkedin, github, twitter, instagram, facebook, youtube)
├─ url
├─ followers_count
└─ updated_at

ROLES
├─ id (UUID, PK)
├─ company_id (FK → COMPANIES)
├─ role_title
├─ department
├─ seniority_level
├─ required_skills (ARRAY)
├─ nice_to_have_skills (ARRAY)
├─ avg_interview_rounds
├─ interview_types (ARRAY)
├─ salary_range_min
├─ salary_range_max
├─ posted_date
└─ updated_at

OPPORTUNITIES
├─ id (UUID, PK)
├─ company_id (FK → COMPANIES)
├─ title
├─ opportunity_type (ENUM: internship, job, hackathon, workshop, challenge, event, campus_drive)
├─ description
├─ start_date
├─ end_date
├─ registration_url
├─ status (ENUM: upcoming, ongoing, completed)
├─ target_skills (ARRAY)
├─ location
├─ is_remote
├─ eligibility_criteria (JSONB)
├─ discovered_at
└─ last_verified_at

INTERVIEW_SESSIONS
├─ id (UUID, PK)
├─ user_id (FK → USERS)
├─ company_id (FK → COMPANIES)
├─ role_id (FK → ROLES)
├─ session_type (ENUM: technical, behavioral, voice, coding, system_design)
├─ interview_mode (ENUM: guided, freestyle)
├─ started_at
├─ ended_at
├─ duration_minutes
├─ status (ENUM: in_progress, completed, abandoned)
├─ overall_score (1-100)
├─ technical_score (1-100)
├─ communication_score (1-100)
├─ confidence_score (1-100)
├─ notes (TEXT)
└─ recording_url (S3)

GENERATED_QUESTIONS
├─ id (UUID, PK)
├─ session_id (FK → INTERVIEW_SESSIONS)
├─ question_number
├─ question_text
├─ question_type (ENUM: technical, coding, dsa, system_design, behavioral, hr, company_specific)
├─ difficulty_level (1-10)
├─ difficulty_category (ENUM: easy, medium, hard)
├─ expected_duration_minutes
├─ related_skills (ARRAY)
├─ source_references (ARRAY)
├─ generated_at
└─ follow_up_topics (ARRAY)

ANSWERS
├─ id (UUID, PK)
├─ question_id (FK → GENERATED_QUESTIONS)
├─ user_id (FK → USERS)
├─ answer_text
├─ answer_audio_url (S3)
├─ answer_video_url (S3)
├─ answer_code (TEXT)
├─ transcription (TEXT)
├─ submitted_at
├─ duration_minutes
├─ technical_score (1-100)
├─ communication_score (1-100)
├─ confidence_score (1-100)
├─ follow_up_question_id (FK → GENERATED_QUESTIONS)
└─ feedback (TEXT)

ANALYTICS
├─ id (UUID, PK)
├─ user_id (FK → USERS)
├─ metric_date
├─ total_interviews
├─ avg_technical_score
├─ avg_communication_score
├─ avg_confidence_score
├─ top_strengths (ARRAY)
├─ top_weaknesses (ARRAY)
├─ company_readiness_scores (JSONB)
├─ role_readiness_scores (JSONB)
├─ skill_progression (JSONB)
└─ confidence_trend (ARRAY)

AUDIT_LOGS
├─ id (UUID, PK)
├─ user_id (FK → USERS)
├─ action (VARCHAR)
├─ entity_type (VARCHAR)
├─ entity_id (UUID)
├─ changes (JSONB)
├─ ip_address
├─ user_agent
└─ created_at
```

## 7. API ARCHITECTURE

### Microservice Endpoints

#### Auth Service
```
POST   /api/v1/auth/signup
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh-token
POST   /api/v1/auth/verify-email
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
POST   /api/v1/auth/oauth/google/callback
POST   /api/v1/auth/oauth/github/callback
GET    /api/v1/auth/me
POST   /api/v1/auth/device-register
GET    /api/v1/auth/devices
DELETE /api/v1/auth/devices/{device_id}
```

#### User Service
```
GET    /api/v1/users/{user_id}
PUT    /api/v1/users/{user_id}
DELETE /api/v1/users/{user_id}
GET    /api/v1/profiles/{user_id}
PUT    /api/v1/profiles/{user_id}
GET    /api/v1/resumes/{user_id}
POST   /api/v1/resumes/upload
GET    /api/v1/resumes/{resume_id}
DELETE /api/v1/resumes/{resume_id}
POST   /api/v1/resumes/{resume_id}/parse
GET    /api/v1/skills/{user_id}
POST   /api/v1/skills
PUT    /api/v1/skills/{skill_id}
DELETE /api/v1/skills/{skill_id}
GET    /api/v1/skill-gaps/{user_id}
```

#### Company Intelligence Service
```
GET    /api/v1/companies/search
GET    /api/v1/companies/{company_id}
GET    /api/v1/companies/{company_id}/profile
GET    /api/v1/companies/{company_id}/tech-stack
GET    /api/v1/companies/{company_id}/social-links
GET    /api/v1/companies/{company_id}/hiring-intelligence
GET    /api/v1/companies/{company_id}/roles
POST   /api/v1/companies/{company_id}/match-jobs
GET    /api/v1/opportunities
POST   /api/v1/opportunities/track
GET    /api/v1/opportunities/{opportunity_id}
```

#### Interview Engine Service
```
POST   /api/v1/interviews/start
GET    /api/v1/interviews/{session_id}
POST   /api/v1/interviews/{session_id}/next-question
POST   /api/v1/interviews/{session_id}/submit-answer
POST   /api/v1/interviews/{session_id}/end
GET    /api/v1/interviews/{session_id}/results
GET    /api/v1/interviews/{user_id}/history
POST   /api/v1/interviews/{session_id}/export-pdf
```

#### Voice Service
```
POST   /api/v1/voice/start-session
POST   /api/v1/voice/{session_id}/process-audio
GET    /api/v1/voice/{session_id}/transcript
POST   /api/v1/voice/{session_id}/end
GET    /api/v1/voice/{session_id}/analysis
```

#### Analytics Service
```
GET    /api/v1/analytics/{user_id}/dashboard
GET    /api/v1/analytics/{user_id}/skill-progress
GET    /api/v1/analytics/{user_id}/weakness-analysis
GET    /api/v1/analytics/{user_id}/company-readiness
GET    /api/v1/analytics/{user_id}/role-readiness
GET    /api/v1/analytics/{user_id}/interview-history
```

### API Response Format

```json
{
  "success": true,
  "status_code": 200,
  "message": "Operation successful",
  "data": { /* resource data */ },
  "meta": {
    "timestamp": "2024-05-03T10:30:00Z",
    "request_id": "uuid",
    "version": "v1"
  }
}
```

### Error Response Format

```json
{
  "success": false,
  "status_code": 400,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Input validation failed",
    "details": [
      {
        "field": "email",
        "message": "Invalid email format"
      }
    ]
  },
  "meta": {
    "timestamp": "2024-05-03T10:30:00Z",
    "request_id": "uuid"
  }
}
```

## 8. AUTHENTICATION FLOW

### JWT Token Strategy

```
Flow: User Login
     ↓
Credentials Validation
     ↓
Generate JWT Tokens:
├─ Access Token (15 minutes)
├─ Refresh Token (7 days) [stored in Redis]
└─ ID Token (for frontend)
     ↓
Return to Client:
├─ access_token
├─ refresh_token
├─ expires_in
└─ user info
     ↓
Client Storage:
├─ access_token → Memory (secure)
├─ refresh_token → Secure HttpOnly Cookie
└─ user info → localStorage
     ↓
Every API Call:
├─ Include Authorization: Bearer {access_token}
└─ If 401: Use refresh_token to get new access_token
```

### OAuth Flow (Google/GitHub)

```
1. User clicks "Login with Google/GitHub"
     ↓
2. Frontend redirects to auth provider
     ↓
3. User grants permissions
     ↓
4. Auth provider redirects with auth code
     ↓
5. Backend exchanges code for access token
     ↓
6. Backend fetches user profile from provider
     ↓
7. Check/Create user in DB
     ↓
8. Generate JWT tokens
     ↓
9. Redirect to frontend with tokens
```

## 9. DEPLOYMENT ARCHITECTURE

### Kubernetes Cluster Architecture

```
┌─────────────────────────────────────────────────────────┐
│              KUBERNETES CLUSTER                         │
├─────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────┐ │
│  │         INGRESS / API GATEWAY                      │ │
│  │  (Nginx Ingress / Kong)                            │ │
│  │  - Rate limiting                                   │ │
│  │  - SSL/TLS termination                             │ │
│  │  - Request routing                                 │ │
│  └────────────────────────────────────────────────────┘ │
│                          ↓                              │
│  ┌────────────────────────────────────────────────────┐ │
│  │       NAMESPACE: PRODUCTION                        │ │
│  ├────────────────────────────────────────────────────┤ │
│  │  Deployments (with 3+ replicas):                   │ │
│  │  ├─ auth-service                                   │ │
│  │  ├─ user-service                                   │ │
│  │  ├─ interview-engine                               │ │
│  │  ├─ company-intelligence                           │ │
│  │  ├─ resume-engine                                  │ │
│  │  ├─ voice-service                                  │ │
│  │  ├─ analytics-service                              │ │
│  │  ├─ opportunity-engine                             │ │
│  │  ├─ frontend                                       │ │
│  │  └─ rag-engine                                     │ │
│  │                                                    │ │
│  │  StatefulSets:                                     │ │
│  │  ├─ PostgreSQL (primary + replicas)                │ │
│  │  └─ Redis (primary + replicas)                     │ │
│  │                                                    │ │
│  │  DaemonSets:                                       │ │
│  │  ├─ Prometheus node exporter                       │ │
│  │  └─ Filebeat                                       │ │
│  │                                                    │ │
│  │  Jobs & CronJobs:                                  │ │
│  │  ├─ Daily data crawling                            │ │
│  │  ├─ Weekly re-indexing                             │ │
│  │  ├─ Backup jobs                                    │ │
│  │  └─ Cleanup jobs                                   │ │
│  └────────────────────────────────────────────────────┘ │
│                          ↓                              │
│  ┌────────────────────────────────────────────────────┐ │
│  │      SERVICES & NETWORKING                        │ │
│  │  - ClusterIP services (internal)                   │ │
│  │  - Headless services (stateful)                    │ │
│  │  - Network policies (micro-segmentation)          │ │
│  │  - Service mesh (Istio) - optional                │ │
│  └────────────────────────────────────────────────────┘ │
│                          ↓                              │
│  ┌────────────────────────────────────────────────────┐ │
│  │       STORAGE & VOLUMES                            │ │
│  │  - PersistentVolumes (EBS for databases)           │ │
│  │  - ConfigMaps (configuration)                      │ │
│  │  - Secrets (credentials, keys)                     │ │
│  │  - EmptyDir (temp storage)                         │ │
│  └────────────────────────────────────────────────────┘ │
│                          ↓                              │
│  ┌────────────────────────────────────────────────────┐ │
│  │      MONITORING & LOGGING                          │ │
│  │  - Prometheus scraping                             │ │
│  │  - Grafana dashboards                              │ │
│  │  - ELK stack logging                               │ │
│  │  - Alerts & notifications                          │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Infrastructure as Code (Terraform)

```hcl
Project Structure:
├─ vpc.tf (networking)
├─ eks.tf (kubernetes cluster)
├─ rds.tf (PostgreSQL)
├─ elasticache.tf (Redis)
├─ s3.tf (object storage)
├─ alb.tf (load balancer)
├─ iam.tf (roles & permissions)
├─ monitoring.tf (CloudWatch, Prometheus)
└─ variables.tf (environment config)
```

### CI/CD Pipeline (GitHub Actions)

```yaml
On: push to main/develop

Jobs:
├─ Lint & Format Check
├─ Security Scanning
│  ├─ SAST (SonarQube)
│  ├─ Dependency scanning (Snyk)
│  └─ Container scanning (Trivy)
├─ Unit Tests (90% coverage minimum)
├─ Integration Tests
├─ Build Docker images
├─ Push to registry (ECR)
├─ Deploy to staging (EKS)
├─ Smoke tests
├─ Load tests
├─ Deploy to production (canary/blue-green)
└─ Notify status
```

## 10. SECURITY ARCHITECTURE

### Authentication & Authorization

```
├─ JWT-based authentication
├─ OAuth2 integration (Google, GitHub)
├─ Rate limiting (10req/sec per user)
├─ CORS policy enforcement
├─ API key rotation (90 days)
├─ MFA support (optional)
├─ Session timeout (15 minutes)
└─ Audit logging (all access)
```

### Data Protection

```
├─ Encryption at rest (AES-256)
├─ Encryption in transit (TLS 1.3)
├─ PII data masking
├─ Password hashing (bcrypt)
├─ Secret management (AWS Secrets Manager)
├─ API request signing
└─ Database encryption
```

### Infrastructure Security

```
├─ Network segmentation (VPC)
├─ Security groups (firewall rules)
├─ Private subnets (databases)
├─ WAF (Web Application Firewall)
├─ DDoS protection (AWS Shield)
├─ Regular penetration testing
└─ Security patching (automatic)
```

## 11. MONITORING & OBSERVABILITY

### Metrics
```
├─ Request latency (p50, p95, p99)
├─ Error rates (4xx, 5xx)
├─ Throughput (requests/sec)
├─ CPU usage
├─ Memory usage
├─ Database query performance
├─ Cache hit rate
└─ API endpoint metrics
```

### Logs
```
├─ Application logs (JSON structured)
├─ Access logs
├─ Error logs
├─ Audit logs
├─ Database logs
├─ Security events
└─ Retention: 30 days hot, 1 year archived
```

### Alerts

```
├─ Error rate > 1%
├─ Latency p95 > 500ms
├─ Memory usage > 80%
├─ CPU usage > 80%
├─ Database connections > 80%
├─ Cache miss rate > 50%
├─ Disk space < 10%
└─ Deployment failures
```

---

**Next Steps**: Phase 2 will establish the monorepo structure and project initialization.
