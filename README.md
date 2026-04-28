# Interview Prep MCP Platform

Production-ready monorepo for AI-powered interview preparation with resume analysis, company targeting, mock interview practice, audio answer transcription, speech scoring, grammar feedback, and MCP tool manifests for automation.

## Monorepo structure

- `frontend/` React + TypeScript + Tailwind application
- `backend/` Spring Boot 3 / Java 21 API with JWT auth and PostgreSQL persistence
- `ai-service/` FastAPI service for resume analysis, answer scoring, grammar feedback, speech analysis, and role recommendation
- `database/` PostgreSQL schema and seed data
- `mcp-tools/` manifest-style MCP tool definitions

## Prerequisites

- Node.js 20+
- Java 21
- Maven 3.9+
- Python 3.11 or 3.12 recommended for full spaCy/scikit NLP mode
- PostgreSQL 15+

`spaCy 3.7.5` and `scikit-learn 1.5.1` are installed automatically on Python 3.11/3.12. On newer interpreters, the AI service still installs and runs with heuristic fallbacks for resume scoring, grammar, and speech analysis so local development remains unblocked.

## 1. Create the database

```sql
CREATE DATABASE interview_prep_mcp;
```

Apply the schema:

```bash
psql -U postgres -d interview_prep_mcp -f database/schema.sql
```

## 2. Configure environment files

Copy the sample files and adjust values if needed:

```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
cp ai-service/.env.example ai-service/.env
cp database/.env.example database/.env
```

On Windows PowerShell, use:

```powershell
Copy-Item backend/.env.example backend/.env
Copy-Item frontend/.env.example frontend/.env
Copy-Item ai-service/.env.example ai-service/.env
Copy-Item database/.env.example database/.env
Copy-Item .env.example .env
```

## Quick start with Docker Compose

After the `.env` files exist, run the full stack with PostgreSQL, FastAPI, Spring Boot, and the React app:

```bash
docker compose up --build
```

Services:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- AI service: `http://localhost:8000`
- PostgreSQL: `localhost:5432`

## 3. Run the AI service

```bash
cd ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

Windows PowerShell:

```powershell
cd ai-service
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

## 4. Run the Spring Boot backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`.

## 5. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`.

## Verification commands

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm run build
```

AI service:

```bash
cd ai-service
python -m unittest discover -s tests -v
```

## API overview

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/resumes`
- `GET /api/resumes/latest`
- `GET /api/roles`
- `GET /api/recommendations/profile`
- `GET /api/mcp/tools`
- `GET /api/mcp/tools/{toolName}`
- `POST /api/mcp/tools/{toolName}/actions/{actionName}/invoke`
- `GET /api/admin/dashboard`
- `POST /api/companies/match`
- `GET /api/companies/search`
- `GET /api/companies/{companyId}`
- `POST /api/interviews/sessions`
- `GET /api/interviews/sessions/{sessionId}`
- `POST /api/interviews/audio`
- `POST /api/interviews/questions/{questionId}/answer`
- `GET /api/reports`
- `GET /api/reports/dashboard`
- `POST /analyze_resume`
- `POST /analyze-resume`
- `POST /score_answer`
- `POST /score-answer`
- `POST /grammar_check`
- `POST /grammar-check`
- `POST /generate_questions`
- `POST /generate-questions`
- `POST /recommend_role`
- `POST /recommend-role`
- `POST /transcribe_audio`
- `POST /speech-analysis`

## MCP tools

Each tool in `mcp-tools/` documents an automation surface:

- `resume_tool`
- `company_tool`
- `grammar_tool`
- `interview_tool`
- `speech_tool`
- `recommendation_tool`

Standalone MCP runtime:

```bash
cd mcp-tools
python runtime_server.py
```

## Notes

- The backend parses uploaded PDF and DOCX resumes before sending text to the AI service.
- Interview sessions now ask the AI service to generate dynamic questions from the selected roles, resume signals, technical focus, strengths, weaknesses, and personality profile before falling back to local heuristics.
- LanguageTool support is optional and activated when `LANGUAGE_TOOL_URL` is configured.
- Voice answers are recorded in the browser, uploaded through `POST /api/interviews/audio`, and analyzed for transcript, confidence, fluency, clarity, tone, and pronunciation guidance.
- Browser speech recognition is used as a transcript hint when available; Whisper-based transcription remains supported in the AI service when the dependency is installed.
- JWT logout is server-enforced through token revocation, so signed-out tokens can no longer be reused on protected APIs.
- Set `APP_BOOTSTRAP_ADMIN_EMAIL`, `APP_BOOTSTRAP_ADMIN_PASSWORD`, and optionally `APP_BOOTSTRAP_ADMIN_FULL_NAME` in `backend/.env` to auto-provision an admin account and unlock the secured `/admin` frontend route plus `/api/admin/dashboard`.
- The MCP catalog is now invokable through the backend, including manifest-driven file-based multipart actions for resume and speech workflows inside the configured workspace root.
- The interview session UI now supports multi-voice panel playback through the browser speech-synthesis runtime for interruption, panel, and lag simulation modes.
- Docker is the most reliable way to run the full monorepo locally because it locks the AI service to a supported Python runtime.
- When optional AI dependencies are unavailable locally, the AI service falls back to heuristic scoring so unit tests can still run; the full dependency-backed path is preserved in Docker or a supported Python 3.11/3.12 environment.
