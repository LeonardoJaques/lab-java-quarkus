#!/bin/bash

# Baseline Performance Test Suite
# Collects metrics BEFORE implementing improvements
# Tests: Response time, Throughput, Error rate, Concurrent load

set -e

echo "======================================"
echo "BASELINE PERFORMANCE TEST SUITE"
echo "Testing BEFORE improvements"
echo "======================================"
echo ""

# Configuration
ELECTION_URL="http://localhost:8080"
VOTING_URL="http://localhost:8081"
RESULT_URL="http://localhost:8082"

# Test parameters
CONCURRENT_USERS=50
TOTAL_REQUESTS=1000
SINGLE_TEST_REQUESTS=100

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Output file
BASELINE_FILE="baseline-results.txt"
echo "Baseline Test Results - $(date)" > $BASELINE_FILE
echo "======================================" >> $BASELINE_FILE
echo "" >> $BASELINE_FILE

# Function to calculate statistics
calculate_stats() {
    local times_file=$1
    local test_name=$2
    
    if [ ! -s "$times_file" ]; then
        echo "No data in $times_file"
        return
    fi
    
    # Calculate statistics
    local count=$(wc -l < "$times_file")
    local sum=$(awk '{s+=$1} END {print s}' "$times_file")
    local avg=$(echo "scale=2; $sum / $count" | bc)
    local min=$(sort -n "$times_file" | head -1)
    local max=$(sort -n "$times_file" | tail -1)
    local median=$(sort -n "$times_file" | awk '{a[NR]=$1} END {print (NR%2==1)?a[(NR+1)/2]:(a[NR/2]+a[NR/2+1])/2}')
    local p95=$(sort -n "$times_file" | awk '{a[NR]=$1} END {idx=int(NR*0.95); print a[idx]}')
    
    echo ""
    echo -e "${GREEN}$test_name Statistics:${NC}"
    echo "  Total requests: $count"
    echo "  Average time: ${avg}ms"
    echo "  Median time: ${median}ms"
    echo "  Min time: ${min}ms"
    echo "  Max time: ${max}ms"
    echo "  95th percentile: ${p95}ms"
    
    # Save to file
    echo "" >> $BASELINE_FILE
    echo "$test_name:" >> $BASELINE_FILE
    echo "  Total requests: $count" >> $BASELINE_FILE
    echo "  Average time: ${avg}ms" >> $BASELINE_FILE
    echo "  Median time: ${median}ms" >> $BASELINE_FILE
    echo "  Min time: ${min}ms" >> $BASELINE_FILE
    echo "  Max time: ${max}ms" >> $BASELINE_FILE
    echo "  95th percentile: ${p95}ms" >> $BASELINE_FILE
}

# Check if services are running
echo -e "${YELLOW}[1/7] Checking if services are running...${NC}"
if ! curl -s $ELECTION_URL/api/candidates > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Election Management service not running on port 8080${NC}"
    echo "Please start: docker-compose up -d && cd election-management && ./mvnw quarkus:dev"
    exit 1
fi

if ! curl -s $VOTING_URL/api/voting/elections > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Voting App not running on port 8081${NC}"
    echo "Please start: cd voting-app && ./mvnw quarkus:dev"
    exit 1
fi

echo -e "${GREEN}✓ All services are running${NC}"

# Test 1: Single request latency (warm-up)
echo ""
echo -e "${YELLOW}[2/7] Warming up services...${NC}"
for i in {1..10}; do
    curl -s $ELECTION_URL/api/candidates > /dev/null 2>&1
done
echo -e "${GREEN}✓ Warm-up complete${NC}"

# Test 2: Get Candidates - Response Time
echo ""
echo -e "${YELLOW}[3/7] Testing GET /api/candidates response time...${NC}"
rm -f get_candidates_times.txt
for i in $(seq 1 $SINGLE_TEST_REQUESTS); do
    time_ms=$(curl -w "%{time_total}\n" -o /dev/null -s $ELECTION_URL/api/candidates | awk '{print $1*1000}')
    echo "$time_ms" >> get_candidates_times.txt
    printf "\r  Progress: $i/$SINGLE_TEST_REQUESTS"
done
echo ""
calculate_stats "get_candidates_times.txt" "GET /api/candidates"

# Test 3: Get Elections - Response Time
echo ""
echo -e "${YELLOW}[4/7] Testing GET /api/elections response time...${NC}"
rm -f get_elections_times.txt
for i in $(seq 1 $SINGLE_TEST_REQUESTS); do
    time_ms=$(curl -w "%{time_total}\n" -o /dev/null -s $ELECTION_URL/api/elections | awk '{print $1*1000}')
    echo "$time_ms" >> get_elections_times.txt
    printf "\r  Progress: $i/$SINGLE_TEST_REQUESTS"
done
echo ""
calculate_stats "get_elections_times.txt" "GET /api/elections"

# Test 4: Concurrent Load Test - Throughput
echo ""
echo -e "${YELLOW}[5/7] Testing concurrent load ($CONCURRENT_USERS users, $TOTAL_REQUESTS total requests)...${NC}"
echo "This may take a few minutes..."

# Using GNU parallel if available, otherwise use background jobs
if command -v parallel > /dev/null 2>&1; then
    echo "Using GNU parallel for concurrent testing..."
    rm -f concurrent_times.txt
    
    start_time=$(date +%s.%N)
    seq 1 $TOTAL_REQUESTS | parallel -j $CONCURRENT_USERS "curl -w '%{time_total}\n' -o /dev/null -s $ELECTION_URL/api/candidates | awk '{print \$1*1000}' >> concurrent_times.txt"
    end_time=$(date +%s.%N)
    
    duration=$(echo "$end_time - $start_time" | bc)
    throughput=$(echo "scale=2; $TOTAL_REQUESTS / $duration" | bc)
    
    echo ""
    echo -e "${GREEN}Concurrent Load Test Results:${NC}"
    echo "  Duration: ${duration}s"
    echo "  Throughput: ${throughput} requests/second"
    
    echo "" >> $BASELINE_FILE
    echo "Concurrent Load Test:" >> $BASELINE_FILE
    echo "  Concurrent users: $CONCURRENT_USERS" >> $BASELINE_FILE
    echo "  Total requests: $TOTAL_REQUESTS" >> $BASELINE_FILE
    echo "  Duration: ${duration}s" >> $BASELINE_FILE
    echo "  Throughput: ${throughput} req/s" >> $BASELINE_FILE
    
    calculate_stats "concurrent_times.txt" "Concurrent Requests"
else
    echo -e "${YELLOW}GNU parallel not found, using background jobs (slower)...${NC}"
    rm -f concurrent_times.txt
    
    start_time=$(date +%s.%N)
    for i in $(seq 1 $TOTAL_REQUESTS); do
        (curl -w "%{time_total}\n" -o /dev/null -s $ELECTION_URL/api/candidates | awk '{print $1*1000}' >> concurrent_times.txt) &
        
        # Limit concurrent jobs
        if [ $(jobs -r | wc -l) -ge $CONCURRENT_USERS ]; then
            wait -n
        fi
        
        if [ $((i % 100)) -eq 0 ]; then
            printf "\r  Progress: $i/$TOTAL_REQUESTS"
        fi
    done
    wait
    end_time=$(date +%s.%N)
    
    duration=$(echo "$end_time - $start_time" | bc)
    throughput=$(echo "scale=2; $TOTAL_REQUESTS / $duration" | bc)
    
    echo ""
    echo -e "${GREEN}Concurrent Load Test Results:${NC}"
    echo "  Duration: ${duration}s"
    echo "  Throughput: ${throughput} requests/second"
    
    echo "" >> $BASELINE_FILE
    echo "Concurrent Load Test:" >> $BASELINE_FILE
    echo "  Concurrent users: $CONCURRENT_USERS" >> $BASELINE_FILE
    echo "  Total requests: $TOTAL_REQUESTS" >> $BASELINE_FILE
    echo "  Duration: ${duration}s" >> $BASELINE_FILE
    echo "  Throughput: ${throughput} req/s" >> $BASELINE_FILE
    
    calculate_stats "concurrent_times.txt" "Concurrent Requests"
fi

# Test 5: Error Rate Test
echo ""
echo -e "${YELLOW}[6/7] Testing error rate...${NC}"
rm -f error_test.txt
success_count=0
error_count=0

for i in $(seq 1 100); do
    http_code=$(curl -w "%{http_code}" -o /dev/null -s $ELECTION_URL/api/candidates)
    if [ "$http_code" = "200" ]; then
        success_count=$((success_count + 1))
    else
        error_count=$((error_count + 1))
    fi
    printf "\r  Progress: $i/100"
done

error_rate=$(echo "scale=2; $error_count * 100 / 100" | bc)

echo ""
echo -e "${GREEN}Error Rate Test:${NC}"
echo "  Success: $success_count"
echo "  Errors: $error_count"
echo "  Error rate: ${error_rate}%"

echo "" >> $BASELINE_FILE
echo "Error Rate:" >> $BASELINE_FILE
echo "  Success: $success_count" >> $BASELINE_FILE
echo "  Errors: $error_count" >> $BASELINE_FILE
echo "  Error rate: ${error_rate}%" >> $BASELINE_FILE

# Test 6: Memory footprint (if available)
echo ""
echo -e "${YELLOW}[7/7] Checking system resources...${NC}"

if command -v ps > /dev/null 2>&1; then
    java_processes=$(ps aux | grep '[j]ava.*quarkus' | wc -l)
    echo "  Java processes running: $java_processes"
    
    if [ $java_processes -gt 0 ]; then
        total_mem=$(ps aux | grep '[j]ava.*quarkus' | awk '{sum+=$6} END {print sum/1024}')
        echo "  Total memory usage: ~${total_mem}MB"
        
        echo "" >> $BASELINE_FILE
        echo "System Resources:" >> $BASELINE_FILE
        echo "  Java processes: $java_processes" >> $BASELINE_FILE
        echo "  Memory usage: ~${total_mem}MB" >> $BASELINE_FILE
    fi
fi

# Summary
echo ""
echo "======================================"
echo -e "${GREEN}BASELINE TEST COMPLETE${NC}"
echo "======================================"
echo ""
echo "Results saved to: $BASELINE_FILE"
echo ""
echo -e "${YELLOW}Key Metrics Summary:${NC}"
cat $BASELINE_FILE | grep -E "(Average time|Throughput|Error rate)" | sed 's/^/  /'

# Cleanup temp files
rm -f get_candidates_times.txt get_elections_times.txt concurrent_times.txt error_test.txt

echo ""
echo -e "${GREEN}Next steps:${NC}"
echo "1. Implement improvements (Virtual Threads, Circuit Breaker, Rate Limiting)"
echo "2. Run: ./post-implementation-test.sh"
echo "3. Compare results"
echo ""
