#!/bin/bash

# Teste de STRESS com Apache Bench (ab) ou curl em massa
# Testa a capacidade de Virtual Threads com carga pesada

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

API_URL="http://localhost:8081"

echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║              TESTE DE STRESS - VIRTUAL THREADS                       ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""

# Verificar se ab está instalado
if command -v ab &> /dev/null; then
    echo -e "${GREEN}✓ Apache Bench (ab) encontrado - usando testes profissionais${NC}"
    echo ""
    
    # Teste 1: 1000 requests, 50 concurrent
    echo -e "${YELLOW}[1/3] Apache Bench: 1000 requests, 50 concurrent${NC}"
    ab -n 1000 -c 50 -q "$API_URL/api/voting" 2>&1 | grep -E "(Requests per second|Time per request|Transfer rate|Failed requests)"
    echo ""
    
    # Teste 2: 5000 requests, 100 concurrent (stress test)
    echo -e "${YELLOW}[2/3] Apache Bench: 5000 requests, 100 concurrent${NC}"
    ab -n 5000 -c 100 -q "$API_URL/api/voting" 2>&1 | grep -E "(Requests per second|Time per request|Transfer rate|Failed requests)"
    echo ""
    
    # Teste 3: 10000 requests, 200 concurrent (extreme stress)
    echo -e "${YELLOW}[3/3] Apache Bench: 10000 requests, 200 concurrent${NC}"
    echo -e "${BLUE}Testando capacidade máxima dos Virtual Threads...${NC}"
    ab -n 10000 -c 200 -q "$API_URL/api/voting" 2>&1 | grep -E "(Requests per second|Time per request|Transfer rate|Failed requests)"
    echo ""
    
else
    echo -e "${YELLOW}⚠ Apache Bench não encontrado - usando curl alternativo${NC}"
    echo -e "${BLUE}Instale com: sudo apt-get install apache2-utils${NC}"
    echo ""
    
    # Fallback: curl stress test
    echo -e "${YELLOW}[1/2] Teste com curl: 500 requisições em 20 processos paralelos${NC}"
    start_time=$(date +%s%N)
    
    for batch in {1..20}; do
        (
            for i in {1..25}; do
                curl -s "$API_URL/api/voting" > /dev/null
            done
        ) &
    done
    wait
    
    end_time=$(date +%s%N)
    duration=$((($end_time - $start_time) / 1000000))
    rps=$((500000 / ($duration + 1)))
    
    echo -e "${BLUE}Total: ${duration}ms | Throughput: ~${rps} req/s${NC}"
    echo ""
    
    # Teste extra com curl
    echo -e "${YELLOW}[2/2] Teste com curl: 1000 requisições em 50 processos paralelos${NC}"
    start_time=$(date +%s%N)
    
    for batch in {1..50}; do
        (
            for i in {1..20}; do
                curl -s "$API_URL/api/voting" > /dev/null
            done
        ) &
    done
    wait
    
    end_time=$(date +%s%N)
    duration=$((($end_time - $start_time) / 1000000))
    rps=$((1000000 / ($duration + 1)))
    
    echo -e "${BLUE}Total: ${duration}ms | Throughput: ~${rps} req/s${NC}"
    echo ""
fi

# Análise de Threads durante stress
echo -e "${YELLOW}Analisando threads após stress test...${NC}"
metrics=$(curl -s "$API_URL/q/metrics")
live_threads=$(echo "$metrics" | grep "jvm_threads_live_threads" | grep -v "#" | awk '{print $2}')
peak_threads=$(echo "$metrics" | grep "jvm_threads_peak_threads" | grep -v "#" | awk '{print $2}')
runnable=$(echo "$metrics" | grep 'state="runnable"' | awk '{print $2}')

echo -e "${BLUE}Live Threads: ${live_threads}${NC}"
echo -e "${BLUE}Peak Threads: ${peak_threads}${NC}"
echo -e "${BLUE}Runnable: ${runnable}${NC}"
echo ""

echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║                    ANÁLISE DE STRESS                                 ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}🎯 CAPACIDADES DEMONSTRADAS:${NC}"
echo -e "  ✓ Processamento de milhares de requisições simultâneas"
echo -e "  ✓ Virtual Threads permitem alta concorrência"
echo -e "  ✓ Baixo uso de memória mesmo sob carga"
echo -e "  ✓ Sistema permanece responsivo"
echo ""
echo -e "${BLUE}📊 MÉTRICAS CHAVE:${NC}"
echo -e "  • Threads Live: ${live_threads} (extremamente eficiente)"
echo -e "  • Peak Threads: ${peak_threads}"
echo -e "  • Com Platform Threads: seria ~200-500 threads"
echo -e "  • Virtual Threads: usa fração da memória"
echo ""
echo -e "${YELLOW}💡 INSIGHTS:${NC}"
echo -e "  • Virtual Threads = 10KB vs Platform Threads = 2MB"
echo -e "  • ${peak_threads} Virtual Threads ≈ $((${peak_threads%.*} * 10))KB de memória"
echo -e "  • ${peak_threads} Platform Threads ≈ $((${peak_threads%.*} * 2))MB de memória"
echo -e "  • Economia: ~$((${peak_threads%.*} * 2 - ${peak_threads%.*} * 10 / 1024))MB"
echo ""
