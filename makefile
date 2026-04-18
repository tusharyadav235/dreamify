# ═══════════════════════════════════════════════════════════════════
#  DREAMIFY — DEVELOPER MAKEFILE
#  Shortcuts for common dev tasks.
#
#  Usage:
#    make help          → Show all commands
#    make dev           → Start all services locally
#    make test          → Run backend tests
#    make build         → Build all Docker images
#    make logs          → Tail all logs
#    make clean         → Stop + remove containers
# ═══════════════════════════════════════════════════════════════════

.PHONY: help dev build test logs clean restart status db-shell backend-shell rollback

# ── COLOURS ─────────────────────────────────────────────────────
CYAN  := \033[0;36m
GREEN := \033[0;32m
RESET := \033[0m

# ── HELP ────────────────────────────────────────────────────────
help:
	@echo ""
	@echo "$(CYAN)╔══════════════════════════════════════════╗$(RESET)"
	@echo "$(CYAN)║       Dreamify Developer Commands        ║$(RESET)"
	@echo "$(CYAN)╚══════════════════════════════════════════╝$(RESET)"
	@echo ""
	@echo "$(GREEN)Setup & Run:$(RESET)"
	@echo "  make setup        Copy .env.example → .env"
	@echo "  make dev          Build + start all services"
	@echo "  make start        Start without rebuilding"
	@echo "  make stop         Stop all services"
	@echo "  make restart      Restart all services"
	@echo "  make clean        Stop + remove containers + volumes"
	@echo ""
	@echo "$(GREEN)Development:$(RESET)"
	@echo "  make test         Run backend unit tests"
	@echo "  make build        Build all Docker images"
	@echo "  make logs         Tail all container logs"
	@echo "  make logs-back    Tail backend logs only"
	@echo "  make logs-front   Tail frontend logs only"
	@echo "  make status       Show container health"
	@echo ""
	@echo "$(GREEN)Database:$(RESET)"
	@echo "  make db-shell     Open MySQL shell"
	@echo "  make db-reset     ⚠️  Drop and recreate database"
	@echo ""
	@echo "$(GREEN)Debugging:$(RESET)"
	@echo "  make backend-shell  Enter backend container"
	@echo "  make smoke-test     Quick API health checks"
	@echo ""

# ── SETUP ───────────────────────────────────────────────────────
setup:
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		echo "$(GREEN)✅ .env created — fill in your values$(RESET)"; \
	else \
		echo ".env already exists"; \
	fi

# ── RUN ─────────────────────────────────────────────────────────
dev: setup
	@echo "$(CYAN)🚀 Starting Dreamify (build + run)...$(RESET)"
	docker compose up -d --build
	@echo ""
	@$(MAKE) status
	@echo ""
	@echo "$(GREEN)✅ Running!$(RESET)"
	@echo "   Website    → http://localhost"
	@echo "   Admin      → http://localhost:8081"
	@echo "   API        → http://localhost:8080/api/testimonials"

start:
	@echo "$(CYAN)▶️  Starting services...$(RESET)"
	docker compose up -d

stop:
	@echo "$(CYAN)⏹️  Stopping services...$(RESET)"
	docker compose stop

restart:
	@echo "$(CYAN)🔄 Restarting services...$(RESET)"
	docker compose restart

clean:
	@echo "$(CYAN)🧹 Cleaning up...$(RESET)"
	docker compose down -v --remove-orphans
	docker image prune -f

# ── BUILD ────────────────────────────────────────────────────────
build:
	@echo "$(CYAN)🏗️  Building all images...$(RESET)"
	docker compose build --no-cache

build-back:
	docker compose build --no-cache backend

build-front:
	docker compose build --no-cache frontend

build-admin:
	docker compose build --no-cache admin

# ── TEST ─────────────────────────────────────────────────────────
test:
	@echo "$(CYAN)🧪 Running backend tests...$(RESET)"
	cd backend && mvn test --batch-mode

test-watch:
	cd backend && mvn test --batch-mode -Dsurefire.useFile=false

# ── LOGS ─────────────────────────────────────────────────────────
logs:
	docker compose logs -f

logs-back:
	docker compose logs -f backend

logs-front:
	docker compose logs -f frontend

logs-admin:
	docker compose logs -f admin

logs-db:
	docker compose logs -f mysql

# ── STATUS ───────────────────────────────────────────────────────
status:
	@echo "$(CYAN)📊 Container Status:$(RESET)"
	docker compose ps

# ── DATABASE ─────────────────────────────────────────────────────
db-shell:
	@echo "$(CYAN)🗄️  Opening MySQL shell...$(RESET)"
	@DB_PASS=$$(grep DB_PASSWORD .env | cut -d '=' -f2); \
	DB_USER=$$(grep DB_USER .env | cut -d '=' -f2); \
	docker exec -it dreamify-mysql mysql -u $$DB_USER -p$$DB_PASS dreamify

db-reset:
	@echo "⚠️  This will DROP and recreate the database!"
	@read -p "Are you sure? (yes/no): " CONFIRM; \
	if [ "$$CONFIRM" = "yes" ]; then \
		docker compose down; \
		docker volume rm dreamify_mysql_data 2>/dev/null || true; \
		docker compose up -d; \
		echo "$(GREEN)✅ Database reset complete$(RESET)"; \
	else \
		echo "Aborted."; \
	fi

# ── DEBUG ─────────────────────────────────────────────────────────
backend-shell:
	docker exec -it dreamify-backend sh

frontend-shell:
	docker exec -it dreamify-frontend sh

# ── SMOKE TEST ───────────────────────────────────────────────────
smoke-test:
	@echo "$(CYAN)🩺 Running smoke tests...$(RESET)"
	@echo -n "  Backend API (testimonials): "
	@STATUS=$$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/testimonials); \
	if [ "$$STATUS" = "200" ]; then echo "$(GREEN)✅ OK$(RESET)"; \
	else echo "❌ FAILED (HTTP $$STATUS)"; fi

	@echo -n "  Backend API (enquiries): "
	@STATUS=$$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/enquiries); \
	if [ "$$STATUS" = "200" ]; then echo "$(GREEN)✅ OK$(RESET)"; \
	else echo "❌ FAILED (HTTP $$STATUS)"; fi

	@echo -n "  Frontend (port 80): "
	@STATUS=$$(curl -s -o /dev/null -w "%{http_code}" http://localhost:80); \
	if [ "$$STATUS" = "200" ]; then echo "$(GREEN)✅ OK$(RESET)"; \
	else echo "❌ FAILED (HTTP $$STATUS)"; fi

	@echo -n "  Admin Panel (port 8081): "
	@STATUS=$$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081); \
	if [ "$$STATUS" = "200" ]; then echo "$(GREEN)✅ OK$(RESET)"; \
	else echo "❌ FAILED (HTTP $$STATUS)"; fi

