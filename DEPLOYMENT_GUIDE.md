# 🚀 HireSense Deployment Guide

Complete guide to deploy HireSense Interview Prep Platform as a live website accessible from anywhere.

## 📋 Table of Contents

1. [Quick Deploy (Railway - Recommended)](#quick-deploy-railway)
2. [Alternative: Render Deployment](#alternative-render)
3. [Alternative: Docker Deployment](#alternative-docker)
4. [Environment Variables](#environment-variables)
5. [Post-Deployment Setup](#post-deployment)

---

## 🚂 Quick Deploy (Railway - Recommended)

Railway provides free hosting with automatic HTTPS and custom domains.

### Step 1: Push to GitHub ✅
Your code is already on GitHub: https://github.com/Karthik-006-lgtm/interviewMCP

### Step 2: Deploy to Railway

1. **Go to Railway**: https://railway.app
2. **Sign in** with your GitHub account
3. **Create New Project** → **Deploy from GitHub repo**
4. **Select**: `Karthik-006-lgtm/interviewMCP`

### Step 3: Create 3 Services

Railway will auto-detect your services, but you need to configure them:

#### Service 1: Backend (Spring Boot)
```bash
Root Directory: /backend
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/*.jar
```

**Environment Variables:**
```env
SERVER_PORT=8080
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION_MS=86400000
AI_SERVICE_BASE_URL=https://your-ai-service-url.railway.app
FRONTEND_ORIGIN=https://your-frontend-url.railway.app
APP_BOOTSTRAP_ADMIN_EMAIL=admin@hiresense.ai
APP_BOOTSTRAP_ADMIN_PASSWORD=admin123
APP_BOOTSTRAP_ADMIN_FULL_NAME=Platform Admin
RESUME_STORAGE_DIR=/app/uploads/resumes
AUDIO_STORAGE_DIR=/app/uploads/audio
RECORDING_STORAGE_DIR=/app/uploads/recordings
```

#### Service 2: AI Service (FastAPI)
```bash
Root Directory: /ai-service
Build Command: pip install -r requirements.txt
Start Command: uvicorn app.main:app --host 0.0.0.0 --port 8000
```

**Environment Variables:**
```env
PORT=8000
```

#### Service 3: Frontend (React + Vite)
```bash
Root Directory: /frontend
Build Command: npm install && npm run build
Start Command: npx serve -s dist -l 80
```

**Environment Variables:**
```env
VITE_API_BASE_URL=https://your-backend-url.railway.app
```

### Step 4: Get Your Live URLs

After deployment, Railway will give you 3 URLs:
- **Frontend**: `https://your-app.railway.app` (share this with friends!)
- **Backend**: `https://your-backend.railway.app`
- **AI Service**: `https://your-ai.railway.app`

### Step 5: Update Environment Variables

Go back and update the URLs in each service's environment variables to point to the actual deployed URLs.

---

## 🎨 Alternative: Render Deployment

Render also offers free hosting with 3 separate services.

### Deploy Each Service:

1. **Go to**: https://render.com
2. **Create New** → **Web Service**
3. **Connect GitHub**: Select `interviewMCP` repo

#### Backend Service:
```yaml
Name: hiresense-backend
Root Directory: backend
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/*.jar
```

#### AI Service:
```yaml
Name: hiresense-ai
Root Directory: ai-service
Build Command: pip install -r requirements.txt
Start Command: uvicorn app.main:app --host 0.0.0.0 --port $PORT
```

#### Frontend Service:
```yaml
Name: hiresense-frontend
Root Directory: frontend
Build Command: npm install && npm run build
Start Command: npx serve -s dist
```

---

## 🐳 Alternative: Docker Deployment (Self-Hosted)

If you have a server (VPS, AWS, etc.):

### Step 1: Install Docker & Docker Compose
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### Step 2: Clone Repository
```bash
git clone https://github.com/Karthik-006-lgtm/interviewMCP.git
cd interviewMCP
```

### Step 3: Update Environment Variables
Edit the `.env` files in each service directory.

### Step 4: Deploy
```bash
docker-compose up -d
```

Your services will be available at:
- Frontend: http://your-server-ip:5173
- Backend: http://your-server-ip:8080
- AI Service: http://your-server-ip:8000

---

## 🔐 Environment Variables

### Required Variables:

**Backend:**
- `JWT_SECRET` - Change in production!
- `AI_SERVICE_BASE_URL` - AI service URL
- `FRONTEND_ORIGIN` - Frontend URL (for CORS)
- `APP_BOOTSTRAP_ADMIN_EMAIL` - Admin email
- `APP_BOOTSTRAP_ADMIN_PASSWORD` - Admin password

**Frontend:**
- `VITE_API_BASE_URL` - Backend API URL

**AI Service:**
- `PORT` - Service port (usually 8000)

---

## ✅ Post-Deployment Setup

### 1. Test Your Deployment

Visit your frontend URL and:
- ✅ Register a new account
- ✅ Login with admin credentials
- ✅ Start a mock interview
- ✅ Test AI coaching
- ✅ Test video recording

### 2. Share Your Website

Your website URL will be something like:
- **Railway**: `https://hiresense-frontend.railway.app`
- **Render**: `https://hiresense.onrender.com`

Share this URL with anyone - they can access it from any device!

### 3. Custom Domain (Optional)

Both Railway and Render support custom domains:
1. Buy a domain (e.g., hiresense.com)
2. Add it in Railway/Render dashboard
3. Update DNS records
4. Get free SSL certificate automatically

---

## 🎯 Access from Anywhere

Once deployed, your website will be accessible from:
- ✅ Your friend's phone
- ✅ Your friend's laptop
- ✅ Any tablet or computer
- ✅ Anywhere in the world with internet

**No local setup needed!** Just share the URL!

---

## 📊 Features Available in Production

- ✅ 31 Role Options
- ✅ 18 Target Companies
- ✅ AI Coaching (Real-time)
- ✅ Video Recording & Playback
- ✅ Speech-to-Text
- ✅ Grammar Checking
- ✅ Interview Reports
- ✅ Admin Dashboard
- ✅ User Authentication

---

## 🆘 Troubleshooting

### Backend not connecting to AI Service
- Update `AI_SERVICE_BASE_URL` to the correct AI service URL

### CORS Errors
- Update `FRONTEND_ORIGIN` in backend to match your frontend URL

### Database resets on restart
- H2 is in-memory. For persistence, switch to PostgreSQL (Railway provides free PostgreSQL)

---

## 🎉 Success!

Your platform is now live and accessible from anywhere!

**Admin Credentials:**
- Email: admin@hiresense.ai
- Password: admin123

**GitHub Repository:**
https://github.com/Karthik-006-lgtm/interviewMCP

---

**Need help?** Check the logs in Railway/Render dashboard.
