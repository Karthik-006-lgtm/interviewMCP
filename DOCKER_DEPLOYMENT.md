# 🐳 Docker Deployment Guide - Windows

Complete guide to deploy HireSense using Docker on Windows.

## Step 1: Install Docker Desktop

### Download and Install

1. **Download Docker Desktop for Windows:**
   - Go to: https://www.docker.com/products/docker-desktop/
   - Click "Download for Windows"
   - Or direct link: https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe

2. **Run the installer:**
   - Double-click `Docker Desktop Installer.exe`
   - Follow the installation wizard
   - Enable WSL 2 when prompted (recommended)
   - Wait for installation to complete

3. **Restart your computer** (required)

4. **Start Docker Desktop:**
   - Open Docker Desktop from Start Menu
   - Wait for Docker to start (icon in system tray will stop animating)
   - You may need to accept the service agreement

5. **Verify installation:**
   ```powershell
   docker --version
   docker-compose --version
   ```
   You should see version numbers.

---

## Step 2: Prepare for Deployment

### Update Environment Variables

Before deploying, you need to set the correct URLs for cross-service communication.

1. **Backend .env** (`backend/.env`):
   ```env
   SERVER_PORT=8080
   DB_URL=jdbc:h2:mem:testdb
   DB_USERNAME=sa
   DB_PASSWORD=
   JWT_SECRET=ZmFrZV9kZXZfb25seV9zZWNyZXRfZmFrZV9kZXZfb25seV9zZWNyZXRfZmFrZQ==
   JWT_EXPIRATION_MS=86400000
   AI_SERVICE_BASE_URL=http://ai-service:8000
   RESUME_STORAGE_DIR=/app/uploads/resumes
   AUDIO_STORAGE_DIR=/app/uploads/audio
   RECORDING_STORAGE_DIR=/app/uploads/recordings
   FRONTEND_ORIGIN=http://localhost:5173
   APP_BOOTSTRAP_ADMIN_EMAIL=admin@hiresense.ai
   APP_BOOTSTRAP_ADMIN_PASSWORD=admin123
   APP_BOOTSTRAP_ADMIN_FULL_NAME=Platform Admin
   ```

2. **Frontend .env** (`frontend/.env`):
   ```env
   VITE_API_BASE_URL=http://localhost:8080
   ```

---

## Step 3: Deploy with Docker Compose

### Option A: Simple Deployment (Without Docker)

Since Docker setup can take time, you can continue using the current working setup:

1. **Keep all 3 services running as they are:**
   - Backend on port 8080
   - AI Service on port 8000
   - Frontend on port 5173

2. **Share your website locally:**
   - Get your local IP: `ipconfig` (look for IPv4 Address)
   - Share with friends on same WiFi: `http://YOUR-IP:5173`
   - Example: `http://192.168.1.100:5173`

### Option B: Docker Deployment (After Installing Docker)

1. **Open PowerShell in project directory:**
   ```powershell
   cd "C:\Users\karthik\Downloads\MCP karthik"
   ```

2. **Stop any running services:**
   ```powershell
   # Stop current services if running
   # (Services will be managed by Docker)
   ```

3. **Build and start all services:**
   ```powershell
   docker-compose up -d --build
   ```

   This command will:
   - Build Docker images for all 3 services
   - Start containers in detached mode
   - Set up networking between services

4. **Wait for services to start** (first time takes 5-10 minutes):
   ```powershell
   docker-compose logs -f
   ```
   Press `Ctrl+C` to exit logs when services are ready.

5. **Check service status:**
   ```powershell
   docker-compose ps
   ```

   You should see:
   ```
   NAME                STATUS    PORTS
   backend             Up        0.0.0.0:8080->8080/tcp
   ai-service          Up        0.0.0.0:8000->8000/tcp
   frontend            Up        0.0.0.0:5173->80/tcp
   ```

6. **Access your application:**
   - Frontend: http://localhost:5173
   - Backend: http://localhost:8080
   - AI Service: http://localhost:8000

---

## Step 4: Make It Accessible from Other Devices

### Local Network Access (Same WiFi)

1. **Find your local IP address:**
   ```powershell
   ipconfig
   ```
   Look for "IPv4 Address" (usually like `192.168.1.x`)

2. **Share this URL with friends on same network:**
   ```
   http://YOUR-IP-ADDRESS:5173
   ```
   Example: `http://192.168.1.100:5173`

3. **Make sure Windows Firewall allows incoming connections:**
   - Open Windows Defender Firewall
   - Click "Advanced settings"
   - Add inbound rule for ports 5173, 8080, 8000

### Internet Access (Port Forwarding - Advanced)

To make it accessible from anywhere:

1. **Configure port forwarding on your router:**
   - Login to your router (usually `192.168.1.1` or `192.168.0.1`)
   - Find "Port Forwarding" section
   - Forward port 5173 to your computer's local IP
   - Forward port 8080 to your computer's local IP
   - Forward port 8000 to your computer's local IP

2. **Get your public IP:**
   - Visit: https://whatismyipaddress.com/
   - Note your IPv4 address

3. **Share with friends:**
   ```
   http://YOUR-PUBLIC-IP:5173
   ```

⚠️ **Security Warning:** 
- Port forwarding exposes your services to the internet
- Only do this temporarily
- For permanent hosting, use Railway/Render instead

---

## Docker Management Commands

### View Running Containers
```powershell
docker-compose ps
```

### View Logs
```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f ai-service
docker-compose logs -f frontend
```

### Stop Services
```powershell
docker-compose down
```

### Restart Services
```powershell
docker-compose restart
```

### Rebuild After Code Changes
```powershell
docker-compose up -d --build
```

### Remove Everything (Clean Slate)
```powershell
docker-compose down -v
docker-compose up -d --build
```

---

## Troubleshooting

### "Docker daemon is not running"
- Open Docker Desktop
- Wait for it to fully start
- Look for green icon in system tray

### "Port already in use"
- Stop your currently running services (backend, frontend, ai-service)
- Or change ports in docker-compose.yml

### "Cannot connect to backend"
- Check if all containers are running: `docker-compose ps`
- Check logs: `docker-compose logs backend`
- Verify environment variables are correct

### Services not starting
- Check logs: `docker-compose logs -f`
- Ensure Docker Desktop is running
- Ensure WSL 2 is enabled

### Out of memory
- Increase Docker Desktop memory:
  - Settings → Resources → Advanced
  - Increase memory to 4GB or more

---

## Recommended Approach

**For Quick Access (Recommended):**
1. **Keep current setup** (services already running)
2. **Find your local IP:** `ipconfig`
3. **Share:** `http://YOUR-IP:5173` with friends on same WiFi

**For Production Deployment:**
1. **Use Railway or Render** (from previous guides)
2. **Get permanent URL** accessible from anywhere
3. **No router configuration needed**

---

## Admin Credentials

- **Email:** admin@hiresense.ai
- **Password:** admin123

---

## Next Steps

Choose your path:

**Path A: Quick Local Share (No Docker needed)**
1. Services are already running
2. Get your IP with `ipconfig`
3. Share `http://YOUR-IP:5173`
4. Friends on same WiFi can access

**Path B: Docker (Requires installation)**
1. Install Docker Desktop
2. Run `docker-compose up -d --build`
3. Access at http://localhost:5173
4. Use port forwarding for internet access

**Path C: Cloud Hosting (Best for internet access)**
1. Use Railway (https://railway.app)
2. Follow LIVE_DEPLOYMENT.md
3. Get permanent URL accessible anywhere

---

## Support

- **Docker Issues:** Check Docker Desktop logs
- **Deployment Issues:** Check `docker-compose logs`
- **Network Issues:** Check Windows Firewall settings

---

**Your platform is ready to deploy! Choose the path that works best for you.** 🚀
