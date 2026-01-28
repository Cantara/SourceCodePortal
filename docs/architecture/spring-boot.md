# Spring Boot Architecture

This document describes the Spring Boot architecture of Source Code Portal, including initialization flow, bean configuration, and Spring integration patterns.

## Table of Contents

- [Overview](#overview)
- [Application Initialization](#application-initialization)
- [Spring Bean Configuration](#spring-bean-configuration)
- [Configuration Management](#configuration-management)
- [Actuator Integration](#actuator-integration)
- [Scheduled Tasks](#scheduled-tasks)
- [Testing Infrastructure](#testing-infrastructure)
- [Migration from Legacy Mode](#migration-from-legacy-mode)

## Overview

Source Code Portal uses Spring Boot 3.2.2 as its primary application framework. Spring Boot provides:

- **Dependency Injection**: Constructor-based DI throughout the application
- **Auto-Configuration**: Automatic setup of common infrastructure
- **Actuator Endpoints**: Production-ready monitoring and management
- **Configuration Management**: Type-safe, hierarchical configuration
- **Testing Support**: Comprehensive test infrastructure

### Why Spring Boot?

The migration to Spring Boot (Phase 2) brought several benefits:

1. **Industry Standard**: Well-known patterns and practices
2. **Better Testability**: Spring Test framework integration
3. **Observability**: Built-in health checks and metrics
4. **Developer Experience**: Better IDE support and tooling
5. **Maintainability**: Less boilerplate, cleaner code

## Application Initialization

### Startup Sequence

The application starts via `SpringBootServer.main()` and follows this initialization sequence:

```
1. Load Configuration
   ├─ application.yml (defaults)
   ├─ application-defaults.properties (legacy)
   ├─ security.properties (credentials)
   ├─ application_override.properties (overrides)
   ├─ Environment variables (SCP_* prefix)
   └─ System properties

2. Initialize Spring Context
   ├─ Component scanning
   ├─ Auto-configuration
   └─ Bean creation

3. Create Core Beans
   ├─ CacheStore (Caffeine + Spring Cache)
   ├─ ExecutorService (virtual threads)
   ├─ ScheduledExecutorService
   └─ RepositoryConfigLoader

4. Run Startup Initializer
   ├─ Load config.json
   ├─ Fetch GitHub repository list
   ├─ Validate configuration
   └─ Log system status

5. Prefetch Data
   ├─ Load repository metadata
   ├─ Fetch recent commits
   ├─ Cache documentation
   └─ Populate build status

6. Start Embedded Server
   ├─ Initialize Undertow
   ├─ Register Spring MVC handlers
   ├─ Start listening on port 9090
   └─ Log ready message
```

### Entry Point

**File**: `src/main/java/no/cantara/docsite/SpringBootServer.java`

```java
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SpringBootServer {

    private static final Logger log = LoggerFactory.getLogger(SpringBootServer.class);

    public static void main(String[] args) {
        // Set system properties for virtual threads
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "10");
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "10");

        // Launch Spring Boot
        SpringApplication.run(SpringBootServer.class, args);

        log.info("Source Code Portal started successfully");
    }
}
```

### Bootstrap Process

**Phase 1: Configuration Loading**

Spring Boot loads configuration in this order (later overrides earlier):

1. `application.yml` - Main configuration
2. `application-defaults.properties` - Legacy defaults
3. `security.properties` - Credentials (excluded from Git)
4. `application_override.properties` - Local overrides
5. Environment variables with `SCP_` prefix
6. Command-line arguments

**Phase 2: Component Scanning**

Spring scans these packages for components:

- `no.cantara.docsite.controller.spring` - Spring MVC controllers
- `no.cantara.docsite.config` - Configuration classes
- `no.cantara.docsite.actuator` - Custom health indicators
- `no.cantara.docsite.scheduled` - Scheduled tasks

**Phase 3: Auto-Configuration**

Spring Boot auto-configures:

- Embedded Undertow server
- Spring MVC (DispatcherServlet)
- Thymeleaf template engine
- Spring Cache (Caffeine)
- Jackson JSON serialization
- Actuator endpoints

## Spring Bean Configuration

### Core Configuration Classes

#### 1. CacheConfiguration

**File**: `src/main/java/no/cantara/docsite/config/CacheConfiguration.java`

```java
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheStore cacheStore(CacheManager cacheManager) {
        return new CacheStore(cacheManager);
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Configure individual caches with Caffeine
        cacheManager.setCaches(Arrays.asList(
            buildCache("repositories"),
            buildCache("commits"),
            buildCache("contents"),
            buildCache("buildStatus"),
            buildCache("badges")
        ));

        return cacheManager;
    }

    private CaffeineCache buildCache(String name) {
        return new CaffeineCache(name, Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(15))
            .recordStats()
            .build());
    }
}
```

#### 2. ExecutorConfiguration

**File**: `src/main/java/no/cantara/docsite/config/ExecutorConfiguration.java`

```java
@Configuration
public class ExecutorConfiguration {

    @Bean
    public ExecutorService executorService() {
        // Use virtual threads for I/O-bound tasks
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(
            5, // Core pool size
            Thread.ofVirtual().factory()
        );
    }
}
```

#### 3. ApplicationProperties

**File**: `src/main/java/no/cantara/docsite/config/ApplicationProperties.java`

Type-safe configuration properties:

```java
@Configuration
@ConfigurationProperties(prefix = "scp")
public class ApplicationProperties {

    private Server server = new Server();
    private Github github = new Github();
    private Cache cache = new Cache();

    public static class Server {
        private String mode = "spring-boot";
        private int port = 9090;
        private String contextPath = "/";
        // getters/setters
    }

    public static class Github {
        private String accessToken;
        private String organization;
        private String visibility = "public";
        // getters/setters
    }

    public static class Cache {
        private boolean enabled = true;
        private Duration ttl = Duration.ofMinutes(15);
        // getters/setters
    }
}
```

#### 4. WebMvcConfiguration

**File**: `src/main/java/no/cantara/docsite/config/WebMvcConfiguration.java`

```java
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/META-INF/views/css/");

        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/META-INF/views/js/");

        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/META-INF/views/images/");
    }
}
```

### Bean Lifecycle

**Creation Order:**

1. Configuration classes (`@Configuration`)
2. Property binding (`@ConfigurationProperties`)
3. Infrastructure beans (CacheManager, ExecutorService)
4. Domain services (RepositoryConfigLoader, CacheStore)
5. Controllers (`@RestController`, `@Controller`)
6. Actuator components (health indicators, info contributors)

**Initialization Callbacks:**

- `@PostConstruct` - Bean initialization
- `ApplicationRunner` - Post-startup tasks
- `SmartLifecycle` - Advanced lifecycle management

## Configuration Management

### Configuration Sources

**1. application.yml (Primary)**

```yaml
scp:
  server:
    mode: spring-boot
    port: 9090

  github:
    organization: cantara
    visibility: public
    api:
      timeout: 75s
      rate-limit-buffer: 100

  cache:
    enabled: true
    ttl: 15m
    max-size: 1000

spring:
  application:
    name: source-code-portal

  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=15m
```

**2. Legacy Properties (Backward Compatibility)**

The `ConfigurationBridge` class provides backward compatibility with legacy property files:

```java
@Component
public class ConfigurationBridge {

    @Autowired
    private Environment environment;

    /**
     * Get property with fallback to legacy property names.
     */
    public String getProperty(String key) {
        // Try new Spring Boot property first
        String value = environment.getProperty(key);

        // Fall back to legacy property name
        if (value == null) {
            String legacyKey = toLegacyKey(key);
            value = environment.getProperty(legacyKey);
        }

        return value;
    }

    private String toLegacyKey(String springBootKey) {
        // Convert: scp.github.access-token -> github.client.accessToken
        return springBootKey.replace("scp.", "").replace("-", ".");
    }
}
```

### Environment Variables

All configuration can be overridden via environment variables with `SCP_` prefix:

```bash
# Override GitHub access token
export SCP_GITHUB_ACCESS_TOKEN=ghp_xxxxx

# Override server port
export SCP_SERVER_PORT=8080

# Override cache TTL
export SCP_CACHE_TTL=30m
```

**Naming Convention:**
- `scp.github.access-token` → `SCP_GITHUB_ACCESS_TOKEN`
- `scp.cache.ttl` → `SCP_CACHE_TTL`
- Dots become underscores, kebab-case becomes UPPER_SNAKE_CASE

### Profile-Based Configuration

**Development Profile:**

```yaml
# application-dev.yml
scp:
  cache:
    enabled: false  # Disable caching for live data

  github:
    api:
      timeout: 30s  # Faster timeouts for dev

logging:
  level:
    no.cantara.docsite: DEBUG
```

**Production Profile:**

```yaml
# application-prod.yml
scp:
  cache:
    ttl: 30m  # Longer cache for production

  github:
    api:
      timeout: 75s

logging:
  level:
    no.cantara.docsite: INFO
```

**Activate Profile:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# or
java -jar app.jar --spring.profiles.active=prod
```

## Actuator Integration

### Available Endpoints

**Health Checks:**

- `/actuator/health` - Overall application health
- `/actuator/health/github` - GitHub API rate limit status
- `/actuator/health/cache` - Cache health and statistics
- `/actuator/health/executor` - Thread pool health

**Metrics:**

- `/actuator/metrics` - All available metrics
- `/actuator/metrics/jvm.memory.used` - JVM memory usage
- `/actuator/metrics/http.server.requests` - HTTP request metrics
- `/actuator/prometheus` - Prometheus format metrics

**Application Info:**

- `/actuator/info` - Application version, build info, runtime details
- `/actuator/caches` - Cache manager details
- `/actuator/scheduledtasks` - Scheduled task list

### Custom Health Indicators

#### GitHubHealthIndicator

```java
@Component
public class GitHubHealthIndicator implements HealthIndicator {

    private final GitHubCommands gitHubCommands;

    @Override
    public Health health() {
        try {
            RateLimit rateLimit = gitHubCommands.getRateLimit();

            Map<String, Object> details = Map.of(
                "remaining", rateLimit.remaining(),
                "limit", rateLimit.limit(),
                "reset", rateLimit.reset()
            );

            if (rateLimit.remaining() < 100) {
                return Health.down()
                    .withDetails(details)
                    .build();
            }

            return Health.up()
                .withDetails(details)
                .build();

        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}
```

#### CacheHealthIndicator

```java
@Component
public class CacheHealthIndicator implements HealthIndicator {

    private final CacheStore cacheStore;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        for (String cacheName : cacheStore.getCacheNames()) {
            CaffeineCache cache = (CaffeineCache) cacheStore.getCache(cacheName);
            CacheStats stats = cache.getNativeCache().stats();

            details.put(cacheName, Map.of(
                "size", cache.getNativeCache().estimatedSize(),
                "hitRate", stats.hitRate(),
                "missRate", stats.missRate()
            ));
        }

        return Health.up()
            .withDetails(details)
            .build();
    }
}
```

### Custom Info Contributor

```java
@Component
public class ApplicationInfoContributor implements InfoContributor {

    @Value("${scp.version:unknown}")
    private String version;

    @Autowired
    private ApplicationProperties properties;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", Map.of(
            "version", version,
            "mode", properties.getServer().getMode(),
            "organization", properties.getGithub().getOrganization()
        ));

        builder.withDetail("runtime", Map.of(
            "javaVersion", System.getProperty("java.version"),
            "processors", Runtime.getRuntime().availableProcessors(),
            "maxMemory", Runtime.getRuntime().maxMemory()
        ));
    }
}
```

## Scheduled Tasks

### Spring @Scheduled Tasks

Spring Boot replaces custom executor-based scheduling with declarative `@Scheduled` annotations:

```java
@Component
public class ScheduledFetchData {

    private final CacheStore cacheStore;
    private final RepositoryConfigLoader configLoader;

    @Scheduled(
        fixedDelayString = "${scp.fetch.interval:PT15M}",
        initialDelayString = "${scp.fetch.initial-delay:PT1M}"
    )
    public void refreshRepositories() {
        log.info("Starting scheduled repository refresh");

        List<Repository> repos = configLoader.getAllRepositories();
        for (Repository repo : repos) {
            try {
                fetchAndCache(repo);
            } catch (Exception e) {
                log.error("Failed to fetch repository: {}", repo.getName(), e);
            }
        }

        log.info("Completed repository refresh");
    }

    @Scheduled(cron = "${scp.fetch.commits.cron:0 */30 * * * *}")
    public void refreshCommits() {
        log.info("Starting scheduled commit refresh");
        // Refresh commit data
    }
}
```

**Configuration:**

```yaml
scp:
  fetch:
    interval: PT15M  # ISO-8601 duration (15 minutes)
    initial-delay: PT1M
    commits:
      cron: "0 */30 * * * *"  # Every 30 minutes
```

### Monitoring Scheduled Tasks

View all scheduled tasks:

```bash
curl http://localhost:9090/actuator/scheduledtasks
```

Response:

```json
{
  "cron": [],
  "fixedDelay": [
    {
      "runnable": {
        "target": "no.cantara.docsite.scheduled.ScheduledFetchData.refreshRepositories"
      },
      "initialDelay": 60000,
      "interval": 900000
    }
  ],
  "fixedRate": []
}
```

## Testing Infrastructure

### Spring Boot Test Support

**Test Configuration:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "scp.github.access-token=test-token",
    "scp.cache.enabled=false"
})
public class SpringBootServerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/health",
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

### Controller Testing

**@WebMvcTest for Controllers:**

```java
@WebMvcTest(DashboardWebController.class)
public class DashboardWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CacheStore cacheStore;

    @Test
    public void testDashboardPage() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("groups"));
    }
}
```

### Integration Testing

**TestServerExtension (JUnit 5):**

```java
@ExtendWith(TestServerExtension.class)
public class IntegrationTest {

    @Test
    public void testFullFlow(TestClient client) {
        String response = client.get("/dashboard");
        assertTrue(response.contains("Source Code Portal"));
    }
}
```

## Migration from Legacy Mode

### Dual-Mode Support

The application supports both Spring Boot and legacy Undertow modes during the transition period:

**Mode Selection:**

```yaml
scp:
  server:
    mode: spring-boot  # or "undertow"
```

**Entry Points:**

- Spring Boot: `SpringBootServer.main()`
- Legacy Undertow: `Server.main()`

### Deprecation Timeline

- **v0.10.17**: Spring Boot mode introduced (Phase 2 complete)
- **v0.11.0**: Legacy mode marked deprecated
- **v1.0.0**: Legacy mode removed (planned)

### Migration Checklist

For teams migrating from legacy to Spring Boot mode:

- [ ] Update startup scripts to use `SpringBootServer`
- [ ] Migrate configuration to `application.yml`
- [ ] Update monitoring to use Actuator endpoints
- [ ] Test all endpoints with Spring MVC controllers
- [ ] Update health check URLs (`/actuator/health`)
- [ ] Verify cache configuration with Caffeine
- [ ] Test webhook integration
- [ ] Update Docker deployment

## Related Documentation

- [Controller Architecture](controllers.md) - Request flow and Spring MVC
- [Caching Architecture](caching.md) - Cache configuration and strategies
- [Configuration Guide](../getting-started/configuration.md) - Detailed configuration reference
- [Monitoring Guide](../operations/monitoring.md) - Actuator and observability

---

**Next Steps**: Read the [Controller Architecture](controllers.md) document to understand request routing and Spring MVC integration.
