# Docker Setup for Restaurant Microservices

## 📦 What's Included

### Services
- **Eureka Service** (Port 8761) - Service Discovery
- **API Gateway** (Port 8080) - Entry point for all requests
- **Auth Service** (Port 8081) - Authentication & Authorization
- **Menu Service** (Port 8082) - Menu management
- **Order Service** (Port 8083) - Order processing
- **Profile Service** (Port 8084) - User profiles
- **Reservation Service** (Port 8085) - Table reservations
- **Table Service** (Port 8086) - Table management

### Infrastructure
- **PostgreSQL** (Port 5432) - Database
- **Kafka** (Port 9092) - Message broker (KRaft mode, no Zookeeper needed)

## 🚀 Quick Start

### Prerequisites
- Docker installed
- Docker Compose installed
- At least 4GB RAM available for Docker

### Start All Services

```bash
# Build and start all services
docker-compose up --build

# Start in detached mode (background)
docker-compose up -d --build
```

### Stop All Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean state)
docker-compose down -v
```

## 🔧 Individual Service Commands

### Build Individual Service
```bash
docker build -t eureka-service ./eureka-service
docker build -t api-gateway ./api-gateway
docker build -t auth-service ./auth-service
# ... and so on
```

### Run Individual Service
```bash
docker run -p 8761:8761 eureka-service
```

## 📊 Access Services

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8081
- **Menu Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Profile Service**: http://localhost:8084
- **Reservation Service**: http://localhost:8085
- **Table Service**: http://localhost:8086
- **PostgreSQL**: localhost:5432 (user: admin, password: admin123, db: restaurant_db)
- **Kafka**: localhost:9092 (KRaft mode)

## 🐛 Troubleshooting

### Check Service Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f eureka-service
docker-compose logs -f api-gateway
```

### Check Running Containers
```bash
docker-compose ps
```

### Restart Specific Service
```bash
docker-compose restart auth-service
```

### Rebuild Specific Service
```bash
docker-compose up -d --build auth-service
```

### Clean Everything
```bash
# Remove all containers, networks, and volumes
docker-compose down -v
docker system prune -a
```

## 🔍 Health Checks

All services have health checks configured. Check status:
```bash
docker-compose ps
```

## 📝 Environment Variables

You can customize environment variables in `docker-compose.yml`:

- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka server address
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` - Eureka server URL

## 🎯 Development Tips

### Local Development (without Docker)
If you want to run services locally but use Dockerized infrastructure:

```bash
# Start only infrastructure
docker-compose up postgres kafka

# Then run services locally with ./mvnw spring-boot:run
```

### Hot Reload
For development, you might want to run services locally instead of in containers for faster iteration.

## 📦 Production Considerations

For production deployment:
1. Change default passwords in `docker-compose.yml`
2. Use Docker secrets for sensitive data
3. Add resource limits to containers
4. Use a proper orchestration platform (Kubernetes)
5. Implement proper logging and monitoring
6. Use health checks and readiness probes
7. Set up proper backup for PostgreSQL

## 🏗️ Architecture

```
┌─────────────────┐
│   API Gateway   │ :8080
└────────┬────────┘
         │
    ┌────┴────┬──────────┬──────────┬──────────┬──────────┬──────────┐
    │         │          │          │          │          │          │
┌───▼──┐  ┌──▼───┐  ┌───▼───┐  ┌───▼───┐  ┌───▼────┐  ┌──▼─────┐  ┌──▼────┐
│ Auth │  │ Menu │  │ Order │  │Profile│  │Reserv. │  │ Table  │  │Eureka │
│:8081 │  │:8082 │  │:8083  │  │:8084  │  │:8085   │  │:8086   │  │:8761  │
└───┬──┘  └──┬───┘  └───┬───┘  └───┬───┘  └───┬────┘  └───┬────┘  └───────┘
    │        │          │          │          │           │
    └────────┴──────────┴──────────┴──────────┴───────────┘
                              │
                    ┌─────────┴──────────┐
                    │                    │
               ┌────▼─────┐         ┌───▼──────┐
               │PostgreSQL│         │  Kafka   │
               │  :5432   │         │  :9092   │
               └──────────┘         │ (KRaft)  │
                                    └──────────┘
```

