# Roadmap de Implementação - AtendeJá

## Fase 0 - Setup do repositório, Docker e documentação inicial

Objetivo: preparar a base do repositório, documentar o escopo e disponibilizar PostgreSQL local via Docker.

Critérios de aceite:

- README principal criado em português PT-BR.
- Docker Compose válido com PostgreSQL.
- `.env.example` com variáveis principais.
- ADRs iniciais criadas.
- Nenhum código de backend ou frontend criado.

## Fase 1 - Backend base, banco, Flyway e CRUDs iniciais

Objetivo: criar a API Spring Boot com conexão ao PostgreSQL, migrations iniciais e CRUDs de clientes, profissionais e serviços.

Critérios de aceite:

- Aplicação Spring Boot sobe localmente.
- Flyway aplica `V1__init.sql`.
- CRUDs iniciais funcionam em `/api/v1`.
- Swagger abre localmente.
- Testes básicos passam.

## Fase 2 - Agendamentos e regra de conflito

Objetivo: implementar criação, listagem, remarcação e cancelamento de agendamentos com detecção de conflito por profissional.

Critérios de aceite:

- Criar agendamento válido retorna `201`.
- Tentar horário sobreposto retorna `409`.
- Remarcação recalcula horário final.
- Cancelamento tardio exige motivo.

## Fase 3 - Autenticação JWT e autorização por roles

Objetivo: proteger a API com login JWT, senhas com BCrypt e roles `ADMIN` e `ATTENDANT`.

Critérios de aceite:

- Login retorna token JWT.
- Endpoints protegidos rejeitam requisições sem token.
- Permissões por role funcionam.

## Fase 4 - Dashboard e filtros

Objetivo: implementar agenda diária filtrada e indicadores simples de ocupação, cancelamentos e no-show.

Critérios de aceite:

- Listagem paginada e filtrada.
- Indicadores diários corretos.
- Consultas com índices adequados.

## Fase 5 - Frontend React

Objetivo: criar a interface web em React + TypeScript consumindo a API.

Critérios de aceite:

- Login funcional.
- Agenda diária com filtros.
- Formulários de cliente, profissional, serviço e agendamento.
- Mensagens de erro e sucesso em PT-BR.

## Fase 6 - Testes automatizados

Objetivo: ampliar cobertura dos fluxos críticos.

Critérios de aceite:

- Services testados com JUnit e Mockito.
- API testada com MockMvc.
- Queries e migrations validadas com Testcontainers.
- Fluxos principais do frontend testados.

## Fase 7 - Docker Compose completo, documentação final e preparação para deploy

Objetivo: empacotar API, frontend e banco para execução local completa e preparar documentação de portfólio.

Critérios de aceite:

- `docker compose up --build` sobe a aplicação completa.
- README final contém instruções executáveis.
- Swagger, prints e decisões técnicas documentados.
