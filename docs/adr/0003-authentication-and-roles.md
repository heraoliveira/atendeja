# ADR 0003 - Autenticação JWT e roles

## Status

Aceita.

## Contexto

As operações de cadastro e agenda do AtendeJá precisam deixar de ficar públicas antes que o backend avance para dashboard e frontend.

## Decisão

Usar Spring Security como Resource Server JWT com tokens Bearer emitidos pelo próprio backend no endpoint `POST /api/v1/auth/login`.

- Usuários persistidos em `users` com e-mail, hash BCrypt, flag `active` e role `ADMIN` ou `ATTENDANT`.
- Segredo HMAC e expiração do JWT configurados por ambiente.
- Claims do token com subject de e-mail e role para autorização stateless.
- Health check, Swagger/OpenAPI e login públicos; endpoints de negócio autenticados.
- `ADMIN` gerencia profissionais e serviços; `ATTENDANT` recebe `403` nessas operações administrativas.
- Bootstrap local opcional cria usuários de demonstração com senhas vindas do ambiente e nunca grava senha em migration.

## Consequências

- Access tokens expiram e devem ser reenviados como `Authorization: Bearer <accessToken>`.
- Usuário inativo não consegue fazer novo login; tokens já emitidos continuam válidos até expirar nesta fase.
- Refresh token, cadastro público, recuperação de senha e frontend ficam fora desta decisão.
