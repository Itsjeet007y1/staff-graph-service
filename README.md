# Staff Graph Service

A small Spring Boot service providing a GraphQL API and REST endpoints for managing staff/employees.

Features
- Spring Boot 3 (Java 17)
- Spring GraphQL with GraphiQL enabled
- Spring Data JPA with H2 in-memory database for local development
- JWT-based authentication for REST/GraphQL access
- Sample data loader and GraphQL schema under `src/main/resources/graphql`

Prerequisites
- Java 17
- Maven 3.6+

Quick start

1. Build

```powershell
# From repository root
mvn clean package
```

2. Run

```powershell
# Run with Maven
mvn spring-boot:run

# Or run the packaged jar
java -jar target/staff-graph-service-0.0.1-SNAPSHOT.jar
```

Configuration

The project reads configuration from `src/main/resources/application.properties`. For local development you can copy the example file:

```powershell
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Important environment variables / properties
- `SECURITY_JWT_SECRET` — JWT signing secret (set in environment or via `security.jwt.secret` property)
- `spring.datasource.*` — DB connection (defaults to in-memory H2 for dev)

Default endpoints
- GraphQL endpoint: `/graphql`
- GraphiQL UI: `/graphiql` (enabled in development)
- H2 Console: `/h2-console` (enabled when using H2)
- Auth (login): `POST /auth/login` — accepts JSON `{ "username": "user", "password": "pass" }` and returns `{ "token": "<jwt>" }`

Example GraphQL query (curl)

```bash
curl -H "Content-Type: application/json" -H "Authorization: Bearer <token>" \
  -d '{"query":"{ employees { content { id firstName lastName } } }"}' \
  http://localhost:8080/graphql
```

Example curl requests

Below are example PowerShell-friendly curl commands you can use against a locally running server (http://localhost:8080). Replace <token> with the JWT returned from the login command.

1) LOGIN (JWT Token Generation)

```powershell
curl.exe -i -X POST "http://localhost:8080/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"adminpass"}'
```

2) LIST EMPLOYEES (GraphQL Query)

```powershell
curl.exe -i -X POST "http://localhost:8080/graphql" `
  -H "Content-Type: application/json" `
  -H "Accept: application/json" `
  -H "Authorization: Bearer <token>" `
  -d '{"query":"query { listEmployees { totalElements totalPages content { id name age className subjects attendance }}}"}'
```

3) ADD EMPLOYEE (GraphQL Mutation)

```powershell
curl.exe -i -X POST "http://localhost:8080/graphql" `
  -H "Content-Type: application/json" `
  -H "Accept: application/json" `
  -H "Authorization: Bearer <token>" `
  -d '{"query":"mutation { addEmployee(input: { name:\"John Doe\", age:30, className:\"A1\", subjects:[\"Math\",\"Science\"], attendance:95 }) { id name age className subjects attendance }}"}'
```

4) UPDATE EMPLOYEE (GraphQL Mutation)

```powershell
curl.exe -i -X POST "http://localhost:8080/graphql" `
  -H "Content-Type: application/json" `
  -H "Accept: application/json" `
  -H "Authorization: Bearer <token>" `
  -d '{"query":"mutation { updateEmployee(id:\"4\", input: { name:\"John Doe\", age:31, className:\"A1\", subjects:[\"Math\",\"Physics\"], attendance:96 }) { id name age className subjects attendance }}"}'
```

Notes
- `src/main/resources/application.properties` is excluded from source control by `.gitignore`. Commit `application.properties.example` instead and never commit real secrets.
- If you accidentally committed large/binary files or secrets, consider cleaning history with BFG or `git filter-branch` (coordinate with your team).

Development
- Run tests

```powershell
mvn test
```

- API docs (OpenAPI/Swagger UI) are available if the app includes the springdoc endpoint (check `/swagger-ui.html` or `/swagger-ui/index.html`).