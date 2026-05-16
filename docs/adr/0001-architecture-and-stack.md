# ADR 0001 - Arquitetura e stack

## Status

Aceita.

## Contexto

O AtendeJá precisa demonstrar uma aplicação full stack de portfólio com domínio simples, mas com regra real de negócio: controle de agenda e conflito de horários por profissional.

## Decisão

Usar monorepo com:

- Backend em Java 17 + Spring Boot.
- Build backend com Maven.
- Persistência com PostgreSQL, Spring Data JPA e Flyway.
- API REST versionada em `/api/v1`.
- Autenticação com Spring Security + JWT.
- Frontend em React + TypeScript.
- Execução local com Docker Compose.
- Documentação em README, Swagger/OpenAPI e ADRs.

## Consequências

- A arquitetura fica próxima de projetos corporativos Java.
- A regra de negócio fica concentrada em services, não em controllers.
- O PostgreSQL permite testar consultas, índices e constraints em ambiente real.
- O monorepo facilita execução local e apresentação em portfólio.
