#!/bin/bash

# Start all services for testing

set -e

echo "Starting services..."
echo ""

# Kill any existing Java processes on these ports
echo "Cleaning up existing processes..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:8081 | xargs kill -9 2>/dev/null || true
lsof -ti:8082 | xargs kill -9 2>/dev/null || true
sleep 2

# Start Docker containers
echo "Starting Docker containers..."
cd /home/leonardojaques/Projetos/lab-java-quarkus
docker compose up -d database caching
sleep 5

# Build all projects if needed
echo ""
echo "Building projects..."

cd /home/leonardojaques/Projetos/lab-java-quarkus/election-management
if [ ! -f "target/quarkus-app/quarkus-run.jar" ]; then
    echo "Building election-management..."
    ./mvnw clean package -DskipTests -Dquarkus.analytics.disabled=true
fi

cd /home/leonardojaques/Projetos/lab-java-quarkus/voting-app
if [ ! -f "target/quarkus-app/quarkus-run.jar" ]; then
    echo "Building voting-app..."
    ./mvnw clean package -DskipTests -Dquarkus.analytics.disabled=true
fi

cd /home/leonardojaques/Projetos/lab-java-quarkus/result-app
if [ ! -f "target/quarkus-app/quarkus-run.jar" ]; then
    echo "Building result-app..."
    ./mvnw clean package -DskipTests -Dquarkus.analytics.disabled=true
fi

# Start services
echo ""
echo "Starting services..."

echo "Starting election-management on port 8080..."
cd /home/leonardojaques/Projetos/lab-java-quarkus/election-management
java -jar target/quarkus-app/quarkus-run.jar > /tmp/election-mgmt.log 2>&1 &
ELECTION_PID=$!
echo "  PID: $ELECTION_PID"

sleep 10

echo "Starting voting-app on port 8081..."
cd /home/leonardojaques/Projetos/lab-java-quarkus/voting-app
java -jar target/quarkus-app/quarkus-run.jar > /tmp/voting-app.log 2>&1 &
VOTING_PID=$!
echo "  PID: $VOTING_PID"

sleep 5

echo "Starting result-app on port 8082..."
cd /home/leonardojaques/Projetos/lab-java-quarkus/result-app
java -jar target/quarkus-app/quarkus-run.jar > /tmp/result-app.log 2>&1 &
RESULT_PID=$!
echo "  PID: $RESULT_PID"

# Wait for services to be ready
echo ""
echo "Waiting for services to be ready..."
sleep 10

# Check if services are responding
echo ""
echo "Checking service health..."

if curl -s http://localhost:8080/q/health > /dev/null 2>&1; then
    echo "✓ Election Management (8080) is ready"
else
    echo "✗ Election Management (8080) is NOT responding"
    echo "  Log: tail -30 /tmp/election-mgmt.log"
fi

if curl -s http://localhost:8081/q/health > /dev/null 2>&1; then
    echo "✓ Voting App (8081) is ready"
else
    echo "✗ Voting App (8081) is NOT responding"
    echo "  Log: tail -30 /tmp/voting-app.log"
fi

if curl -s http://localhost:8082/q/health > /dev/null 2>&1; then
    echo "✓ Result App (8082) is ready"
else
    echo "✗ Result App (8082) is NOT responding"
    echo "  Log: tail -30 /tmp/result-app.log"
fi

echo ""
echo "Services started!"
echo ""
echo "PIDs:"
echo "  Election Management: $ELECTION_PID"
echo "  Voting App: $VOTING_PID"
echo "  Result App: $RESULT_PID"
echo ""
echo "To stop services: kill $ELECTION_PID $VOTING_PID $RESULT_PID"
echo ""
echo "Logs:"
echo "  tail -f /tmp/election-mgmt.log"
echo "  tail -f /tmp/voting-app.log"
echo "  tail -f /tmp/result-app.log"
echo ""
