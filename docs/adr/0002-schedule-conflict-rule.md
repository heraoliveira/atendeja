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

## Consequências

- Um atendimento que termina exatamente no início de outro não gera conflito.
- A regra pode ser testada com casos unitários e com Testcontainers.
- A primeira implementação deve usar transação no service.
- Uma evolução futura pode usar bloqueio pessimista ou exclusion constraint do PostgreSQL para reforçar concorrência entre requisições simultâneas.
