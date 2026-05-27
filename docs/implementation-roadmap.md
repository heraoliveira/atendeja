# Histórico técnico - AtendeJá

Este documento registra a evolução técnica do AtendeJá em formato resumido. Ele existe como referência de manutenção; a apresentação principal do projeto está no [README](../README.md).

## Linha do tempo técnica

| Área | Entrega consolidada |
|---|---|
| Fundação | Monorepo com backend, frontend, Docker Compose, `.env.example`, ADRs e CI. |
| Backend base | API Spring Boot, PostgreSQL, Flyway, OpenAPI, Actuator e CRUDs de clientes, profissionais e serviços. |
| Agenda | Agendamentos com criação, remarcação, cancelamento, cálculo de horário final e conflito por profissional. |
| Segurança | Login JWT, BCrypt, roles `ADMIN` e `ATTENDANT`, respostas `401`/`403` padronizadas. |
| Operação | Confirmação, check-in, conclusão, no-show, disponibilidade semanal e exceções de agenda. |
| Dashboard | Indicadores diários de agenda, cancelamentos, faltas e ocupação por disponibilidade real. |
| Interface web | React + TypeScript com login, dashboard, agenda, CRUDs e experiência responsiva em PT-BR. |
| Qualidade | Testes automatizados de backend e frontend, Testcontainers com PostgreSQL real e GitHub Actions. |
| Publicação | Dockerfiles, Vercel para frontend, Render para API e PostgreSQL gerenciado documentados. |

## Decisões mantidas

- Controllers permanecem finos; regras de negócio ficam nos services.
- A API usa contratos DTO e respostas paginadas estáveis.
- O banco é versionado por Flyway; migrations aplicadas não devem ser alteradas.
- A regra de agenda usa intervalo semiaberto `[startAt, effectiveEndAt)`.
- Criação e remarcação de agendamentos usam transação e bloqueio pessimista por profissional.
- O frontend reflete permissões e estados operacionais, mas o backend continua como fonte de verdade.

## Evoluções futuras possíveis

As melhorias abaixo são opcionais e não fazem parte da versão atual:

- Deploy automático com secrets de provedores no GitHub Actions.
- Domínio próprio.
- Observabilidade mais completa.
- Refresh token e recuperação de senha.
- Auditoria de autoria das operações.
- Tela administrativa para disponibilidade profissional.
