# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Source Code Portal (SCP) is an organizational dashboard that aggregates and displays status and documentation from GitHub repositories. It's designed for small to medium-sized organizations (<2500 commits/hour).

**Key Capabilities:**
- Aggregates repository data from GitHub organizations
- Groups multiple codebases into logical systems
- Displays commit logs, documentation, and build status
- Integrates with Jenkins, Snyk, and Shields.io for status badges
- Supports GitHub webhooks for real-time updates
- Renders Markdown and AsciiDoc documentation

**Technology Stack:**
- Java 21 LTS (with virtual threads enabled)
- **Spring Boot 3.2.2 (primary mode)** with Undertow 2.3.17 (embedded server)
- Thymeleaf 3.1.2 (server-side templating)
- **Caffeine cache (Spring Cache abstraction)** / JSR-107 JCache (legacy)
- Resilience4j 2.2.0 (circuit breaker pattern for external calls)
- **Spring Boot Actuator** (health checks, metrics, monitoring)
- JUnit 5.11.3 (test framework)
- Sass/SCSS (frontend styling)
- Maven (build system)

## Build Commands

```bash
# Full clean build with tests
mvn clean install

# Build without tests (faster)
mvn clean install -DskipTests

# Compile Sass to CSS using Maven plugin (slower)
mvn com.github.warmuuh:libsass-maven-plugin:watch

# Compile Sass using native command (requires sass installed)
sass --watch src/main/sass/scss:target/classes/META-INF/views/css

# Run single test
mvn test -Dtest=TestClassName

# Run specific test method
mvn test -Dtest=TestClassName#methodName
```

## Running the Application

### Spring Boot Mode (Recommended)

**Spring Boot is the recommended mode** for running the application. It provides modern Spring ecosystem features including dependency injection, auto-configuration, actuator endpoints, and better testability.

```bash
# Run with Maven
mvn spring-boot:run

# Run with Maven in dev mode (auto-reload)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run JAR after building
mvn clean package
java -jar target/source-code-portal-*.jar

# Run with IntelliJ: Execute SpringBootServer.main()

# Run with custom profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Run with environment variables
SCP_GITHUB_ACCESS_TOKEN=your_token mvn spring-boot:run

# Default server URL: http://localhost:9090
```

**Spring Boot Actuator Endpoints:**
- `/actuator/health` - Overall health with custom indicators (GitHub, cache, executor)
- `/actuator/health/github` - GitHub API rate limit status
- `/actuator/health/cache` - Cache health and statistics
- `/actuator/health/executor` - Thread pool health
- `/actuator/info` - Application information (version, runtime, configuration)
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics endpoint
- `/actuator/caches` - Cache manager details
- `/actuator/scheduledtasks` - Scheduled task list

**Application Endpoints:**
- `/ping` - Simple health check
- `/health` - Health status (legacy endpoint, prefer /actuator/health)
- `/docs` - Main documentation portal
- `/dashboard` or `/` - Dashboard view
- `/group/{groupId}` - Group view
- `/contents/{org}/{repo}/{branch}` - Repository contents
- `/commits/{org}/{repo}` - Commit log
- `/github/webhook` - GitHub webhook receiver

### Legacy Undertow Mode (Deprecated)

The legacy Undertow mode is still available but deprecated. It will be removed in a future version.

```bash
# Run main class (after building)
mvn clean package
java -cp target/source-code-portal-*.jar no.cantara.docsite.Server

# Run with IntelliJ: Execute Server.main()

# Default server URL: http://localhost:9090
```

**Note**: When running in legacy mode, Spring Boot actuator endpoints are not available. Use `/health` for basic health checks.

## Configuration

**Configuration files are loaded in this order (later overrides earlier):**
1. `src/main/resources/application-defaults.properties` (defaults)
2. `application.properties` (custom overrides)
3. `security.properties` (credentials)
4. `application_override.properties` (final overrides)
5. Environment variables with `SCP_` prefix
6. System properties

**Repository Configuration:**
- Primary config: `src/main/resources/conf/config.json`
- Defines GitHub organization, repository groups, and external service integrations
- Supports regex patterns for repository matching (e.g., "Whydah*")
- Each group has a `groupId`, `display-name`, `description`, and list of repo patterns

**GitHub Authentication:**
Configure in `security.properties`:
```properties
github.oauth2.client.clientId=YOUR_CLIENT_ID
github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET
github.client.accessToken=YOUR_ACCESS_TOKEN
```

To generate an access token, use `ObtainGitHubAccessTokenTestTool` or Docker:
```bash
docker run -it -e SCP_github.oauth2.client.clientId=CLIENT_ID \
  -e SCP_github.oauth2.client.clientSecret=CLIENT_SECRET \
  cantara/sourcecodeportal /github-access-token
```

## Architecture

### Core Components

**Spring Boot Application Initialization (SpringBootServer.java):**
1. Load configuration from Spring Boot properties (application.yml, environment variables, system properties)
2. Initialize Spring context with auto-configuration
3. Create CacheStore bean (Spring Cache with Caffeine backend)
4. Create ExecutorService and ScheduledExecutorService beans
5. Load RepositoryConfigLoader (parse config.json, fetch GitHub repos)
6. Run SpringBootInitializer for startup tasks
7. Enable PreFetchData (initial cache population)
8. Start embedded Undertow server (via Spring Boot)

**Request Flow (Spring Boot Mode):**
```
HTTP Request
  → Spring DispatcherServlet (Spring MVC)
    → Spring MVC Controllers (@Controller / @RestController)
      → Web Pages:
        - DashboardWebController (/dashboard, /)
        - GroupWebController (/group/{groupId})
        - CommitsWebController (/commits/*)
        - ContentsWebController (/contents/{org}/{repo}/{branch})
        - WikiWebController (/wiki/{pageName})
      → REST/API:
        - PingRestController (/ping)
        - HealthRestController (/health, /health/github, /health/threads)
        - EchoRestController (/echo)
        - GitHubWebhookRestController (/github/webhook)
      → Resources:
        - BadgeResourceController (/badge/*)
        - Spring Boot Static Resource Handler (CSS, JS, images)
      → ThymeleafViewEngineProcessor (render HTML for web pages)
```

**Request Flow (Legacy Undertow Mode - Deprecated):**
```
HTTP Request
  → ApplicationController (routing) [@Deprecated]
    → WebController (web pages) [@Deprecated]
      → WebHandler implementations (DashboardHandler, CardHandler, etc.)
        → ThymeleafViewEngineProcessor (render HTML)
    → OR StaticContentController (static resources)
    → OR HealthController (health checks)
    → OR GithubWebhookController (webhook events)
```

### Package Structure

**`no.cantara.docsite.cache`** - JCache-based caching layer
- `CacheStore` - Central cache manager with typed caches for repositories, commits, contents, build status, etc.
- Cache keys: `CacheKey`, `CacheRepositoryKey`, `CacheGroupKey`, `CacheShaKey`

**`no.cantara.docsite.domain`** - Domain models and business logic
- `config/` - Repository configuration loading and parsing
- `github/` - GitHub API models (repos, commits, releases, contents)
- `renderer/` - Markdown/AsciiDoc rendering logic
- `jenkins/` - Jenkins build status integration
- `snyk/` - Snyk security test integration
- `shields/` - Shields.io badge integration
- `scm/` - Source control abstractions (currently GitHub-only)

**`no.cantara.docsite.controller`** - HTTP request handlers
- **Spring MVC Controllers (Recommended)**:
  - `spring/` - Spring MVC @Controller and @RestController classes
    - `DashboardWebController` - Dashboard page
    - `GroupWebController` - Group view page
    - `CommitsWebController` - Commit history page
    - `ContentsWebController` - Repository contents page
    - `WikiWebController` - Wiki page
    - `PingRestController` - Ping endpoint
    - `HealthRestController` - Health endpoints (/health, /health/github, /health/threads)
    - `EchoRestController` - Echo diagnostic endpoint
    - `GitHubWebhookRestController` - GitHub webhook receiver
    - `BadgeResourceController` - Badge serving (SVG images)
- **Legacy Undertow Controllers (Deprecated)**:
  - `ApplicationController` - Main routing controller [@Deprecated since 0.10.17-SNAPSHOT]
  - `WebController` - Web page routing [@Deprecated since 0.10.17-SNAPSHOT]
  - `handler/` - Page-specific handlers (DashboardHandler, CardHandler, CommitsHandler, etc.) [@Deprecated]
  - See `DEPRECATED_UNDERTOW_CONTROLLERS.md` for migration guide

**`no.cantara.docsite.commands`** - Resilience4j command pattern for external HTTP calls
- `BaseResilientCommand` - Base class with circuit breaker, bulkhead, and time limiter
- `GetGitHubCommand` - GitHub API requests with circuit breaker
- `GetCommand` - Generic HTTP requests
- `GetShieldsCommand` - Shields.io requests

**`no.cantara.docsite.fetch`** - Data fetching services
- `PreFetchData` - Initial data population on startup
- `ScheduledFetchData` - Periodic background updates

**`no.cantara.docsite.web`** - Web templating infrastructure
- `ThymeleafViewEngineProcessor` - Thymeleaf rendering
- `ResourceContext` - Request path parsing
- `WebHandler` - Handler interface

**`no.cantara.docsite.executor`** - Thread pool management
- `ExecutorService` - Async task executor
- `ScheduledExecutorService` - Scheduled periodic tasks

### Key Patterns

**Caching Strategy:**
- All GitHub data is cached using JSR-107 JCache (RI implementation)
- Cache keys are strongly typed: `CacheKey` (org/repo/branch), `CacheRepositoryKey` (includes groupId), etc.
- Data is prefetched on startup and refreshed periodically via scheduled tasks
- Webhooks trigger immediate cache updates for push events

**Circuit Breaker Pattern:**
- All external HTTP calls use Resilience4j commands (`BaseResilientCommand`)
- Implements Circuit Breaker (50% failure threshold, 60s open state)
- Implements Bulkhead (25 max concurrent calls)
- Implements Time Limiter (75s timeout)
- Uses Java 21 virtual threads for async execution
- Protects against API rate limits and network failures

**Configuration-Driven Repository Groups:**
- `config.json` defines logical groupings of repositories
- Supports regex patterns for dynamic repository inclusion
- Each group has a "default group repo" for navigation

## Development

**Frontend Development:**
Install Node.js (10.x+) and Sass (3.5+) for live frontend development:

```bash
# Native Sass watch (fastest)
sass --watch src/main/sass/scss:target/classes/META-INF/views/css

# In IntelliJ: Map keyboard shortcut to Build > Rebuild
# Rebuild syncs templates to target/classes/META-INF/views
```

Thymeleaf templates are in `src/main/resources/META-INF/views/`:
- `template.html` - Base layout
- `index.html` - Dashboard
- `group/card.html` - Group view
- `contents/content.html` - Repository contents
- `commits/commits.html` - Commit history

**Testing:**
- Tests use JUnit 5 (Jupiter) framework
- `TestServerExtension` provides embedded server for integration tests
- `TestClient` provides HTTP client utilities
- Test data can be dumped to files using `DumpTestDataToFile`
- Run tests: `mvn test`
- Run specific test: `mvn test -Dtest=TestClassName`

## Docker

**Build Docker image:**
```bash
# Build specific version
docker build --build-arg DOCKER_TAG=0.10.17 -t cantara/sourcecodeportal .

# Build latest (downloads from Maven repository)
docker build -t cantara/sourcecodeportal .
```

**Run Docker container:**
```bash
# Run with OAuth credentials
docker run --env github.oauth2.client.clientId=xyz \
  --env github.oauth2.client.clientSecret=secret \
  --rm -p 80:9090 cantara/sourcecodeportal

# Copy config to running container
docker inspect -f '{{.Id}}' cantara/sourcecodeportal
sudo cp config.json /var/lib/docker/aufs/mnt/CONTAINER_ID/home/sourcecodeportal/config_override/conf/config.json
```

## GitHub Webhooks

To receive real-time updates from GitHub:

1. Set up webhook at `https://github.com/organizations/YOUR_ORG/settings/hooks/`
2. URL: `https://your-server.com/github/webhook`
3. Content type: `application/json`
4. Secret: Same as `github.webhook.securityAccessToken` in `security.properties`
5. Events: Branch or tag creation, Pushes, Releases

**For local development with ngrok:**
```bash
ngrok http 9090
# Use the ngrok URL for webhook: https://XXXXX.ngrok.io/github/webhook
```

Webhook handling is in `GitHubWebhookRestController` (Spring MVC) or `GithubWebhookController` (legacy Undertow), which updates the cache when push events are received.

## Important Notes

- ChromeDriver is required for GitHub OAuth token generation (not runtime)
- The application requires **JDK 21** (LTS)
- Maven Shade plugin creates a fat JAR with all dependencies
- Virtual threads are enabled for better I/O performance
- Repository visibility (public/private/all) is controlled by `github.repository.visibility` property

## Modernization Documentation

**Phase 1 (Complete)**:
- `PHASE1_COMPLETE.md` - Full Phase 1 summary
- `MODERNIZATION_PHASE1.md` - Technical details
- `GOTCHAS_AND_LEARNINGS.md` - Common issues and solutions
- `MIGRATION_JUNIT5_SUMMARY.md` - Test framework migration

**Next Phase**: Phase 2 - Spring Boot Migration (see modernization plan)

## Claude Code Skills

**IMPORTANT**: After completing any significant task, always create or update Claude Code skills to capture learnings.

### Available Skills

Skills are located in `~/.claude/skills/` and can be invoked with `/skill-name`:

1. **`/modernize-dependency`** - Safely update dependencies
   - Handles breaking changes
   - API migration patterns
   - Common gotchas (group ID changes, API renames)
   - Use when: Updating any library version

2. **`/migrate-test-framework`** - Migrate test frameworks
   - TestNG → JUnit 5 (complete guide)
   - Listener → Extension conversion
   - Assertion order changes
   - Use when: Changing test frameworks

3. **`/verify-build`** - Comprehensive build verification
   - Systematic error resolution
   - Common fixes (dependencies, APIs, imports)
   - Build optimization tips
   - Use when: Build fails or after major changes

### Creating New Skills

**After completing a task**, create or update skills following this process:

1. **Identify reusable patterns**:
   - Did you solve a complex problem?
   - Did you discover gotchas?
   - Would this help in similar projects?

2. **Create skill file**: `~/.claude/skills/skill-name.yaml`
   ```yaml
   name: skill-name
   description: One-line description
   version: 1.0.0

   invoke:
     prompt: |
       [Detailed instructions...]
   ```

3. **Document learnings**: Add to `GOTCHAS_AND_LEARNINGS.md`
   - What went wrong?
   - How you fixed it
   - Code examples (before/after)
   - Why it happened

4. **Update CLAUDE.md**: Add skill to list above

### Skill Creation Guidelines

**Good skill candidates**:
- Complex migrations (framework upgrades, library replacements)
- Build issues with non-obvious solutions
- Multi-step processes (testing, deployment, configuration)
- Domain-specific patterns (this codebase's architecture)

**What to include in skills**:
- Step-by-step instructions
- Common gotchas with solutions
- Code examples (before/after)
- Verification steps
- Rollback procedures
- Tool commands (bash, mvn, etc.)

**Example template**:
```yaml
name: example-skill
description: Do something useful
version: 1.0.0

invoke:
  prompt: |
    ## 1. Analysis
    [What to check first]

    ## 2. Process
    [Step-by-step instructions]

    ## 3. Common Issues
    [Gotchas and fixes]

    ## 4. Verification
    [How to verify success]

    ## 5. Examples
    [Code examples]
```

### When to Create Skills

Create skills after:
- ✅ Completing Phase 1, 2, 3, etc. of a project
- ✅ Solving a tricky build issue
- ✅ Discovering non-obvious solutions
- ✅ Migrating between technologies
- ✅ Implementing complex patterns

**Rule of thumb**: If you spent >1 hour solving something, make it a skill.

### Maintaining Skills

- **Version skills**: Increment version when updating
- **Test skills**: Verify instructions work
- **Keep current**: Update when technology changes
- **Share learnings**: Document in GOTCHAS_AND_LEARNINGS.md

---

## Spring Boot Migration (Phase 2 - Completed)

The application has been migrated from standalone Undertow to Spring Boot 3.2.2. Both modes are supported for backward compatibility, but **Spring Boot is the recommended mode**.

### What Changed

**Infrastructure:**
- Added Spring Boot 3.2.2 with BOM dependency management
- Added Spring Boot starters (web, cache, actuator, thymeleaf)
- Migrated configuration to Spring Boot properties (application.yml)
- Added Spring Boot Actuator for observability

**Configuration:**
- Type-safe configuration with @ConfigurationProperties (`ApplicationProperties.java`)
- Backward compatibility bridge for legacy code (`ConfigurationBridge.java`)
- Environment variable support (`SCP_*` prefix)
- Profile-based configuration (dev, prod, test)

**Caching:**
- Spring Cache abstraction with Caffeine backend
- Micrometer metrics integration
- Prometheus metrics export

**Controllers:**
- Spring MVC controllers (`@RestController`, `@Controller`)
- Automatic JSON serialization
- Declarative routing with annotations
- CORS configuration via `WebMvcConfiguration`

**Scheduling:**
- Spring @Scheduled tasks replacing custom executors
- Configuration-driven intervals
- Actuator endpoint: `/actuator/scheduledtasks`

**Observability:**
- Custom health indicators (GitHub, Cache, Executor)
- Custom info contributor (application metadata)
- Prometheus metrics
- Health probes for Kubernetes

### Migration Files

**Key Files Created:**
- `src/main/java/no/cantara/docsite/SpringBootServer.java` - Main entry point
- `src/main/resources/application.yml` - Spring Boot configuration
- `src/main/java/no/cantara/docsite/config/ApplicationProperties.java` - Type-safe config
- `src/main/java/no/cantara/docsite/config/ConfigurationBridge.java` - Legacy compatibility
- `src/main/java/no/cantara/docsite/config/SpringBootInitializer.java` - Startup initialization
- `src/main/java/no/cantara/docsite/actuator/*` - Custom health indicators
- `src/main/java/no/cantara/docsite/controller/spring/*` - Spring MVC controllers

**Documentation:**
- `PHASE2_PROGRESS.md` - Migration progress and status
- `TASK*_SUMMARY.md` - Task-specific documentation
- `CLAUDE_SKILLS.md` - Skills for common tasks

### Available Skills

See `CLAUDE_SKILLS.md` for detailed skill documentation. Key skills:

**Tier 1 (Immediate Value):**
- `/migrate-controller` - Convert Undertow controller to Spring MVC
- `/add-scheduled-task` - Create Spring @Scheduled task
- `/add-health-indicator` - Create custom health indicator

**Tier 2 (High Value):**
- `/add-repository-group` - Add repository group configuration
- `/configure-spring-cache` - Set up cache configuration
- `/modernize-dependency` - Safely update dependencies

**Tier 3 (Strategic Value):**
- `/add-spring-config` - Add configuration properties
- `/add-integration` - Add external service integration
- `/add-webhook-handler` - Add webhook support
- `/migrate-to-spring-boot` - General migration orchestrator

### Running Tests

```bash
# Run all tests
mvn test

# Run Spring Boot tests only
mvn test -Dtest=*SpringBoot*

# Run specific test class
mvn test -Dtest=SpringBootServerTest

# Run with specific profile
mvn test -Dspring.profiles.active=test
```

### Dual-Mode Support

The application supports both Spring Boot and legacy Undertow modes:

**Spring Boot Mode (Recommended):**
```bash
mvn spring-boot:run
# or
java -jar target/source-code-portal-*.jar
```

**Legacy Undertow Mode (Deprecated):**
```bash
java -cp target/source-code-portal-*.jar no.cantara.docsite.Server
```

Mode can be configured via `scp.server.mode` property (values: `spring-boot`, `undertow`).

---

## Controller Migration (Week 2-3 - Completed)

All Undertow controllers and handlers have been migrated to Spring MVC as part of Phase 2 completion. The legacy Undertow controllers are now deprecated and will be removed in a future version.

### What Changed

**Controllers Migrated (13 total):**
1. **StaticContentController** → Spring Boot static resource configuration
2. **ImageResourceController** → Spring Boot static resource configuration
3. **CORSController** → `CorsConfiguration` (Spring Boot CORS config)
4. **EchoController** → `EchoRestController` (Spring MVC)
5. **PingController** → `PingRestController` (already existed from Phase 2)
6. **GithubWebhookController** → `GitHubWebhookRestController` (Spring MVC)
7. **HealthController** → `HealthRestController` (enhanced with all legacy features)
8. **DashboardHandler** → `DashboardWebController` (already existed from Phase 2)
9. **CardHandler** → `GroupWebController` (Spring MVC)
10. **CommitsHandler** → `CommitsWebController` (Spring MVC)
11. **ContentsHandler** → `ContentsWebController` (Spring MVC)
12. **CantaraWikiHandler** → `WikiWebController` (Spring MVC)
13. **BadgeResourceHandler** → `BadgeResourceController` (Spring MVC)

**Routing Controllers Deprecated:**
- `ApplicationController` - Marked @Deprecated (forRemoval = true)
- `WebController` - Marked @Deprecated (forRemoval = true)
- These remain for Undertow standalone mode compatibility

### Benefits Achieved

**Code Quality:**
- 50-70% less boilerplate code with Spring MVC
- Cleaner code following Spring Boot conventions
- Better separation of concerns
- Comprehensive inline documentation

**Developer Experience:**
- Industry-standard patterns (Spring MVC)
- Better IDE support (Spring tooling)
- Easier testability with @WebMvcTest
- Constructor injection for dependencies
- Declarative routing with annotations

**Backward Compatibility:**
- All URLs unchanged (e.g., `/dashboard`, `/group/{id}`, `/health`)
- Identical JSON response formats maintained
- Dual-mode support (Spring Boot primary, Undertow legacy)
- Zero breaking changes for existing users

**Observability:**
- Spring Boot Actuator integration ready
- Better logging at controller level
- Metrics collection (future enhancement)

### Documentation

- `WEEK2-3_PROGRESS.md` - Detailed migration progress and statistics
- `SESSION_SUMMARY.md` - Complete session summary
- `DEPRECATED_UNDERTOW_CONTROLLERS.md` - Deprecation guide and timeline
- `DEPRECATED_UNDERTOW_CONTROLLERS.md` lists all deprecated controllers with Spring MVC replacements

### Migration Pattern

**Before (Undertow):**
```java
public class MyController implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // Manual thread dispatch
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        // Manual JSON building
        String json = "{\"result\": \"value\"}";
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json);
    }
}
```

**After (Spring MVC):**
```java
@RestController
@RequestMapping("/my-endpoint")
public class MyRestController {
    @GetMapping
    public ResponseEntity<Map<String, Object>> handle() {
        return ResponseEntity.ok(Map.of("result", "value"));
    }
}
```

**Code Reduction**: 50-70% (Spring handles boilerplate)

---

## Quick Start for New Claude Code Sessions

When starting work on this project:

1. **Read this file first** (`CLAUDE.md`)
2. **Read project overview** (`README.md`) - User-facing overview
3. **Review learnings** (`LEARNINGS.md`) - All gotchas and best practices
4. **Check skills** (`CLAUDE_SKILLS.md`) - Automation skills available
5. **Verification** (`VERIFICATION_GUIDE.md`) - How to verify Spring Boot works
6. **Use skills** for common tasks:
   - `/migrate-controller` - Convert Undertow → Spring MVC
   - `/add-scheduled-task` - Create @Scheduled tasks
   - `/add-health-indicator` - Add health checks
7. **After task**: Update `LEARNINGS.md` and `CHANGELOG.md`

### Documentation Structure

```
Root Level (quick access):
- README.md              - Project overview
- CHANGELOG.md           - Version history
- LEARNINGS.md           - All gotchas & learnings
- CLAUDE.md              - This file (Claude Code guide)
- CLAUDE_SKILLS.md       - Automation skills
- VERIFICATION_GUIDE.md  - Verify Spring Boot
- PHASE2_PROGRESS.md     - Phase 2 reference

docs/ (detailed guides):
- getting-started/       - Quick start, building, config
- architecture/          - System design, patterns
- features/              - Feature documentation
- operations/            - Deployment, monitoring
- development/           - Contributing, testing
- history/               - Archived docs (Phase 1 & 2)
```

### Important References

- **Phase 2 Complete**: Spring Boot migration finished (see `PHASE2_PROGRESS.md`)
- **Phase 2 Learnings**: Consolidated in `LEARNINGS.md` (Phase 2 section)
- **Historical Docs**: Archived in `docs/history/` (still accessible)
- **Next Steps**: See `NEXT_STEPS.md` for planned work
