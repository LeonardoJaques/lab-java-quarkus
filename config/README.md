# Configuração

Arquivos de configuração para infraestrutura e ferramentas do projeto.

## 📁 Estrutura

```
config/
├── common.yml                  # Configurações comuns do Docker Compose
├── docker-compose.yml          # Orquestração de containers
└── postman-collection.json     # Collection Postman para testes de API
```

## 🐳 Docker Compose

### Uso Básico

```bash
# A partir da raiz do projeto
cd /home/leonardojaques/Projetos/lab-java-quarkus

# Iniciar infraestrutura básica (dev/test)
docker compose -f config/docker-compose.yml up -d database caching

# Iniciar todos os serviços
docker compose -f config/docker-compose.yml up -d

# Parar todos os containers
docker compose -f config/docker-compose.yml down
```

### Serviços Disponíveis

**Infraestrutura Básica (Desenvolvimento):**
- `database` - MariaDB 10.11.2 (porta 3306)
- `caching` - Redis 7.0.9 (porta 6379)

**Infraestrutura Completa:**
- `reverse-proxy` - Traefik (portas 80, 443, 8080)
- `jaeger` - Distributed tracing (portas 4317, 16686)
- `mongodb` - MongoDB para Graylog (porta 27017)
- `opensearch` - OpenSearch para logs (portas 9200, 9600)
- `graylog` - Centralized logging (portas 9000, 12201)

**Aplicações (quando disponíveis):**
- `election-management` - Gerenciamento de eleições (porta 8080)
- `voting-app` - Aplicação de votação (porta 8081)
- `result-app` - Resultados em tempo real (porta 8082)

## 📮 Postman Collection

### Importar no Postman

1. Abra o Postman
2. Clique em **Import**
3. Selecione o arquivo `config/postman-collection.json`
4. A collection "Quarkus Voting System API" será importada

### Endpoints Disponíveis

**Candidatos:**
- GET `/api/candidates` - Listar candidatos
- POST `/api/candidates` - Criar candidato
- PUT `/api/candidates/{id}` - Atualizar candidato

**Eleições:**
- GET `/api/elections` - Listar eleições
- POST `/api/elections` - Criar eleição

**Votação:**
- GET `/api/voting` - Listar eleições disponíveis
- POST `/api/voting/elections/{electionId}/candidates/{candidateId}` - Votar

**Resultados:**
- GET `/` (porta 8082) - Stream de resultados (SSE)

### Variáveis de Ambiente (Postman)

```
baseUrl: http://localhost:8080
votingUrl: http://localhost:8081
resultUrl: http://localhost:8082
candidateId: (copiar do response)
electionId: (copiar do response)
```

## 🔧 Configuração Personalizada

### Sobrescrever Variáveis

```bash
# Exemplo: usar porta diferente para MariaDB
MARIADB_PORT=3307 docker compose -f config/docker-compose.yml up -d database

# Exemplo: definir senha do Redis
REDIS_PASSWORD=mypassword docker compose -f config/docker-compose.yml up -d caching
```

### Volumes Persistentes

Os dados são armazenados em volumes Docker:
- `mariadb_data` - Dados do MariaDB
- `redis_data` - Dados do Redis (se configurado)
- `mongodb_data` - Dados do MongoDB (Graylog)
- `opensearch_data` - Dados do OpenSearch

### Limpar Volumes

```bash
# ATENÇÃO: Isto apaga todos os dados!
docker compose -f config/docker-compose.yml down -v
```

## 🌐 Acessar Serviços

Após iniciar os containers:

- **MariaDB:** `localhost:3306`
- **Redis:** `localhost:6379`
- **Traefik Dashboard:** `http://localhost:8080`
- **Jaeger UI:** `http://localhost:16686`
- **Graylog:** `http://logging.private.jaques.localhost:9000` (admin/admin)
- **Election Management:** `http://localhost:8080`
- **Voting App:** `http://localhost:8081`
- **Result App:** `http://localhost:8082`

## 📊 Monitoramento

### Health Checks

```bash
# Election Management
curl http://localhost:8080/q/health

# Voting App
curl http://localhost:8081/q/health

# Result App
curl http://localhost:8082/q/health
```

### Métricas Prometheus

```bash
# Election Management
curl http://localhost:8080/q/metrics

# Voting App
curl http://localhost:8081/q/metrics
```

## 🐛 Troubleshooting

**Erro: "port is already allocated"**
```bash
# Verificar portas em uso
ss -ltnp | grep -E ':3306|:6379|:8080|:8081|:8082'

# Parar containers conflitantes
docker ps
docker stop <container_id>
```

**Erro: "network not found"**
```bash
# Recriar rede
docker compose -f config/docker-compose.yml down
docker compose -f config/docker-compose.yml up -d
```

**Logs dos Containers:**
```bash
# Ver logs de um serviço específico
docker compose -f config/docker-compose.yml logs -f database
docker compose -f config/docker-compose.yml logs -f caching

# Ver logs de todos os serviços
docker compose -f config/docker-compose.yml logs -f
```

## 📖 Mais Informações

Consulte o README principal na raiz do projeto para documentação completa.
