# 🎯 HireSense - AI-Powered Interview Prep Platform

Complete interview preparation platform with AI coaching, video recording, speech analysis, and personalized feedback.

![Platform Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)
![Services](https://img.shields.io/badge/Services-3-blue)
![Features](https://img.shields.io/badge/Features-15+-orange)

## 🚀 Quick Deploy - Make Your Website Live!

### Deploy to Railway (Recommended - Free)

1. **Click here**: [Deploy to Railway](https://railway.app/template)
2. **Or manually**:
   - Go to https://railway.app
   - Connect your GitHub: `Karthik-006-lgtm/interviewMCP`
   - Create 3 services: `backend`, `ai-service`, `frontend`
   - Follow [LIVE_DEPLOYMENT.md](./LIVE_DEPLOYMENT.md)

### Deploy to Render (Alternative - Free)

1. **Click here**: [Deploy to Render](https://render.com/deploy)
2. Select repository: `Karthik-006-lgtm/interviewMCP`
3. Services auto-deploy from `render.yaml`

**Full deployment guide**: [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

---

## ✨ Features

### Core Features
- ✅ **31 Technical Roles** - Java, Python, Full Stack, Data Science, ML, Cloud, DevOps, Security, etc.
- ✅ **18 Target Companies** - Practice for specific companies with tailored questions
- ✅ **AI Coaching** - Real-time hints and feedback during interviews
- ✅ **Video Recording** - Auto-record and save interview sessions
- ✅ **Speech-to-Text** - Convert voice answers to text automatically
- ✅ **Grammar Checking** - AI-powered language feedback
- ✅ **Emotion Analysis** - Confidence, fluency, clarity scoring
- ✅ **Interview Reports** - Detailed performance analytics
- ✅ **Adaptive Difficulty** - Questions adjust to your skill level
- ✅ **Multiple Interview Modes** - Panel, one-on-one, stress scenarios

### User Features
- ✅ User Registration & Authentication
- ✅ Resume Upload & Analysis
- ✅ Role & Company Matching
- ✅ Practice Sessions History
- ✅ Progress Tracking
- ✅ Admin Dashboard

---

## 🏗️ Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Frontend   │────▶│   Backend    │────▶│  AI Service  │
│ React + Vite │     │  Spring Boot │     │   FastAPI    │
│  Port: 5173  │     │  Port: 8080  │     │  Port: 8000  │
└──────────────┘     └──────────────┘     └──────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │  H2 Database │
                     │   In-Memory  │
                     └──────────────┘
```

---

## 🛠️ Tech Stack

### Frontend
- **Framework**: React 18 + TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **State Management**: Context API
- **Routing**: React Router v6

### Backend
- **Framework**: Spring Boot 3.5
- **Language**: Java 24
- **Database**: H2 (In-Memory)
- **Security**: Spring Security + JWT
- **API**: RESTful

### AI Service
- **Framework**: FastAPI
- **Language**: Python 3.12
- **NLP**: spaCy + language_tool_python
- **Speech**: Web Speech API integration

---

## 📦 Local Development

### Prerequisites
- Java 24+
- Node.js 20+
- Python 3.12+
- Maven

### Quick Start

1. **Clone Repository**
```bash
git clone https://github.com/Karthik-006-lgtm/interviewMCP.git
cd interviewMCP
```

2. **Start Backend**
```bash
cd backend
mvn spring-boot:run
```

3. **Start AI Service**
```bash
cd ai-service
python -m venv venv
venv\Scripts\activate  # Windows
source venv/bin/activate  # Mac/Linux
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

4. **Start Frontend**
```bash
cd frontend
npm install
npm run dev
```

5. **Access Application**
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- AI Service: http://localhost:8000

---

## 🔐 Default Credentials

**Admin Account:**
- Email: `admin@hiresense.ai`
- Password: `admin123`

---

## 📊 Database

### Roles (31 total)
Java Developer, Python Developer, Full Stack Developer, Data Scientist, Machine Learning Engineer, Cloud Architect, Site Reliability Engineer, Security Engineer, Product Manager, UX Designer, UI Developer, Mobile Developer, QA Engineer, Database Administrator, Business Analyst, Scrum Master, Technical Writer, Solutions Architect, Blockchain Developer, Game Developer, AI Engineer, Data Engineer, Data Analyst, DevOps Engineer, Frontend Engineer, Backend Engineer, Cybersecurity Analyst, iOS Developer, Android Developer, Engineering Manager, Platform Engineer

### Companies (18 total)
Quantum AI Research, SecureNet Technologies, DataFlow Engineering, MobileFirst Studios, CloudScale Infrastructure, GameForge Interactive, FinTech Solutions Group, HealthTech Innovations, EcoTech Systems, StreamMedia Networks, Blockchain Ventures Inc, AutoDrive Technologies, SaaS Platform Co, EdTech Learning Systems, LogiChain Solutions, RoboTech Dynamics, SocialConnect Platform, InsurTech Innovations

---

## 🐳 Docker Deployment

```bash
docker-compose up -d
```

Services will be available at:
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- AI Service: http://localhost:8000

---

## 🌐 Make It Live

### Option 1: Railway (Recommended)
- Free tier available
- Automatic HTTPS
- Custom domains supported
- Auto-deploy from GitHub
- **Guide**: [LIVE_DEPLOYMENT.md](./LIVE_DEPLOYMENT.md)

### Option 2: Render
- Free tier available
- Blueprint deployment (render.yaml included)
- Automatic SSL
- **Guide**: [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

### Option 3: Your Own Server
- Use Docker Compose
- Configure reverse proxy (Nginx/Apache)
- Set up SSL (Let's Encrypt)

---

## 📱 Access from Anywhere

Once deployed, your website will be accessible from:
- ✅ Any smartphone (iOS/Android)
- ✅ Any laptop/desktop (Windows/Mac/Linux)
- ✅ Any tablet
- ✅ Any browser
- ✅ Anywhere in the world

**Just share your deployment URL!**

---

## 🎯 Use Cases

- **Job Seekers**: Practice interviews for dream companies
- **Students**: Prepare for campus placements
- **Career Switchers**: Learn new role requirements
- **Companies**: Use as internal training tool
- **Educators**: Help students prepare for industry

---

## 🔄 Auto-Deploy

Push to GitHub → Automatic deployment (if connected to Railway/Render)

```bash
git add .
git commit -m "Your changes"
git push origin main
```

---

## 📸 Screenshots

### Dashboard
Interview selection with 31 roles and 18 companies

### Interview Session
Real-time AI coaching, video recording, speech-to-text

### Reports
Detailed performance analytics and improvement suggestions

---

## 🤝 Contributing

This is a production-ready platform. Feel free to:
- Report bugs
- Suggest features
- Submit pull requests

---

## 📄 License

This project is open source and available for educational purposes.

---

## 🆘 Support

- **Documentation**: Check [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)
- **Issues**: Create an issue on GitHub
- **Logs**: Check Railway/Render dashboard for deployment logs

---

## 🎉 Success!

Your interview prep platform is ready to go live!

**Repository**: https://github.com/Karthik-006-lgtm/interviewMCP

**Next Steps**:
1. ✅ Code pushed to GitHub
2. 📦 Deploy to Railway/Render
3. 🌐 Share your live URL
4. 🎯 Start practicing interviews!

---

Made with ❤️ for job seekers everywhere
