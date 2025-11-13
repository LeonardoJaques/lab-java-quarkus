#!/bin/bash

# Quick service starter using Quarkus dev mode

echo "Quick Start - Services for Testing"
echo ""

# Kill existing processes
echo "Cleaning up..."
pkill -f "quarkus:dev" 2>/dev/null || true
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:8081 | xargs kill -9 2>/dev/null || true
lsof -ti:8082 | xargs kill -9 2>/dev/null || true
sleep 2

# Start Docker
echo "Starting Docker containers..."
cd /home/leonardojaques/Projetos/lab-java-quarkus
docker compose up -d database caching 2>/dev/null
sleep 3

# Function to start a service
start_service() {
    local dir=$1
    local port=$2
    local name=$3
    
    echo "Starting $name on port $port..."
    cd "$dir"
    
    # Start in background with analytics disabled
    nohup ./mvnw quarkus:dev \
        -Ddebug=false \
        -Dquarkus.analytics.disabled=true \
        -Dquarkus.log.console.enable=true \
        > "/tmp/${name}.log" 2>&1 &
    
    echo "$!" > "/tmp/${name}.pid"
    echo "  PID: $(cat /tmp/${name}.pid)"
}

# Start services
start_service "/home/leonardojaques/Projetos/lab-java-quarkus/election-management" "8080" "election-mgmt"
sleep 20

start_service "/home/leonardojaques/Projetos/lab-java-quarkus/voting-app" "8081" "voting-app"
sleep 15

start_service "/home/leonardojaques/Projetos/lab-java-quarkus/result-app" "8082" "result-app"
sleep 10

# Check health
echo ""
echo "Checking services..."

for port in 8080 8081 8082; do
    if curl -s "http://localhost:$port/q/health" > /dev/null 2>&1; then
        echo "✓ Port $port is responding"
    else
        echo "✗ Port $port is NOT responding (may need more time)"
    fi
done

echo ""
echo "Services starting... Check logs:"
echo "  tail -f /tmp/election-mgmt.log"
echo "  tail -f /tmp/voting-app.log"
echo "  tail -f /tmp/result-app.log"
echo ""
echo "To stop: pkill -f 'quarkus:dev'"
echo ""
