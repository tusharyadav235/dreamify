# Dreamify — Full Stack Deployment Guide

## Project Structure

```
dreamify/
├── frontend/               ← Main website (HTML + Nginx)
│   ├── index.html
│   ├── Dockerfile
│   └── nginx.conf
├── admin-panel/            ← Admin dashboard (HTML + Nginx)
│   ├── index.html
│   ├── Dockerfile
│   └── nginx.conf
├── backend/                ← REST API (Java + Spring Boot)
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── init.sql                ← MySQL schema + seed data
├── docker-compose.yml      ← Orchestrates all services
├── .env.example            ← Environment variables template
└── README.md
```

## Ports

| Service      | Port | URL                          |
|--------------|------|------------------------------|
| Frontend     | 80   | http://your-ip               |
| Admin Panel  | 8081 | http://your-ip:8081          |
| Backend API  | 8080 | http://your-ip:8080/api      |
| MySQL        | 3306 | internal only (no public)    |

## Admin Login

```
Username: admin
Password: dreamify2025
```

---

## Local Development

### Prerequisites
- Docker Desktop installed
- Git

### Steps

```bash
# 1. Clone / copy the project
cd dreamify

# 2. Create your .env file
cp .env.example .env
# Edit .env and set strong passwords

# 3. Build and start everything
docker compose up -d --build

# 4. Wait ~60 seconds for MySQL + Spring Boot to start
docker compose logs -f backend   # watch until "Started DreamifyApplication"

# 5. Open in browser
#    Website   → http://localhost
#    Admin     → http://localhost:8081
#    API       → http://localhost:8080/api/testimonials
```

---

## AWS EC2 Deployment (Step by Step)

### Step 1 — Launch EC2 Instance

1. Go to AWS Console → EC2 → Launch Instance
2. Choose **Ubuntu Server 22.04 LTS (HVM)**
3. Instance type: **t2.medium** (recommended) or t2.micro (free tier, slower)
4. Key pair: Create or select an existing `.pem` key
5. Security Group — open these inbound ports:

   | Port | Protocol | Source    | Purpose        |
   |------|----------|-----------|----------------|
   | 22   | TCP      | Your IP   | SSH access     |
   | 80   | TCP      | 0.0.0.0/0 | Website        |
   | 8080 | TCP      | 0.0.0.0/0 | Backend API    |
   | 8081 | TCP      | 0.0.0.0/0 | Admin Panel    |

6. Storage: **20 GB** minimum
7. Launch the instance

---

### Step 2 — Connect to EC2

```bash
# On your local machine
chmod 400 your-key.pem
ssh -i your-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

---

### Step 3 — Install Docker on EC2

```bash
# Update packages
sudo apt update && sudo apt upgrade -y

# Install required packages
sudo apt install -y ca-certificates curl gnupg lsb-release git

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo "deb [arch=$(dpkg --print-architecture) \
  signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine + Compose
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Add ubuntu user to docker group (no sudo needed)
sudo usermod -aG docker ubuntu

# Apply group change
newgrp docker

# Verify
docker --version
docker compose version
```

---

### Step 4 — Upload Project to EC2

**Option A — Using SCP (upload from your machine)**
```bash
# On your LOCAL machine — zip and upload
zip -r dreamify.zip dreamify/
scp -i your-key.pem dreamify.zip ubuntu@YOUR_EC2_IP:~/
ssh -i your-key.pem ubuntu@YOUR_EC2_IP
unzip dreamify.zip
cd dreamify
```

**Option B — Using Git (if you push to GitHub)**
```bash
# On EC2
git clone https://github.com/YOUR_USERNAME/dreamify.git
cd dreamify
```

---

### Step 5 — Configure Environment

```bash
# On EC2 inside /dreamify
cp .env.example .env
nano .env
```

Edit `.env`:
```
DB_ROOT_PASSWORD=YourStrongRootPassword123!
DB_NAME=dreamify
DB_USER=dreamify_user
DB_PASSWORD=YourStrongDBPassword123!
APP_DOMAIN=http://YOUR_EC2_PUBLIC_IP
```

Save with `Ctrl+X → Y → Enter`

---

### Step 6 — Update API URL in Frontend & Admin

The HTML files have `const API = 'http://localhost:8080/api'`.
You need to update this to your EC2 IP:

```bash
# Replace localhost with your EC2 IP in frontend
sed -i "s|http://localhost:8080|http://YOUR_EC2_IP:8080|g" frontend/index.html

# Replace in admin panel
sed -i "s|http://localhost:8080|http://YOUR_EC2_IP:8080|g" admin-panel/index.html
```

**Example** (replace `54.123.45.67` with your actual IP):
```bash
sed -i "s|http://localhost:8080|http://54.123.45.67:8080|g" frontend/index.html
sed -i "s|http://localhost:8080|http://54.123.45.67:8080|g" admin-panel/index.html
```

---

### Step 7 — Build and Start All Containers

```bash
# Build images and start all services
docker compose up -d --build

# Watch the logs (wait until Spring Boot says "Started DreamifyApplication")
docker compose logs -f backend
# Press Ctrl+C to exit logs when you see "Started"
```

**Expected startup time:** ~2-3 minutes first time (Maven downloads dependencies).

---

### Step 8 — Verify Everything is Running

```bash
# Check all containers are "Up"
docker compose ps

# Test backend API
curl http://localhost:8080/api/testimonials

# Test frontend (should return HTML)
curl -s http://localhost:80 | head -5
```

Open in your browser:
- **Website:** `http://YOUR_EC2_IP`
- **Admin Panel:** `http://YOUR_EC2_IP:8081`
- **API:** `http://YOUR_EC2_IP:8080/api/testimonials`

---

### Step 9 — Set Up Auto-Restart on Reboot

```bash
# Enable Docker to start on boot
sudo systemctl enable docker

# The containers already have restart: unless-stopped
# so they will auto-restart after reboot
```

---

## Useful Commands

```bash
# View running containers
docker compose ps

# View logs for a specific service
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql

# Restart a single service
docker compose restart backend

# Stop everything
docker compose down

# Stop and remove all data (CAREFUL - deletes database!)
docker compose down -v

# Rebuild after code changes
docker compose up -d --build frontend
docker compose up -d --build backend
docker compose up -d --build admin

# Enter MySQL shell
docker exec -it dreamify-mysql mysql -u dreamify_user -p dreamify

# Enter backend container for debugging
docker exec -it dreamify-backend sh
```

---

## API Reference

### Enquiries

| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| GET    | /api/enquiries                  | Get all enquiries        |
| GET    | /api/enquiries/{id}             | Get single enquiry       |
| POST   | /api/enquiries                  | Submit new enquiry       |
| PATCH  | /api/enquiries/{id}/status      | Update status            |
| DELETE | /api/enquiries/{id}             | Delete enquiry           |
| GET    | /api/enquiries/stats            | Get counts by status     |

### Testimonials

| Method | Endpoint                | Description              |
|--------|-------------------------|--------------------------|
| GET    | /api/testimonials       | Get all testimonials     |
| POST   | /api/testimonials       | Add testimonial (admin)  |
| DELETE | /api/testimonials/{id}  | Delete testimonial       |

### POST /api/enquiries — Request Body
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul@company.com",
  "phone": "+91 98765 43210",
  "company": "My Startup",
  "service": "Web Development",
  "budget": "₹50K–₹2L",
  "description": "We need a website with..."
}
```

### POST /api/testimonials — Request Body
```json
{
  "clientName": "Amit Singh",
  "clientRole": "CEO, TechStart",
  "content": "Amazing work by the Dreamify team!",
  "rating": 5
}
```

---

## Optional: Add a Domain Name

1. Buy a domain (GoDaddy, Namecheap, etc.)
2. Point an **A record** to your EC2 Elastic IP
3. Install Certbot for free HTTPS:

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

4. Update nginx.conf in both frontend and admin to handle SSL.

---

## Contact

- **Phone:** +91 7451947631
- **Email:** hellodreamify.info@gmail.com
- **Address:** Meerut, Uttar Pradesh, India
