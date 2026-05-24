# Frontend AtendeJá

Aplicação web da Fase 5 do AtendeJá, fortalecida na Fase 6 com testes automatizados em Vitest e React Testing Library e empacotada na Fase 7 com Docker e Nginx.

## Status Atual

Implementado:

- Login consumindo `POST /api/v1/auth/login`.
- Armazenamento simples do Bearer Token em `localStorage`.
- Rotas protegidas por autenticação.
- Logout e tratamento de respostas `401` e `403`.
- Cliente HTTP centralizado com `VITE_API_BASE_URL`.
- Dashboard diário consumindo `GET /api/v1/dashboard/daily`.
- Agenda diária com filtros, paginação, criação, remarcação, cancelamento e ações operacionais.
- CRUDs funcionais de clientes, profissionais e serviços.
- Restrições visuais por role para ações administrativas.
- Mensagens, labels, filtros e feedback em português PT-BR.
- Testes automatizados para login, logout, rotas protegidas, cliente HTTP, dashboard, agenda e formulários principais.
- Dockerfile multi-stage para gerar o build estático e servir com Nginx.

Não implementado nesta fase:

- Refresh token.
- Cadastro público.
- Recuperação de senha.
- Frontend para disponibilidade profissional.
- Deploy automático.

## Pré-Requisitos

- Node.js 22 ou compatível com Vite 7.
- API Spring Boot em execução.
- Usuários locais criados pelo backend com `DEMO_AUTH_USERS_ENABLED=true`.
- Docker Desktop ou Docker Engine com Docker Compose para execução conteinerizada.

## Configuração

Crie o arquivo de ambiente do frontend:

```powershell
Copy-Item .env.example .env
```

No Linux/macOS:

```bash
cp .env.example .env
```

Variável disponível:

| Variável | Uso |
|---|---|
| `VITE_API_BASE_URL` | URL base da API Spring Boot consumida pelo browser. |

Valor local padrão:

```text
VITE_API_BASE_URL=http://localhost:8080
```

O backend deve permitir a origem do Vite em `CORS_ALLOWED_ORIGINS`, por exemplo:

```text
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

## Comandos

Instalar dependências:

```bash
npm install
```

Rodar em desenvolvimento:

```bash
npm run dev
```

Build de produção:

```bash
npm run build
```

Testes automatizados:

```bash
npm test
```

A suíte cobre utilitários, cliente HTTP, autenticação, rotas protegidas, dashboard, agenda e formulários de clientes, profissionais e serviços.

Rodar pelo Docker Compose completo, a partir da raiz do repositório:

```bash
docker compose up --build
```

No Docker local, o frontend fica em `http://localhost:5173` e consome a API configurada em `VITE_API_BASE_URL`.

## Fluxos Disponíveis

- Entrar com e-mail e senha de usuário existente.
- Consultar dashboard diário por data e profissional.
- Listar agenda diária com filtros por data, profissional, cliente, serviço e status.
- Criar agendamento com cliente, profissional, serviço e horário inicial.
- Remarcar, cancelar, confirmar, registrar check-in, concluir e marcar falta quando a API permitir.
- Criar, editar, listar e inativar clientes.
- Criar, editar, listar e inativar profissionais como `ADMIN`.
- Criar, editar, listar e inativar serviços como `ADMIN`.

As regras de negócio continuam no backend. O frontend apenas orienta a experiência e mostra feedback em português PT-BR.
