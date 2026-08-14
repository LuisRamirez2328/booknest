# 📚 BookNest — Sistema de gestión de biblioteca

API REST de gestión de una biblioteca construida con **Java 17 + Spring Boot 4** (JPA, Security con JWT, Validation, OpenAPI) y un frontend en **React + Vite**. Demuestra un backend empresarial completo: autenticación por roles, CRUD, préstamos con control de inventario y tests (unitarios + integración).

## Características

- **Autenticación JWT** con roles `ADMIN` y `USER` (BCrypt, filtros stateless).
- **Catálogo de libros** público con búsqueda por título/autor y filtro por categoría (paginado).
- **Préstamos**: tomar y devolver libros con descuento/restauración automática de ejemplares; un usuario no puede tener el mismo libro dos veces.
- **Panel admin**: alta de autores, categorías y libros.
- **Swagger UI** en `/swagger-ui.html` y OpenAPI en `/v3/api-docs`.
- **Tests**: backend unitarios con Mockito + integración con MockMvc y H2 (29 tests); frontend con Vitest + React Testing Library (30 tests).

## Stack

| Capa      | Tecnología |
|-----------|------------|
| Backend   | Java 17, Spring Boot 4.0.7, Spring Data JPA, Spring Security + jjwt 0.13, springdoc 3.1.0 |
| Base datos| H2 (dev, en memoria) · PostgreSQL (Docker/producción) |
| Frontend  | React 19, Vite 6 |
| Infra     | Docker Compose, GitHub Actions CI |

## Estructura

```
booknest/
├── pom.xml                  # Maven (incluye wrapper ./mvnw)
├── src/main/java/...        # auth, security, book, author, category, loan, user, exception
├── src/test/java/...        # tests unitarios + integración
├── frontend/                # React + Vite
├── docker-compose.yml       # db (Postgres) + backend + frontend
└── .github/workflows/ci.yml # CI: tests backend + frontend y builds
```

## Arranque rápido

### 1. Backend (usa H2 en memoria, cero configuración)

```bash
./mvnw spring-boot:run
```

Con la API corriendo, Swagger UI estará en <http://localhost:8080/swagger-ui.html>.

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Abrir <http://localhost:5173>. El proxy de Vite reenvía `/api` al backend.

### 3. Con Docker (PostgreSQL)

```bash
docker compose up --build
```

- Frontend: <http://localhost:5173>
- API: <http://localhost:8080>

## Credenciales demo (creadas por el seeder)

| Rol   | Email                 | Contraseña |
|-------|-----------------------|------------|
| ADMIN | `admin@booknest.dev`  | `admin123` |
| USER  | `user@booknest.dev`   | `user123`  |

## Endpoints principales

| Método | Ruta                  | Acceso   | Descripción |
|--------|-----------------------|----------|-------------|
| POST   | `/api/auth/register`  | Público  | Crear cuenta (USER) |
| POST   | `/api/auth/login`     | Público  | Obtener token JWT |
| GET    | `/api/books`          | Público  | Catálogo paginado (`q`, `categoryId`) |
| GET    | `/api/books/{id}`     | Público  | Detalle de libro |
| POST   | `/api/books`          | ADMIN    | Crear libro |
| PUT    | `/api/books/{id}`     | ADMIN    | Actualizar libro |
| DELETE | `/api/books/{id}`     | ADMIN    | Eliminar libro |
| GET    | `/api/authors`        | Público  | Lista de autores |
| GET    | `/api/categories`     | Público  | Lista de categorías |
| POST   | `/api/loans`          | USER/ADMIN | Tomar prestado (`{bookId}`) |
| PUT    | `/api/loans/{id}/return` | USER/ADMIN | Devolver libro |
| GET    | `/api/loans/me`       | USER/ADMIN | Mis préstamos |

## Tests

```bash
./mvnw test
```

- **Unitarios** (Mockito): `AuthServiceTest`, `BookServiceTest`, `LoanServiceTest`.
- **Integración** (MockMvc + H2): `BooknestApiTests` cubre el flujo completo (register/login, catálogo, roles, préstamo/devolución, 401/403/409/400).

### Frontend (Vitest + React Testing Library)

```bash
cd frontend
npm test
```

- **api.test.js**: capa de API (query strings, bearer token, 401/204, `fieldErrors`).
- **App.test.jsx**: flujo de login, navegación por rol y logout.
- **BookList / MyLoans / AdminBookForm**: catálogo, préstamos y alta de libro.

## Variables de entorno

| Variable              | Default              | Descripción |
|-----------------------|----------------------|-------------|
| `JWT_SECRET`          | (Base64 incluida)    | Clave HS256 (mín. 32 bytes) |
| `JWT_EXPIRATION_MS`   | `86400000`           | Expiración del token |
| `SPRING_DATASOURCE_URL` | H2 en memoria      | JDBC URL (usar PostgreSQL en Docker) |
