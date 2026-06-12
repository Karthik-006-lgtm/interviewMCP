# 🌐 Share Your Website Right Now!

Your services are already running! Here's how to let friends access your website immediately.

## 🚀 Quick Access (No Docker/Cloud needed!)

### Your Local IP Address
**10.1.0.228**

### Share These URLs With Friends (Same WiFi Network):

- **Main Website:** http://10.1.0.228:5173
- **API Backend:** http://10.1.0.228:8080
- **AI Service:** http://10.1.0.228:8000

---

## ✅ Step-by-Step Instructions

### 1. Keep Your Services Running

Your services are already running on:
- ✅ Frontend: Port 5173
- ✅ Backend: Port 8080  
- ✅ AI Service: Port 8000

**DO NOT close the terminal windows!**

### 2. Configure Windows Firewall

Allow incoming connections so friends can access:

```powershell
# Run PowerShell as Administrator
netsh advfirewall firewall add rule name="HireSense Frontend" dir=in action=allow protocol=TCP localport=5173
netsh advfirewall firewall add rule name="HireSense Backend" dir=in action=allow protocol=TCP localport=8080
netsh advfirewall firewall add rule name="HireSense AI" dir=in action=allow protocol=TCP localport=8000
```

Or manually:
1. Open "Windows Defender Firewall"
2. Click "Advanced settings"
3. Click "Inbound Rules" → "New Rule"
4. Select "Port" → Next
5. Enter ports: 5173, 8080, 8000
6. Allow the connection
7. Apply to all profiles
8. Name it "HireSense"

### 3. Update Frontend API URL

Update frontend to use your IP address:

**Edit: `frontend/.env`**
```env
VITE_API_BASE_URL=http://10.1.0.228:8080
```

**Restart frontend:**
```powershell
# Stop current frontend (Ctrl+C in terminal)
# Then restart:
cd frontend
npm run dev
```

### 4. Update Backend CORS Settings

**Edit: `backend/.env`**
```env
FRONTEND_ORIGIN=http://10.1.0.228:5173
```

**Restart backend:**
```powershell
# Stop current backend (Ctrl+C in terminal)
# Then restart:
cd backend
mvn spring-boot:run
```

### 5. Share With Friends

Send them this URL:
**http://10.1.0.228:5173**

They can:
- ✅ Access from their laptop
- ✅ Access from their phone
- ✅ Register accounts
- ✅ Practice interviews
- ✅ Use all features

**Requirements:**
- They must be on the **same WiFi network** as you
- Your computer must stay on
- Services must keep running

---

## 🌍 For Internet Access (Friends Not On Same WiFi)

### Option A: Use Ngrok (Quick & Easy)

Ngrok creates a public URL for your local services.

1. **Download Ngrok:**
   - Go to: https://ngrok.com/download
   - Download for Windows
   - Extract ngrok.exe

2. **Sign up for free account:**
   - https://dashboard.ngrok.com/signup
   - Get your authtoken

3. **Configure authtoken:**
   ```powershell
   ngrok config add-authtoken YOUR_TOKEN_HERE
   ```

4. **Expose frontend:**
   ```powershell
   ngrok http 5173
   ```

5. **You'll get a public URL like:**
   ```
   https://abc123.ngrok.io
   ```

6. **Share this URL** - works from anywhere!

**Note:** Free ngrok URLs change when you restart. For permanent URLs, use Railway/Render.

### Option B: Railway/Render (Permanent Solution)

For a permanent website accessible from anywhere:

1. **Railway (Recommended):**
   - Go to: https://railway.app
   - Follow: LIVE_DEPLOYMENT.md
   - Get permanent URL in 10 minutes

2. **Render:**
   - Go to: https://render.com
   - Follow: DEPLOYMENT_GUIDE.md
   - Auto-deploy from GitHub

---

## 📱 Testing Access

### From Your Friend's Phone (Same WiFi):

1. Connect to same WiFi network
2. Open browser
3. Go to: http://10.1.0.228:5173
4. Should see login page!

### From Your Friend's Laptop (Same WiFi):

1. Connect to same WiFi network
2. Open any browser
3. Go to: http://10.1.0.228:5173
4. Register and start using!

---

## ⚠️ Important Notes

### Same WiFi Access:
- ✅ Free
- ✅ Instant
- ✅ No configuration needed (after firewall)
- ❌ Only works on same network
- ❌ Your computer must stay on

### Ngrok Access:
- ✅ Works from anywhere
- ✅ Quick setup
- ✅ Free tier available
- ❌ URL changes on restart (free plan)
- ❌ Your computer must stay on

### Railway/Render:
- ✅ Works from anywhere
- ✅ Permanent URL
- ✅ Always online
- ✅ Free tier available
- ✅ Your computer can be off
- ❌ Takes 10-15 minutes to setup

---

## 🎯 Recommended Approach

**For Quick Demo (Same Room/Building):**
1. Use local IP: http://10.1.0.228:5173
2. Friends connect to same WiFi
3. Demo the platform!

**For Remote Friends:**
1. Use ngrok for quick demo
2. Or deploy to Railway for permanent solution

**For Production Use:**
1. Deploy to Railway or Render
2. Get permanent URL
3. Share with everyone!

---

## 🔐 Admin Credentials

Share these with friends to demo admin features:
- **Email:** admin@hiresense.ai
- **Password:** admin123

Or let them register their own accounts!

---

## ✅ Quick Checklist

- [ ] Services running (Frontend, Backend, AI)
- [ ] Firewall rules added
- [ ] Frontend .env updated with IP address
- [ ] Backend .env updated with IP address
- [ ] Services restarted
- [ ] Friends on same WiFi
- [ ] Share URL: http://10.1.0.228:5173

---

## 🆘 Troubleshooting

**Friends can't access:**
- Check Windows Firewall rules
- Verify services are running
- Confirm same WiFi network
- Try pinging: `ping 10.1.0.228`

**CORS errors:**
- Update FRONTEND_ORIGIN in backend .env
- Restart backend service

**Can't connect to API:**
- Update VITE_API_BASE_URL in frontend .env
- Restart frontend service

---

## 🎉 Success!

Your website is now accessible on your local network!

**Local Access:** http://10.1.0.228:5173

**For Internet Access:** Use Railway/Render or ngrok

**GitHub Repo:** https://github.com/Karthik-006-lgtm/interviewMCP

---

Choose your deployment path and enjoy your live platform! 🚀
