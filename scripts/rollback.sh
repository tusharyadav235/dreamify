#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
#  DREAMIFY — ROLLBACK SCRIPT (Docker Hub Optimized)
# ═══════════════════════════════════════════════════════════════════

set -e

# Configuration - Updated to match your Docker Hub
REGISTRY="docker.io"
DOCKER_USER="tusharyadaav"
SERVICE="${1:-backend}"
TAG="${2:-}"
APP_DIR="$HOME/dreamify"

echo "🔄 Initiating Rollback for Dreamify..."

# 1. Ask for tag if not provided
if [ -z "$TAG" ]; then
  echo "⚠️  No tag specified. Looking for local images..."
  docker images | grep "dreamify" || true
  echo ""
  read -p "Enter the tag/SHA to roll back to: " TAG
fi

cd "$APP_DIR"

rollback_service() {
  local svc="$1"
  local image="${DOCKER_USER}/dreamify-${svc}:${TAG}"

  echo "🚀 Reverting $svc to image: $image"

  # 2. Ensure image exists (Pull if missing)
  if ! docker image inspect "$image" > /dev/null 2>&1; then
    echo "📥 Image not found locally. Attempting to pull from Docker Hub..."
    docker pull "$image" || {
      echo "❌ Error: Could not find image $image on Docker Hub."
      exit 1
    }
  fi

  # 3. Update the specific service
  # We use a temporary environment variable to override the image if needed,
  # but since we use 'latest' in compose, the best way is to tag it back.
  docker tag "$image" "${DOCKER_USER}/dreamify-${svc}:latest"
  docker compose up -d --no-deps "$svc"

  echo "✅ $svc successfully rolled back to $TAG"
}

# 4. Logic to handle "all" or specific service
if [ "$SERVICE" = "all" ]; then
  for s in backend frontend admin; do
    rollback_service "$s"
  done
else
  rollback_service "$SERVICE"
fi

# 5. Verify & Cleanup
echo -e "\n📊 Current status:"
docker compose ps
echo -e "\n✅ Rollback verified. Check site at: https://dreamify.info"
