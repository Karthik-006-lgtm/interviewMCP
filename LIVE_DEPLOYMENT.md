# 🌐 Deploy HireSense Live - Quick Start

Make your website accessible from anywhere in 10 minutes!

## 🚀 Fastest Way: Railway (Free)

### Step 1: Click to Deploy

1. Go to: https://railway.app
2. Click "Start a New Project"
3. Choose "Deploy from GitHub repo"
4. Select: `Karthik-006-lgtm/interviewMCP`

### Step 2: Create 3 Services

#### Service 1: Backend
- Click "New" → "Empty Service"
- Name it: `backend`
- Settings → Source → Root Directory: `backend`
- Settings → Deploy → Start Command: `java -jar target/*.jar`
- Variables tab → Add all environment variables from below

**Backend Environment Variables:**
```
SERVER_PORT=8080
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=
JWT_SECRET=change-this-to-a-random-secret-key-in-production
JWT_EXPIRATION_MS=86400000
APP_BOOTSTRAP_ADMIN_EMAIL=admin@hiresense.ai
APP_BOOTSTRAP_ADMIN_PASSWORD=admin123
APP_BOOTSTRAP_ADMIN_FULL_NAME=Platform Admin
RESUME_STORAGE_DIR=/app/uploads/resumes
AUDIO_STORAGE_DIR=/app/uploads/audio
RECORDING_STORAGE_DIR=/app/uploads/recordings
```

After backend deploys, copy its URL (looks like: `backend-production-xxxx.up.railway.app`)

#### Service 2: AI Service
- Click "New" → "Empty Service"
- Name it: `ai-service`
- Settings → Source → Root Directory: `ai-service`
- Settings → Deploy → Start Command: `uvicorn app.main:app --host 0.0.0.0 --port $PORT`
- No extra variables needed

After AI service deploys, copy its URL (looks like: `ai-service-production-xxxx.up.railway.app`)

#### Service 3: Frontend
- Click "New" → "Empty Service"
- Name it: `frontend`
- Settings → Source → Root Directory: `frontend`
- Settings → Deploy → Build Command: `npm install && npm run build`
- Settings → Deploy → Start Command: `npx serve -s dist -l $PORT`

**Frontend Environment Variables:**
```
VITE_API_BASE_URL=https://YOUR-BACKEND-URL.up.railway.app
```
(Replace YOUR-BACKEND-URL with the actual backend URL from step 1)

After frontend deploys, copy its URL (looks like: `frontend-production-xxxx.up.railway.app`)

### Step 3: Update Cross-Service URLs

Now go back and update these variables:

**In Backend Service:**
```
AI_SERVICE_BASE_URL=https://YOUR-AI-SERVICE-URL.up.railway.app
FRONTEND_ORIGIN=https://YOUR-FRONTEND-URL.up.railway.app
```

**Services will auto-redeploy with new variables!**

### Step 4: Access Your Live Website! 🎉

Your frontend URL is your live website!

**Example:** `https://frontend-production-abcd.up.railway.app`

Share this with anyone - they can access from:
- ✅ Phone (iOS/Android)
- ✅ Laptop (Windows/Mac/Linux)
- ✅ Tablet
- ✅ Any browser, anywhere in the world!

---

## 🎯 What You Get

- ✅ **Live Website** - Accessible from anywhere
- ✅ **HTTPS** - Automatic SSL certificate
- ✅ **Free Hosting** - Railway free tier
- ✅ **Auto Deployment** - Updates when you push to GitHub
- ✅ **Custom Domain** - Can add your own domain later

---

## 🔐 Default Admin Access

Once deployed, you can login with:
- **Email:** admin@hiresense.ai
- **Password:** admin123

---

## 📱 Share Your Website

Send your friends this URL:
`https://your-frontend-url.up.railway.app`

They can:
- Register their own accounts
- Practice mock interviews
- Get AI coaching
- Record videos
- View reports

**No installation needed!** Just open the link in any browser!

---

## 🆘 Troubleshooting

### "Service failed to deploy"
- Check the logs in Railway dashboard
- Make sure all environment variables are set correctly

### "Cannot connect to backend"
- Verify `VITE_API_BASE_URL` in frontend points to your backend URL
- Verify `FRONTEND_ORIGIN` in backend points to your frontend URL

### "CORS errors"
- Update `FRONTEND_ORIGIN` in backend to match your actual frontend URL (with https://)

---

## 🎨 Alternative: One-Click Deploy to Render

1. Go to: https://render.com
2. Sign up with GitHub
3. Click "New" → "Blueprint"
4. Select repository: `Karthik-006-lgtm/interviewMCP`
5. Click "Apply" - all 3 services deploy automatically!

**Note:** Render.yaml is already configured in your repo!

---

## 💡 Tips

1. **Bookmark your URLs** - You'll use them often
2. **Share with friends** - They can create accounts and practice too
3. **Check logs** - If something breaks, check Railway/Render logs
4. **Update code** - Push to GitHub, services auto-redeploy

---

## ✅ Success Checklist

- [ ] All 3 services deployed on Railway
- [ ] Frontend URL accessible in browser
- [ ] Can register a new account
- [ ] Can login with admin credentials
- [ ] Can start a mock interview
- [ ] AI coaching works
- [ ] Video recording works

---

## 🎉 You're Live!

**Your platform is now:**
- ✅ Live on the internet
- ✅ Accessible from any device
- ✅ Shareable with friends
- ✅ Auto-updating from GitHub

**GitHub Repo:** https://github.com/Karthik-006-lgtm/interviewMCP

**Need help?** Check Railway/Render documentation or logs!
