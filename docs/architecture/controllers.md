# Controller Architecture

This document describes the controller architecture of Source Code Portal, including request flow, Spring MVC patterns, and the Thymeleaf view engine integration.

## Table of Contents

- [Overview](#overview)
- [Request Flow](#request-flow)
- [Spring MVC Architecture](#spring-mvc-architecture)
- [Legacy Undertow Architecture](#legacy-undertow-architecture-deprecated)
- [Thymeleaf View Engine](#thymeleaf-view-engine)
- [Controller Patterns](#controller-patterns)
- [Migration Guide](#migration-guide)

## Overview

Source Code Portal uses Spring MVC as its primary web framework (since Phase 2). The architecture provides:

- **Declarative Routing**: URL mapping via annotations
- **Automatic Serialization**: JSON/HTML via content negotiation
- **Type Safety**: Strongly-typed request/response models
- **Testability**: Mock-based testing with `@WebMvcTest`
- **Observability**: Built-in metrics and tracing

### Controller Types

The application has three types of controllers:

1. **Web Controllers** (`@Controller`) - Render HTML pages via Thymeleaf
2. **REST Controllers** (`@RestController`) - Return JSON/XML data
3. **Resource Controllers** - Serve static resources (CSS, JS, images, badges)

## Request Flow

### Spring Boot Mode (Current)

```
HTTP Request (e.g., GET /dashboard)
  │
  ├─→ Undertow Server (embedded)
  │     │
  │     ├─→ Spring DispatcherServlet
  │           │
  │           ├─→ HandlerMapping (finds controller method)
  │           │     │
  │           │     ├─→ @Controller / @RestController
  │           │           │
  │           │           ├─→ Web Page Controllers:
  │           │           │     ├─ DashboardWebController (/dashboard, /)
  │           │           │     ├─ GroupWebController (/group/{groupId})
  │           │           │     ├─ CommitsWebController (/commits/*)
  │           │           │     ├─ ContentsWebController (/contents/{org}/{repo}/{branch})
  │           │           │     └─ WikiWebController (/wiki/{pageName})
  │           │           │
  │           │           ├─→ REST/API Controllers:
  │           │           │     ├─ PingRestController (/ping)
  │           │           │     ├─ HealthRestController (/health, /health/*)
  │           │           │     ├─ EchoRestController (/echo)
  │           │           │     └─ GitHubWebhookRestController (/github/webhook)
  │           │           │
  │           │           └─→ Resource Controllers:
  │           │                 ├─ BadgeResourceController (/badge/*)
  │           │                 └─ Static Resource Handler (CSS, JS, images)
  │           │
  │           ├─→ ViewResolver (for @Controller)
  │           │     │
  │           │     └─→ ThymeleafViewEngineProcessor
  │           │           │
  │           │           └─→ Render HTML template
  │           │
  │           └─→ HttpMessageConverter (for @RestController)
  │                 │
  │                 └─→ Serialize to JSON (via Jackson)
  │
  └─→ HTTP Response
```

### Request Processing Flow

**1. Request Reception:**
- Undertow receives HTTP request
- Routes to Spring DispatcherServlet

**2. Handler Mapping:**
- DispatcherServlet finds matching controller
- Uses `@RequestMapping` annotations
- Path variables extracted (e.g., `{groupId}`)

**3. Controller Execution:**
- Spring creates controller instance (or reuses singleton)
- Injects dependencies via constructor
- Invokes handler method
- Returns model and view name (or data object)

**4. View Resolution (Web Controllers):**
- ThymeleafViewResolver finds template
- ThymeleafViewEngineProcessor renders HTML
- Model data merged with template

**5. Response Serialization (REST Controllers):**
- Jackson converts object to JSON
- Content-Type header set automatically
- HTTP status code applied

**6. Response Transmission:**
- Response sent via Undertow
- Metrics recorded
- Connection closed or kept alive

## Spring MVC Architecture

### Web Controllers (@Controller)

Web controllers render HTML pages using Thymeleaf templates.

#### Example: DashboardWebController

**File**: `src/main/java/no/cantara/docsite/controller/spring/DashboardWebController.java`

```java
/**
 * Spring MVC controller for the main dashboard page.
 *
 * <p>Displays an overview of all repository groups with their associated
 * repositories, build status, and recent activity.
 *
 * <p><b>Endpoints:</b>
 * <ul>
 *   <li>GET / - Main dashboard (redirects to /dashboard)</li>
 *   <li>GET /dashboard - Dashboard page with all groups</li>
 * </ul>
 *
 * @since 0.10.17-SNAPSHOT (Phase 2 - Spring Boot migration)
 */
@Controller
public class DashboardWebController {

    private static final Logger log = LoggerFactory.getLogger(DashboardWebController.class);

    private final RepositoryConfigLoader configLoader;
    private final CacheStore cacheStore;

    /**
     * Constructor injection of dependencies.
     *
     * @param configLoader Repository configuration loader
     * @param cacheStore Cache store for repository data
     */
    public DashboardWebController(
            RepositoryConfigLoader configLoader,
            CacheStore cacheStore) {
        this.configLoader = configLoader;
        this.cacheStore = cacheStore;
    }

    /**
     * Root endpoint - redirects to dashboard.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    /**
     * Main dashboard page.
     *
     * @param model Spring MVC model for template rendering
     * @return Template name (index.html)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.debug("Rendering dashboard page");

        try {
            // Load repository groups
            List<RepositoryGroup> groups = configLoader.getRepositoryGroups();

            // Enrich with cached data
            for (RepositoryGroup group : groups) {
                enrichGroupWithCachedData(group);
            }

            // Add to model for template
            model.addAttribute("groups", groups);
            model.addAttribute("pageTitle", "Dashboard");

            return "index";  // Resolves to META-INF/views/index.html

        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Failed to load dashboard");
            return "error";
        }
    }

    private void enrichGroupWithCachedData(RepositoryGroup group) {
        for (Repository repo : group.getRepositories()) {
            // Load cached commit count
            Integer commitCount = cacheStore.getCommitCount(repo);
            repo.setCommitCount(commitCount);

            // Load cached build status
            BuildStatus status = cacheStore.getBuildStatus(repo);
            repo.setBuildStatus(status);
        }
    }
}
```

**Key Patterns:**

- `@Controller` - Marks as web controller (returns view names)
- `@GetMapping` - Maps HTTP GET to method
- `Model` parameter - Spring MVC model for template data
- Constructor injection - Dependencies injected automatically
- Return view name - Resolved to template file

### REST Controllers (@RestController)

REST controllers return data (JSON/XML) instead of HTML.

#### Example: HealthRestController

**File**: `src/main/java/no/cantara/docsite/controller/spring/HealthRestController.java`

```java
/**
 * Spring MVC REST controller for health check endpoints.
 *
 * <p>Provides health status for the application and its dependencies:
 * <ul>
 *   <li>Overall application health</li>
 *   <li>GitHub API connectivity and rate limits</li>
 *   <li>Thread pool status</li>
 * </ul>
 *
 * <p><b>Note:</b> For Spring Boot Actuator health checks, use /actuator/health
 *
 * @since 0.10.17-SNAPSHOT (Phase 2 - Spring Boot migration)
 */
@RestController
@RequestMapping("/health")
public class HealthRestController {

    private static final Logger log = LoggerFactory.getLogger(HealthRestController.class);

    private final GitHubCommands gitHubCommands;
    private final ExecutorService executorService;

    public HealthRestController(
            GitHubCommands gitHubCommands,
            ExecutorService executorService) {
        this.gitHubCommands = gitHubCommands;
        this.executorService = executorService;
    }

    /**
     * Overall health check.
     *
     * @return Health status with timestamp
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Health check requested");

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        health.put("application", "Source Code Portal");

        return ResponseEntity.ok(health);
    }

    /**
     * GitHub API health check.
     *
     * @return GitHub API status and rate limit info
     */
    @GetMapping("/github")
    public ResponseEntity<Map<String, Object>> githubHealth() {
        log.debug("GitHub health check requested");

        try {
            RateLimit rateLimit = gitHubCommands.getRateLimit();

            Map<String, Object> health = new LinkedHashMap<>();
            health.put("status", "UP");
            health.put("rateLimit", Map.of(
                "remaining", rateLimit.remaining(),
                "limit", rateLimit.limit(),
                "reset", rateLimit.reset()
            ));

            // Warning if rate limit is low
            if (rateLimit.remaining() < 100) {
                health.put("warning", "Rate limit is low");
            }

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("GitHub health check failed", e);

            Map<String, Object> health = Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(health);
        }
    }

    /**
     * Thread pool health check.
     *
     * @return Thread pool statistics
     */
    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> threadHealth() {
        log.debug("Thread health check requested");

        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;

        Map<String, Object> health = Map.of(
            "status", "UP",
            "pool", Map.of(
                "coreSize", executor.getCorePoolSize(),
                "maxSize", executor.getMaximumPoolSize(),
                "activeThreads", executor.getActiveCount(),
                "queuedTasks", executor.getQueue().size(),
                "completedTasks", executor.getCompletedTaskCount()
            )
        );

        return ResponseEntity.ok(health);
    }
}
```

**Key Patterns:**

- `@RestController` - Combines `@Controller` + `@ResponseBody`
- `@RequestMapping("/health")` - Base path for all methods
- `ResponseEntity<T>` - Typed response with status code control
- `Map<String, Object>` - Generic JSON structure
- Automatic JSON serialization via Jackson

### Resource Controllers

Resource controllers serve binary content (images, badges, etc.).

#### Example: BadgeResourceController

```java
@RestController
@RequestMapping("/badge")
public class BadgeResourceController {

    private final BadgeService badgeService;

    @GetMapping("/{type}/{org}/{repo}")
    public ResponseEntity<byte[]> getBadge(
            @PathVariable String type,
            @PathVariable String org,
            @PathVariable String repo) {

        byte[] svg = badgeService.generateBadge(type, org, repo);

        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("image/svg+xml"))
            .body(svg);
    }
}
```

**Key Patterns:**

- `@PathVariable` - Extract URL path segments
- `byte[]` return type - Binary data
- `MediaType` - Content-Type header
- `ResponseEntity` - Full control over response

## Legacy Undertow Architecture (Deprecated)

### Request Flow (Legacy Mode)

```
HTTP Request
  │
  ├─→ Undertow Server
  │     │
  │     ├─→ ApplicationController (main router) [@Deprecated]
  │           │
  │           ├─→ Path matching (manual)
  │           │     │
  │           │     ├─→ Static content? → StaticContentController
  │           │     ├─→ Health check? → HealthController
  │           │     ├─→ Webhook? → GithubWebhookController
  │           │     └─→ Web page? → WebController
  │           │
  │           └─→ WebController (web pages) [@Deprecated]
  │                 │
  │                 ├─→ Path matching (manual)
  │                 │     │
  │                 │     ├─→ /dashboard → DashboardHandler
  │                 │     ├─→ /group/* → CardHandler
  │                 │     ├─→ /commits/* → CommitsHandler
  │                 │     ├─→ /contents/* → ContentsHandler
  │                 │     └─→ /wiki/* → CantaraWikiHandler
  │                 │
  │                 └─→ WebHandler implementation
  │                       │
  │                       └─→ ThymeleafViewEngineProcessor
  │
  └─→ HTTP Response
```

### Legacy Controller Example

**Before (Undertow):**

```java
@Deprecated(since = "0.10.17-SNAPSHOT", forRemoval = true)
public class HealthController implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // Manual thread dispatch
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        // Manual JSON building
        String json = String.format(
            "{\"status\": \"UP\", \"timestamp\": \"%s\"}",
            Instant.now()
        );

        // Manual header setting
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(200);

        // Manual response sending
        exchange.getResponseSender().send(json);
    }
}
```

**Code Characteristics:**

- Manual thread dispatch (`isInIoThread()`)
- String-based JSON construction
- Manual header management
- No type safety
- Hard to test

**After (Spring MVC):**

```java
@RestController
@RequestMapping("/health")
public class HealthRestController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString()
        ));
    }
}
```

**Benefits:**

- 70% less code
- Automatic JSON serialization
- Type-safe response
- Easy to test with `@WebMvcTest`
- Standard Spring MVC patterns

## Thymeleaf View Engine

### Architecture

```
Controller
  │
  ├─→ Returns view name (e.g., "index")
  │
  └─→ Spring MVC ViewResolver
        │
        ├─→ ThymeleafViewResolver
        │     │
        │     ├─→ Find template: META-INF/views/{viewName}.html
        │     │
        │     └─→ ThymeleafViewEngineProcessor
        │           │
        │           ├─→ Parse template
        │           ├─→ Process th:* attributes
        │           ├─→ Merge with model data
        │           └─→ Render HTML
        │
        └─→ HTTP Response (text/html)
```

### ThymeleafViewEngineProcessor

**File**: `src/main/java/no/cantara/docsite/web/ThymeleafViewEngineProcessor.java`

```java
/**
 * Thymeleaf view engine for server-side HTML rendering.
 *
 * <p>Supports both Spring MVC (recommended) and legacy Undertow modes.
 */
@Component
public class ThymeleafViewEngineProcessor {

    private final TemplateEngine templateEngine;

    public ThymeleafViewEngineProcessor() {
        this.templateEngine = createTemplateEngine();
    }

    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/META-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        return engine;
    }

    /**
     * Render template with model data.
     *
     * @param templateName Template name (without .html extension)
     * @param model Model data
     * @return Rendered HTML
     */
    public String render(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);

        return templateEngine.process(templateName, context);
    }

    /**
     * Render template to HTTP response (Spring MVC).
     */
    public void renderToResponse(
            String templateName,
            Map<String, Object> model,
            HttpServletResponse response) throws IOException {

        String html = render(templateName, model);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }

    /**
     * Render template to Undertow exchange (legacy mode).
     *
     * @deprecated Use Spring MVC controllers instead
     */
    @Deprecated(since = "0.10.17-SNAPSHOT")
    public void renderToExchange(
            String templateName,
            Map<String, Object> model,
            HttpServerExchange exchange) {

        String html = render(templateName, model);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html;charset=UTF-8");
        exchange.getResponseSender().send(html);
    }
}
```

### Template Structure

**Base Template**: `src/main/resources/META-INF/views/template.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${pageTitle}">Source Code Portal</title>
    <link rel="stylesheet" th:href="@{/css/main.css}">
</head>
<body>
    <header th:insert="~{fragments/header :: header}"></header>

    <main>
        <div th:insert="~{${contentTemplate} :: content}"></div>
    </main>

    <footer th:insert="~{fragments/footer :: footer}"></footer>

    <script th:src="@{/js/main.js}"></script>
</body>
</html>
```

**Page Template**: `src/main/resources/META-INF/views/index.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <div th:fragment="content">
        <h1>Dashboard</h1>

        <div th:each="group : ${groups}">
            <h2 th:text="${group.displayName}">Group Name</h2>

            <ul>
                <li th:each="repo : ${group.repositories}">
                    <a th:href="@{/contents/{org}/{repo}/master(org=${repo.organization}, repo=${repo.name})}"
                       th:text="${repo.name}">Repository Name</a>

                    <span th:if="${repo.buildStatus != null}"
                          th:class="|badge badge-${repo.buildStatus.status}|"
                          th:text="${repo.buildStatus.status}">Status</span>
                </li>
            </ul>
        </div>
    </div>
</body>
</html>
```

**Thymeleaf Features Used:**

- `th:text` - Set element text content
- `th:each` - Loop over collections
- `th:if` - Conditional rendering
- `th:href` - Dynamic URL generation
- `th:class` - Dynamic CSS classes
- `th:insert` - Template fragments
- `@{...}` - URL expression (context path aware)
- `${...}` - Variable expression

## Controller Patterns

### Pattern 1: Simple Page Controller

**Use Case**: Render a page with static or simple dynamic content.

```java
@Controller
public class SimpleController {

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("version", "1.0.0");
        return "about";
    }
}
```

### Pattern 2: Data-Driven Page Controller

**Use Case**: Render a page with data from services/cache.

```java
@Controller
public class DataDrivenController {

    private final DataService dataService;

    public DataDrivenController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/data")
    public String data(Model model) {
        List<Item> items = dataService.getAllItems();
        model.addAttribute("items", items);
        return "data";
    }
}
```

### Pattern 3: Path Variable Controller

**Use Case**: Dynamic URL segments (e.g., `/group/{id}`).

```java
@Controller
public class PathVariableController {

    @GetMapping("/group/{groupId}")
    public String group(
            @PathVariable String groupId,
            Model model) {

        RepositoryGroup group = findGroup(groupId);
        model.addAttribute("group", group);
        return "group/card";
    }
}
```

### Pattern 4: REST API Controller

**Use Case**: Return JSON data for AJAX or external clients.

```java
@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/stats")
    public ResponseEntity<Statistics> stats() {
        Statistics stats = calculateStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh() {
        triggerRefresh();
        return ResponseEntity.accepted().build();
    }
}
```

### Pattern 5: Error Handling Controller

**Use Case**: Custom error pages.

```java
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("message", getErrorMessage(statusCode));

        return "error";
    }
}
```

## Migration Guide

### Converting Undertow to Spring MVC

**Step-by-Step Process:**

1. **Identify Handler Type**:
   - Web page? → `@Controller`
   - JSON API? → `@RestController`
   - Static resource? → Configure in `WebMvcConfiguration`

2. **Create Spring Controller**:
   ```java
   @Controller  // or @RestController
   public class MyController {
       // Constructor injection
       public MyController(Dependencies deps) { ... }
   }
   ```

3. **Add Request Mapping**:
   ```java
   @GetMapping("/my-path")
   public String/ResponseEntity handle(...) { ... }
   ```

4. **Extract Request Data**:
   - Path variable: `@PathVariable String id`
   - Query param: `@RequestParam String filter`
   - Request body: `@RequestBody MyData data`

5. **Return Response**:
   - View name: `return "template-name";`
   - Data object: `return ResponseEntity.ok(data);`

6. **Test**:
   ```java
   @WebMvcTest(MyController.class)
   class MyControllerTest { ... }
   ```

### Example Migration

**Before (Undertow)**:

```java
public class MyHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        String pathParam = extractPathParam(exchange);
        String json = buildJsonResponse(pathParam);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json);
    }
}
```

**After (Spring MVC)**:

```java
@RestController
@RequestMapping("/my-endpoint")
public class MyRestController {

    @GetMapping("/{id}")
    public ResponseEntity<MyData> handle(@PathVariable String id) {
        MyData data = buildResponse(id);
        return ResponseEntity.ok(data);
    }
}
```

**Code Reduction**: 60-70%

## Related Documentation

- [Spring Boot Architecture](spring-boot.md) - Application initialization and configuration
- [Caching Architecture](caching.md) - Cache integration with controllers
- [Package Structure](packages.md) - Controller package organization
- [Testing Guide](../development/testing.md) - Controller testing strategies

---

**Next Steps**: Read the [Caching Architecture](caching.md) document to understand how controllers interact with the cache layer.
