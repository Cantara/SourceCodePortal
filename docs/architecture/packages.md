# Package Structure

This document describes the package organization of Source Code Portal, including the responsibilities of each package and key classes.

## Table of Contents

- [Overview](#overview)
- [Package Hierarchy](#package-hierarchy)
- [Core Packages](#core-packages)
- [Domain Packages](#domain-packages)
- [Controller Packages](#controller-packages)
- [Infrastructure Packages](#infrastructure-packages)
- [Configuration Packages](#configuration-packages)
- [Testing Packages](#testing-packages)
- [Package Dependencies](#package-dependencies)

## Overview

Source Code Portal follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│              Presentation Layer                          │
│  (Controllers, REST APIs, Web Pages)                     │
├─────────────────────────────────────────────────────────┤
│              Business Logic Layer                        │
│  (Domain Services, Commands, Fetch)                      │
├─────────────────────────────────────────────────────────┤
│              Infrastructure Layer                        │
│  (Cache, Executor, Configuration)                        │
├─────────────────────────────────────────────────────────┤
│              External Integration Layer                  │
│  (GitHub, Jenkins, Snyk, Shields.io)                     │
└─────────────────────────────────────────────────────────┘
```

### Package Naming Convention

All packages follow the base namespace: `no.cantara.docsite`

**Rationale**:
- `no` = Norway (country code)
- `cantara` = Organization name
- `docsite` = Project name (historical, now "Source Code Portal")

## Package Hierarchy

```
no.cantara.docsite/
├── SpringBootServer.java          # Spring Boot entry point
├── Server.java                     # Legacy Undertow entry point (deprecated)
│
├── cache/                          # Caching layer
│   ├── CacheStore.java
│   ├── CacheKey.java
│   ├── CacheRepositoryKey.java
│   ├── CacheGroupKey.java
│   └── CacheShaKey.java
│
├── domain/                         # Domain models and business logic
│   ├── config/                     # Configuration models
│   ├── github/                     # GitHub domain models
│   ├── renderer/                   # Markdown/AsciiDoc rendering
│   ├── jenkins/                    # Jenkins integration
│   ├── snyk/                       # Snyk integration
│   ├── shields/                    # Shields.io integration
│   └── scm/                        # Source control abstractions
│
├── controller/                     # HTTP request handlers
│   ├── spring/                     # Spring MVC controllers (recommended)
│   ├── handler/                    # Legacy Undertow handlers (deprecated)
│   ├── ApplicationController.java  # Legacy router (deprecated)
│   └── WebController.java          # Legacy web router (deprecated)
│
├── commands/                       # Resilience4j command pattern
│   ├── BaseResilientCommand.java
│   ├── GetGitHubCommand.java
│   ├── GetCommand.java
│   └── GetShieldsCommand.java
│
├── fetch/                          # Data fetching services
│   ├── PreFetchData.java
│   └── ScheduledFetchData.java
│
├── web/                            # Web templating infrastructure
│   ├── ThymeleafViewEngineProcessor.java
│   ├── ResourceContext.java
│   └── WebHandler.java
│
├── executor/                       # Thread pool management
│   ├── ExecutorService.java
│   └── ScheduledExecutorService.java
│
├── config/                         # Spring Boot configuration
│   ├── ApplicationProperties.java
│   ├── CacheConfiguration.java
│   ├── ExecutorConfiguration.java
│   ├── WebMvcConfiguration.java
│   ├── ConfigurationBridge.java
│   └── SpringBootInitializer.java
│
├── actuator/                       # Spring Boot Actuator components
│   ├── health/                     # Custom health indicators
│   └── info/                       # Custom info contributors
│
├── scheduled/                      # Spring @Scheduled tasks
│   └── ScheduledFetchData.java
│
└── util/                           # Utility classes
    ├── JsonUtil.java
    ├── DateUtil.java
    └── UrlUtil.java
```

## Core Packages

### no.cantara.docsite.cache

**Purpose**: Caching layer for GitHub data and external API responses.

**Key Classes**:

#### CacheStore.java
Central cache manager providing typed access to all caches.

```java
@Component
public class CacheStore {
    // Repository cache
    public Repository getRepository(CacheRepositoryKey key);
    public void putRepository(CacheRepositoryKey key, Repository repo);

    // Commits cache
    public List<Commit> getCommits(CacheKey key);
    public void putCommits(CacheKey key, List<Commit> commits);

    // Contents cache
    public Contents getContents(CacheKey key);
    public void putContents(CacheKey key, Contents contents);

    // Build status cache
    public BuildStatus getBuildStatus(CacheKey key);
    public void putBuildStatus(CacheKey key, BuildStatus status);

    // Cache management
    public void invalidate(String cacheName, Object key);
    public void clearCache(String cacheName);
    public CacheStats getStats(String cacheName);
}
```

#### CacheKey.java
Base cache key for org/repo/branch lookup.

```java
public record CacheKey(
    String organization,
    String repository,
    String branch
) {
    // Used for: commits, contents, build status, releases
}
```

#### CacheRepositoryKey.java
Cache key for repository metadata with group support.

```java
public record CacheRepositoryKey(
    String groupId,
    String organization,
    String repository
) {
    // Used for: repository metadata, grouped repos
}
```

**Dependencies**:
- Spring Cache abstraction
- Caffeine cache (implementation)
- Domain models (Repository, Commit, etc.)

---

### no.cantara.docsite.domain

**Purpose**: Domain models and business logic.

**Sub-packages**:

#### domain/config
Configuration loading and parsing.

**Key Classes**:

- `RepositoryConfigLoader.java` - Loads config.json, fetches GitHub repos
- `RepositoryGroup.java` - Model for repository group
- `RepositoryConfig.java` - Configuration data class
- `ConfigValidator.java` - Validates configuration

**Example**:

```java
@Component
public class RepositoryConfigLoader {
    /**
     * Load repository groups from config.json.
     */
    public List<RepositoryGroup> getRepositoryGroups();

    /**
     * Get all repositories across all groups.
     */
    public List<Repository> getAllRepositories();

    /**
     * Find group by ID.
     */
    public RepositoryGroup getGroup(String groupId);
}
```

#### domain/github
GitHub API models and domain logic.

**Key Classes**:

- `Repository.java` - GitHub repository model
- `Commit.java` - Git commit model
- `Release.java` - GitHub release model
- `Contents.java` - Repository file contents
- `GitHubCommands.java` - GitHub API operations
- `RateLimit.java` - API rate limit info

**Example**:

```java
public class Repository {
    private String name;
    private String organization;
    private String description;
    private String defaultBranch;
    private int stars;
    private int forks;
    private Instant updatedAt;
    private boolean isPrivate;

    // Business logic
    public boolean isActive();
    public String getGitHubUrl();
}
```

#### domain/renderer
Markdown and AsciiDoc rendering.

**Key Classes**:

- `MarkdownRenderer.java` - Markdown → HTML conversion
- `AsciiDocRenderer.java` - AsciiDoc → HTML conversion
- `CodeHighlighter.java` - Syntax highlighting
- `LinkResolver.java` - Resolve relative links

**Example**:

```java
@Component
public class MarkdownRenderer {
    /**
     * Render Markdown to HTML.
     */
    public String render(String markdown);

    /**
     * Render with custom base URL for link resolution.
     */
    public String render(String markdown, String baseUrl);
}
```

#### domain/jenkins
Jenkins build status integration.

**Key Classes**:

- `JenkinsClient.java` - Jenkins API client
- `BuildStatus.java` - Build status model
- `JenkinsConfig.java` - Jenkins configuration

#### domain/snyk
Snyk security test integration.

**Key Classes**:

- `SnykClient.java` - Snyk API client
- `SecurityTestResult.java` - Security test results
- `Vulnerability.java` - Security vulnerability model

#### domain/shields
Shields.io badge integration.

**Key Classes**:

- `ShieldsClient.java` - Shields.io API client
- `Badge.java` - Badge model
- `BadgeStyle.java` - Badge styling options

#### domain/scm
Source control management abstractions.

**Key Classes**:

- `ScmProvider.java` - Interface for SCM providers (GitHub, GitLab, etc.)
- `GitHubScmProvider.java` - GitHub implementation
- `ScmRepository.java` - Generic repository interface

**Note**: Currently only GitHub is supported, but this package provides abstraction for future multi-SCM support.

---

### no.cantara.docsite.controller

**Purpose**: HTTP request handlers (controllers and routing).

**Sub-packages**:

#### controller/spring (Recommended)
Spring MVC controllers for web and REST endpoints.

**Web Controllers** (`@Controller`):

- `DashboardWebController.java` - Dashboard page (/, /dashboard)
- `GroupWebController.java` - Group view page (/group/{groupId})
- `CommitsWebController.java` - Commit history (/commits/{org}/{repo})
- `ContentsWebController.java` - Repository contents (/contents/{org}/{repo}/{branch})
- `WikiWebController.java` - Wiki pages (/wiki/{pageName})

**REST Controllers** (`@RestController`):

- `PingRestController.java` - Ping endpoint (/ping)
- `HealthRestController.java` - Health checks (/health, /health/*)
- `EchoRestController.java` - Echo diagnostic (/echo)
- `GitHubWebhookRestController.java` - GitHub webhooks (/github/webhook)
- `BadgeResourceController.java` - SVG badges (/badge/*)

**Example**:

```java
@Controller
public class DashboardWebController {
    private final RepositoryConfigLoader configLoader;
    private final CacheStore cacheStore;

    public DashboardWebController(
            RepositoryConfigLoader configLoader,
            CacheStore cacheStore) {
        this.configLoader = configLoader;
        this.cacheStore = cacheStore;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<RepositoryGroup> groups = configLoader.getRepositoryGroups();
        model.addAttribute("groups", groups);
        return "index";
    }
}
```

#### controller/handler (Deprecated)
Legacy Undertow web handlers.

**Classes**:

- `DashboardHandler.java` - Dashboard page [@Deprecated]
- `CardHandler.java` - Group view [@Deprecated]
- `CommitsHandler.java` - Commit history [@Deprecated]
- `ContentsHandler.java` - Repository contents [@Deprecated]
- `CantaraWikiHandler.java` - Wiki pages [@Deprecated]
- `BadgeResourceHandler.java` - SVG badges [@Deprecated]

**Status**: Deprecated since 0.10.17-SNAPSHOT, scheduled for removal in 1.0.0.

#### Legacy Routing Controllers (Deprecated)

- `ApplicationController.java` - Main router [@Deprecated]
- `WebController.java` - Web page router [@Deprecated]

**Migration**: See `DEPRECATED_UNDERTOW_CONTROLLERS.md` for Spring MVC equivalents.

---

### no.cantara.docsite.commands

**Purpose**: Resilience4j command pattern for external HTTP calls.

**Key Classes**:

#### BaseResilientCommand.java
Base class with circuit breaker, bulkhead, and time limiter.

```java
public abstract class BaseResilientCommand<T> {
    protected final CircuitBreaker circuitBreaker;
    protected final Bulkhead bulkhead;
    protected final TimeLimiter timeLimiter;

    protected BaseResilientCommand() {
        // Circuit Breaker: 50% failure threshold, 60s open state
        this.circuitBreaker = CircuitBreaker.of("default", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .build());

        // Bulkhead: 25 max concurrent calls
        this.bulkhead = Bulkhead.of("default", BulkheadConfig.custom()
            .maxConcurrentCalls(25)
            .build());

        // Time Limiter: 75s timeout
        this.timeLimiter = TimeLimiter.of("default", TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(75))
            .build());
    }

    protected abstract T executeCommand() throws Exception;

    public T execute() {
        return Decorators.ofSupplier(this::executeCommand)
            .withCircuitBreaker(circuitBreaker)
            .withBulkhead(bulkhead)
            .withTimeLimiter(timeLimiter)
            .get();
    }
}
```

#### GetGitHubCommand.java
GitHub API requests with circuit breaker.

```java
public class GetGitHubCommand extends BaseResilientCommand<String> {
    private final String url;
    private final String accessToken;

    public GetGitHubCommand(String url, String accessToken) {
        super();
        this.url = url;
        this.accessToken = accessToken;
    }

    @Override
    protected String executeCommand() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Accept", "application/vnd.github.v3+json")
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        return response.body();
    }
}
```

#### GetCommand.java
Generic HTTP requests (Jenkins, Snyk).

#### GetShieldsCommand.java
Shields.io badge requests.

**Benefits**:
- Automatic retry on transient failures
- Circuit breaker prevents cascading failures
- Bulkhead limits concurrent calls
- Timeout protection
- Metrics and monitoring

---

### no.cantara.docsite.fetch

**Purpose**: Data fetching services (startup and scheduled).

**Key Classes**:

#### PreFetchData.java
Initial data population on startup.

```java
@Component
public class PreFetchData implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting data prefetch...");

        // Prefetch repository metadata
        prefetchRepositories();

        // Prefetch recent commits
        prefetchCommits();

        // Prefetch README files
        prefetchContents();

        log.info("Data prefetch complete");
    }
}
```

#### ScheduledFetchData.java
Periodic background updates.

```java
@Component
public class ScheduledFetchData {
    /**
     * Refresh repositories every 15 minutes.
     */
    @Scheduled(fixedDelay = 900000, initialDelay = 60000)
    public void refreshRepositories() {
        // Refresh repository metadata
    }

    /**
     * Refresh commits every 10 minutes.
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void refreshCommits() {
        // Refresh commit data
    }
}
```

---

### no.cantara.docsite.web

**Purpose**: Web templating infrastructure.

**Key Classes**:

#### ThymeleafViewEngineProcessor.java
Thymeleaf template rendering.

```java
@Component
public class ThymeleafViewEngineProcessor {
    private final TemplateEngine templateEngine;

    /**
     * Render template with model data.
     */
    public String render(String templateName, Map<String, Object> model);

    /**
     * Render to HTTP response (Spring MVC).
     */
    public void renderToResponse(
        String templateName,
        Map<String, Object> model,
        HttpServletResponse response
    ) throws IOException;
}
```

#### ResourceContext.java
Request path parsing and context.

```java
public class ResourceContext {
    private final String path;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;

    public static ResourceContext parse(String requestPath);

    public String getPathParam(String name);
    public String getQueryParam(String name);
}
```

#### WebHandler.java
Legacy handler interface (deprecated).

```java
@Deprecated(since = "0.10.17-SNAPSHOT")
public interface WebHandler {
    void handle(HttpServerExchange exchange);
}
```

---

### no.cantara.docsite.executor

**Purpose**: Thread pool management.

**Key Classes**:

#### ExecutorService.java
Async task executor using virtual threads.

```java
@Configuration
public class ExecutorConfiguration {
    @Bean
    public ExecutorService executorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

#### ScheduledExecutorService.java
Scheduled periodic tasks.

```java
@Bean
public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newScheduledThreadPool(
        5,
        Thread.ofVirtual().factory()
    );
}
```

**Note**: In Spring Boot mode, prefer `@Scheduled` annotations over manual executor usage.

---

## Configuration Packages

### no.cantara.docsite.config

**Purpose**: Spring Boot configuration and setup.

**Key Classes**:

#### ApplicationProperties.java
Type-safe configuration properties.

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
    }

    public static class Github {
        private String accessToken;
        private String organization;
    }
}
```

#### CacheConfiguration.java
Cache manager and cache setup.

#### ExecutorConfiguration.java
Thread pool configuration.

#### WebMvcConfiguration.java
Spring MVC configuration (CORS, resource handlers).

```java
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/META-INF/views/css/");
    }
}
```

#### ConfigurationBridge.java
Backward compatibility bridge for legacy properties.

#### SpringBootInitializer.java
Startup initialization tasks.

```java
@Component
public class SpringBootInitializer implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        // Log startup info
        // Validate configuration
        // Check GitHub connectivity
    }
}
```

---

## Testing Packages

### no.cantara.docsite (test)

**Purpose**: Test infrastructure and test cases.

**Key Test Classes**:

#### TestServerExtension.java
JUnit 5 extension for embedded server testing.

```java
@ExtendWith(TestServerExtension.class)
public class IntegrationTest {
    @Test
    public void testEndpoint(TestClient client) {
        String response = client.get("/dashboard");
        assertTrue(response.contains("Source Code Portal"));
    }
}
```

#### TestClient.java
HTTP client utilities for testing.

```java
public class TestClient {
    public String get(String path);
    public String post(String path, String body);
    public HttpResponse<String> getResponse(String path);
}
```

#### DumpTestDataToFile.java
Utility to dump test data for debugging.

---

## Package Dependencies

### Dependency Graph

```
┌─────────────────────────────────────────────────────────┐
│              controller (Presentation)                   │
│  Depends on: domain, cache, web, commands                │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│         domain (Business Logic)                          │
│  Depends on: cache, commands, renderer                   │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│         commands (External Calls)                        │
│  Depends on: executor, util                              │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│         cache + executor (Infrastructure)                │
│  Depends on: config, util                                │
└─────────────────────────────────────────────────────────┘
```

### Allowed Dependencies

**Controllers** may depend on:
- ✅ domain (services, models)
- ✅ cache (CacheStore)
- ✅ web (ThymeleafViewEngineProcessor)
- ✅ config (ApplicationProperties)
- ❌ commands (use domain services instead)

**Domain** may depend on:
- ✅ cache (read/write)
- ✅ commands (external API calls)
- ✅ renderer (Markdown/AsciiDoc)
- ✅ config (configuration reading)
- ❌ controller (no circular dependencies)
- ❌ web (keep domain independent)

**Commands** may depend on:
- ✅ executor (thread pools)
- ✅ util (utilities)
- ❌ domain (keep infrastructure independent)
- ❌ cache (commands shouldn't cache directly)

**Cache** may depend on:
- ✅ config (cache configuration)
- ✅ util (utilities)
- ❌ domain (cache is infrastructure)
- ❌ commands (cache is lower level)

### Circular Dependency Prevention

**Rule**: Lower layers must not depend on higher layers.

**Layer Hierarchy** (bottom to top):
1. util (utilities)
2. executor (thread pools)
3. config (configuration)
4. cache (caching infrastructure)
5. commands (external API calls)
6. domain (business logic)
7. web (templating)
8. controller (presentation)

**Enforcement**:
- Package structure enforces layer separation
- Spring dependency injection prevents circular dependencies
- Code reviews check for violations

## Related Documentation

- [Architecture Overview](overview.md) - High-level architecture
- [Spring Boot Architecture](spring-boot.md) - Spring Boot integration
- [Controller Architecture](controllers.md) - Controller patterns
- [Caching Architecture](caching.md) - Cache implementation

---

**Next Steps**: Explore specific packages in the codebase to understand implementation details.
