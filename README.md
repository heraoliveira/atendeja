# AtendeJá

AtendeJá é uma aplicação full stack para gestão de agenda e fila de atendimento de prestadores locais, como clínicas pequenas, barbearias, salões, consultórios e assistências técnicas.

- Demonstração: https://atendeja.vercel.app/
- API local: `http://localhost:8080`
- Frontend local: `http://localhost:5173`
- Swagger local: `http://localhost:8080/swagger-ui.html`

## Visão geral

O projeto reúne uma API Java/Spring Boot, uma interface React/Vite e um banco PostgreSQL versionado por Flyway. A aplicação cobre o fluxo operacional de atendimento: autenticação, cadastros base, agenda diária, conflito de horários por profissional, check-in, conclusão, registro de falta e dashboard.

O foco técnico está em regras reais de agenda, consistência transacional, validação server-side, integração frontend/API, testes automatizados e documentação suficiente para execução local e deploy.

## Acesso de demonstração

A conta abaixo é pública e existe apenas para avaliação do projeto:

```text
E-mail: demo@atendeja.com
Senha: Demo@AtendeJa
```

No frontend publicado, a tela de login exibe um painel de acesso demo com preenchimento automático. Caso a conta demo seja alterada em ambiente externo, mantenha as variáveis do backend e do frontend sincronizadas.

## Funcionalidades principais

- Login com Bearer JWT e autorização por roles `ADMIN` e `ATTENDANT`.
- CRUD de clientes, profissionais e serviços.
- Busca de clientes por nome, e-mail e telefone normalizado.
- Criação, listagem, remarcação e cancelamento de agendamentos.
- Validação de conflito de agenda por profissional.
- Check-in, conclusão de atendimento e registro de no-show.
- Status visual “Em atendimento” no frontend quando aplicável.
- Calendário profissional com disponibilidade semanal e exceções.
- Dashboard diário com indicadores de agenda e ocupação.
- Interface web em português brasileiro.

## Tecnologias

**Backend**

- Java 17, Spring Boot, Spring Web, Spring Data JPA, Bean Validation.
- Spring Security, OAuth2 Resource Server, JWT e BCrypt.
- PostgreSQL, Flyway, Springdoc OpenAPI e Actuator.
- JUnit, Mockito, MockMvc e Testcontainers.

**Frontend**

- React, TypeScript, Vite, React Router e React Hook Form.
- Vitest e React Testing Library.
- CSS global com layout responsivo.

**Infraestrutura**

- Docker e Docker Compose.
- Dockerfiles multi-stage para API e frontend.
- GitHub Actions para testes, build e validação Docker.

## Arquitetura

```text
atendeja/
  backend/              API REST Java 17 + Spring Boot
  frontend/             aplicação web React + TypeScript
  docs/                 decisões técnicas, deploy e histórico técnico
  docker-compose.yml    stack local com PostgreSQL, API e frontend
  .env.example          variáveis de ambiente de exemplo
```

No backend, controllers coordenam HTTP, services concentram regras de negócio e repositories cuidam da persistência. DTOs isolam o contrato da API e mappers mantêm a conversão entre entidades e payloads.

No frontend, o cliente HTTP centraliza autenticação, tratamento de erros e URL da API. Rotas protegidas direcionam o usuário para a experiência operacional após login.

## Aplicação web

A interface começa pela tela de login e, após autenticação, apresenta navegação lateral para dashboard, agenda, clientes, profissionais e serviços. O layout é responsivo e mantém textos visíveis em português PT-BR.

O painel de login inclui acesso de demonstração para avaliadores e recrutadores. Essa conta é intencionalmente pública e não representa credencial real de produção.

## Execução local

### Pré-requisitos

- Java 17.
- Node.js 22 ou compatível com Vite 7.
- Docker Desktop ou Docker Engine com Docker Compose.
- Git.

### Subir a stack completa

Copie o arquivo de exemplo quando quiser customizar variáveis locais:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Suba banco, API e frontend:

```bash
docker compose up --build
```

Serviços locais:

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

Para encerrar:

```bash
docker compose down
```

### Rodar Separadamente

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm ci
npm run dev
```

## Variáveis de ambiente

O arquivo `.env.example` contém placeholders seguros para execução local. Não versione `.env`, tokens, senhas reais ou secrets de provedores.

| Variável | Ambiente | Exemplo seguro | Uso |
|---|---|---|---|
| `DATABASE_URL` | Backend/Render | `jdbc:postgresql://host:5432/atendeja` | URL JDBC do PostgreSQL. |
| `DATABASE_USERNAME` | Backend/Render | `atendeja_user` | Usuário do banco. |
| `DATABASE_PASSWORD` | Backend/Render | `********` | Senha do banco. |
| `JWT_SECRET` | Backend/Render | `********` | Segredo JWT com no mínimo 32 caracteres. |
| `JWT_EXPIRATION` | Backend/Render | `PT8H` | Duração do token. |
| `CORS_ALLOWED_ORIGINS` | Backend/Render | `https://atendeja.vercel.app` | Origem pública permitida para o frontend. |
| `VITE_API_BASE_URL` | Frontend/Vercel | `https://sua-api.onrender.com/api/v1` | URL base versionada da API. |
| `DEMO_ADMIN_EMAIL` | Backend demo | `demo@atendeja.com` | E-mail da conta demo criada pelo backend. |
| `DEMO_ADMIN_PASSWORD` | Backend demo | `Demo@AtendeJa` | Senha demo pública para portfólio. |
| `VITE_DEMO_ADMIN_EMAIL` | Frontend demo | `demo@atendeja.com` | E-mail exibido no login demo. |
| `VITE_DEMO_ADMIN_PASSWORD` | Frontend demo | `Demo@AtendeJa` | Senha exibida no login demo. |

## Testes e qualidade

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend
npm test
npm run build
```

Validação do Docker Compose:

```bash
docker compose config --quiet
```

O pipeline de GitHub Actions executa testes do backend, testes do frontend, build do frontend, validação do Docker Compose e build das imagens Docker.

## Deploy e infraestrutura

O projeto está preparado para publicação com:

- Frontend na Vercel.
- Backend Java/Spring Boot no Render.
- PostgreSQL gerenciado no Render.

As credenciais de produção devem ser configuradas nos painéis do Render e da Vercel, nunca no repositório. O deploy automático não está habilitado porque exigiria secrets de provedor no GitHub Actions.

O passo a passo operacional está em [docs/deploy-prep.md](docs/deploy-prep.md).

## Documentação técnica

- [Backend](backend/README.md)
- [Frontend](frontend/README.md)
- [Briefing do produto](docs/briefing/atendeja-briefing.md)
- [Deploy real](docs/deploy-prep.md)
- [Histórico técnico](docs/implementation-roadmap.md)
- [ADR 0001 - Arquitetura e stack](docs/adr/0001-architecture-and-stack.md)
- [ADR 0002 - Regra de conflito de agenda](docs/adr/0002-schedule-conflict-rule.md)
- [ADR 0003 - Autenticação JWT e roles](docs/adr/0003-authentication-and-roles.md)

## Segurança

- Secrets reais não são versionados.
- `.env` está no `.gitignore`.
- Senhas de usuários reais devem ser armazenadas como hash BCrypt.
- A conta demo é pública por decisão de portfólio e deve ser usada apenas em ambiente de demonstração.
