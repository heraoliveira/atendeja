# Frontend AtendeJá

Aplicação web do AtendeJá, construída com React, TypeScript e Vite.

## Visão geral

O frontend entrega a experiência operacional do sistema: login, dashboard diário, agenda, clientes, profissionais e serviços. A interface consome a API Spring Boot, protege rotas autenticadas e exibe mensagens em português brasileiro.

Principais recursos:

- Login com Bearer Token.
- Painel de acesso demo com preenchimento automático.
- Rotas protegidas por autenticação.
- Dashboard diário com filtros.
- Agenda diária com filtros, paginação e ações operacionais.
- CRUD de clientes, profissionais e serviços.
- Busca remota de clientes por nome, e-mail e telefone.
- Tratamento de erros `400`, `401`, `403` e `409`.
- Layout responsivo.

## Stack

- React.
- TypeScript.
- Vite.
- React Router.
- React Hook Form.
- Lucide React.
- Vitest e React Testing Library.

## Configuração

Variáveis lidas pelo Vite:

| Variável | Uso |
|---|---|
| `VITE_API_BASE_URL` | URL base versionada da API. |
| `VITE_DEMO_ADMIN_EMAIL` | E-mail exibido no painel demo. |
| `VITE_DEMO_ADMIN_PASSWORD` | Senha exibida no painel demo. |

Exemplo local:

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_DEMO_ADMIN_EMAIL=demo@atendeja.com
VITE_DEMO_ADMIN_PASSWORD=Demo@AtendeJa
```

Se a conta demo for alterada no backend, mantenha `VITE_DEMO_ADMIN_EMAIL` e `VITE_DEMO_ADMIN_PASSWORD` com os mesmos valores usados em `DEMO_ADMIN_EMAIL` e `DEMO_ADMIN_PASSWORD`.

## Comandos

Instalar dependências:

```bash
npm ci
```

Rodar em desenvolvimento:

```bash
npm run dev
```

Executar testes:

```bash
npm test
```

Gerar build de produção:

```bash
npm run build
```

## Integração com a API

O cliente HTTP centralizado usa `VITE_API_BASE_URL`, injeta o token quando há sessão válida e limpa a sessão ao receber `401`. Erros da API são exibidos com mensagens amigáveis ao usuário.

O backend deve permitir a origem do frontend via `CORS_ALLOWED_ORIGINS`. Para desenvolvimento local, use:

```text
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

## Deploy na Vercel

Configuração recomendada:

- Root directory: `frontend`.
- Install command: `npm ci`.
- Build command: `npm run build`.
- Output directory: `dist`.
- Variável obrigatória: `VITE_API_BASE_URL=https://sua-api.onrender.com/api/v1`.
- Variáveis opcionais: `VITE_DEMO_ADMIN_EMAIL` e `VITE_DEMO_ADMIN_PASSWORD`.

O arquivo `vercel.json` mantém fallback de rotas SPA para `index.html`, evitando erro ao recarregar páginas internas.

## Testes cobertos

A suíte cobre login, logout, rotas protegidas, cliente HTTP, dashboard, agenda diária, autocomplete de clientes e formulários principais.
