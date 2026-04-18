#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
#  DREAMIFY — EC2 SERVER BOOTSTRAP SCRIPT (SSL & Registry Optimized)
# ═══════════════════════════════════════════════════════════════════

set -e

GITHUB_USER="${1:-tusharyadav235}"
REPO_NAME="${2:-dreamify}"
APP_DIR="$HOME/$REPO_NAME"

echo "🚀 Starting Dreamify Bootstrap..."

# ── 1. SYSTEM UPDATE ─────────────────────────────────────────────
sudo apt-get update -y -q && sudo apt-get upgrade -y -q
sudo apt-get install -y -q ca-certificates curl gnupg lsb-release git unzip htop

# ── 2. INSTALL DOCKER ────────────────────────────────────────────
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update -y -q && sudo apt-get install -y -q docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker "$USER"

# ── 3. CLONE REPO ────────────────────────────────────────────────
if [ -d "$APP_DIR" ]; then
  cd "$APP_DIR" && git pull
else
  git clone "https://github.com/$GITHUB_USER/$REPO_NAME.git" "$APP_DIR"
fi
cd "$APP_DIR"

# ── 4. CREATE .env ───────────────────────────────────────────────
if [ ! -f "$APP_DIR/.env" ]; then
  cp "$APP_DIR/.env.example" "$APP_DIR/.env"
  DB_PASS=$(openssl rand -base64 12)
  sed -i "s/your_password/$DB_PASS/g" "$APP_DIR/.env"
  echo "✅ .env created"
fi

# ── 5. GENERATE DEPLOY SSH KEY ───────────────────────────────────
KEY_FILE="$HOME/.ssh/dreamify_deploy"
if [ ! -f "$KEY_FILE" ]; then
  ssh-keygen -t ed25519 -C "dreamify-deploy" -f "$KEY_FILE" -N ""
  cat "${KEY_FILE}.pub" >> "$HOME/.ssh/authorized_keys"
  echo "📋 PUBLIC KEY CREATED. ADD TO GITHUB SECRETS AS 'EC2_SSH_KEY':"
  cat "${KEY_FILE}.pub"
fi

# ── 6. SSL DIRECTORY PREP ────────────────────────────────────────
# Prevents Nginx from failing if certs aren't there yet
sudo mkdir -p /etc/letsencrypt/live/dreamify.info
sudo chmod -R 755 /etc/letsencrypt

# ── 7. REGISTRY LOGIN & START ────────────────────────────────────
echo "🔐 Log in to your Docker Registry (Docker Hub/GHCR):"
docker login

cd "$APP_DIR"
# Pull pre-built images from registry instead of building on EC2
docker compose pull
docker compose up -d

echo "🎉 Bootstrap Complete! Site: https://$(curl -s ifconfig.me)"
