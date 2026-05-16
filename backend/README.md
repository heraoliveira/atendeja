# Backend AtendeJa

API REST da Fase 1 do AtendeJa, construida com Java 17, Spring Boot, PostgreSQL e Flyway.

## Stack Atual

- Java 17.
- Spring Boot.
- Maven e Maven Wrapper.
- Spring Web.
- Spring Data JPA.
- Bean Validation.
- Flyway.
- PostgreSQL.
- Springdoc OpenAPI.
- Spring Boot Actuator.
- JUnit, Mockito, MockMvc e Testcontainers.

Spring Security, JWT, roles, agendamentos e dashboard ainda nao estao implementados neste backend.

## Execucao Local

Suba o banco na raiz do repositorio:

```bash
docker compose up -d db
```

Rode a API no Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

URLs locais:

- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testes

No Windows PowerShell:

```powershell
.\mvnw test
```

Com Maven global:

```bash
mvn test
```

Teste de integracao isolado:

```bash
mvn test -Dtest=PersistenceIntegrationTest
```

Os testes cobrem services, controllers e integracao de persistencia. `PersistenceIntegrationTest` usa Testcontainers para subir PostgreSQL real, aplicar Flyway e validar persistencia/constraints. Docker precisa estar disponivel para a JVM; testes ignorados nessa classe indicam problema de ambiente ou compatibilidade e devem ser investigados.

## Endpoints Da Fase 1

| Metodo | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/v1/customers` | Lista clientes com paginacao e filtros. |
| `GET` | `/api/v1/customers/{id}` | Busca cliente por id. |
| `POST` | `/api/v1/customers` | Cria cliente. |
| `PUT` | `/api/v1/customers/{id}` | Atualiza cliente. |
| `DELETE` | `/api/v1/customers/{id}` | Inativa cliente. |
| `GET` | `/api/v1/professionals` | Lista profissionais com paginacao e filtros. |
| `GET` | `/api/v1/professionals/{id}` | Busca profissional por id. |
| `POST` | `/api/v1/professionals` | Cria profissional. |
| `PUT` | `/api/v1/professionals/{id}` | Atualiza profissional. |
| `DELETE` | `/api/v1/professionals/{id}` | Inativa profissional. |
| `GET` | `/api/v1/services` | Lista servicos com paginacao e filtros. |
| `GET` | `/api/v1/services/{id}` | Busca servico por id. |
| `POST` | `/api/v1/services` | Cria servico. |
| `PUT` | `/api/v1/services/{id}` | Atualiza servico. |
| `DELETE` | `/api/v1/services/{id}` | Inativa servico. |
