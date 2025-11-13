# Scripts

Scripts auxiliares para build, deploy, testes e performance do projeto.

## 📁 Estrutura

```
scripts/
├── baseline-performance-test.sh        # Teste de performance baseline
├── cicd-blue-green-deployment.sh       # Deploy blue-green para CI/CD
├── cicd-build.sh                       # Build para CI/CD
├── performance-test-curl.sh            # Testes de performance com curl
├── quick-start.sh                      # Inicialização rápida dos serviços
├── start-services.sh                   # Iniciar todos os serviços
├── stress-test-virtual-threads.sh      # Stress test com Virtual Threads
├── test-api-curl.sh                    # Testes da API com curl
└── test-virtual-threads.sh             # Validação de Virtual Threads
```

## 🚀 Como Usar

Todos os scripts devem ser executados a partir da **raiz do projeto**:

```bash
cd /home/leonardojaques/Projetos/lab-java-quarkus

# Exemplo: iniciar serviços
./scripts/quick-start.sh

# Exemplo: testar API
./scripts/test-api-curl.sh

# Exemplo: teste de performance
./scripts/performance-test-curl.sh
```

## 📝 Descrição dos Scripts

### Build e Deploy

- **`cicd-build.sh`** - Build automatizado para CI/CD
  - Uso: `./scripts/cicd-build.sh <app-name>`
  - Exemplo: `./scripts/cicd-build.sh election-management`

- **`cicd-blue-green-deployment.sh`** - Deploy blue-green
  - Uso: `./scripts/cicd-blue-green-deployment.sh <app> <tag>`
  - Exemplo: `./scripts/cicd-blue-green-deployment.sh voting-app 1.0.1`

### Inicialização

- **`quick-start.sh`** - Inicia infraestrutura Docker + serviços Quarkus em modo dev
  - Portas: 8080 (election-management), 8081 (voting-app), 8082 (result-app)

- **`start-services.sh`** - Inicia todos os serviços com build completo
  - Mais lento que quick-start, mas faz rebuild completo

### Testes

- **`test-api-curl.sh`** - Testa endpoints da API (CRUD de candidatos e eleições)
  - Pré-requisito: Serviços rodando

- **`test-virtual-threads.sh`** - Valida Virtual Threads (Java 21)
  - Verifica se Virtual Threads estão habilitados
  - Testa health checks e métricas

### Performance

- **`baseline-performance-test.sh`** - Estabelece baseline de performance
  - Coleta métricas antes de mudanças
  - Gera relatório de baseline

- **`performance-test-curl.sh`** - Teste básico de performance
  - 10 req health check
  - 50 req GET elections
  - 100 req concorrentes
  - Throughput: ~806 req/s

- **`stress-test-virtual-threads.sh`** - Stress test pesado
  - 500-1000 requisições simultâneas
  - Validação de Virtual Threads sob carga
  - Throughput: ~1.450 req/s

## ⚙️ Pré-requisitos

- **Docker** e **Docker Compose** instalados
- **Java 21** (para Virtual Threads)
- **Maven** (wrapper incluído: `./mvnw`)
- **curl** e **jq** (para testes)

## 🔧 Configuração

Os scripts assumem:
- Docker Compose: `config/docker-compose.yml`
- Projeto na raiz: `/home/leonardojaques/Projetos/lab-java-quarkus`
- Portas: 8080, 8081, 8082 disponíveis

## 📊 Resultados de Performance

Para detalhes dos resultados de testes:
- `docs/PERFORMANCE-REPORT.txt` - Relatório completo de performance
- `docs/JAVA21-UPGRADE-SUMMARY.md` - Sumário do upgrade para Java 21

## 🐛 Troubleshooting

**Erro: "Permission denied"**
```bash
chmod +x scripts/*.sh
```

**Erro: "Port already in use"**
```bash
# Matar processos nas portas
pkill -f "quarkus:dev"
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
lsof -ti:8082 | xargs kill -9
```

**Erro: "docker compose: command not found"**
```bash
# Verificar instalação
docker compose version
```

## 📖 Mais Informações

Consulte o README principal na raiz do projeto para documentação completa.
