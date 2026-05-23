# Backend AtendeJá

API REST do AtendeJá, construída com Java 17, Spring Boot, PostgreSQL e Flyway.

## Status Atual

O backend contém a base das Fases 1, 2, 3, pré-Fase 4 e Fase 4:

- CRUDs de clientes, profissionais e serviços.
- Listagens paginadas com contrato `PageResponse<T>`.
- Migrations Flyway para o schema inicial, índices, agendamentos, usuários e calendário profissional.
- Módulo de agendamentos com criação, listagem, detalhe, remarcação, cancelamento e transições operacionais.
- Regra de conflito de horários por profissional, usando intervalo semiaberto `[startAt, endAt)`.
- Login Bearer JWT com expiração configurável, senhas BCrypt e roles `ADMIN` e `ATTENDANT`.
- Disponibilidade semanal e exceções por data para profissionais.
- Dashboard diário com indicadores de agenda, cancelamentos, faltas e ocupação.
- Endpoints de negócio protegidos por Spring Security.
- Testes unitários, testes de controller com MockMvc e integração com PostgreSQL real via Testcontainers.

Frontend React e deploy ainda não foram implementados neste backend.

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
- Spring Security e OAuth2 Resource Server.
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

Antes de iniciar a API, configure `JWT_SECRET` com ao menos 32 caracteres. Usuários de demonstração locais são criados somente quando `DEMO_AUTH_USERS_ENABLED=true` e suas senhas são informadas por ambiente.
Para integração com frontend local, configure `CORS_ALLOWED_ORIGINS`; o padrão aceito é `http://localhost:5173`.

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

## Endpoints Da Fase 3

| Método | Rota | Objetivo |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autentica usuário ativo e retorna Bearer JWT. |

## Endpoints Da Pré-Fase 4

| Método | Rota | Objetivo |
|---|---|---|
| `PATCH` | `/api/v1/appointments/{id}/confirm` | Confirma agendamento pendente. |
| `PATCH` | `/api/v1/appointments/{id}/check-in` | Registra check-in em agendamento confirmado do dia. |
| `PATCH` | `/api/v1/appointments/{id}/complete` | Conclui agendamento confirmado ou com check-in. |
| `PATCH` | `/api/v1/appointments/{id}/no-show` | Registra falta após o horário final previsto. |
| `GET` | `/api/v1/professionals/{professionalId}/availability-rules` | Lista disponibilidade semanal. |
| `POST` | `/api/v1/professionals/{professionalId}/availability-rules` | Cria regra semanal. |
| `PUT` | `/api/v1/professionals/{professionalId}/availability-rules/{id}` | Atualiza regra semanal. |
| `DELETE` | `/api/v1/professionals/{professionalId}/availability-rules/{id}` | Inativa regra semanal. |
| `GET` | `/api/v1/professionals/{professionalId}/schedule-exceptions?from=&to=` | Lista exceções por período. |
| `POST` | `/api/v1/professionals/{professionalId}/schedule-exceptions` | Cria exceção de agenda. |
| `PUT` | `/api/v1/professionals/{professionalId}/schedule-exceptions/{id}` | Atualiza exceção de agenda. |
| `DELETE` | `/api/v1/professionals/{professionalId}/schedule-exceptions/{id}` | Remove exceção de agenda. |

## Endpoints Da Fase 4

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/dashboard/daily?date=&professionalId=` | Consulta dashboard diário com indicadores de agenda e ocupação. |

Envie o token retornado em `Authorization: Bearer <accessToken>` para os endpoints de negócio.

## Roles

| Recurso | `ADMIN` | `ATTENDANT` |
|---|---|---|
| Consultar clientes, profissionais, serviços e agendamentos | Permitido | Permitido |
| Criar, atualizar e inativar clientes | Permitido | Permitido |
| Criar, atualizar e inativar profissionais ou serviços | Permitido | Negado com `403` |
| Criar, remarcar, cancelar e avançar status de agendamentos | Permitido | Permitido |
| Consultar disponibilidade e exceções de agenda | Permitido | Permitido |
| Alterar disponibilidade e exceções de agenda | Permitido | Negado com `403` |
| Consultar dashboard diário | Permitido | Permitido |

## Regra De Conflito

Um profissional não pode ter dois agendamentos ativos em horários sobrepostos. A API calcula `endAt` no backend usando `durationMinutes + bufferMinutes` do serviço.

O intervalo é semiaberto: `[startAt, endAt)`. Assim, um agendamento pode começar exatamente no horário em que outro termina.

Agendamentos com status `CANCELED` ou `NO_SHOW` não bloqueiam novos horários.

## Calendário Profissional

Regras semanais definem a disponibilidade recorrente. Exceções por data usam `BLOCKED` para remover capacidade pontual e `AVAILABLE` para adicionar um período extra. Essas janelas são a base do percentual de ocupação da Fase 4.

## Dashboard Diário

`GET /api/v1/dashboard/daily` aceita `date` e `professionalId`. Quando `date` não é informado, a API usa o dia de negócio atual em `America/Sao_Paulo`.

O cálculo retorna total de agendamentos, totais por status, cancelamentos, faltas, minutos disponíveis, minutos ocupados e percentual de ocupação. Minutos ocupados consideram `SCHEDULED`, `CONFIRMED`, `CHECKED_IN` e `COMPLETED`; `CANCELED` não ocupa agenda e `NO_SHOW` entra como indicador próprio.
