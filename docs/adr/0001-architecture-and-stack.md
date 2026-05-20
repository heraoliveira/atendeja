# ADR 0001 - Arquitetura e stack

## Status

Aceita.

## Contexto

O AtendeJá precisa demonstrar uma aplicação full stack de portfólio com domínio simples, mas com regra real de negócio: controle de agenda e conflito de horários por profissional.

## Decisão

Usar monorepo com uma fundação backend evolutiva.

Stack implementada até a Fase 2:

- Backend em Java 17 + Spring Boot.
- Build backend com Maven e Maven Wrapper.
- Persistência com PostgreSQL, Spring Data JPA e Flyway.
- API REST versionada em `/api/v1`.
- CRUDs iniciais de clientes, profissionais e serviços.
- Contrato paginado estável com `PageResponse<T>` nas listagens.
- Agendamentos com criação, listagem, detalhe, remarcação e cancelamento.
- Regra de conflito por profissional com cálculo de `endAt` no backend.
- Execução local do PostgreSQL com Docker Compose.
- Documentação em README, Swagger/OpenAPI e ADRs.

Stack planejada para fases futuras:

- Autenticação com Spring Security + JWT em fase posterior.
- Frontend em React + TypeScript em fase posterior.
- Dashboard operacional em fase posterior.
- Docker Compose completo, CI/CD e deploy em fase posterior.

## Consequências

- A arquitetura fica próxima de projetos corporativos Java.
- A regra de negócio fica concentrada em services, não em controllers.
- O PostgreSQL permite testar consultas, índices e constraints em ambiente real.
- O monorepo facilita execução local e apresentação em portfólio.
- Tecnologias ainda não implementadas ficam documentadas como planejamento, sem sugerir entrega prematura.
