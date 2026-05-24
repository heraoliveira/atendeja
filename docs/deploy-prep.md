# Preparação De Deploy - AtendeJá

Este documento registra a preparação de deploy da Fase 7. Ele não executa deploy automático e não exige credenciais no repositório.

## Princípios

- Nunca versionar `.env`, tokens, senhas reais, chaves JWT ou credenciais de provedor.
- Usar variáveis de ambiente do provedor para banco, JWT, CORS e URL pública da API.
- Validar localmente antes de publicar: testes, build, Docker Compose e health check.
- Manter deploy backend e frontend independentes, porque o frontend precisa conhecer a URL pública da API no momento do build.

## Backend

Opções adequadas para este projeto:

- Render.
- Railway.
- Fly.io.

Artefatos já disponíveis:

- `backend/Dockerfile`.
- Health check em `/actuator/health`.
- Swagger UI em `/swagger-ui.html`.
- OpenAPI JSON em `/v3/api-docs`.
- Migrações Flyway aplicadas automaticamente na inicialização.

Variáveis obrigatórias no provedor:

| Variável | Exemplo | Observação |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://host:5432/atendeja` | Use a URL privada/pública fornecida pelo provedor. |
| `SPRING_DATASOURCE_USERNAME` | `atendeja_user` | Não versionar valor real. |
| `SPRING_DATASOURCE_PASSWORD` | `********` | Secret do provedor. |
| `JWT_SECRET` | `********` | Mínimo de 32 caracteres. |
| `JWT_EXPIRATION` | `PT8H` | Ajuste conforme política do ambiente. |
| `CORS_ALLOWED_ORIGINS` | `https://seu-frontend.vercel.app` | Deve apontar para o domínio público do frontend. |
| `DEMO_AUTH_USERS_ENABLED` | `false` | Em produção, prefira `false`. |

Variáveis opcionais para ambiente de demonstração:

| Variável | Uso |
|---|---|
| `DEMO_ADMIN_EMAIL` | E-mail local/demonstração do usuário `ADMIN`. |
| `DEMO_ADMIN_PASSWORD` | Senha de demonstração, armazenada como secret. |
| `DEMO_ATTENDANT_EMAIL` | E-mail local/demonstração do usuário `ATTENDANT`. |
| `DEMO_ATTENDANT_PASSWORD` | Senha de demonstração, armazenada como secret. |

## Frontend

Opções adequadas:

- Vercel.
- Netlify.

Artefatos já disponíveis:

- `frontend/Dockerfile`, quando o provedor aceitar container.
- Build Vite com `npm run build`.
- Saída estática em `frontend/dist`.

Variável obrigatória no build:

| Variável | Exemplo | Observação |
|---|---|---|
| `VITE_API_BASE_URL` | `https://sua-api.onrender.com` | Deve ser a URL pública do backend acessível pelo browser. |

Configuração típica sem Docker:

- Build command: `npm run build`.
- Publish directory: `dist`.
- Root directory: `frontend`.

## Banco De Dados

Use PostgreSQL gerenciado do provedor ou um add-on equivalente. A aplicação espera:

- Banco PostgreSQL acessível pela API.
- Usuário com permissão para criar/alterar tabelas, porque Flyway aplica migrations.
- Backup e retenção configurados no provedor quando deixar de ser apenas demonstração.

## Checklist Antes Do Deploy

Na máquina local:

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm test
npm run build
```

Na raiz:

```bash
docker compose config --quiet
docker compose up --build
```

Validar:

- `http://localhost:8080/actuator/health`.
- `http://localhost:8080/swagger-ui.html`.
- `http://localhost:5173`.
- Login com usuário de demonstração quando `DEMO_AUTH_USERS_ENABLED=true`.

## Pós-Deploy

Depois de publicar:

- Confirmar health check público da API.
- Abrir Swagger público, se for desejado para portfólio.
- Fazer login no frontend publicado.
- Testar criação de cliente, profissional, serviço e agendamento.
- Testar conflito de horário para confirmar `409 Conflict`.
- Ajustar `CORS_ALLOWED_ORIGINS` se o frontend receber erro de CORS.

## Fora Do Escopo Atual

- Deploy automático por GitHub Actions.
- Domínio customizado.
- Observabilidade avançada.
- Rotação automática de secrets.
- Pipeline com ambientes separados de homologação e produção.
