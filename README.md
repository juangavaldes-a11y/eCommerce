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

## HOW TO

**Make This Production-Ready:**
- **Use a persistent database:** replace the in-memory repo with a real DB (see MongoDB section below) or an RDBMS. Add connection pooling and connection/credential management.
- **Caching:** switch the simple `ConcurrentMapCacheManager` to a production cache such as Caffeine (in-process with TTL and size limits) or Redis (distributed). Use TTLs and cache invalidation strategies appropriate to your domain.
- **Configuration & Secrets:** externalize configuration (Spring `application-*.yml` profiles, environment variables, Vault/Secret Manager for credentials). Do not commit secrets to the repo.
- **Observability:** add metrics (Micrometer + Prometheus), structured logging, and distributed tracing (OpenTelemetry). Expose `/actuator` endpoints (health, metrics) and secure them.
- **Resilience:** add retries, timeouts, and circuit breakers (Resilience4j) around external calls.
- **Security:** add authentication/authorization (JWT/OAuth2) and validate/request sanitize inputs. Protect endpoints and the Swagger UI in non-dev environments.
- **Testing & CI:** add comprehensive unit and integration tests, and run them in CI (GitHub Actions/GitLab CI). Add stage-specific environments (dev/staging/prod).
- **Deployment:** containerize with a small base image (e.g., Eclipse Temurin/OpenJDK), build reproducible images, and deploy with an orchestration platform (Kubernetes) or PaaS. Add readiness/liveness probes.
- **Data & Migrations:** add migration tooling (for Mongo use Mongock; for SQL use Flyway/Liquibase). Plan backups and restore procedures.
- **Performance & Cost:** load test critical endpoints and tune JVM, heap, thread pools, and DB indexes.

**Integrating with MongoDB:**
1. **Add dependency (pom.xml):**

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

2. **Update the domain model:** annotate the `Product` class with `@Document` and use `String` (or `ObjectId`) as the id type.

Example snippet for `Product`:

```java
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
	@Id
	private String id;
	// rest of fields
}
```

3. **Create a Spring Data repository:** replace `InMemoryProductRepository` with an interface:

```java
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
	// add query methods if needed, e.g. List<Product> findByBrand(String brand);
}
```

4. **Configuration:** set your Mongo connection in `src/main/resources/application.properties` (or `application.yml`):

```properties
spring.data.mongodb.uri=mongodb://uri/ecommercedb
spring.data.mongodb.database=ecommercedb
```

For production, do not hardcode credentials — use environment variables or a secret manager.

5. **Migrations & Indexes:** use Mongock or a similar migration tool to define index creation and data migrations. Define indexes for frequently queried fields (e.g., `brand`, `categories`, `price`).

6. **Performance considerations:** use proper indexing, projection, and pagination via Mongo queries rather than in-memory filtering for large datasets.

