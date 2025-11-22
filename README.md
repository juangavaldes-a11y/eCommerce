# eCommerce (app module)

This is a small Spring Boot application providing a Product REST API backed by an in-memory repository.

## Requirements
- Java 17
- Maven (or use the included Maven wrapper `mvnw` / `mvnw.cmd`)

## Build
From the workspace root run (PowerShell):

```powershell
# run tests
.\app\mvnw.cmd -f app\pom.xml test

# build package (skip tests)
.\app\mvnw.cmd -f app\pom.xml -DskipTests package
```

## Run
After building you can run the app with:

```powershell
java -jar app\target\app-0.0.1-SNAPSHOT.jar
```

The application starts on port 8080 by default.

## API Endpoints (basic)
- `POST /products` — create a product. Expects JSON `ProductRequest` and returns `201 Created` with `Location` header.
- `GET /products/{id}` — retrieve a product by id.
- `GET /products` — list products with optional query params: `brand`, `category`, `priceMin`, `priceMax`, `sort` (price|name|date), `page` (0-based), `size`.
- `PUT /products/{id}` — update an existing product.
- `DELETE /products/{id}` — delete product by id.
- `GET /health` — basic health check returning "OK".

## Cache
A simple in-memory cache is configured via `com.ecomerce.config.CacheConfig` using `ConcurrentMapCacheManager` with a cache named `products`. The service uses `@Cacheable` / `@CacheEvict` on `getById`, `update`, and `delete`.

## OpenAPI / Swagger
The project includes `springdoc-openapi-starter-webmvc-ui`. After running the app open the Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

## Tests
- Unit tests are under `app/src/test/java` (uses JUnit 5 + Mockito).
- Integration tests use `@SpringBootTest` + `MockMvc`.

