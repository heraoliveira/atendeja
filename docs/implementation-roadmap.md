# Roadmap de Implementação - AtendeJá

## Fase 0 - Setup do repositório, Docker e documentação inicial

Status: concluída.

Objetivo: preparar a base do repositório, documentar o escopo e disponibilizar PostgreSQL local via Docker.

Critérios de aceite:

- README principal criado em português PT-BR.
- Docker Compose válido com PostgreSQL.
- `.env.example` com variáveis principais.
- ADRs iniciais criadas.
- Nenhum código de backend ou frontend criado.

## Fase 1 - Backend base, banco, Flyway e CRUDs iniciais

Status: concluída.

Objetivo: criar a API Spring Boot com conexão ao PostgreSQL, migrations iniciais e CRUDs de clientes, profissionais e serviços.

Critérios de aceite:

- Aplicação Spring Boot sobe localmente.
- Flyway aplica `V1__init.sql`.
- CRUDs iniciais funcionam em `/api/v1`.
- Swagger abre localmente.
- Testes básicos passam.

## Fase 2 - Agendamentos e regra de conflito

Status: concluída.

Objetivo: implementar criação, listagem, remarcação e cancelamento de agendamentos com detecção de conflito por profissional.

Critérios de aceite:

- Criar agendamento válido retorna `201`.
- Tentar horário sobreposto retorna `409`.
- Remarcação recalcula horário final.
- Cancelamento exige motivo.
- Criação e remarcação usam transação e bloqueio pessimista por profissional.

## Fase 3 - Autenticação JWT e autorização por roles

Status: concluída.

Objetivo: proteger a API com login JWT, senhas com BCrypt e roles `ADMIN` e `ATTENDANT`.

Critérios de aceite:

- Login retorna token JWT.
- Endpoints protegidos rejeitam requisições sem token.
- Permissões por role funcionam.

## Pré-Fase 4 - Fluxo operacional e calendário profissional

Status: concluída.

Objetivo: preparar o domínio do dashboard com transições operacionais de agendamento e disponibilidade real por profissional.

Critérios de aceite:

- Confirmação, check-in, conclusão e falta aplicam transições válidas.
- Regras semanais e exceções por data persistem com constraints de período.
- Escrita do calendário profissional exige role `ADMIN`.
- Dashboard ainda não é exposto nesta etapa.

## Fase 4 - Dashboard e filtros

Status: concluída.

Objetivo: implementar agenda diária filtrada e indicadores simples de ocupação, cancelamentos e no-show.

Critérios de aceite:

- Listagem paginada e filtrada.
- Indicadores diários corretos com total por status, cancelamentos e no-show.
- Ocupação calculada por disponibilidade real, exceções `BLOCKED` e `AVAILABLE` e normalização de intervalos.
- Consultas com índices adequados.

## Fase 5 - Frontend React

Status: concluída.

Objetivo: criar a interface web em React + TypeScript consumindo a API.

Critérios de aceite:

- Login funcional.
- Agenda diária com filtros.
- Formulários de cliente, profissional, serviço e agendamento.
- Mensagens de erro e sucesso em PT-BR.
- Dashboard diário consumindo a API.
- Rotas protegidas por autenticação.
- Ações administrativas ocultas para perfis sem permissão.

## Fase 6 - Testes automatizados

Status: concluída.

Objetivo: ampliar cobertura dos fluxos críticos.

Critérios de aceite:

- Services testados com JUnit e Mockito.
- API testada com MockMvc.
- Queries e migrations validadas com Testcontainers.
- Fluxos principais do frontend testados.
- Login, rotas protegidas, cliente HTTP, dashboard, agenda e formulários principais cobertos com Vitest e React Testing Library.

## Fase 7 - Docker Compose completo, documentação final e preparação para deploy

Status: concluída.

Objetivo: empacotar API, frontend e banco para execução local completa e preparar documentação de portfólio.

Critérios de aceite:

- `docker compose up --build` sobe a aplicação completa.
- README final contém instruções executáveis.
- Swagger, prints e decisões técnicas documentados.
- Logs da API expõem correlation id simples via `X-Correlation-Id`.
- GitHub Actions executa testes/build do backend e frontend.
- Deploy fica preparado com instruções e variáveis, sem secrets reais no repositório.
