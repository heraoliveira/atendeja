# Backend AtendeJá

API REST do AtendeJá, construída com Java 17, Spring Boot, PostgreSQL e Flyway.

## Visão geral

O backend concentra autenticação, autorização, cadastros, agendamentos, regras de conflito de horários, calendário profissional e dashboard diário. A API é versionada em `/api/v1`, usa DTOs para entrada/saída e mantém regras de negócio nos services.

Principais recursos:

- Login Bearer JWT com senhas BCrypt e roles `ADMIN` e `ATTENDANT`.
- CRUD de clientes, profissionais e serviços.
- Busca de clientes por nome, e-mail e telefone normalizado.
- Agendamentos com criação, remarcação, cancelamento e transições operacionais.
- Conflito de horários por profissional com intervalo semiaberto.
- Disponibilidade semanal e exceções por data.
- Dashboard diário com indicadores de agenda e ocupação.
- Erros padronizados com `ProblemDetail`.
- Testes unitários, controller tests e integração com PostgreSQL real via Testcontainers.

## Stack

- Java 17.
- Spring Boot, Spring Web, Spring Data JPA e Bean Validation.
- Spring Security, OAuth2 Resource Server, JWT e BCrypt.
- PostgreSQL e Flyway.
- Springdoc OpenAPI e Actuator.
- JUnit, Mockito, MockMvc e Testcontainers.
- Docker.

## Execução local

Na raiz do repositório, suba a stack completa:

```bash
docker compose up --build
```

Para rodar apenas a API com banco local já disponível:

```bash
cd backend
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
cd backend
.\mvnw spring-boot:run
```

URLs locais:

- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Variáveis

O Docker Compose injeta as variáveis locais a partir de `.env` ou dos defaults definidos em `docker-compose.yml`.

| Variável | Uso |
|---|---|
| `DATABASE_URL` ou `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL. |
| `DATABASE_USERNAME` ou `SPRING_DATASOURCE_USERNAME` | Usuário do banco. |
| `DATABASE_PASSWORD` ou `SPRING_DATASOURCE_PASSWORD` | Senha do banco. |
| `JWT_SECRET` | Segredo JWT com ao menos 32 caracteres. |
| `JWT_EXPIRATION` | Duração do access token, como `PT8H`. |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para o frontend. |
| `DEMO_AUTH_USERS_ENABLED` | Habilita bootstrap de usuários demo. |
| `DEMO_ADMIN_EMAIL` | E-mail da conta demo. |
| `DEMO_ADMIN_PASSWORD` | Senha da conta demo pública. |

## Endpoints principais

Todos os endpoints de negócio exigem `Authorization: Bearer <accessToken>`.

| Método | Rota | Objetivo |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autentica usuário ativo. |
| `GET/POST/PUT/DELETE` | `/api/v1/customers` | Gestão de clientes. |
| `GET` | `/api/v1/customers/search?q=` | Busca remota de clientes ativos. |
| `GET/POST/PUT/DELETE` | `/api/v1/professionals` | Gestão de profissionais. |
| `GET/POST/PUT/DELETE` | `/api/v1/services` | Gestão de serviços. |
| `GET/POST` | `/api/v1/appointments` | Listagem e criação de agendamentos. |
| `PATCH` | `/api/v1/appointments/{id}/reschedule` | Remarca agendamento. |
| `PATCH` | `/api/v1/appointments/{id}/cancel` | Cancela agendamento. |
| `PATCH` | `/api/v1/appointments/{id}/confirm` | Confirma agendamento. |
| `PATCH` | `/api/v1/appointments/{id}/check-in` | Registra check-in. |
| `PATCH` | `/api/v1/appointments/{id}/complete` | Conclui atendimento. |
| `PATCH` | `/api/v1/appointments/{id}/no-show` | Registra falta. |
| `GET/POST/PUT/DELETE` | `/api/v1/professionals/{id}/availability-rules` | Disponibilidade semanal. |
| `GET/POST/PUT/DELETE` | `/api/v1/professionals/{id}/schedule-exceptions` | Exceções de agenda. |
| `GET` | `/api/v1/dashboard/daily` | Dashboard diário. |

## Regras de agenda

- A API calcula `endAt` com base na duração e no buffer do serviço.
- Intervalos são semiabertos: `[startAt, effectiveEndAt)`.
- Agendamentos `CANCELED` e `NO_SHOW` não bloqueiam a agenda.
- Agendamentos `COMPLETED` com `completedAt` bloqueiam até `completedAt + bufferMinutes`.
- Criação e remarcação usam transação e bloqueio pessimista por profissional.
- Check-in exige agendamento confirmado dentro da janela operacional.
- Conclusão exige check-in e horário inicial já alcançado.

## Permissões

| Recurso | `ADMIN` | `ATTENDANT` |
|---|---|---|
| Clientes | Leitura e escrita | Leitura e escrita |
| Profissionais | Leitura e escrita | Leitura |
| Serviços | Leitura e escrita | Leitura |
| Agendamentos | Leitura e escrita | Leitura e escrita |
| Calendário profissional | Leitura e escrita | Leitura |
| Dashboard | Leitura | Leitura |

## Testes

```bash
cd backend
./mvnw test
```

Teste de integração isolado:

```bash
./mvnw test -Dtest=PersistenceIntegrationTest
```

`PersistenceIntegrationTest` usa Testcontainers para validar migrations, constraints e regras de persistência em PostgreSQL real.

## Deploy

O backend pode ser publicado no Render como Web Service.

- Root directory: `backend`.
- Build command: `./mvnw -B -DskipTests package`.
- Start command: `java -jar target/atendeja-api-1.0.0.jar`.
- Health check path: `/actuator/health`.

Detalhes operacionais estão em [docs/deploy-prep.md](../docs/deploy-prep.md).
