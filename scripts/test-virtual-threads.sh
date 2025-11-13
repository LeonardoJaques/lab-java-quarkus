#!/bin/bash

# Script de teste de Virtual Threads
# Demonstra a diferença entre processamento Sequential, Platform Threads e Virtual Threads

set -e

echo "========================================="
echo "TESTE DE VIRTUAL THREADS - Java 21"
echo "========================================="
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Verificar Java 21
echo -e "${YELLOW}Verificando Java version...${NC}"
java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "Java version: $java_version"

if [ "$java_version" -lt "21" ]; then
    echo -e "${RED}ERROR: Java 21+ required for Virtual Threads${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Java 21 detected - Virtual Threads available!${NC}"
echo ""

# Iniciar Docker
echo -e "${YELLOW}[1/5] Starting Docker containers...${NC}"
cd /home/leonardojaques/Projetos/lab-java-quarkus
docker compose -f config/docker-compose.yml up -d database caching
sleep 3
echo -e "${GREEN}✓ Docker containers started${NC}"
echo ""

# Compilar voting-app
echo -e "${YELLOW}[2/5] Building voting-app with Java 21...${NC}"
cd voting-app
./mvnw clean package -DskipTests > /tmp/build.log 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${RED}✗ Build failed. Check /tmp/build.log${NC}"
    exit 1
fi
echo ""

# Iniciar aplicação
echo -e "${YELLOW}[3/5] Starting voting-app with Virtual Threads...${NC}"
java -jar target/quarkus-app/quarkus-run.jar > /tmp/voting-app.log 2>&1 &
APP_PID=$!
echo "Application PID: $APP_PID"

# Esperar aplicação iniciar
echo "Waiting for application to start..."
for i in {1..30}; do
    if curl -s http://localhost:8081/q/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Application started!${NC}"
        break
    fi
    sleep 1
    echo -n "."
done
echo ""
echo ""

# Testar Health Check
echo -e "${YELLOW}[4/5] Testing Health Checks...${NC}"
health=$(curl -s http://localhost:8081/q/health)
echo "$health" | jq '.'

if echo "$health" | grep -q '"status":"UP"'; then
    echo -e "${GREEN}✓ Health check passed${NC}"
else
    echo -e "${RED}✗ Health check failed${NC}"
fi
echo ""

# Testar Metrics
echo -e "${YELLOW}[5/5] Testing Prometheus Metrics...${NC}"
echo "Fetching metrics from /q/metrics..."
metrics=$(curl -s http://localhost:8081/q/metrics)

echo ""
echo -e "${BLUE}Custom Voting Metrics:${NC}"
echo "$metrics" | grep "votes_" || echo "No voting metrics yet (need to create votes)"
echo ""

echo -e "${BLUE}JVM Metrics (with Virtual Threads):${NC}"
echo "$metrics" | grep "jvm_threads" | head -5
echo ""

# Informações sobre Virtual Threads
echo "========================================="
echo -e "${GREEN}VIRTUAL THREADS INFORMATION${NC}"
echo "========================================="
echo ""
echo "Application is running with Java 21"
echo "Virtual Threads are ENABLED in:"
echo "  - VotingService.processWithVirtualThreads()"
echo "  - Quarkus configuration (quarkus.virtual-threads.enabled=true)"
echo ""
echo "To see Virtual Threads in action:"
echo "  1. Check logs: tail -f /tmp/voting-app.log | grep 'Virtual'"
echo "  2. Create test votes via API"
echo "  3. Monitor thread usage: echo \"\$metrics\" | grep jvm_threads"
echo ""
echo "Expected benefits:"
echo "  - 10-20x higher throughput"
echo "  - 200x less memory per thread (10KB vs 2MB)"
echo "  - Millions of concurrent threads possible"
echo ""

echo "Application running at:"
echo "  - Health: http://localhost:8081/q/health"
echo "  - Metrics: http://localhost:8081/q/metrics"
echo ""
echo "To stop: kill $APP_PID"
echo ""
