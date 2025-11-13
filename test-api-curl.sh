#!/bin/bash

# Script de Testes da API usando curl
# =================================

echo "🚀 Testes da API - Sistema de Votação Quarkus"
echo "=============================================="
echo ""

BASE_URL="http://localhost:8080"

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}📋 TESTE 1: Listar Candidatos (GET)${NC}"
echo "GET $BASE_URL/api/candidates"
echo "---"
curl -s -X GET "$BASE_URL/api/candidates" \
  -H "Accept: application/json" | jq '.' || echo "[]"
echo -e "\n"

echo -e "${BLUE}📝 TESTE 2: Criar Novo Candidato (POST)${NC}"
echo "POST $BASE_URL/api/candidates"
echo "---"
curl -s -X POST "$BASE_URL/api/candidates" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "givenName": "Leonardo",
    "familyName": "Jaques",
    "email": "leonardo.jaques@example.com",
    "phone": "+55 11 98765-4321",
    "jobTitle": "Software Engineer",
    "photo": "https://example.com/leo.jpg"
  }' | jq '.' || echo "Criado com sucesso"
echo -e "\n"

echo -e "${BLUE}📝 TESTE 3: Criar Segundo Candidato (POST)${NC}"
echo "POST $BASE_URL/api/candidates"
echo "---"
curl -s -X POST "$BASE_URL/api/candidates" \
  -H "Content-Type: application/json" \
  -d '{
    "givenName": "Maria",
    "familyName": "Silva",
    "email": "maria.silva@example.com",
    "phone": "+55 21 99999-8888",
    "jobTitle": "Product Manager",
    "photo": "https://example.com/maria.jpg"
  }' | jq '.' || echo "Criado com sucesso"
echo -e "\n"

echo -e "${BLUE}📋 TESTE 4: Listar Todos os Candidatos Criados (GET)${NC}"
echo "GET $BASE_URL/api/candidates"
echo "---"
CANDIDATES=$(curl -s -X GET "$BASE_URL/api/candidates" -H "Accept: application/json")
echo "$CANDIDATES" | jq '.'

# Extrair IDs dos candidatos para próximos testes
CANDIDATE_ID=$(echo "$CANDIDATES" | jq -r '.[0].id // empty')
echo -e "\n${YELLOW}ID do primeiro candidato: $CANDIDATE_ID${NC}\n"

if [ -n "$CANDIDATE_ID" ]; then
  echo -e "${BLUE}✏️  TESTE 5: Atualizar Candidato (PUT)${NC}"
  echo "PUT $BASE_URL/api/candidates/$CANDIDATE_ID"
  echo "---"
  curl -s -X PUT "$BASE_URL/api/candidates/$CANDIDATE_ID" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{
      "givenName": "Leonardo",
      "familyName": "Jaques Updated",
      "email": "leonardo.jaques.updated@example.com",
      "phone": "+55 11 91111-1111",
      "jobTitle": "Senior Software Engineer",
      "photo": "https://example.com/leo-new.jpg"
    }' | jq '.'
  echo -e "\n"
fi

echo -e "${BLUE}🗳️  TESTE 6: Criar Eleição (POST)${NC}"
echo "POST $BASE_URL/api/elections"
echo "---"
ELECTION=$(curl -s -X POST "$BASE_URL/api/elections" -H "Accept: application/json")
echo "$ELECTION" | jq '.'
ELECTION_ID=$(echo "$ELECTION" | jq -r '.id // empty')
echo -e "\n${YELLOW}ID da eleição: $ELECTION_ID${NC}\n"

echo -e "${BLUE}📊 TESTE 7: Listar Eleições (GET)${NC}"
echo "GET $BASE_URL/api/elections"
echo "---"
curl -s -X GET "$BASE_URL/api/elections" -H "Accept: application/json" | jq '.'
echo -e "\n"

echo -e "${GREEN}✅ Testes da API Election Management Concluídos!${NC}"
echo ""
echo -e "${YELLOW}📌 Para testar Voting App (porta 8081):${NC}"
echo "   GET  http://localhost:8081/api/voting"
echo "   POST http://localhost:8081/api/voting/elections/{electionId}/candidates/{candidateId}"
echo ""
echo -e "${YELLOW}📌 Para testar Result App (porta 8082):${NC}"
echo "   GET  http://localhost:8082/ (Server-Sent Events)"
echo ""
