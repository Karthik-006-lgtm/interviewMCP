# Interview Prep MCP Platform

AI-powered interview preparation platform built with MCP (Model Context Protocol), RAG, multi-agent orchestration, guardrails, and observability.

## Architecture

![Interview Prep MCP Platform Architecture](Architecture%20diagram.png)

| Service      | Tech Stack                        | Port |
|--------------|-----------------------------------|------|
| frontend     | React 18, TypeScript, Tailwind    | 5173 |
| backend      | Spring Boot 3, Java 21, JWT       | 8080 |
| ai-service   | FastAPI, Python 3.11+, spaCy      | 8000 |
| database     | PostgreSQL 16                     | 5432 |

## Key Concepts

### MCP (Model Context Protocol)
- Six tool manifests in `mcp-tools/` (resume, company, interview, grammar, speech, recommendation) with JSON Schema contracts
- Standalone stdio runtime (`runtime_server.py`) implementing JSON-RPC 2.0 with `initialize`, `tools/list`, `tools/call`
- Backend MCP bridge: `GET /api/mcp/tools`, `POST /api/mcp/tools/{tool}/actions/{action}/invoke` for dynamic tool invocation

### RAG (Retrieval-Augmented Generation)
- Resumes parsed (PDF/DOCX) → structured sections extracted → context assembled → personalized questions generated
- TF-IDF + cosine similarity for role matching and answer relevance scoring
- Company context (culture, history, focus areas) retrieved from DB and merged into question generation

### Agentic Framework
- `AiOrchestrationService` coordinates the full pipeline: resume → skills → roles → companies → questions → scoring → coaching → reports
- Every AI call has a fallback method for graceful degradation when services are unavailable
- Adaptive difficulty automatically adjusts question level based on answer performance

### Multi-Agent System
- **NLPService** — resume analysis, skill extraction, role recommendation, question generation
- **GrammarService** — 8-dimension answer scoring, coaching, weakness detection, adaptive difficulty
- **SpeechService** — audio transcription (Whisper), confidence/fluency/clarity scoring
- **AiOrchestrationService** — backend coordinator with fallback chains
- **McpToolInvocationService** — dynamic tool routing from manifests
- **InterviewService, CompanyService, ResumeService, ReportService** — domain-specific agents

### Guardrails
- JWT auth with token revocation on logout, role-based access control
- Pydantic validation (min lengths), file size limits (10MB resume, 15MB audio), audio type whitelisting
- MCP workspace sandboxing (path traversal protection), user-scoped audio access
- Score clamping to realistic ranges, structured error responses via `GlobalExceptionHandler`

### Observability
- SLF4J logging with configurable level (`APP_LOG_LEVEL`), key operations logged
- Spring Actuator (`/actuator/health`, `/actuator/info`), FastAPI health endpoint
- Docker Compose healthchecks with dependency ordering
- Hibernate SQL formatting for query debugging

## Project Structure

```
├── frontend/src/          # React UI (pages, components, API client, auth context)
├── backend/src/main/      # Spring Boot API (controllers, services, entities, security)
├── backend/src/test/      # JUnit 5 + Mockito tests
├── ai-service/app/        # FastAPI (routes, services: NLP, grammar, speech)
├── ai-service/tests/      # Python unit tests
├── database/schema.sql    # PostgreSQL schema + seed data
├── mcp-tools/             # Tool manifests + MCP stdio runtime
└── compose.yaml           # Docker Compose orchestration
```

## Quick Start

```bash
# Copy env files
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
cp ai-service/.env.example ai-service/.env
cp database/.env.example database/.env
cp .env.example .env

# Run everything
docker compose up --build
```

**Without Docker:**

```bash
# Database
psql -U postgres -c "CREATE DATABASE interview_prep_mcp;"
psql -U postgres -d interview_prep_mcp -f database/schema.sql

# AI Service (port 8000)
cd ai-service && python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt && uvicorn app.main:app --reload --port 8000

# Backend (port 8080)
cd backend && mvn spring-boot:run

# Frontend (port 5173)
cd frontend && npm install && npm run dev
```

## Running Tests

```bash
# Backend
cd backend && mvn test

# AI Service
cd ai-service && python -m unittest discover -s tests -v

# Frontend
cd frontend && npm run build
```

## API Endpoints

**Auth:** `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`

**Resume:** `POST /api/resumes`, `GET /api/resumes/latest`

**Interview:** `POST /api/interviews/sessions`, `GET /api/interviews/sessions/{id}`, `POST /api/interviews/questions/{id}/answer`, `POST /api/interviews/questions/{id}/coach`, `POST /api/interviews/audio`

**Company:** `POST /api/companies/match`, `GET /api/companies/search`, `GET /api/companies/{id}`

**MCP:** `GET /api/mcp/tools`, `GET /api/mcp/tools/{name}`, `POST /api/mcp/tools/{name}/actions/{action}/invoke`

**Reports:** `GET /api/reports`, `GET /api/reports/dashboard`, `GET /api/recommendations/profile`

**AI Service:** `/health`, `/analyze-resume`, `/score-answer`, `/coach-answer`, `/grammar-check`, `/generate-questions`, `/speech-analysis`
