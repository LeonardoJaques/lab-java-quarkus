# Configuração e Uso do Swagger/OpenAPI

## Visão Geral

Todos os três microserviços do projeto (`election-management`, `voting-app` e `result-app`) foram configurados com Swagger UI para documentação automática das APIs utilizando a extensão **SmallRye OpenAPI** do Quarkus.

## Instalação Realizada

### 1. Dependências Adicionadas

**election-management** (já possuía):
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

**voting-app** (adicionado):
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

**result-app** (adicionado):
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

### 2. Configurações Aplicadas

Em cada `application.properties` dos três módulos:

**election-management:**
```properties
# SWAGGER/OPENAPI
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/q/swagger-ui
mp.openapi.extensions.smallrye.info.title=Election Management API
mp.openapi.extensions.smallrye.info.version=1.0.0
mp.openapi.extensions.smallrye.info.description=API para gerenciamento de eleições
mp.openapi.extensions.smallrye.info.contact.name=Jaques Projetos
mp.openapi.extensions.smallrye.info.contact.email=contato@jaquesprojetos.com.br
```

**voting-app:**
```properties
# SWAGGER/OPENAPI
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/q/swagger-ui
mp.openapi.extensions.smallrye.info.title=Voting App API
mp.openapi.extensions.smallrye.info.version=1.0.0
mp.openapi.extensions.smallrye.info.description=API para votação com suporte a virtual threads
mp.openapi.extensions.smallrye.info.contact.name=Jaques Projetos
mp.openapi.extensions.smallrye.info.contact.email=contato@jaquesprojetos.com.br
```

**result-app:**
```properties
# SWAGGER/OPENAPI
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/q/swagger-ui
mp.openapi.extensions.smallrye.info.title=Result App API
mp.openapi.extensions.smallrye.info.version=1.0.0
mp.openapi.extensions.smallrye.info.description=API para consulta de resultados das eleições
mp.openapi.extensions.smallrye.info.contact.name=Jaques Projetos
mp.openapi.extensions.smallrye.info.contact.email=contato@jaquesprojetos.com.br
```

## Como Testar

### Pré-requisitos

1. Serviços de infraestrutura rodando (MariaDB e Redis):

```bash
cd /home/leonardojaques/Projetos/lab-java-quarkus
docker compose -f config/docker-compose.yml up -d database caching
```

### Testar Election Management API

1. Inicie a aplicação:

```bash
cd /home/leonardojaques/Projetos/lab-java-quarkus/election-management
./mvnw quarkus:dev
```

2. Aguarde a mensagem indicando que a aplicação está rodando

3. Acesse no navegador:
   - **Swagger UI:** http://localhost:8080/q/swagger-ui
   - **OpenAPI Spec (JSON):** http://localhost:8080/q/openapi
   - **OpenAPI Spec (YAML):** http://localhost:8080/q/openapi?format=yaml

### Testar Voting App API

1. Inicie a aplicação:

```bash
cd /home/leonardojaques/Projetos/lab-java-quarkus/voting-app
./mvnw quarkus:dev
```

2. Acesse no navegador:
   - **Swagger UI:** http://localhost:8081/q/swagger-ui
   - **OpenAPI Spec:** http://localhost:8081/q/openapi

### Testar Result App API

1. Inicie a aplicação:

```bash
cd /home/leonardojaques/Projetos/lab-java-quarkus/result-app
./mvnw quarkus:dev
```

2. Acesse no navegador:
   - **Swagger UI:** http://localhost:8082/q/swagger-ui
   - **OpenAPI Spec:** http://localhost:8082/q/openapi

## Endpoints Disponíveis

### Swagger UI
- **Porta 8080:** http://localhost:8080/q/swagger-ui (Election Management)
- **Porta 8081:** http://localhost:8081/q/swagger-ui (Voting App)
- **Porta 8082:** http://localhost:8082/q/swagger-ui (Result App)

### OpenAPI Specification
- **Porta 8080:** http://localhost:8080/q/openapi (Election Management)
- **Porta 8081:** http://localhost:8081/q/openapi (Voting App)
- **Porta 8082:** http://localhost:8082/q/openapi (Result App)

## Recursos do Swagger UI

O Swagger UI permite:

- ✅ Visualizar todos os endpoints disponíveis
- ✅ Ver os modelos de dados (schemas)
- ✅ Testar as APIs diretamente pelo navegador
- ✅ Ver exemplos de requisições e respostas
- ✅ Validar payloads
- ✅ Exportar especificação OpenAPI

## Testando via curl

### Obter especificação OpenAPI

```bash
# JSON (padrão)
curl http://localhost:8080/q/openapi

# YAML
curl http://localhost:8080/q/openapi?format=yaml
```

### Exemplo de teste de endpoint

Após verificar os endpoints disponíveis no Swagger UI, você pode testá-los via curl:

```bash
# Exemplo: Listar candidatos (election-management)
curl http://localhost:8080/api/candidates

# Exemplo: Health check
curl http://localhost:8080/q/health
```

## Verificação de Instalação

Para verificar se o Swagger foi corretamente instalado:

1. **Verificar dependência no pom.xml:**

```bash
grep -n "quarkus-smallrye-openapi" election-management/pom.xml
grep -n "quarkus-smallrye-openapi" voting-app/pom.xml
grep -n "quarkus-smallrye-openapi" result-app/pom.xml
```

2. **Verificar configuração:**

```bash
grep -n "swagger-ui" election-management/src/main/resources/application.properties
grep -n "swagger-ui" voting-app/src/main/resources/application.properties
grep -n "swagger-ui" result-app/src/main/resources/application.properties
```

## Notas Importantes

- ⚠️ O Swagger UI está sempre habilitado devido à configuração `quarkus.swagger-ui.always-include=true`
- 📝 A especificação OpenAPI é gerada automaticamente a partir das anotações JAX-RS nos endpoints
- 🔄 Em modo dev (`./mvnw quarkus:dev`), as alterações são refletidas automaticamente
- 🌐 Em produção, considere adicionar autenticação ao Swagger UI por questões de segurança

## Troubleshooting

### Swagger UI não carrega

1. Verifique se a aplicação está rodando:
```bash
curl http://localhost:808X/q/health
```

2. Verifique os logs da aplicação

3. Confirme que a dependência `quarkus-smallrye-openapi` está no pom.xml

### Endpoints não aparecem no Swagger

1. Verifique se os endpoints possuem anotações JAX-RS (`@Path`, `@GET`, `@POST`, etc.)
2. Confirme que as classes estão sendo escaneadas pelo Quarkus
3. Reinicie a aplicação em modo dev

## Referências

- [Quarkus - OpenAPI and Swagger UI](https://quarkus.io/guides/openapi-swaggerui)
- [SmallRye OpenAPI](https://github.com/smallrye/smallrye-open-api)
- [OpenAPI Specification](https://swagger.io/specification/)
