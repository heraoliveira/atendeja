# AtendeJá

AtendeJá é uma aplicação full stack de agenda e fila de atendimento para prestadores locais, como clínicas pequenas, barbearias, salões, consultórios e assistências técnicas.

> Status atual: Fase 4 implementada no backend. O repositório contém API Spring Boot com Maven Wrapper, PostgreSQL, Flyway, OpenAPI, Actuator, CRUDs iniciais, agendamentos com regra de conflito por profissional, autenticação Bearer JWT, transições operacionais de atendimento, calendário de disponibilidade profissional e dashboard diário. Frontend React e deploy ainda não foram implementados.

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

Implementado na Fase 3:

- Entidade `UserAccount`, enum `UserRole` e migration `V4__auth_users.sql`.
- Login em `POST /api/v1/auth/login`.
- Senhas armazenadas como hash BCrypt.
- JWT assinado com segredo e expiração configurados por ambiente.
- Spring Security protegendo endpoints de negócio com roles `ADMIN` e `ATTENDANT`.
- Respostas `401 Unauthorized` e `403 Forbidden` padronizadas.
- Bearer JWT documentado no Swagger/OpenAPI.
- Bootstrap local opcional de usuários de demonstração sem senha em migration.

Implementado na pré-Fase 4:

- Transições explícitas de confirmação, check-in, conclusão e falta em agendamentos.
- Dia de negócio em `America/Sao_Paulo` reutilizado pela agenda operacional.
- Migration `V5__professional_calendar.sql` com regras semanais de disponibilidade e exceções por data.
- Endpoints para disponibilidade recorrente e exceções `AVAILABLE` ou `BLOCKED` por profissional.
- Constraints e testes com PostgreSQL real para períodos de calendário.

Implementado na Fase 4:

- Endpoint `GET /api/v1/dashboard/daily`.
- Filtros por `date` e `professionalId`.
- Indicadores diários de total de agendamentos, totais por status, cancelamentos e faltas.
- Cálculo de minutos disponíveis a partir da disponibilidade real do calendário profissional.
- Cálculo de minutos ocupados com status `SCHEDULED`, `CONFIRMED`, `CHECKED_IN` e `COMPLETED`.
- Percentual de ocupação com proteção contra divisão por zero.
- Testes unitários, de controller, segurança e integração com PostgreSQL real para o dashboard.

Ainda planejado:

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
- Spring Security
- OAuth2 Resource Server para Bearer JWT
- BCrypt
- JUnit, Mockito, MockMvc e Testcontainers
- Docker Compose para PostgreSQL local

## Stack Planejada

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
    api/                exemplos HTTP por fase do backend
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
- `security`: JWT, autorização, handlers 401/403 e bootstrap local de usuários.

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

Ao executar o backend diretamente pelo Maven, exponha no terminal as variáveis de autenticação necessárias. O arquivo `.env` é lido pelo Docker Compose, mas não é carregado automaticamente pelo processo Java.

### Subir banco local

```bash
docker compose up -d db
```

### Rodar backend

No Windows PowerShell:

```powershell
cd backend
$env:JWT_SECRET = "change_this_local_demo_secret_with_at_least_32_characters"
$env:DEMO_AUTH_USERS_ENABLED = "true"
$env:DEMO_ADMIN_PASSWORD = "change_me_admin"
$env:DEMO_ATTENDANT_PASSWORD = "change_me_attendant"
.\mvnw spring-boot:run
```

No Linux/macOS:

```bash
cd backend
export JWT_SECRET="change_this_local_demo_secret_with_at_least_32_characters"
export DEMO_AUTH_USERS_ENABLED="true"
export DEMO_ADMIN_PASSWORD="change_me_admin"
export DEMO_ATTENDANT_PASSWORD="change_me_attendant"
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
| `JWT_SECRET` | Segredo de assinatura do JWT; deve ter ao menos 32 caracteres. |
| `JWT_EXPIRATION` | Duração ISO-8601 do access token, como `PT8H`. |
| `DEMO_AUTH_USERS_ENABLED` | Habilita bootstrap local de usuários de demonstração. |
| `DEMO_ADMIN_EMAIL` | E-mail do usuário local com role `ADMIN`. |
| `DEMO_ADMIN_PASSWORD` | Senha local usada para gerar hash BCrypt do admin no bootstrap. |
| `DEMO_ATTENDANT_EMAIL` | E-mail do usuário local com role `ATTENDANT`. |
| `DEMO_ATTENDANT_PASSWORD` | Senha local usada para gerar hash BCrypt do atendente no bootstrap. |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para chamadas browser/API, separadas por vírgula. |

## Endpoints Implementados

### Base

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/actuator/health` | Health check da API. |

### Autenticação

| Método | Rota | Objetivo |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autentica usuário ativo e retorna Bearer JWT. |

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
| `PATCH` | `/api/v1/appointments/{id}/confirm` | Confirma agendamento pendente. |
| `PATCH` | `/api/v1/appointments/{id}/check-in` | Registra check-in em agendamento confirmado do dia. |
| `PATCH` | `/api/v1/appointments/{id}/complete` | Conclui agendamento confirmado ou com check-in. |
| `PATCH` | `/api/v1/appointments/{id}/no-show` | Registra falta após o horário final previsto. |

### Calendário Profissional

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/professionals/{professionalId}/availability-rules` | Lista regras semanais de disponibilidade. |
| `POST` | `/api/v1/professionals/{professionalId}/availability-rules` | Cria regra semanal de disponibilidade. |
| `PUT` | `/api/v1/professionals/{professionalId}/availability-rules/{id}` | Atualiza regra semanal. |
| `DELETE` | `/api/v1/professionals/{professionalId}/availability-rules/{id}` | Inativa regra semanal. |
| `GET` | `/api/v1/professionals/{professionalId}/schedule-exceptions?from=&to=` | Lista exceções por período. |
| `POST` | `/api/v1/professionals/{professionalId}/schedule-exceptions` | Cria exceção pontual de agenda. |
| `PUT` | `/api/v1/professionals/{professionalId}/schedule-exceptions/{id}` | Atualiza exceção pontual. |
| `DELETE` | `/api/v1/professionals/{professionalId}/schedule-exceptions/{id}` | Remove exceção pontual. |

### Dashboard

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/dashboard/daily?date=&professionalId=` | Consulta indicadores diários de agenda e ocupação. |

Filtros disponíveis em `GET /api/v1/appointments`:

- `date`: data em formato `YYYY-MM-DD`.
- `professionalId`: id do profissional.
- `customerId`: id do cliente.
- `serviceId`: id do serviço.
- `status`: um dos valores de `AppointmentStatus`.
- Parâmetros de paginação do Spring, como `page`, `size` e `sort`.

Todos os endpoints de negócio exigem Bearer Token. O health check, Swagger/OpenAPI e o endpoint de login permanecem públicos.

O backend aplica CORS em `/api/**` com base em `CORS_ALLOWED_ORIGINS`. O valor padrão local é `http://localhost:5173`, previsto para o frontend React da Fase 5.

## Login E Bearer Token

Defina `JWT_SECRET` antes de iniciar a API. Para login local de demonstração, habilite `DEMO_AUTH_USERS_ENABLED` e informe e-mails e senhas no ambiente; o bootstrap cria hashes BCrypt somente quando os usuários ainda não existem.

Exemplo de login:

```json
{
  "email": "admin@atendeja.local",
  "password": "change_me_admin"
}
```

A resposta contém `accessToken`, `tokenType`, `expiresAt`, `email` e `role`. Envie o token nos endpoints protegidos:

```text
Authorization: Bearer <accessToken>
```

## Matriz De Permissões

| Recurso | `ADMIN` | `ATTENDANT` |
|---|---|---|
| Listar e consultar clientes, profissionais, serviços e agendamentos | Permitido | Permitido |
| Criar, atualizar e inativar clientes | Permitido | Permitido |
| Criar, atualizar e inativar profissionais | Permitido | Negado com `403` |
| Criar, atualizar e inativar serviços | Permitido | Negado com `403` |
| Criar, remarcar, cancelar e avançar status operacionais de agendamentos | Permitido | Permitido |
| Consultar disponibilidade e exceções de agenda | Permitido | Permitido |
| Alterar disponibilidade e exceções de agenda | Permitido | Negado com `403` |
| Consultar dashboard diário | Permitido | Permitido |

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

Nesta pré-Fase 4, a API também confirma agendamentos, registra check-in, conclui atendimentos e marca falta.

## Calendário Profissional

Regras de disponibilidade definem janelas semanais recorrentes por profissional. Exceções de agenda complementam esse calendário por data:

- `BLOCKED`: remove capacidade de um período pontual, como folga ou bloqueio.
- `AVAILABLE`: adiciona capacidade pontual fora da regra recorrente.

As janelas exigem `endTime` posterior a `startTime`. O dashboard diário usa essas janelas reais e normaliza sobreposições antes de transformar capacidade em percentual.

## Dashboard Diário

O endpoint `GET /api/v1/dashboard/daily` aceita:

- `date`: data do dia operacional em formato `YYYY-MM-DD`. Quando ausente, usa a data atual em `America/Sao_Paulo`.
- `professionalId`: id do profissional para filtrar indicadores e capacidade. Quando ausente, considera todos os profissionais ativos.

Indicadores retornados:

- `totalAppointments`: total de agendamentos do dia.
- `appointmentsByStatus`: totais por status.
- `cancellations`: quantidade de agendamentos cancelados.
- `noShows`: quantidade de faltas.
- `availableMinutes`: minutos disponíveis calculados por regras semanais e exceções.
- `occupiedMinutes`: minutos ocupados por `SCHEDULED`, `CONFIRMED`, `CHECKED_IN` e `COMPLETED`.
- `occupancyPercentage`: percentual de ocupação, retornando `0.00` quando não houver minutos disponíveis.

O cálculo de disponibilidade parte das regras semanais ativas do profissional. Exceções `BLOCKED` removem capacidade e exceções `AVAILABLE` adicionam capacidade pontual. Intervalos sobrepostos são normalizados para evitar dupla contagem.

## Roadmap

1. Fase 0: setup do repositório, Docker e documentação inicial.
2. Fase 1: backend base, banco, Flyway e CRUDs iniciais.
3. Estabilização da Fase 1: documentação, Maven Wrapper, Testcontainers e testes ampliados.
4. Fase 2: agendamentos e regra de conflito.
5. Fase 3: autenticação JWT e autorização por roles.
6. Pré-Fase 4: transições operacionais e calendário profissional.
7. Fase 4: dashboard diário e filtros operacionais.
8. Fase 5: frontend React.
9. Fase 6: testes automatizados ampliados.
10. Fase 7: Docker Compose completo, documentação final e preparação para deploy.

## Documentação Complementar

- [Briefing técnico](docs/briefing/atendeja-briefing.md)
- [Roadmap de implementação](docs/implementation-roadmap.md)
- [Exemplos HTTP da Fase 1](docs/api/phase-1-cruds.http)
- [Exemplos HTTP da Fase 2](docs/api/phase-2-appointments.http)
- [Exemplos HTTP da Fase 3](docs/api/phase-3-auth.http)
- [Exemplos HTTP da pré-Fase 4](docs/api/pre-phase-4-operational-calendar.http)
- [Exemplos HTTP da Fase 4](docs/api/phase-4-dashboard.http)
- [ADR 0001 - Arquitetura e stack](docs/adr/0001-architecture-and-stack.md)
- [ADR 0002 - Regra de conflito de agenda](docs/adr/0002-schedule-conflict-rule.md)
- [ADR 0003 - Autenticação JWT e roles](docs/adr/0003-authentication-and-roles.md)
