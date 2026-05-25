# AtendeJá

AtendeJá é uma aplicação full stack de agenda e fila de atendimento para prestadores locais, como clínicas pequenas, barbearias, salões, consultórios e assistências técnicas.

> Status atual: primeira versão estável v1.0.0 preparada. O repositório contém API Spring Boot com Maven Wrapper, PostgreSQL, Flyway, OpenAPI, Actuator, CRUDs iniciais, busca de clientes por telefone normalizado, agendamentos com regra de conflito por profissional, autenticação Bearer JWT, transições operacionais de atendimento, calendário de disponibilidade profissional, dashboard diário, frontend React + TypeScript funcional, testes automatizados ampliados, Docker Compose completo com API/banco/frontend, CI com GitHub Actions e documentação de deploy real. Deploy automático ainda não foi implementado.

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
- Busca remota de clientes por nome, e-mail e telefone, comparando também telefone normalizado apenas com dígitos.
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
- Regra de conflito por profissional usando intervalo semiaberto `[startAt, effectiveEndAt)`.
- Registro de `completedAt` para liberar agenda após conclusão antecipada, respeitando o buffer do serviço.
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

Implementado na Fase 5:

- Aplicação React + TypeScript em `frontend/` com Vite.
- React Router com rotas protegidas.
- React Hook Form nos formulários principais.
- Login consumindo `POST /api/v1/auth/login`.
- Armazenamento simples do Bearer Token no browser.
- Cliente HTTP centralizado com `VITE_API_BASE_URL`.
- Tratamento de `401`, `403`, erros de validação e conflitos retornados pela API.
- Dashboard diário com filtros por data e profissional.
- Agenda diária com filtros, paginação, criação, remarcação, cancelamento, ações operacionais e status visual derivado `Em atendimento`.
- CRUDs de clientes, profissionais e serviços consumindo os endpoints existentes.
- Ocultação de ações administrativas para `ATTENDANT`, mantendo a API como fonte real de autorização.

Implementado na Fase 6:

- Auditoria e ampliação dos testes automatizados do backend e do frontend.
- Testes unitários adicionais para autenticação em `AuthService`, normalização de e-mail, usuário inativo, senha inválida e emissão de JWT.
- Teste de segurança para rejeição de Bearer Token inválido com `401 Unauthorized`.
- Testes frontend com Vitest e React Testing Library para login, erro de credenciais, token armazenado, logout, rotas protegidas e sessão expirada.
- Testes frontend para cliente HTTP cobrindo `401`, `403`, validação `400` e conflito `409`.
- Testes frontend para dashboard com filtros, agenda diária com filtros, paginação, criação, conflito e ações operacionais.
- Testes frontend para formulários principais de clientes, profissionais e serviços, incluindo criação, edição, inativação e ocultação de ações administrativas para `ATTENDANT`.
- Correção de bugs encontrados pelos testes nos formulários React de edição e no redirecionamento pós-login.

Implementado na Fase 7:

- Dockerfile multi-stage para a API Spring Boot.
- Dockerfile multi-stage para o frontend React com Nginx.
- Docker Compose completo com `db`, `api` e `frontend`.
- Health checks para PostgreSQL, API e frontend.
- Correlation id HTTP com `X-Correlation-Id` nos logs da API.
- `.env.example` revisado com placeholders locais e sem secrets reais.
- GitHub Actions com testes/build do backend, testes/build do frontend e validação/build Docker.
- Documentação final de execução local, CI e preparação de deploy.

Preparado para publicação:

- Deploy real em Render, PostgreSQL gerenciado no Render e frontend na Vercel, condicionado à configuração de contas e secrets fora do repositório.
- Domínio próprio e deploy automático avançado permanecem opcionais.

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
- Logs com correlation id via `X-Correlation-Id`
- Spring Security
- OAuth2 Resource Server para Bearer JWT
- BCrypt
- JUnit, Mockito, MockMvc e Testcontainers
- React
- TypeScript
- Vite
- React Router
- React Hook Form
- Vitest e React Testing Library
- Docker
- Docker Compose completo com PostgreSQL, API e frontend
- Nginx para servir o frontend empacotado
- GitHub Actions

## Stack De Deploy Planejada Para A Primeira Publicação

- Backend Java 17 + Spring Boot no Render.
- PostgreSQL gerenciado no Render.
- Frontend React + TypeScript na Vercel.
- CI/CD com deploy automático somente após configuração segura de credenciais.

## Arquitetura Atual

```text
atendeja/
  backend/              API REST Java 17 + Spring Boot
  docs/
    adr/                decisões técnicas
    api/                exemplos HTTP por fase do backend
    briefing/           contexto do produto e escopo
  frontend/             aplicação React + TypeScript da Fase 5
  .github/workflows/    pipeline CI da Fase 7
  docker-compose.yml    stack local completa com db, api e frontend
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
- Node.js 22 ou compatível com Vite 7 para desenvolvimento frontend fora do Docker.
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

Os valores de `.env.example` são placeholders locais. Troque senhas e `JWT_SECRET` antes de usar qualquer ambiente compartilhado. Ao executar o backend diretamente pelo Maven, exponha no terminal as variáveis de autenticação necessárias. O arquivo `.env` é lido pelo Docker Compose, mas não é carregado automaticamente pelo processo Java.

### Rodar stack completa com Docker

Na raiz do repositório:

```bash
docker compose up --build
```

Em modo destacado:

```bash
docker compose up --build -d
```

Serviços locais:

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- PostgreSQL: `localhost:5432`

Usuários de demonstração são criados pelo backend quando `DEMO_AUTH_USERS_ENABLED=true`:

- `admin@atendeja.local`
- `attendant@atendeja.local`

As senhas vêm de `DEMO_ADMIN_PASSWORD` e `DEMO_ATTENDANT_PASSWORD` no `.env`.

### Subir somente o banco local

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

### Rodar frontend

Em outro terminal, instale as dependências e inicie o Vite:

```powershell
cd frontend
npm install
npm run dev
```

No Linux/macOS:

```bash
cd frontend
npm install
npm run dev
```

Frontend local:

- Aplicação web: `http://localhost:5173`
- Variável da API: `VITE_API_BASE_URL=http://localhost:8080/api/v1`

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

Os testes de integração usam Testcontainers com PostgreSQL real. Docker precisa estar disponível para a JVM. Testes ignorados em `PersistenceIntegrationTest` não devem ser aceitos como sucesso.

### Rodar build e testes do frontend

```bash
cd frontend
npm test
npm run build
```

### Verificar Docker Compose

```bash
docker compose config --quiet
docker compose build api frontend
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

| Variável | Onde configurar | Exemplo seguro | Obrigatória | Uso |
|---|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Render/backend local | `prod` no Render, `dev` local | Sim em produção | Ativa o profile de produção. |
| `DATABASE_URL` | Render/backend local | `jdbc:postgresql://host:5432/atendeja` | Sim em produção | URL JDBC do PostgreSQL usado pela API. |
| `DATABASE_USERNAME` | Render/backend local | `atendeja_user` | Sim em produção | Usuário do PostgreSQL. |
| `DATABASE_PASSWORD` | Render/backend local | `********` | Sim em produção | Senha do PostgreSQL. |
| `PORT` | Render | `10000` | Automática no Render | Porta HTTP usada pelo Render. |
| `SERVER_PORT` | Backend local opcional | `8080` | Não | Porta HTTP alternativa para rodar fora do Docker. |
| `POSTGRES_DB` | Docker Compose local | `atendeja` | Sim local | Nome do banco PostgreSQL local. |
| `POSTGRES_USER` | Docker Compose local | `atendeja_user` | Sim local | Usuário do banco local. |
| `POSTGRES_PASSWORD` | Docker Compose local | `change_me_local_password` | Sim local | Senha local; não usar em produção. |
| `POSTGRES_PORT` | Docker Compose local | `5432` | Não | Porta exposta localmente para o PostgreSQL. |
| `API_PORT` | Docker Compose local | `8080` | Não | Porta da API exposta no host. |
| `FRONTEND_PORT` | Docker Compose local | `5173` | Não | Porta do frontend exposta no host. |
| `VITE_API_BASE_URL` | Vercel/frontend local | `https://sua-api.onrender.com/api/v1` | Sim no frontend | URL base versionada da API embutida no build Vite. |
| `JWT_SECRET` | Render/backend local | `********` | Sim | Segredo de assinatura do JWT; usar no mínimo 32 caracteres. |
| `JWT_EXPIRATION` | Render/backend local | `PT8H` | Sim | Duração ISO-8601 do access token. |
| `DEMO_AUTH_USERS_ENABLED` | Render/backend local | `false` em produção | Não | Habilita bootstrap de usuários de demonstração. |
| `DEMO_ADMIN_EMAIL` | Backend local/demo | `admin@atendeja.local` | Só se demo habilitado | E-mail do usuário com role `ADMIN`. |
| `DEMO_ADMIN_PASSWORD` | Backend local/demo | `********` | Só se demo habilitado | Senha usada para gerar hash BCrypt do admin. |
| `DEMO_ATTENDANT_EMAIL` | Backend local/demo | `attendant@atendeja.local` | Só se demo habilitado | E-mail do usuário com role `ATTENDANT`. |
| `DEMO_ATTENDANT_PASSWORD` | Backend local/demo | `********` | Só se demo habilitado | Senha usada para gerar hash BCrypt do atendente. |
| `CORS_ALLOWED_ORIGINS` | Render | `https://seu-frontend.vercel.app` | Sim | Origens permitidas para chamadas browser/API, separadas por vírgula. |

## CI E Deploy Real

O workflow `.github/workflows/ci.yml` roda em pull requests para `main`, pushes em `main` e tags `v*`:

- Testes do backend com Maven Wrapper.
- Testes do frontend com Vitest.
- Build do frontend com TypeScript e Vite.
- Validação de `docker compose config --quiet`.
- Build das imagens Docker da API e do frontend.

O deploy real da primeira publicação é manual pelas plataformas: PostgreSQL gerenciado e backend no Render, frontend na Vercel. Secrets não ficam no GitHub porque tokens, senhas, chaves JWT e URLs privadas de banco precisam ser configurados nos cofres de variáveis de cada provedor.

Contas necessárias:

- GitHub, para hospedar o repositório e a release.
- Render, para criar o PostgreSQL gerenciado e o Web Service do backend.
- Vercel, para publicar o frontend.

Opcional:

- Domínio próprio.
- Conta paga.
- Deploy automático avançado com secrets de provedor no GitHub Actions.

Ordem recomendada:

1. Criar o PostgreSQL no Render.
2. Criar o backend no Render apontando para `backend/`.
3. Configurar variáveis do backend no Render.
4. Testar `https://sua-api.onrender.com/actuator/health`.
5. Testar `https://sua-api.onrender.com/swagger-ui.html`.
6. Criar o frontend na Vercel apontando para `frontend/`.
7. Configurar `VITE_API_BASE_URL=https://sua-api.onrender.com/api/v1`.
8. Atualizar `CORS_ALLOWED_ORIGINS` no Render com a URL pública da Vercel.
9. Testar login, CRUDs, agendamento, conflito `409` e dashboard.

Backend no Render:

- Root directory: `backend`.
- Build command, se não usar Docker: `./mvnw -B -DskipTests package`.
- Start command, se não usar Docker: `java -jar target/atendeja-api-1.0.0.jar`.
- Health check path: `/actuator/health`.
- Porta: o Render fornece `PORT`; a aplicação também aceita `SERVER_PORT` e mantém `API_PORT` para uso local.
- Banco: use uma URL JDBC em `DATABASE_URL`, como `jdbc:postgresql://host:5432/atendeja`. Se o Render exibir `postgresql://user:pass@host:5432/db`, converta para o formato JDBC e configure usuário/senha em `DATABASE_USERNAME` e `DATABASE_PASSWORD`.
- Flyway aplica as migrations automaticamente na inicialização. Se falhar, verifique logs do Render, permissões do usuário do banco, ordem das migrations e se o banco já possui objetos criados manualmente.

Frontend na Vercel:

- Root directory: `frontend`.
- Install command: `npm ci`.
- Build command: `npm run build`.
- Output directory: `dist`.
- Variável obrigatória: `VITE_API_BASE_URL=https://sua-api.onrender.com/api/v1`.
- O arquivo `frontend/vercel.json` mantém fallback de rotas SPA para `index.html`.

Checklist pós-deploy:

- API responde em `/actuator/health`.
- Swagger abre em `/swagger-ui.html`.
- Login retorna Bearer JWT.
- Frontend carrega na Vercel.
- Frontend chama API sem erro de CORS.
- CRUDs de clientes, profissionais e serviços funcionam.
- Criação de agendamento funciona.
- Conflito de agenda retorna `409 Conflict`.
- Dashboard diário carrega.

Troubleshooting:

- CORS: confirme se `CORS_ALLOWED_ORIGINS` contém exatamente a URL pública da Vercel, sem caminho `/api/v1`.
- Banco: confirme se `DATABASE_URL` está em formato JDBC e se usuário/senha correspondem ao PostgreSQL do Render.
- Flyway: confira se a migration falhou por permissão, objeto pré-existente ou banco errado.
- Frontend sem API: confira se `VITE_API_BASE_URL` foi definida antes do build na Vercel.
- Backend dormindo: em plano gratuito, o primeiro acesso pode demorar.
- `401` ou `403`: refaça login e confira a role do usuário.
- `500` por JWT: confirme se `JWT_SECRET` existe e tem ao menos 32 caracteres.

As instruções detalhadas estão em [docs/deploy-prep.md](docs/deploy-prep.md).

## Frontend Implementado

O frontend da Fase 5 é uma aplicação operacional, sem landing page. A primeira tela é o login e, após autenticação, o usuário acessa o painel com navegação lateral.

Telas disponíveis:

- Login.
- Dashboard diário.
- Agenda diária.
- Clientes.
- Profissionais.
- Serviços.

Fluxos disponíveis:

- Login com Bearer JWT.
- Logout.
- Consulta de indicadores diários por data e profissional.
- Listagem da agenda com filtros por data, profissional, cliente, serviço e status.
- Busca remota de clientes por nome, e-mail ou telefone sem máscara na criação e no filtro de agendamentos.
- Criação, remarcação, cancelamento, confirmação, check-in, conclusão e falta em agendamentos.
- CRUD de clientes.
- CRUD administrativo de profissionais e serviços.

Textos visíveis, mensagens e feedbacks ficam em português PT-BR. Código, rotas, payloads e identificadores técnicos permanecem em inglês.

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
| `GET` | `/api/v1/customers/search?q=` | Busca clientes ativos para autocomplete por nome, e-mail ou telefone normalizado. |
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
| `PATCH` | `/api/v1/appointments/{id}/check-in` | Registra check-in em agendamento confirmado dentro da janela operacional. |
| `PATCH` | `/api/v1/appointments/{id}/complete` | Conclui agendamento com check-in e horário inicial já alcançado. |
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

O intervalo é tratado como semiaberto: `[startAt, effectiveEndAt)`. Isso significa que um agendamento pode começar exatamente no horário em que outro termina, sem gerar conflito.

Para agendamentos ainda abertos, `effectiveEndAt` é o `endAt` previsto, calculado com duração e buffer do serviço. Para agendamentos concluídos com `completedAt`, a agenda fica bloqueada até `completedAt + service.bufferMinutes`. Assim, uma conclusão antecipada libera novos horários depois do intervalo técnico do serviço, sem esperar até o fim previsto originalmente.

A condição usada para detectar conflito é:

```sql
existing.start_at < :newEndAt
AND existing.effective_end_at > :newStartAt
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

O fluxo operacional esperado é `SCHEDULED` -> `CONFIRMED` -> `CHECKED_IN` -> `COMPLETED`. A interface exibe `CHECKED_IN` como "Check-in realizado" antes do início agendado e como "Em atendimento" quando o horário atual é igual ou posterior ao `startAt`. Esse "Em atendimento" é um status visual derivado; o valor persistido continua sendo `CHECKED_IN`.

O check-in só é permitido para agendamentos `CONFIRMED`, dentro da janela de 60 minutos antes do `startAt` até o `endAt` previsto. A conclusão só é permitida para agendamentos `CHECKED_IN` quando o horário atual do servidor é igual ou posterior ao `startAt`. Agendamentos `COMPLETED`, `CANCELED` e `NO_SHOW` são estados fechados para novas transições operacionais, salvo regras explícitas já implementadas.

## Busca De Clientes

Clientes podem ser pesquisados por nome, e-mail ou telefone. Para telefone, o backend compara também uma versão normalizada apenas com dígitos usando PostgreSQL, então um cliente salvo como `(67) 2643-1365` pode ser encontrado por `672643`, `26431365` ou `6726431365`.

Essa regra vale para a listagem de clientes e para o autocomplete remoto usado na agenda diária, mantendo paginação e limite de resultados.

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
8. Fase 5: frontend React concluído.
9. Fase 6: testes automatizados ampliados concluídos.
10. Fase 7: Docker Compose completo, documentação final, CI e preparação para deploy concluídos.

## Documentação Complementar

- [Briefing técnico](docs/briefing/atendeja-briefing.md)
- [Roadmap de implementação](docs/implementation-roadmap.md)
- [Exemplos HTTP da Fase 1](docs/api/phase-1-cruds.http)
- [Exemplos HTTP da Fase 2](docs/api/phase-2-appointments.http)
- [Exemplos HTTP da Fase 3](docs/api/phase-3-auth.http)
- [Exemplos HTTP da pré-Fase 4](docs/api/pre-phase-4-operational-calendar.http)
- [Exemplos HTTP da Fase 4](docs/api/phase-4-dashboard.http)
- [Preparação de deploy](docs/deploy-prep.md)
- [Documentação do frontend](frontend/README.md)
- [ADR 0001 - Arquitetura e stack](docs/adr/0001-architecture-and-stack.md)
- [ADR 0002 - Regra de conflito de agenda](docs/adr/0002-schedule-conflict-rule.md)
- [ADR 0003 - Autenticação JWT e roles](docs/adr/0003-authentication-and-roles.md)
