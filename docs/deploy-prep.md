# Deploy Real - AtendeJá

Este documento descreve a publicação inicial do AtendeJá com backend no Render, PostgreSQL gerenciado no Render e frontend na Vercel. Ele não contém secrets reais e não configura deploy automático por GitHub Actions.

## Princípios

- Nunca versionar `.env`, tokens, senhas reais, chaves JWT ou credenciais de provedor.
- Configurar secrets no painel do Render e da Vercel.
- Manter execução local preservada com `docker compose up --build`.
- Publicar backend e frontend separadamente, porque o frontend Vite precisa conhecer a URL pública da API no momento do build.
- Usar domínio próprio apenas se desejar; não é obrigatório para a primeira publicação.

## Contas Necessárias

- GitHub, para hospedar o código, tags e releases.
- Render, para PostgreSQL gerenciado e backend Java/Spring Boot.
- Vercel, para frontend React + TypeScript.

## Ordem Do Deploy

1. Criar PostgreSQL gerenciado no Render.
2. Criar Web Service do backend no Render.
3. Configurar variáveis do backend no Render.
4. Testar health check e Swagger do backend.
5. Criar projeto do frontend na Vercel.
6. Configurar `VITE_API_BASE_URL` na Vercel.
7. Atualizar `CORS_ALLOWED_ORIGINS` no backend com a URL pública da Vercel.
8. Testar login, CRUDs, agenda, conflito de horários e dashboard.

## PostgreSQL No Render

1. No Render, crie um novo PostgreSQL.
2. Escolha a mesma região que será usada pelo backend, quando possível.
3. Após a criação, abra a área de conexão do banco.
4. Use a conexão interna para o backend, porque ela evita tráfego público entre serviços Render.
5. Configure no backend:
   - `DATABASE_URL`: URL JDBC do banco, por exemplo `jdbc:postgresql://host-interno:5432/atendeja`.
   - `DATABASE_USERNAME`: usuário do banco.
   - `DATABASE_PASSWORD`: senha do banco.

O Render costuma exibir URLs no formato `postgresql://user:password@host:5432/database`. Para esta aplicação, use formato JDBC em `DATABASE_URL`: `jdbc:postgresql://host:5432/database`, mantendo usuário e senha nas variáveis separadas.

Flyway roda automaticamente na inicialização do backend. Se uma migration falhar:

- Verifique se a API está conectando no banco correto.
- Verifique se o usuário tem permissão para criar e alterar tabelas.
- Verifique se não existem tabelas criadas manualmente com nomes conflitantes.
- Leia o log do Render para identificar a migration exata que falhou.
- Não edite migrations já aplicadas em produção; crie uma nova migration corretiva.

## Backend Spring Boot No Render

Opção recomendada para esta primeira publicação: Web Service conectado ao repositório GitHub.

Configuração sem Docker:

- Root directory: `backend`.
- Build command: `./mvnw -B -DskipTests package`.
- Start command: `java -jar target/atendeja-api-1.0.0.jar`.
- Health check path: `/actuator/health`.

Configuração com Docker:

- Root directory: `backend`.
- Dockerfile: `backend/Dockerfile`.
- O Dockerfile usa Java 17, gera o `.jar` com Maven Wrapper e executa a API com `java -jar`.

Variáveis obrigatórias no Render:

| Variável | Exemplo seguro | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Ativa configurações de produção. |
| `DATABASE_URL` | `jdbc:postgresql://host:5432/atendeja` | URL JDBC do PostgreSQL gerenciado. |
| `DATABASE_USERNAME` | `atendeja_user` | Usuário do banco. |
| `DATABASE_PASSWORD` | `********` | Senha do banco. |
| `JWT_SECRET` | `********` | Segredo HMAC com ao menos 32 caracteres. |
| `JWT_EXPIRATION` | `PT8H` | Duração do token JWT em formato ISO-8601. |
| `CORS_ALLOWED_ORIGINS` | `https://seu-frontend.vercel.app` | Origem pública do frontend. |

Variáveis opcionais:

| Variável | Exemplo seguro | Uso |
|---|---|---|
| `PORT` | Definida pelo Render | Porta HTTP. A aplicação lê automaticamente. |
| `DEMO_AUTH_USERS_ENABLED` | `false` | Em produção, prefira `false`. |
| `DEMO_ADMIN_EMAIL` | `admin@atendeja.local` | Usuário de demonstração quando bootstrap estiver habilitado. |
| `DEMO_ADMIN_PASSWORD` | `********` | Senha de demonstração em secret do Render. |
| `DEMO_ATTENDANT_EMAIL` | `attendant@atendeja.local` | Usuário atendente de demonstração. |
| `DEMO_ATTENDANT_PASSWORD` | `********` | Senha de demonstração em secret do Render. |

URLs esperadas após o deploy:

- Health check: `https://sua-api.onrender.com/actuator/health`.
- Swagger UI: `https://sua-api.onrender.com/swagger-ui.html`.
- OpenAPI JSON: `https://sua-api.onrender.com/v3/api-docs`.
- Login: `POST https://sua-api.onrender.com/api/v1/auth/login`.

## Frontend React + TypeScript Na Vercel

1. Na Vercel, importe o repositório GitHub.
2. Configure:
   - Framework preset: Vite.
   - Root directory: `frontend`.
   - Install command: `npm ci`.
   - Build command: `npm run build`.
   - Output directory: `dist`.
3. Adicione a variável:
   - `VITE_API_BASE_URL=https://sua-api.onrender.com/api/v1`.
4. Faça o deploy.

O Vite só expõe ao bundle variáveis com prefixo `VITE_`. Por isso, a URL da API precisa ser `VITE_API_BASE_URL`. O arquivo `frontend/vercel.json` direciona rotas internas da SPA para `index.html`, evitando erro ao recarregar páginas protegidas.

Depois que a Vercel gerar a URL pública, volte ao Render e atualize:

```text
CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app
```

Não inclua `/api/v1` em `CORS_ALLOWED_ORIGINS`; CORS usa apenas origem, com protocolo, host e porta.

## Execução Local Preservada

Na raiz:

```bash
docker compose up --build
```

URLs locais:

- Frontend: `http://localhost:5173`.
- API: `http://localhost:8080`.
- Health check: `http://localhost:8080/actuator/health`.
- Swagger UI: `http://localhost:8080/swagger-ui.html`.

O arquivo `.env.example` contém placeholders seguros para desenvolvimento local. Copie para `.env` quando precisar:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

## Checklist Pós-Deploy

- API responde `UP` em `/actuator/health`.
- Swagger abre em `/swagger-ui.html`.
- `POST /api/v1/auth/login` retorna Bearer JWT.
- Frontend carrega na Vercel.
- Frontend chama a API sem erro de CORS.
- CRUDs de clientes, profissionais e serviços funcionam.
- Busca de clientes funciona por nome, e-mail e telefone sem máscara.
- Criação de agendamento funciona.
- Conflito de horário por profissional retorna `409 Conflict`.
- Check-in e conclusão respeitam as regras de horário.
- Dashboard diário carrega.

## Troubleshooting

### Erro De CORS

- Confirme se `CORS_ALLOWED_ORIGINS` no Render contém a URL exata da Vercel.
- Não use `*` em produção.
- Não inclua caminho `/api/v1` no CORS.

### Erro De Conexão Com Banco

- Confirme se `DATABASE_URL` é JDBC.
- Confirme usuário e senha.
- Prefira conexão interna do Render quando backend e banco estiverem no mesmo provedor e região.

### Flyway Falhando

- Verifique a migration informada no log.
- Confirme permissões do usuário.
- Não altere migrations já aplicadas; crie uma nova migration.

### Frontend Aponta Para Localhost

- Confirme se `VITE_API_BASE_URL` foi configurada na Vercel antes do build.
- Refaça o deploy do frontend após alterar a variável.

### Backend Demora No Primeiro Acesso

- Em plano gratuito, serviços podem dormir e acordar no primeiro request.

### Erros 401 Ou 403

- Faça login novamente.
- Verifique se o usuário existe, está ativo e tem a role esperada.

### Erro 500 Por JWT

- Confirme `JWT_SECRET`.
- Use pelo menos 32 caracteres.
- Não deixe valor vazio em produção.

## Fora Do Escopo Da Primeira Publicação

- Deploy automático por GitHub Actions.
- Domínio customizado.
- Observabilidade avançada.
- Rotação automática de secrets.
- Ambientes separados de homologação e produção.

## Referências Oficiais

- Render Web Services: https://render.com/docs/web-services
- Render Postgres: https://render.com/docs/databases
- Variáveis de ambiente no Render: https://render.com/docs/environment-variables
- Vite na Vercel: https://vercel.com/docs/frameworks/frontend/vite
- Variáveis de ambiente na Vercel: https://vercel.com/docs/environment-variables
