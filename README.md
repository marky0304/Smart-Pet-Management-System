Smart Pet Management System
A full-stack pet management platform with an AI multi-agent chat system that intelligently routes user requests to domain-specific agents for health advice, shopping assistance, appointment booking, and more.

智慧宠物管理系统 — 基于多Agent协作的智能宠物管理平台

Highlights
Multi-Agent AI Chat — 6 specialized agents (Health, Mall, Community, Advisor, Archive, General) orchestrated via topology-sorted parallel execution
Intent Routing — LLM-powered multi-intent detection with automatic dependency resolution
Blackboard Pattern — Inter-agent communication via AgentBus for shared context and collaboration
Full-Stack — Spring Boot + Vue 3 + MySQL + Redis + Docker Compose
Tech Stack
Layer	Technology
Backend	Spring Boot 2.7, MyBatis-Plus 3.5, JWT, WebSocket
Frontend	Vue 3, Element Plus, Pinia, ECharts, Vite
Database	MySQL 8.0, Redis 7
AI/LLM	DeepSeek API (OpenAI-compatible), OkHttp
DevOps	Docker Compose, Nginx
Project Structure
smart-pet-system/
├── backend/                 # Spring Boot application
│   ├── src/main/java/com/pet/
│   │   ├── agent/           # Multi-agent AI system
│   │   │   ├── agents/      # HealthAgent, MallAgent, CommunityAgent, etc.
│   │   │   ├── core/        # Orchestrator, AgentBus, CollaborationManager
│   │   │   ├── llm/         # LLM client
│   │   │   └── controller/  # Chat & reminder endpoints
│   │   ├── controller/      # REST API controllers
│   │   ├── service/         # Business logic layer
│   │   └── mapper/          # MyBatis-Plus data access
│   └── pom.xml
├── frontend/                # Vue 3 SPA
│   └── src/
│       ├── views/           # 20+ page components
│       ├── api/             # Axios HTTP clients
│       ├── router/          # Vue Router config
│       └── store/           # Pinia state management
├── database/                # SQL schema + seed data
├── docs/                    # Project documentation (Chinese)
├── docker-compose.yml       # One-command deployment
└── README.md
AI Agent Architecture
User Message
    │
    ▼
┌─────────────────┐
│  IntentRouter    │  LLM-powered multi-intent detection
│  (LLM analysis)  │  Extracts domain + operation + dependencies
└────────┬────────┘
         │
    ┌────▼────┐
    │ Single? │──Yes──▶ Direct single-agent invoke
    └────┬────┘
         │ No
         ▼
┌──────────────────────┐
│ CollaborationManager  │  Topological sort → parallel batches
│                       │  Resolves inter-agent dependencies
└──────────┬───────────┘
           │
     ┌─────▼──────┐
     │  AgentBus   │  Blackboard — agents publish/read shared context
     └─────┬──────┘
           │
    ┌──────▼──────┐
    │  6 Agents   │  Parallel execution (thread pool, 30s timeout)
    │  HEALTH      │
    │  MALL        │  Each: build prompt → call LLM → extract actions
    │  COMMUNITY   │
    │  ADVISOR     │
    │  ARCHIVE     │
    │  GENERAL     │
    └──────┬──────┘
           │
    ┌──────▼──────┐
    │ ConflictResolver│ Detect conflicting outputs, escalate choices
    └──────┬──────┘
           │
    ┌──────▼──────┐
    │ ResultAggregator│ Merge into a unified natural-language reply
    └──────┬──────┘
           │
           ▼
      Final Reply + UI navigation + action executions
Agent Responsibilities
Agent	Domain	Responsibilities
HealthAgent	HEALTH	Health records, symptoms, vaccination schedules, medical advice
MallAgent	MALL	Product search, orders, shopping cart management
CommunityAgent	COMMUNITY	Posts, comments, likes, follows
AdvisorAgent	ADVISOR	Pet care advice, breed recommendations, feeding guidance
ArchiveAgent	ARCHIVE	Historical records, pet files, document retrieval
GeneralAgent	GENERAL	Casual chat, fallback for unclassified queries
Multi-Intent Example
User: "My cat has a skin rash, also find me some hypoallergenic cat food"

IntentRouter detects: [HEALTH:symptom_check, MALL:product_search]
CollaborationManager builds dependency graph: MALL depends on HEALTH (allergy info needed)
HealthAgent runs first → publishes findings to AgentBus
MallAgent reads health context from AgentBus → searches hypoallergenic products
ConflictResolver finds no conflict → ResultAggregator merges output into a natural reply
Dependency Graph
MALL ──depends on──▶ HEALTH
GENERAL ──depends on──▶ PET
HEALTH ──depends on──▶ PET
Agents are scheduled in topological order: independent agents run in parallel batches; dependent agents wait for their dependencies' outputs via the AgentBus blackboard.

Features
Core Modules
Module	Status	Description
User Management	Done	JWT auth, role-based access (Admin/User), registration & login
Pet Management	Done	Full CRUD with breed, medical history, chip tracking
Health Records	Done	Vaccination tracking, symptom logging, weight charts (ECharts)
Smart Reminders	Done	Auto-scheduled vaccine/checkup/deworming reminders
Service Appointments	Done	Browse services, book, status tracking
Pet Shop	Done	Product catalog, cart, orders
Community	Done	Posts, comments, likes, follows
Dashboard	Done	Admin analytics with ECharts visualizations
Address Book	Done	Saved addresses for orders
AI Features
Conversational interface accessible from any page
Automatic intent detection — no manual mode switching
Cross-domain multi-intent handling with dependency resolution
Structured action extraction (form fills, page navigation, confirmations)
Session persistence for contextual conversations
Reminder scheduling from chat
Quick Start
Prerequisites
JDK 8+, Maven 3.6+, Node.js 16+, MySQL 8.0+
Docker Compose (Recommended)
git clone https://github.com/YOUR_USERNAME/smart-pet-system.git
cd smart-pet-system

cp .env.example .env
# Edit .env with your MySQL passwords and LLM API key

docker-compose up -d

# Frontend: http://localhost
# Backend API: http://localhost:8080/api
Manual Setup
1. Database

mysql -u root -p < database/smart_pet_system.sql
mysql -u root -p < database/optimize_database.sql
mysql -u root -p < database/agent_system.sql
2. Backend

cd backend
# Edit src/main/resources/application.yml with DB credentials
mvn clean package -DskipTests
java -jar target/smart-pet-system-1.0.0.jar
3. Frontend

cd frontend
npm install
npm run dev          # → http://localhost:3000
Default Account
Admin: admin / admin123
API Overview
POST   /api/auth/login               # Login
POST   /api/auth/register            # Register
GET    /api/pets                      # List user's pets
POST   /api/pets                     # Add pet
PUT    /api/pets/{id}                # Update pet
DELETE /api/pets/{id}                # Delete pet
GET    /api/health/records/{petId}   # Health records
POST   /api/health/records           # Add health record
GET    /api/appointments             # List appointments
POST   /api/appointments             # Create appointment
GET    /api/products                 # Product catalog
POST   /api/orders                   # Place order
POST   /api/chat/send                # AI chat message
GET    /api/chat/history/{sessionId} # Chat history
GET    /api/reminders                # Upcoming reminders
GET    /api/dashboard/stats          # Admin dashboard stats
All responses follow a unified envelope: { "success": bool, "data": T, "error": string }.

Configuration
Environment Variables
Variable	Purpose	Default
MYSQL_ROOT_PASSWORD	MySQL root password	change-me
MYSQL_PASSWORD	Pet admin DB password	change-me
ANTHROPIC_AUTH_TOKEN	LLM API key	—
ANTHROPIC_BASE_URL	LLM API base URL	https://api.deepseek.com/anthropic
ANTHROPIC_MODEL	LLM model name	DeepSeek-V4-pro
JWT_SECRET	JWT signing secret	— (set in production)
Database Schema
Table	Purpose	Key Relationships
user	User accounts	Parent of pets, orders, posts
pet	Pet profiles	FK → user; parent of health_records
health_record	Medical records	FK → pet
service	Service catalog	—
appointment	Bookings	FK → user, pet, service
product	Shop products	—
order	Purchase orders	FK → user
order_item	Line items	FK → order, product
post	Community posts	FK → user
comment	Post comments	FK → post, user
follow	User follows	FK → user × 2
chat_message	AI chat history	FK → user
reminder	Health reminders	FK → user, pet
License
MIT License — see LICENSE for details.
