# 🎯 Interview Prep MCP Platform

> AI-Powered Interview Preparation Platform with Model Context Protocol Integration

## 🚀 Quick Start

### Option 1: Automated Startup (Recommended)
Double-click the startup script:
```
start-all.bat
```

### Option 2: Manual Startup
Open 3 terminals and run:

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2 - AI Service:**
```bash
cd ai-service
..\.venv\Scripts\activate
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

**Terminal 3 - Frontend:**
```bash
cd frontend
npm run dev
```

## 🌐 Access Points

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:5173 | Main application |
| **Backend API** | http://localhost:8080/api | REST API |
| **H2 Console** | http://localhost:8080/h2-console | Database viewer |
| **AI Service** | http://localhost:8000 | AI microservice |
| **API Docs** | http://localhost:8000/docs | Swagger UI |

## 🔐 Default Login

**Email:** admin@hiresense.ai  
**Password:** admin123

## 🏗️ Architecture

```
┌─────────────┐
│   Frontend  │ (React + TypeScript + Vite)
│   :5173     │
└──────┬──────┘
       │
┌──────▼──────┐
│   Backend   │ (Spring Boot 3.5 + Java 24)
│   :8080     │
└──────┬──────┘
       │
┌──────▼──────┐     ┌──────────────┐
│  AI Service │────▶│  MCP Tools   │
│   :8000     │     │  (6 Tools)   │
└──────┬──────┘     └──────────────┘
       │
┌──────▼──────┐
│  H2 Database│
│  (In-Memory)│
└─────────────┘
```

## 📦 Tech Stack

### Frontend
- React 18.3
- TypeScript
- Vite
- Tailwind CSS
- React Router DOM
- Axios

### Backend
- Spring Boot 3.5.14
- Java 24
- Spring Security + JWT
- Spring Data JPA
- H2 Database
- Maven

### AI Service
- FastAPI 0.112
- Python 3.14
- Uvicorn
- Pydantic

### MCP Tools (6 AI-Powered Tools)
1. 📄 Resume Tool - Parsing & skill extraction
2. 🎯 Interview Tool - Dynamic question generation
3. 🏢 Company Tool - Company research
4. ✍️ Grammar Tool - Answer polishing
5. 🗣️ Speech Tool - Speech-to-text
6. 💡 Recommendation Tool - Personalized learning paths

## ✨ Features

- ✅ JWT-based authentication
- ✅ Resume upload & intelligent parsing
- ✅ AI-powered interview simulations
- ✅ 15+ scoring metrics per answer
- ✅ Adaptive difficulty adjustment
- ✅ Live coaching & feedback
- ✅ Company-specific preparation
- ✅ Skill gap analysis
- ✅ Performance reports & analytics
- ✅ Speech-to-text & grammar checking

## 📚 Documentation

- **[Start Guide](START_LOCAL.md)** - Detailed startup instructions
- **[Services Status](SERVICES_RUNNING.md)** - Current service information
- **[Architecture Prompt](ARCHITECTURE_DIAGRAM_PROMPT.txt)** - For diagram generation
- **[PPT Prompt](PPT_GENERATION_PROMPT.txt)** - Presentation generation

## 🛠️ Development

### Prerequisites
- ✅ Java 24+
- ✅ Maven 3.9+
- ✅ Node.js 24+
- ✅ Python 3.14+

### Database
Using H2 in-memory database for easy local development:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)
- Console: http://localhost:8080/h2-console

### API Testing
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hiresense.ai","password":"admin123"}'

# Get roles (with token)
curl -X GET http://localhost:8080/api/roles \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🔧 Troubleshooting

### Port Already in Use
```bash
# Windows - Kill process on port
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Clean Build
```bash
cd backend
mvn clean install
```

### Reset Node Modules
```bash
cd frontend
rmdir /s /q node_modules
npm install
```

## 📊 Database Schema

**14+ Tables:**
- users, user_roles, user_skills
- resumes, parsed_resumes
- skills, skill_gaps
- companies, company_roles, roles
- interviews, interview_questions
- answers (15+ scoring fields)
- reports, revoked_tokens

## 🎯 Project Structure

```
├── backend/          # Spring Boot backend
├── frontend/         # React frontend
├── ai-service/       # FastAPI AI service
├── mcp-tools/        # MCP tool implementations
├── database/         # Database schemas
├── uploads/          # File storage
├── start-all.bat     # Windows startup script
├── start-all.ps1     # PowerShell startup script
└── README.md         # This file
```

## 🤝 Contributing

This is an interview preparation platform with MCP integration. The codebase demonstrates:
- Production-ready Spring Boot architecture
- Clean separation of concerns
- RESTful API design
- AI service integration
- MCP protocol implementation

## 📝 License

Proprietary - Interview Prep MCP Platform

---

**Ready to start?** Run `start-all.bat` and navigate to http://localhost:5173

**Questions?** Check `SERVICES_RUNNING.md` for detailed service information.
