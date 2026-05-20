# Backend AtendeJá

API REST do AtendeJá, construída com Java 17, Spring Boot, PostgreSQL e Flyway.

## Status Atual

O backend contém a base das Fases 1 e 2:

- CRUDs de clientes, profissionais e serviços.
- Listagens paginadas com contrato `PageResponse<T>`.
- Migrations Flyway para o schema inicial, índices e agendamentos.
- Módulo de agendamentos com criação, listagem, detalhe, remarcação e cancelamento.
- Regra de conflito de horários por profissional, usando intervalo semiaberto `[startAt, endAt)`.
- Testes unitários, testes de controller com MockMvc e integração com PostgreSQL real via Testcontainers.

Spring Security, JWT, roles, dashboard, frontend React e deploy ainda não foram implementados neste backend.

## Stack Atual

- Java 17.
- Spring Boot.
- Maven e Maven Wrapper.
- Spring Web.
- Spring Data JPA.
- Bean Validation.
- Flyway.
- PostgreSQL.
- Springdoc OpenAPI.
- Spring Boot Actuator.
- JUnit, Mockito, MockMvc e Testcontainers.

## Execução Local

Suba o banco na raiz do repositório:

```bash
docker compose up -d db
```

Rode a API no Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

URLs locais:

- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testes

No Windows PowerShell:

```powershell
.\mvnw test
```

Com Maven global:

```bash
mvn test
```

Teste de integração isolado:

```bash
mvn test -Dtest=PersistenceIntegrationTest
```

`PersistenceIntegrationTest` usa Testcontainers para subir PostgreSQL real, aplicar Flyway e validar persistência, constraints e regra de conflito. Docker precisa estar disponível para a JVM; testes ignorados nessa classe indicam problema de ambiente ou compatibilidade e devem ser investigados.

## Endpoints Da Fase 1

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/customers` | Lista clientes com paginação e filtros. |
| `GET` | `/api/v1/customers/{id}` | Busca cliente por id. |
| `POST` | `/api/v1/customers` | Cria cliente. |
| `PUT` | `/api/v1/customers/{id}` | Atualiza cliente. |
| `DELETE` | `/api/v1/customers/{id}` | Inativa cliente. |
| `GET` | `/api/v1/professionals` | Lista profissionais com paginação e filtros. |
| `GET` | `/api/v1/professionals/{id}` | Busca profissional por id. |
| `POST` | `/api/v1/professionals` | Cria profissional. |
| `PUT` | `/api/v1/professionals/{id}` | Atualiza profissional. |
| `DELETE` | `/api/v1/professionals/{id}` | Inativa profissional. |
| `GET` | `/api/v1/services` | Lista serviços com paginação e filtros. |
| `GET` | `/api/v1/services/{id}` | Busca serviço por id. |
| `POST` | `/api/v1/services` | Cria serviço. |
| `PUT` | `/api/v1/services/{id}` | Atualiza serviço. |
| `DELETE` | `/api/v1/services/{id}` | Inativa serviço. |

## Endpoints Da Fase 2

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/appointments` | Lista agendamentos com paginação e filtros. |
| `GET` | `/api/v1/appointments/{id}` | Busca agendamento por id. |
| `POST` | `/api/v1/appointments` | Cria agendamento. |
| `PATCH` | `/api/v1/appointments/{id}/reschedule` | Remarca agendamento. |
| `PATCH` | `/api/v1/appointments/{id}/cancel` | Cancela agendamento. |

## Regra De Conflito

Um profissional não pode ter dois agendamentos ativos em horários sobrepostos. A API calcula `endAt` no backend usando `durationMinutes + bufferMinutes` do serviço.

O intervalo é semiaberto: `[startAt, endAt)`. Assim, um agendamento pode começar exatamente no horário em que outro termina.

Agendamentos com status `CANCELED` ou `NO_SHOW` não bloqueiam novos horários.
