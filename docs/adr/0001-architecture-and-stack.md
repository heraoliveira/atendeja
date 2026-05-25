# ADR 0001 - Arquitetura e stack

## Status

Aceita.

## Contexto

O AtendeJá precisa demonstrar uma aplicação full stack de portfólio com domínio simples, mas com regra real de negócio: controle de agenda e conflito de horários por profissional.

## Decisão

Usar monorepo com uma fundação backend evolutiva.

Stack implementada até a Fase 7:

- Backend em Java 17 + Spring Boot.
- Build backend com Maven e Maven Wrapper.
- Persistência com PostgreSQL, Spring Data JPA e Flyway.
- API REST versionada em `/api/v1`.
- CRUDs iniciais de clientes, profissionais e serviços.
- Busca de clientes por nome, e-mail e telefone normalizado.
- Contrato paginado estável com `PageResponse<T>` nas listagens.
- Agendamentos com criação, listagem, detalhe, remarcação, cancelamento e transições operacionais.
- Regra de conflito por profissional com cálculo de `endAt` no backend e uso de `completedAt + buffer` após conclusão antecipada.
- Autenticação Bearer JWT com Spring Security, hash BCrypt e roles.
- Calendário profissional com disponibilidade semanal e exceções por data.
- Dashboard diário com indicadores operacionais e ocupação por disponibilidade real.
- Frontend em React + TypeScript com Vite, React Router e React Hook Form.
- Testes automatizados de backend e frontend com JUnit, Mockito, MockMvc, Testcontainers, Vitest e React Testing Library.
- Execução local completa com Docker Compose para PostgreSQL, API e frontend.
- Imagens Docker multi-stage para backend e frontend.
- CI com GitHub Actions para testes, build e validação Docker.
- Documentação em README, Swagger/OpenAPI e ADRs.

Stack planejada para fases futuras:

- Deploy real em provedor externo e automação de CD após configuração segura de secrets.

## Consequências

- A arquitetura fica próxima de projetos corporativos Java.
- A regra de negócio fica concentrada em services, não em controllers.
- O PostgreSQL permite testar consultas, índices e constraints em ambiente real.
- O monorepo facilita execução local e apresentação em portfólio.
- Tecnologias ainda não implementadas ficam documentadas como planejamento, sem sugerir entrega prematura.
