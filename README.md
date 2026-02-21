# NoteVault API

REST API for creating and managing notes. Notes can be organized into notebooks and grouped by tags.
It uses built in Spring Boot Auth for role-based access control ensuring users can only access their own data.

## How to Run

### You will need:
- Docker and Docker Compose
- Java 21 and Maven (for running tests locally)

### Start the Application

```
docker compose up --build -d
```

### Stop the Application

```
# Stop containers (preserves data)
docker compose down

# Stop and wipe database volume (fresh start)
docker compose down -v
```

NoteVault is a fully containerized Docker API this will:
- Create a PostgreSQL database
- Run schema initialization
- Start the Spring Boot application on `localhost:8080`

## API Endpoints

- **API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI spec:** `http://localhost:8080/api-docs`

| Method   | Path                 | Description                        |
|----------|----------------------|------------------------------------|
| `GET`    | `/api/v1/notes`      | List notes (filtered by ownership) |
| `GET`    | `/api/v1/notes/{id}` | Get a note by ID                   |
| `POST`   | `/api/v1/notes`      | Create a new note                  |
| `PUT`    | `/api/v1/notes/{id}` | Update an existing note            |
| `DELETE` | `/api/v1/notes/{id}` | Delete a note                      |

## Tests

### Run Unit Tests
These are JUnit tests that use Mockito and do not require a database.

```
mvn test
```

### Run Acceptance Tests
API must be running either locally or deployed

```
# Against localhost
mvn test "-Dtest=RunCucumberTest"

# Against a remote server
mvn test "-Dtest=RunCucumberTest" "-Dtest.base-url=http://10.0.0.67:8080"
```

## Authentication

The API uses HTTP Basic Auth that is checked against the `auth.users` table. Currently, this table is only updated with direct SQL updates
```sql
INSERT INTO auth.users (username, password, role)
VALUES ('myuser', '$2a$10$...', 'USER');
```

### Roles

| Role    | Permissions                                   |
|---------|-----------------------------------------------|
| `USER`  | Can only access notes owned by their username |
| `ADMIN` | Can access all notes regardless of ownership  |


## API Usage Examples (See Swagger UI for more)

### Create a Note

```
curl -X POST http://localhost:8080/api/v1/notes \
  -H "Content-Type: application/json" \
  -d '{"name": "Meeting Notes", "content": "Discuss Q3 roadmap", "tags": ["work", "meetings"]}'
```

### Get All Notes

```
curl http://localhost:8080/api/v1/notes
```

### Get a Note by ID

```
curl http://localhost:8080/api/v1/notes/1
```

### Delete a Note

```
curl -X DELETE http://localhost:8080/api/v1/notes/1
```

### Error Responses

| Status | Meaning                                     |
|--------|---------------------------------------------|
| `400`  | Validation failed or invalid parameter type |
| `401`  | Invalid credentials                         |
| `403`  | Access denied (not the note owner)          |
| `404`  | Note not found                              |
| `503`  | Database unavailable                        |

### Logging
Current logging is basic request/response logging, logs can be viewed through docker

```
docker compose logs api
```


### Architecture

```
|--------------|       |-----------------|       |--------------|
|   Client     | ----> | Spring Boot API | ----> |  PostgreSQL  |
|--------------|       |-----------------|       |--------------|
                                |
                                |
                       |------------------|
                       | Swagger API Docs |
                       | Spring Security  |
                       | slf4j logging    |
                       | Junit + Mockito  |
                       | Cucumber         |
                       |------------------|
```

## Tech Choices

**Docker Compose**
- Quick and easy single command deployment for database and API, also has health checks and volume persistence

**PostgreSQL**
- Full-text search allows for searching the contents of notes
- Easy to use with Spring and Docker

**Spring Boot**
- Spring Security allows for easy Basic Auth
- Spring Data JPA simple integration with Database
- SpringDoc OpenAPI to create some great API documentation with Swagger UI
- Lombok for cleaner easy to manage code
- slf4j easy integration for logging

**JUnit 5 + Mockito**
- Just a good testing suite

**Cucumber**
- Easy to read feature files
- Able to run directly against deployed code.

### Future Improvements

- **OAuth2 / SSO** — Adding even better security then just basic auth
- **User Creation Endpoint** — Add `POST /auth/register` so users can be added without direct SQL input.
- **Database Migrations** — Using Flyway will make it simple for future updates.
- **Notebooks** — The `Notebook` entity was created but is under used
- **Full-text Search** — PostgreSQL's strength in this case is searching note content so lets use it.
- **CI/CD Pipeline** — Unit tests, acceptance tests and docker builds can be easily ran for safe and easy deployments
- **Expand Logging** - Current logging is simple and viewed through docker, 
