# Interview Prep MCP Platform

Production-ready monorepo for AI-powered interview preparation built around **Model Context Protocol (MCP)**, **Retrieval-Augmented Generation (RAG)**, **agentic orchestration**, **multi-agent collaboration**, **guardrails**, and **observability**. The platform handles resume analysis, company targeting, mock interview simulation, audio transcription, multi-signal answer scoring, grammar feedback, live coaching, and adaptive difficulty progression.

---

## Architecture Overview

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────────┐     ┌────────────┐
│   Frontend   │────▶│  Spring Boot API  │────▶│  FastAPI AI Svc   │     │ PostgreSQL │
│  React + TS  │◀────│  (Backend)        │◀────│  (AI Service)     │     │  Database   │
│  Tailwind    │     │  Port 8080        │     │  Port 8000        │     │  Port 5432  │
└─────────────┘     └──────────────────┘     └──────────────────┘     └────────────┘
                            │                         │
                            ▼                         │
                    ┌──────────────────┐              │
                    │  MCP Tool Layer   │◀─────────────┘
                    │  Manifests + Stdio│
                    │  Runtime Server   │
                    └──────────────────┘
```

The system is composed of four services orchestrated via Docker Compose:

| Service        | Technology                  | Port  | Role                                                        |
|----------------|-----------------------------|-------|-------------------------------------------------------------|
| **frontend**   | React 18 + TypeScript + Tailwind | 5173  | Interview UI, resume upload, session management, reports    |
| **backend**    | Spring Boot 3 / Java 21     | 8080  | REST API, JWT auth, MCP bridge, orchestration, persistence  |
| **ai-service** | FastAPI / Python 3.11+      | 8000  | NLP analysis, scoring, grammar, speech, question generation |
| **postgres**   | PostgreSQL 16               | 5432  | Relational persistence with seed data                       |

---

## Key Concept Implementations

### 1. MCP (Model Context Protocol)

The MCP layer provides a standardized tool interface for AI agents to interact with the platform.

**Manifest-driven tool definitions** — Six tool manifests in `mcp-tools/` define actions with JSON Schema input/output contracts:

| Tool                 | Actions                                                    | Description                                      |
|----------------------|------------------------------------------------------------|--------------------------------------------------|
| `resume_tool`        | `upload_resume`, `get_latest_resume`                       | Resume upload and analysis retrieval              |
| `company_tool`       | `match_companies`, `search_companies`, `get_company`, `company_interview_context` | Company matching and context retrieval |
| `interview_tool`     | `create_session`, `get_session`, `submit_answer`, `coach_answer` | Interview session lifecycle                 |
| `grammar_tool`       | `score_answer`, `grammar_check`, `coach_answer`            | Multi-signal answer evaluation                    |
| `speech_tool`        | `upload_audio_for_analysis`                                | Audio transcription and speech scoring            |
| `recommendation_tool`| `get_profile_recommendations`, `list_roles`, `get_improvement_roadmap`, `get_reports_dashboard`, `list_reports` | Profile guidance |

**Standalone MCP stdio runtime** (`mcp-tools/runtime_server.py`) — A JSON-RPC 2.0 server implementing the MCP protocol:
- `initialize` — Returns server info and capabilities with protocol version `2024-11-05`
- `tools/list` — Discovers all available tools from manifest files
- `tools/call` — Resolves manifests, substitutes path variables, handles multipart/JSON payloads, and forwards HTTP requests to backend or AI service
- `ping` — Health check

**Backend MCP bridge** — The Spring Boot backend exposes the MCP catalog as invokable REST endpoints:
- `GET /api/mcp/tools` — List all tool manifests (`McpToolCatalogService`)
- `GET /api/mcp/tools/{toolName}` — Get a specific tool manifest
- `POST /api/mcp/tools/{toolName}/actions/{actionName}/invoke` — Execute any tool action (`McpToolInvocationService`)

The invocation service dynamically resolves path variables, routes GET/POST/multipart requests, passes JWT auth headers, and sandboxes file access to the configured workspace root.

**Key files:**
- `mcp-tools/runtime_server.py` — Stdio MCP server
- `mcp-tools/*/tool.json` — Tool manifest definitions
- `backend/.../service/McpToolCatalogService.java` — Manifest catalog reader
- `backend/.../service/McpToolInvocationService.java` — Dynamic action invocation with path resolution and workspace sandboxing
- `backend/.../controller/McpToolController.java` — REST endpoints for MCP bridge

### 2. RAG (Retrieval-Augmented Generation)

The platform implements a retrieval-then-augment pipeline where structured data is retrieved from multiple sources and used to augment AI-generated outputs.

**Resume-driven retrieval and augmentation:**
1. **Extract** — PDF/DOCX resumes are parsed into raw text (`ResumeService.extractText`)
2. **Retrieve** — The NLP service extracts structured sections: skills, experience, projects, education, certifications, summary (`NLPService.analyze_resume`)
3. **Augment** — Extracted signals are fed into question generation, producing context-aware interview prompts personalized to the candidate's profile, target role, and company context (`NLPService.generate_questions`)

**TF-IDF semantic retrieval:**
- `NLPService.recommend_roles` uses TF-IDF vectorization with cosine similarity to match candidate skill profiles against role definitions
- `GrammarService._semantic_similarity` uses TF-IDF cosine similarity to score answer relevance against expected answer points
- Fallback to keyword-overlap similarity when scikit-learn is unavailable

**Company context retrieval:**
- `CompanyService.computeMatchScore` retrieves company data (culture, history, focus areas, supported roles) from PostgreSQL and computes match scores against the candidate's resume profile
- `CompanyService.buildWhyUserMatches` generates personalized match explanations by combining retrieved company context with candidate skills

**Multi-source context assembly for question generation:**
The `generate_questions` endpoint assembles context from resume signals, company data, personality profile, strengths, weaknesses, missing skills, interviewer tone, and reality mode to produce tailored interview questions across 8 categories (EXPERIENCE, TECHNICAL, PROBLEM_SOLVING, HR, REAL_WORLD_SCENARIO, COMMUNICATION, BEHAVIORAL, SYSTEM_DESIGN).

**Key files:**
- `ai-service/app/services/nlp_service.py` — Resume analysis, skill extraction, TF-IDF role matching, context-aware question generation
- `ai-service/app/services/grammar_service.py` — TF-IDF answer relevance scoring
- `backend/.../service/CompanyService.java` — Company context retrieval and match scoring
- `backend/.../service/ResumeService.java` — PDF/DOCX text extraction pipeline

### 3. Agentic Frameworks

The `AiOrchestrationService` acts as the central agent orchestrator in the backend, coordinating the full interview preparation pipeline.

**Orchestration flow:**
```
Resume Upload → Text Extraction → AI Analysis → Skill Extraction → Role Recommendation
    → Company Matching → Question Generation → Answer Submission → Multi-Signal Scoring
    → Live Coaching → Adaptive Difficulty → Report Generation
```

**Autonomous decision-making:**
- The orchestrator decides whether to call the AI service or fall back to local heuristics based on service availability
- Every AI service call has a corresponding `fallback*` method that provides degraded but functional results
- Adaptive difficulty progression automatically adjusts question difficulty based on answer performance scores

**Tool-use agent pattern:**
- `McpToolInvocationService` acts as a tool-use agent — it reads tool manifests, resolves parameters, selects the correct HTTP method and content type, and executes actions dynamically
- The MCP runtime server similarly acts as an agent that interprets JSON-RPC requests and routes them to the appropriate backend service

**Key files:**
- `backend/.../service/AiOrchestrationService.java` — Central orchestrator with fallback chains (1000+ lines covering resume analysis, scoring, coaching, speech analysis, question generation)
- `backend/.../service/InterviewService.java` — Interview session lifecycle with adaptive difficulty
- `backend/.../service/McpToolInvocationService.java` — Dynamic tool-use agent

### 4. Multi-Agent Systems

The platform is composed of multiple specialized agents, each with a distinct responsibility, communicating through well-defined interfaces:

| Agent (Service)              | Responsibility                                                    | Location        |
|------------------------------|-------------------------------------------------------------------|-----------------|
| **NLPService**               | Resume parsing, skill extraction, role recommendation, question generation | `ai-service`    |
| **GrammarService**           | Multi-signal answer scoring (8 dimensions), grammar checking, live coaching, weakness detection, adaptive difficulty | `ai-service`    |
| **SpeechService**            | Audio transcription (Whisper), confidence/fluency/clarity scoring, tone and pronunciation feedback | `ai-service`    |
| **AiOrchestrationService**   | Coordinates all AI agents from the backend with fallback logic    | `backend`       |
| **InterviewService**         | Session lifecycle, question persistence, answer evaluation, report generation | `backend`       |
| **CompanyService**           | Company matching, context retrieval, match score computation      | `backend`       |
| **ResumeService**            | File parsing (PDF/DOCX), text extraction, skill resolution        | `backend`       |
| **SpeechProcessingService**  | Audio file validation, storage, and AI service coordination       | `backend`       |
| **McpToolInvocationService** | Dynamic MCP tool routing and execution                            | `backend`       |
| **RecommendationService**    | Profile recommendations and improvement roadmaps                  | `backend`       |
| **ReportService**            | Practice report aggregation and dashboard metrics                 | `backend`       |

**Inter-agent communication:**
- Backend agents call AI service agents via HTTP (WebClient)
- The orchestrator merges results from multiple agents (e.g., grammar scores + speech scores + coaching hints) into unified answer evaluations
- Interview sessions aggregate signals from resume analysis, company matching, and question generation agents

### 5. Guardrails (Safety, Validation, Constraints)

**Authentication and authorization:**
- JWT-based stateless authentication with HMAC-SHA signing (`JwtService`)
- Token revocation on logout — revoked token hashes are stored in `revoked_tokens` table and checked on every request (`TokenRevocationService`, `JwtAuthenticationFilter`)
- Role-based access control with `@EnableMethodSecurity` — admin endpoints require ADMIN role
- CORS restricted to configured frontend origins

**Input validation:**
- Pydantic models enforce minimum lengths on AI service inputs (`resumeText: min_length=20`, `answerText: min_length=10`, grammar check: `min_length=5`)
- Spring Boot `@Valid` annotations on request DTOs
- File upload constraints: 10MB max for resumes, 15MB max for audio
- Audio file type whitelisting: only WEBM, WAV, MP3, M4A, OGG extensions and matching content types accepted (`SpeechProcessingService.validateAudio`)

**Path traversal protection:**
- MCP workspace file access is sandboxed — `McpToolInvocationService.resolveWorkspaceFile` verifies resolved paths stay within the configured workspace root
- Audio file access is user-scoped — `SpeechProcessingService.resolveAudioPath` verifies the audio reference belongs to the requesting user

**Score clamping and safety bounds:**
- All scoring functions clamp outputs to realistic ranges (e.g., correctness: 48–98, confidence: 45–97, readiness: 52–96)
- Prevents unrealistic perfect or zero scores from reaching the UI

**Graceful degradation:**
- AI service falls back to heuristic scoring when optional dependencies (spaCy, scikit-learn, Whisper, LanguageTool) are unavailable
- Backend falls back to local analysis when the AI service is unreachable
- Every external call has error handling that returns structured error responses

**Structured error handling:**
- `GlobalExceptionHandler` catches validation errors, authentication failures, response status exceptions, and unhandled exceptions
- All errors return consistent JSON with timestamp, status code, message, and optional details

**Key files:**
- `backend/.../security/SecurityConfig.java` — Spring Security configuration
- `backend/.../security/JwtService.java` — JWT token generation and validation
- `backend/.../security/JwtAuthenticationFilter.java` — Request-level token verification
- `backend/.../service/TokenRevocationService.java` — Logout token revocation
- `backend/.../exception/GlobalExceptionHandler.java` — Centralized error handling
- `backend/.../service/SpeechProcessingService.java` — Audio validation and path security
- `ai-service/app/models/schemas.py` — Pydantic input validation

### 6. Observability (Logging, Monitoring, Tracing)

**Structured logging:**
- SLF4J + Logback logging throughout the backend with class-level loggers (`LoggerFactory.getLogger`)
- Configurable log level via `APP_LOG_LEVEL` environment variable (`logging.level.com.interviewprep.platform`)
- Key operations are logged: audio storage, MCP manifest reads, error conditions, unhandled exceptions

**Health monitoring:**
- Spring Boot Actuator exposes `/actuator/health` and `/actuator/info` endpoints
- FastAPI health endpoint at `GET /health`
- Docker Compose healthchecks on postgres (pg_isready), ai-service (HTTP health check), and backend (service dependency ordering)

**Service dependency tracing:**
- Docker Compose `depends_on` with `condition: service_healthy` ensures correct startup ordering
- Backend waits for both postgres and ai-service to be healthy before starting
- Frontend waits for backend availability

**Error tracing:**
- `GlobalExceptionHandler` logs all unhandled exceptions with full stack traces at ERROR level
- Response status exceptions are logged at WARN level with reason messages
- AI service operations that fail silently (LanguageTool, Whisper) are caught and logged

**Operational visibility:**
- Hibernate SQL formatting enabled for query debugging (`spring.jpa.properties.hibernate.format_sql=true`)
- File storage paths are logged on audio upload for audit trail
- MCP catalog read failures are logged with manifest paths

---

## Project Structure

```
interview-prep-mcp/
├── frontend/                    # React + TypeScript + Tailwind UI
│   ├── src/
│   │   ├── api/client.ts        # Axios API client with JWT interceptor
│   │   ├── components/          # UI components (interview, layout, ui)
│   │   ├── context/             # React auth context
│   │   ├── hooks/               # Custom hooks (useAuth)
│   │   ├── lib/                 # Interview simulation, role preferences
│   │   ├── pages/               # Route pages (auth, dashboard, interview, upload, etc.)
│   │   ├── types/index.ts       # TypeScript type definitions
│   │   ├── App.tsx              # Root component with routing
│   │   └── main.tsx             # Entry point
│   ├── Dockerfile               # Multi-stage build with nginx
│   └── package.json
│
├── backend/                     # Spring Boot 3 / Java 21 API
│   ├── src/main/java/com/interviewprep/platform/
│   │   ├── config/              # App config, admin bootstrap
│   │   ├── controller/          # REST controllers (Auth, Resume, Interview, MCP, Company, etc.)
│   │   ├── dto/                 # Request/response DTOs organized by domain
│   │   ├── entity/              # JPA entities (User, Resume, Interview, Company, etc.)
│   │   ├── exception/           # Global exception handler
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── security/            # JWT auth, filters, user details service
│   │   └── service/             # Business logic services
│   │       ├── AiOrchestrationService.java   # Central AI agent orchestrator
│   │       ├── McpToolCatalogService.java    # MCP manifest catalog
│   │       ├── McpToolInvocationService.java # MCP action invocation engine
│   │       ├── InterviewService.java         # Interview session lifecycle
│   │       ├── CompanyService.java           # Company matching agent
│   │       ├── ResumeService.java            # Resume parsing pipeline
│   │       ├── SpeechProcessingService.java  # Audio processing agent
│   │       └── ...                           # Auth, Report, Recommendation services
│   ├── src/test/java/           # Unit tests (AuthServiceTest, CompanyServiceTest)
│   ├── Dockerfile
│   └── pom.xml
│
├── ai-service/                  # FastAPI AI service
│   ├── app/
│   │   ├── api/routes.py        # REST endpoints for all AI operations
│   │   ├── core/config.py       # Settings (LanguageTool URL, Whisper model)
│   │   ├── models/schemas.py    # Pydantic request/response models with validation
│   │   └── services/
│   │       ├── nlp_service.py       # Resume analysis, TF-IDF role matching, question generation
│   │       ├── grammar_service.py   # Multi-signal scoring, coaching, adaptive difficulty
│   │       └── speech_service.py    # Audio transcription, fluency/clarity/confidence scoring
│   ├── tests/test_services.py   # Unit tests for NLP, grammar, and speech services
│   ├── Dockerfile
│   └── requirements.txt
│
├── database/
│   ├── schema.sql               # Full schema with tables, views, seed data (roles, companies)
│   └── .env.example
│
├── mcp-tools/                   # MCP tool manifests and runtime
│   ├── resume_tool/tool.json
│   ├── company_tool/tool.json
│   ├── interview_tool/tool.json
│   ├── grammar_tool/tool.json
│   ├── speech_tool/tool.json
│   ├── recommendation_tool/tool.json
│   ├── runtime_server.py        # Standalone MCP stdio server (JSON-RPC 2.0)
│   └── README.md
│
├── compose.yaml                 # Docker Compose for full-stack orchestration
└── README.md
```

---

## Prerequisites

- **Node.js** 20+
- **Java** 21
- **Maven** 3.9+
- **Python** 3.11 or 3.12 (recommended for full spaCy/scikit-learn NLP mode)
- **PostgreSQL** 15+
- **Docker** and **Docker Compose** (recommended for quickest setup)

`spaCy 3.7.5` and `scikit-learn 1.5.1` install automatically on Python 3.11/3.12. On newer interpreters, the AI service runs with heuristic fallbacks so development remains unblocked.

---

## Quick Start with Docker Compose

The fastest way to run the full stack:

```bash
# 1. Copy environment files
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
cp ai-service/.env.example ai-service/.env
cp database/.env.example database/.env
cp .env.example .env
```

On Windows PowerShell:
```powershell
Copy-Item backend/.env.example backend/.env
Copy-Item frontend/.env.example frontend/.env
Copy-Item ai-service/.env.example ai-service/.env
Copy-Item database/.env.example database/.env
Copy-Item .env.example .env
```

```bash
# 2. Build and start all services
docker compose up --build
```

Services will be available at:
- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080
- **AI Service:** http://localhost:8000
- **PostgreSQL:** localhost:5432

---

## Manual Setup (Without Docker)

### 1. Create the database

```sql
CREATE DATABASE interview_prep_mcp;
```

Apply the schema:
```bash
psql -U postgres -d interview_prep_mcp -f database/schema.sql
```

### 2. Configure environment files

Copy the `.env.example` files as shown in the Quick Start section above.

### 3. Run the AI service

```bash
cd ai-service
python -m venv .venv
source .venv/bin/activate        # On Windows: .venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

### 4. Run the Spring Boot backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on http://localhost:8080.

### 5. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on http://localhost:5173.

---

## Running Tests

### Backend (Java / JUnit 5 + Mockito)

```bash
cd backend
mvn test
```

Tests cover:
- `AuthServiceTest` — Registration with password encoding, JWT token generation, duplicate email rejection, logout with token revocation
- `CompanyServiceTest` — Company matching with role normalization, empty result handling, server-side search filtering (query, min score, company size)

### AI Service (Python / unittest)

```bash
cd ai-service
python -m unittest discover -s tests -v
```

Tests cover:
- `NLPServiceTest` — Resume analysis with section extraction, skill detection, role recommendation, candidate name parsing, readiness scoring; question generation with role/resume/personality/company context integration
- `GrammarServiceTest` — Multi-signal answer scoring (correctness, relevance, clarity, completeness), grammar feedback with capitalization detection, live coaching with hints and continuation prompts
- `SpeechServiceTest` — Speech analysis with transcript hint fallback, confidence/fluency/clarity scoring, tone and pronunciation feedback generation

### Frontend

```bash
cd frontend
npm run build
```

---

## API Reference

### Authentication
| Method | Endpoint                | Auth     | Description                    |
|--------|-------------------------|----------|--------------------------------|
| POST   | `/api/auth/register`    | Public   | Register a new user            |
| POST   | `/api/auth/login`       | Public   | Login and receive JWT token    |
| POST   | `/api/auth/logout`      | Bearer   | Revoke current JWT token       |
| GET    | `/api/auth/me`          | Bearer   | Get current user profile       |

### Resume
| Method | Endpoint                | Auth     | Description                    |
|--------|-------------------------|----------|--------------------------------|
| POST   | `/api/resumes`          | Bearer   | Upload and analyze a resume    |
| GET    | `/api/resumes/latest`   | Bearer   | Get latest resume analysis     |

### Interview
| Method | Endpoint                                          | Auth   | Description                          |
|--------|---------------------------------------------------|--------|--------------------------------------|
| POST   | `/api/interviews/sessions`                        | Bearer | Create an interview session          |
| GET    | `/api/interviews/sessions/{sessionId}`            | Bearer | Get session with questions           |
| POST   | `/api/interviews/questions/{questionId}/answer`   | Bearer | Submit and evaluate an answer        |
| POST   | `/api/interviews/questions/{questionId}/coach`    | Bearer | Get live coaching hints              |
| POST   | `/api/interviews/audio`                           | Bearer | Upload audio for speech analysis     |

### Company
| Method | Endpoint                    | Auth   | Description                          |
|--------|-----------------------------|--------|--------------------------------------|
| POST   | `/api/companies/match`      | Bearer | Match companies to target roles      |
| GET    | `/api/companies/search`     | Bearer | Search with filters                  |
| GET    | `/api/companies/{companyId}`| Bearer | Get company details                  |

### MCP Tools
| Method | Endpoint                                                    | Auth   | Description                    |
|--------|-------------------------------------------------------------|--------|--------------------------------|
| GET    | `/api/mcp/tools`                                            | Bearer | List all MCP tool manifests    |
| GET    | `/api/mcp/tools/{toolName}`                                 | Bearer | Get specific tool manifest     |
| POST   | `/api/mcp/tools/{toolName}/actions/{actionName}/invoke`     | Bearer | Invoke an MCP tool action      |

### Reports and Recommendations
| Method | Endpoint                          | Auth   | Description                    |
|--------|-----------------------------------|--------|--------------------------------|
| GET    | `/api/roles`                      | Bearer | List available roles           |
| GET    | `/api/recommendations/profile`    | Bearer | Get profile recommendations    |
| GET    | `/api/reports`                    | Bearer | List practice reports          |
| GET    | `/api/reports/dashboard`          | Bearer | Get dashboard metrics          |
| GET    | `/api/admin/dashboard`            | Admin  | Admin dashboard                |

### AI Service (Direct)
| Method | Endpoint              | Description                              |
|--------|-----------------------|------------------------------------------|
| GET    | `/health`             | Health check                             |
| POST   | `/analyze-resume`     | Analyze resume text                      |
| POST   | `/recommend-role`     | Recommend roles from skills              |
| POST   | `/score-answer`       | Score an interview answer                |
| POST   | `/coach-answer`       | Get live coaching for an answer draft    |
| POST   | `/grammar-check`      | Check grammar, tone, vocabulary          |
| POST   | `/generate-questions`  | Generate context-aware interview questions |
| POST   | `/speech-analysis`    | Analyze uploaded audio                   |

---

## MCP Standalone Runtime

Run the local MCP server for agent integration:

```bash
cd mcp-tools
python runtime_server.py
```

Example JSON-RPC messages over stdio:

```json
{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"interview_tool","arguments":{"action":"create_session","authorization":"Bearer <jwt>","selectedRoles":["Backend Engineer"],"personalityProfile":"Analytical and structured"}}}
```

---

## Environment Variables

### Backend (`backend/.env`)
| Variable                        | Default                          | Description                          |
|---------------------------------|----------------------------------|--------------------------------------|
| `DB_URL`                        | `jdbc:postgresql://localhost:5432/interview_prep_mcp` | Database connection URL |
| `DB_USERNAME`                   | `postgres`                       | Database username                    |
| `DB_PASSWORD`                   | `postgres`                       | Database password                    |
| `JWT_SECRET`                    | (dev default)                    | Base64-encoded JWT signing key       |
| `JWT_EXPIRATION_MS`             | `86400000`                       | Token expiration (24h)               |
| `AI_SERVICE_BASE_URL`           | `http://localhost:8000`          | AI service URL                       |
| `FRONTEND_ORIGIN`               | `http://localhost:5173`          | Allowed CORS origin                  |
| `APP_BOOTSTRAP_ADMIN_EMAIL`     | (empty)                          | Auto-provision admin account         |
| `APP_BOOTSTRAP_ADMIN_PASSWORD`  | (empty)                          | Admin account password               |
| `APP_LOG_LEVEL`                 | `INFO`                           | Application log level                |

### AI Service (`ai-service/.env`)
| Variable              | Default  | Description                              |
|-----------------------|----------|------------------------------------------|
| `LANGUAGE_TOOL_URL`   | (empty)  | Optional LanguageTool server URL         |
| `WHISPER_MODEL_NAME`  | `base`   | Whisper model size for transcription     |

---

## Notes

- Docker is the most reliable way to run the full stack because it locks the AI service to a supported Python runtime.
- When optional AI dependencies are unavailable locally, the AI service falls back to heuristic scoring so unit tests can still run.
- JWT logout is server-enforced through token revocation — signed-out tokens cannot be reused.
- The MCP catalog is invokable through the backend, including manifest-driven file-based multipart actions for resume and speech workflows.
- The interview session UI supports multi-voice panel playback through browser speech-synthesis for interruption, panel, and lag simulation modes.
- Set `APP_BOOTSTRAP_ADMIN_EMAIL` and `APP_BOOTSTRAP_ADMIN_PASSWORD` in `backend/.env` to auto-provision an admin account.
