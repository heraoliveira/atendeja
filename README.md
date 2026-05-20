# AtendeJá

AtendeJá é uma aplicação full stack de agenda e fila de atendimento para prestadores locais, como clínicas pequenas, barbearias, salões, consultórios e assistências técnicas.

> Status atual: Fase 2 implementada no backend. O repositório contém API Spring Boot com Maven Wrapper, PostgreSQL, Flyway, OpenAPI, Actuator, CRUDs iniciais de clientes, profissionais e serviços, além do módulo de agendamentos com regra de conflito de horário por profissional. Autenticação/JWT, roles, dashboard, frontend React e deploy ainda não foram implementados.

## Estado Atual

Implementado na Fase 0:

- Setup base do repositório.
- Docker Compose com PostgreSQL local.
- `.env.example`, `.editorconfig`, `.gitattributes` e documentação inicial.
- ADRs e roadmap técnico em `docs/`.

Implementado na Fase 1:

- API REST Java 17 + Spring Boot em `backend/`.
- Maven Wrapper em `backend/mvnw` e `backend/mvnw.cmd`.
- Persistência com Spring Data JPA, PostgreSQL e Flyway.
- Migrations `V1__init.sql` e `V2__indexes.sql`.
- CRUDs de `customers`, `professionals` e `services`.
- Validação com Bean Validation.
- Tratamento global de erros com `ProblemDetail`.
- Swagger UI em `/swagger-ui.html`.
- Health check em `/actuator/health`.
- Testes unitários, testes de controller e integração com PostgreSQL real via Testcontainers.

Implementado na Fase 2:

- Entidade `Appointment` e enum `AppointmentStatus`.
- Migration `V3__appointments.sql` com FKs, constraints e índices para listagem e conflito de agenda.
- Endpoints de criação, listagem, detalhe, remarcação e cancelamento de agendamentos.
- Cálculo de `endAt` no backend com base em `durationMinutes + bufferMinutes` do serviço.
- Regra de conflito por profissional usando intervalo semiaberto `[startAt, endAt)`.
- Retorno `409 Conflict` para sobreposição de horários.
- Testes unitários, testes de controller e teste de integração com PostgreSQL real para a regra de conflito.

Ainda planejado:

- Fase 3: autenticação JWT, Spring Security e roles.
- Fase 4: dashboard e filtros operacionais avançados.
- Fase 5: frontend React + TypeScript.
- Fase 6: ampliação de testes automatizados.
- Fase 7: Docker Compose completo com API/frontend, CI e preparação para deploy.

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
- CI/CD e preparação para deploy

## Arquitetura Atual

```text
atendeja/
  backend/              API REST Java 17 + Spring Boot
  docs/
    adr/                decisões técnicas
    api/                exemplos HTTP da Fase 1
    briefing/           contexto do produto e escopo
  frontend/             placeholder documental; sem código React ainda
  docker-compose.yml    PostgreSQL local
  .env.example          variáveis de ambiente de exemplo
```

Pacotes principais do backend:

- `controller`: entrada HTTP e versionamento `/api/v1`.
- `dto`: payloads de entrada e saída.
- `service`: regras de negócio, transações e coordenação de persistência.
- `repository`: acesso a dados com Spring Data JPA.
- `entity`: entidades JPA.
- `mapper`: conversão entre entidades e DTOs.
- `exception`: erros padronizados e handler global.
- `config`: configurações da aplicação.

O pacote `security` ainda não existe. Ele será criado somente na fase de autenticação.

## Execução Local

### Pré-requisitos

- Java 17.
- Docker Desktop ou Docker Engine com Docker Compose.
- Git.

Maven instalado globalmente é opcional, porque o backend possui Maven Wrapper.

### Configuração

Crie um arquivo `.env` a partir de `.env.example` quando quiser subir os serviços locais:

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

Os testes de integração usam Testcontainers com PostgreSQL real. Docker precisa estar disponível para a JVM. Testes ignorados em `PersistenceIntegrationTest` não devem ser aceitos como sucesso nesta fase.

### Verificar Docker Compose

```bash
docker compose config --quiet
```

### Parar ambiente local

```bash
docker compose down
```

Para remover também o volume do banco local:

```bash
docker compose down -v
```

## Variáveis de Ambiente

| Variável | Uso |
|---|---|
| `POSTGRES_DB` | Nome do banco PostgreSQL local. |
| `POSTGRES_USER` | Usuário do banco. |
| `POSTGRES_PASSWORD` | Senha do banco. |
| `POSTGRES_PORT` | Porta exposta localmente para o PostgreSQL. |
| `API_PORT` | Porta da API Spring Boot. |
| `FRONTEND_PORT` | Porta planejada para o futuro frontend React. |
| `JWT_SECRET` | Planejada para a futura fase de JWT; ainda não usada. |
| `CORS_ALLOWED_ORIGINS` | Planejada para integração futura com frontend; ainda não aplicada. |

## Endpoints Implementados

### Base

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/actuator/health` | Health check da API. |

### Clientes

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/customers` | Lista clientes com paginação e filtros. |
| `GET` | `/api/v1/customers/{id}` | Busca cliente por id. |
| `POST` | `/api/v1/customers` | Cria cliente. |
| `PUT` | `/api/v1/customers/{id}` | Atualiza cliente. |
| `DELETE` | `/api/v1/customers/{id}` | Inativa cliente. |

### Profissionais

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/professionals` | Lista profissionais com paginação e filtros. |
| `GET` | `/api/v1/professionals/{id}` | Busca profissional por id. |
| `POST` | `/api/v1/professionals` | Cria profissional. |
| `PUT` | `/api/v1/professionals/{id}` | Atualiza profissional. |
| `DELETE` | `/api/v1/professionals/{id}` | Inativa profissional. |

### Serviços

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/services` | Lista serviços com paginação e filtros. |
| `GET` | `/api/v1/services/{id}` | Busca serviço por id. |
| `POST` | `/api/v1/services` | Cria serviço. |
| `PUT` | `/api/v1/services/{id}` | Atualiza serviço. |
| `DELETE` | `/api/v1/services/{id}` | Inativa serviço. |

### Agendamentos

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/appointments` | Lista agendamentos com paginação e filtros. |
| `GET` | `/api/v1/appointments/{id}` | Busca agendamento por id. |
| `POST` | `/api/v1/appointments` | Cria agendamento. |
| `PATCH` | `/api/v1/appointments/{id}/reschedule` | Remarca agendamento. |
| `PATCH` | `/api/v1/appointments/{id}/cancel` | Cancela agendamento. |

Filtros disponíveis em `GET /api/v1/appointments`:

- `date`: data em formato `YYYY-MM-DD`.
- `professionalId`: id do profissional.
- `customerId`: id do cliente.
- `serviceId`: id do serviço.
- `status`: um dos valores de `AppointmentStatus`.
- Parâmetros de paginação do Spring, como `page`, `size` e `sort`.

## Exemplo De Criação De Agendamento

```json
{
  "customerId": 1,
  "professionalId": 2,
  "serviceId": 3,
  "startAt": "2030-01-20T10:00:00Z"
}
```

O campo `endAt` não é enviado pelo cliente. Ele é calculado no backend usando:

```text
endAt = startAt + service.durationMinutes + service.bufferMinutes
```

## Regra De Conflito De Horário

A regra central do AtendeJá impede que um mesmo profissional tenha dois agendamentos ativos com horários sobrepostos.

O intervalo é tratado como semiaberto: `[startAt, endAt)`. Isso significa que um agendamento pode começar exatamente no horário em que outro termina, sem gerar conflito.

A condição usada para detectar conflito é:

```sql
existing.start_at < :newEndAt
AND existing.end_at > :newStartAt
AND existing.professional_id = :professionalId
AND existing.status NOT IN ('CANCELED', 'NO_SHOW')
```

Quando existe conflito, a API retorna `409 Conflict` com o código:

```json
{
  "code": "APPOINTMENT_TIME_CONFLICT"
}
```

## Status De Agendamento

Os status existentes no backend são:

- `SCHEDULED`
- `CONFIRMED`
- `CHECKED_IN`
- `COMPLETED`
- `CANCELED`
- `NO_SHOW`

Nesta fase, a API implementa criação, listagem, detalhe, remarcação e cancelamento. Check-in, conclusão de atendimento, falta e dashboard ficam para fases posteriores.

## Roadmap

1. Fase 0: setup do repositório, Docker e documentação inicial.
2. Fase 1: backend base, banco, Flyway e CRUDs iniciais.
3. Estabilização da Fase 1: documentação, Maven Wrapper, Testcontainers e testes ampliados.
4. Fase 2: agendamentos e regra de conflito.
5. Fase 3: autenticação JWT e autorização por roles.
6. Fase 4: dashboard e filtros.
7. Fase 5: frontend React.
8. Fase 6: testes automatizados ampliados.
9. Fase 7: Docker Compose completo, documentação final e preparação para deploy.

## Documentação Complementar

- [Briefing técnico](docs/briefing/atendeja-briefing.md)
- [Roadmap de implementação](docs/implementation-roadmap.md)
- [Exemplos HTTP da Fase 1](docs/api/phase-1-cruds.http)
- [ADR 0001 - Arquitetura e stack](docs/adr/0001-architecture-and-stack.md)
- [ADR 0002 - Regra de conflito de agenda](docs/adr/0002-schedule-conflict-rule.md)
