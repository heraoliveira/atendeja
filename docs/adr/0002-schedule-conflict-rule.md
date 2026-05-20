# ADR 0002 - Regra de conflito de agenda

## Status

Aceita.

## Contexto

O principal diferencial técnico do AtendeJá é impedir que um mesmo profissional tenha dois agendamentos ativos no mesmo intervalo de tempo.

## Decisão

Representar agendamentos com `startAt` e `endAt`, tratando intervalos como semiabertos: `[startAt, endAt)`.

Um novo agendamento conflita com outro existente quando:

```sql
existing.start_at < :newEndAt
AND existing.end_at > :newStartAt
AND existing.professional_id = :professionalId
AND existing.status NOT IN ('CANCELED', 'NO_SHOW')
```

O backend será responsável por calcular `endAt` com base na duração do serviço e no intervalo entre atendimentos.

As operações de criação e remarcação devem executar em transação e adquirir bloqueio pessimista no registro do profissional antes da consulta de conflito. Esse lock serializa tentativas simultâneas de agendamento para o mesmo profissional sem bloquear profissionais diferentes.

## Consequências

- Um atendimento que termina exatamente no início de outro não gera conflito.
- A regra pode ser testada com casos unitários e com Testcontainers.
- A implementação usa transação no service e bloqueio pessimista por profissional para reduzir risco de duplo agendamento em requisições concorrentes.
- Uma evolução futura pode usar exclusion constraint do PostgreSQL com range types caso o projeto precise reforçar a regra diretamente no banco.
