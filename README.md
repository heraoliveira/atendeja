# AtendeJa

AtendeJa e uma aplicacao full stack de agenda e fila de atendimento para prestadores locais, como clinicas pequenas, barbearias, saloes, consultorios e assistencias tecnicas.

> Status atual: Fase 1 estabilizada. O repositorio contem backend Spring Boot com Maven Wrapper, PostgreSQL, Flyway, OpenAPI, Actuator e CRUDs iniciais de clientes, profissionais e servicos. Agendamentos, autenticacao/JWT, roles, dashboard, frontend React e deploy ainda nao foram implementados.

## Estado Atual

Implementado na Fase 0:

- Setup base do repositorio.
- Docker Compose com PostgreSQL local.
- `.env.example`, `.editorconfig`, `.gitattributes` e documentacao inicial.
- ADRs e roadmap tecnico em `docs/`.

Implementado na Fase 1:

- API REST Java 17 + Spring Boot em `backend/`.
- Maven Wrapper em `backend/mvnw` e `backend/mvnw.cmd`.
- Persistencia com Spring Data JPA, PostgreSQL e Flyway.
- Migrations `V1__init.sql` e `V2__indexes.sql`.
- CRUDs de `customers`, `professionals` e `services`.
- Validacao com Bean Validation.
- Tratamento global de erros com `ProblemDetail`.
- Swagger UI em `/swagger-ui.html`.
- Health check em `/actuator/health`.
- Testes unitarios, controller tests e integracao com PostgreSQL real via Testcontainers.

Ainda planejado:

- Fase 2: agendamentos e regra de conflito por profissional.
- Fase 3: autenticacao JWT, Spring Security e roles.
- Fase 4: dashboard e filtros operacionais.
- Fase 5: frontend React + TypeScript.
- Fase 6: ampliacao de testes automatizados.
- Fase 7: Docker Compose completo com API/frontend, CI e preparacao para deploy.

## Stack Atual

- Java 17
- Spring Boot
- Maven e Maven Wrapper
- Spring Web
- Spring Data JPA
- Bean Validation
- Flyway
- PostgreSQL
- Springdoc OpenAPI
- Spring Boot Actuator
- JUnit, Mockito, MockMvc e Testcontainers
- Docker Compose para PostgreSQL local

## Stack Planejada

- Spring Security + JWT
- Roles `ADMIN` e `ATTENDANT`
- React
- TypeScript
- React Router
- React Hook Form
- Docker Compose completo com API, banco e frontend
- CI/CD e preparacao para deploy

## Arquitetura Atual

```text
atendeja/
  backend/              API REST Java 17 + Spring Boot
  docs/
    adr/                decisoes tecnicas
    api/                exemplos HTTP da Fase 1
    briefing/           contexto do produto e escopo
  frontend/             placeholder documental; sem codigo React ainda
  docker-compose.yml    PostgreSQL local
  .env.example          variaveis de ambiente de exemplo
```

Pacotes principais do backend:

- `controller`: entrada HTTP e versionamento `/api/v1`.
- `dto`: payloads de entrada e saida.
- `service`: regras dos CRUDs, transacoes e coordenacao de persistencia.
- `repository`: acesso a dados com Spring Data JPA.
- `entity`: entidades JPA.
- `mapper`: conversao entre entidades e DTOs.
- `exception`: erros padronizados e handler global.
- `config`: configuracoes da aplicacao.

O pacote `security` ainda nao existe. Ele sera criado somente na fase de autenticacao.

## Execucao Local

### Pre-requisitos

- Java 17.
- Docker Desktop ou Docker Engine com Docker Compose.
- Git.

Maven instalado globalmente e opcional, porque o backend possui Maven Wrapper.

### Configuracao

Crie um arquivo `.env` a partir de `.env.example` quando quiser subir os servicos locais:

```powershell
Copy-Item .env.example .env
```

No Linux/macOS:

```bash
cp .env.example .env
```

### Subir banco local

```bash
docker compose up -d db
```

### Rodar backend

No Windows PowerShell:

```powershell
cd backend
.\mvnw spring-boot:run
```

No Linux/macOS:

```bash
cd backend
./mvnw spring-boot:run
```

API local:

- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Rodar testes do backend

No Windows PowerShell:

```powershell
cd backend
.\mvnw test
```

Com Maven global:

```bash
cd backend
mvn test
```

Os testes de integracao usam Testcontainers com PostgreSQL real. Docker precisa estar disponivel para a JVM. Testes ignorados em `PersistenceIntegrationTest` nao devem ser aceitos como sucesso nesta fase de estabilizacao.

### Verificar Docker Compose

```bash
docker compose config --quiet
```

### Parar ambiente local

```bash
docker compose down
```

Para remover tambem o volume do banco local:

```bash
docker compose down -v
```

## Variaveis de Ambiente

| Variavel | Uso |
|---|---|
| `POSTGRES_DB` | Nome do banco PostgreSQL local. |
| `POSTGRES_USER` | Usuario do banco. |
| `POSTGRES_PASSWORD` | Senha do banco. |
| `POSTGRES_PORT` | Porta exposta localmente para o PostgreSQL. |
| `API_PORT` | Porta da API Spring Boot. |
| `FRONTEND_PORT` | Porta planejada para o futuro frontend React. |
| `JWT_SECRET` | Planejada para a futura fase de JWT; ainda nao usada. |
| `CORS_ALLOWED_ORIGINS` | Planejada para integracao futura com frontend; ainda nao aplicada. |

## Endpoints Implementados Na Fase 1

| Metodo | Rota | Objetivo |
|---|---|---|
| `GET` | `/actuator/health` | Health check da API. |
| `GET` | `/api/v1/customers` | Lista clientes com paginacao e filtros. |
| `GET` | `/api/v1/customers/{id}` | Busca cliente por id. |
| `POST` | `/api/v1/customers` | Cria cliente. |
| `PUT` | `/api/v1/customers/{id}` | Atualiza cliente. |
| `DELETE` | `/api/v1/customers/{id}` | Inativa cliente. |
| `GET` | `/api/v1/professionals` | Lista profissionais com paginacao e filtros. |
| `GET` | `/api/v1/professionals/{id}` | Busca profissional por id. |
| `POST` | `/api/v1/professionals` | Cria profissional. |
| `PUT` | `/api/v1/professionals/{id}` | Atualiza profissional. |
| `DELETE` | `/api/v1/professionals/{id}` | Inativa profissional. |
| `GET` | `/api/v1/services` | Lista servicos com paginacao e filtros. |
| `GET` | `/api/v1/services/{id}` | Busca servico por id. |
| `POST` | `/api/v1/services` | Cria servico. |
| `PUT` | `/api/v1/services/{id}` | Atualiza servico. |
| `DELETE` | `/api/v1/services/{id}` | Inativa servico. |

## Regra Central Planejada

A regra mais importante do projeto sera impedir que um mesmo profissional tenha dois agendamentos ativos em horarios sobrepostos. Essa regra ainda nao esta implementada, porque pertence a Fase 2.

Condicao planejada para detectar conflito:

```sql
existing.start_at < :newEndAt
AND existing.end_at > :newStartAt
AND existing.professional_id = :professionalId
AND existing.status NOT IN ('CANCELED', 'NO_SHOW')
```

O intervalo sera tratado como semiaberto: `[startAt, endAt)`.

## Roadmap

1. Fase 0: setup do repositorio, Docker e documentacao inicial.
2. Fase 1: backend base, banco, Flyway e CRUDs iniciais.
3. Estabilizacao da Fase 1: documentacao, Maven Wrapper, Testcontainers e testes ampliados.
4. Fase 2: agendamentos e regra de conflito.
5. Fase 3: autenticacao JWT e autorizacao por roles.
6. Fase 4: dashboard e filtros.
7. Fase 5: frontend React.
8. Fase 6: testes automatizados ampliados.
9. Fase 7: Docker Compose completo, documentacao final e preparacao para deploy.

## Documentacao Complementar

- [Briefing tecnico](docs/briefing/atendeja-briefing.md)
- [Roadmap de implementacao](docs/implementation-roadmap.md)
- [Exemplos HTTP da Fase 1](docs/api/phase-1-cruds.http)
- [ADR 0001 - Arquitetura e stack](docs/adr/0001-architecture-and-stack.md)
- [ADR 0002 - Regra de conflito de agenda](docs/adr/0002-schedule-conflict-rule.md)
