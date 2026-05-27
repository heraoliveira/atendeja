# Briefing do produto - AtendeJá

## Resumo

AtendeJá é uma aplicação full stack para prestadores locais que precisam organizar agenda, fila de atendimento e indicadores básicos de operação diária.

O sistema atende negócios como clínicas pequenas, barbearias, salões, consultórios e assistências técnicas com atendimento por horário.

## Problema

Prestadores locais costumam perder produtividade por conflitos de agenda, remarcações mal registradas, falta de controle de status do atendimento e baixa visibilidade da ocupação diária.

## Proposta

O AtendeJá centraliza clientes, profissionais, serviços e agendamentos em uma experiência web simples. A aplicação valida conflitos por profissional, registra o avanço operacional dos atendimentos e apresenta um dashboard diário para acompanhamento.

## Usuários

- `ADMIN`: gerencia cadastros, profissionais, serviços, calendário e agenda.
- `ATTENDANT`: opera clientes, agenda, check-in, conclusão e dashboard.

## Capacidades do produto

- Autenticação com JWT.
- Gestão de clientes, profissionais e serviços.
- Agenda diária com filtros.
- Criação, remarcação e cancelamento de agendamentos.
- Validação de conflito de horários por profissional.
- Check-in, conclusão e registro de no-show.
- Calendário profissional com disponibilidade e exceções.
- Dashboard diário com ocupação, cancelamentos e faltas.
- Frontend responsivo em português brasileiro.

## Diferenciais técnicos

- Regras de negócio concentradas no backend.
- Persistência relacional com PostgreSQL e Flyway.
- Validação de conflitos com intervalo semiaberto.
- Bloqueio pessimista por profissional para reduzir risco de concorrência.
- Testes automatizados de backend e frontend.
- Testcontainers para validar PostgreSQL real.
- Docker Compose para execução local completa.

## Stack

- Backend: Java 17, Spring Boot, Spring Security, JPA, Flyway e PostgreSQL.
- Frontend: React, TypeScript, Vite, React Router e React Hook Form.
- Qualidade: JUnit, Mockito, MockMvc, Testcontainers, Vitest e React Testing Library.
- Infraestrutura: Docker, Docker Compose, GitHub Actions, Render e Vercel.

## Escopo atual

A versão atual entrega uma aplicação funcional para demonstração e avaliação técnica. Melhorias futuras podem incluir domínio próprio, observabilidade avançada, refresh token, recuperação de senha e automação de deploy com secrets de provedores.
