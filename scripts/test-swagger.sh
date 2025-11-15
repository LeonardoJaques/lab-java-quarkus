#!/bin/bash

# Script para testar o Swagger nos microserviços
# Uso: ./test-swagger.sh [election-management|voting-app|result-app|all]

set -e

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Teste de Swagger/OpenAPI ===${NC}\n"

# Função para verificar se a porta está ativa
check_port() {
    local port=$1
    local app=$2
    
    echo -e "${YELLOW}Verificando $app na porta $port...${NC}"
    
    if curl -s http://localhost:$port/q/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $app está rodando!${NC}"
        return 0
    else
        echo -e "${RED}❌ $app não está respondendo na porta $port${NC}"
        return 1
    fi
}

# Função para testar o Swagger
test_swagger() {
    local port=$1
    local app=$2
    
    echo -e "\n${YELLOW}Testando Swagger de $app...${NC}"
    
    # Teste OpenAPI endpoint
    if curl -s http://localhost:$port/q/openapi | grep -q "openapi"; then
        echo -e "${GREEN}✅ OpenAPI Spec disponível em: http://localhost:$port/q/openapi${NC}"
    else
        echo -e "${RED}❌ OpenAPI Spec não disponível${NC}"
    fi
    
    # Teste Swagger UI
    if curl -s http://localhost:$port/q/swagger-ui/ | grep -q "swagger"; then
        echo -e "${GREEN}✅ Swagger UI disponível em: http://localhost:$port/q/swagger-ui${NC}"
    else
        echo -e "${RED}❌ Swagger UI não disponível${NC}"
    fi
    
    # Abrir no navegador (opcional)
    echo -e "${YELLOW}Para abrir no navegador, execute:${NC}"
    echo -e "  xdg-open http://localhost:$port/q/swagger-ui"
}

# Processar argumentos
APP=${1:-all}

case $APP in
    election-management)
        if check_port 8080 "Election Management"; then
            test_swagger 8080 "Election Management"
        fi
        ;;
    voting-app)
        if check_port 8081 "Voting App"; then
            test_swagger 8081 "Voting App"
        fi
        ;;
    result-app)
        if check_port 8082 "Result App"; then
            test_swagger 8082 "Result App"
        fi
        ;;
    all)
        echo -e "${YELLOW}Testando todos os serviços...${NC}\n"
        
        if check_port 8080 "Election Management"; then
            test_swagger 8080 "Election Management"
        fi
        
        echo ""
        
        if check_port 8081 "Voting App"; then
            test_swagger 8081 "Voting App"
        fi
        
        echo ""
        
        if check_port 8082 "Result App"; then
            test_swagger 8082 "Result App"
        fi
        ;;
    *)
        echo -e "${RED}Uso: $0 [election-management|voting-app|result-app|all]${NC}"
        exit 1
        ;;
esac

echo -e "\n${GREEN}=== Teste Concluído ===${NC}"
echo -e "\n${YELLOW}Dica: Para iniciar um serviço, execute:${NC}"
echo -e "  cd election-management && ./mvnw quarkus:dev"
echo -e "  cd voting-app && ./mvnw quarkus:dev"
echo -e "  cd result-app && ./mvnw quarkus:dev"
