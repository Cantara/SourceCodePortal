# Claude Code Skills for Source Code Portal

This document defines high-value Claude Code skills for the Source Code Portal codebase. These skills automate common development tasks and accelerate feature development.

**Last Updated**: 2026-01-27
**Status**: Skills defined, ready for implementation

---

## Overview

Skills are specialized capabilities that Claude Code can perform. Each skill below represents a common task that would normally take hours or days but can be automated to take minutes.

Skills are organized into three categories:
1. **Migration Skills** - Spring Boot migration and modernization
2. **Integration Skills** - Adding new external service integrations
3. **Configuration Skills** - Managing repository groups and settings

---

## Migration Skills

### 1. `/migrate-to-spring-boot`

**Purpose**: Incrementally migrate components from Undertow to Spring Boot

**Status**: ✅ Knowledge complete (7/8 tasks done in Phase 2)

**What it does**:
- Analyzes the component to migrate (controller, service, configuration)
- Determines the appropriate Spring Boot pattern
- Creates Spring Boot equivalent with annotations
- Maintains dual-mode compatibility (Undertow + Spring Boot coexist)
- Adds @Profile("!test") for proper test isolation
- Compiles and verifies the migration
- Documents the changes

**Example Usage**:
```
/migrate-to-spring-boot GroupController
```

**What gets created**:
- Spring MVC controller with @RestController or @Controller
- Dependency injection with constructor injection
- Request mapping annotations (@GetMapping, @PostMapping)
- Automatic JSON serialization with ResponseEntity
- Model attributes for Thymeleaf templates
- Error handling with @ExceptionHandler

**Time Saved**: 2-4 hours per controller → 5-10 minutes

**Knowledge Source**: Tasks 1-7 in PHASE2_PROGRESS.md, TASK5_SUMMARY.md

---

### 2. `/migrate-controller`

**Purpose**: Convert a specific Undertow controller to Spring MVC

**Status**: ✅ Implemented (2026-01-27)

**What it does**:
- Reads the existing Undertow controller
- Identifies handler methods and routing
- Creates Spring MVC equivalent:
  - @RestController for REST endpoints (automatic JSON)
  - @Controller for web pages (Thymeleaf)
  - @RequestMapping for routing
  - @GetMapping/@PostMapping/@PutMapping/@DeleteMapping
  - @RequestParam/@PathVariable for parameters
  - ResponseEntity for REST responses
  - Model for Thymeleaf attributes
- Handles CORS, error handling, validation
- Maintains dual-mode operation
- Reduces code by 70-80%

**Example Usage**:
```
/migrate-controller src/main/java/no/cantara/docsite/controller/GroupController.java
```

**Before (Undertow - 180 lines)**:
```java
public class GroupController implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String groupId = exchange.getQueryParameters().get("groupId").getFirst();
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("group", groupId);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(builder.build().toString());
    }
}
```

**After (Spring MVC - 40 lines)**:
```java
@RestController
@RequestMapping("/group")
public class GroupRestController {
    @GetMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroup(@PathVariable String groupId) {
        Map<String, Object> response = new HashMap<>();
        response.put("group", groupId);
        return ResponseEntity.ok(response);
    }
}
```

**Time Saved**: 1-3 hours per controller → 5 minutes

**Knowledge Source**: TASK5_SUMMARY.md, PingRestController.java, HealthRestController.java, DashboardWebController.java

---

### 3. `/add-health-indicator`

**Purpose**: Create custom Spring Boot Actuator health indicator

**Status**: ✅ Implemented (2026-01-27)

**What it does**:
- Prompts for health indicator name and purpose
- Prompts for health check logic (service to monitor)
- Creates HealthIndicator implementation:
  - Component annotation with name
  - @Profile("!test") exclusion
  - Health check logic
  - Status determination (UP/DOWN/DEGRADED)
  - Detailed health information
  - Error handling with try-catch
- Adds logging
- Compiles and verifies
- Documents the endpoint

**Example Usage**:
```
/add-health-indicator jenkins
```

**What gets created**:
```java
@Component("jenkins")
@Profile("!test")
public class JenkinsHealthIndicator implements HealthIndicator {
    private static final Logger LOG = LoggerFactory.getLogger(JenkinsHealthIndicator.class);

    @Override
    public Health health() {
        try {
            // Check Jenkins connectivity
            boolean jenkinsUp = checkJenkinsAPI();

            if (!jenkinsUp) {
                return Health.down()
                    .withDetail("error", "Jenkins API unreachable")
                    .build();
            }

            return Health.up()
                .withDetail("jenkinsUrl", jenkinsUrl)
                .withDetail("lastSeen", lastSeenTimestamp)
                .build();

        } catch (Exception e) {
            LOG.error("Jenkins health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

**Endpoint Created**: `/actuator/health/jenkins`

**Time Saved**: 1-2 hours → 5 minutes

**Knowledge Source**: TASK7_SUMMARY.md, GitHubHealthIndicator.java, CacheHealthIndicator.java, ExecutorHealthIndicator.java

---

### 4. `/add-scheduled-task`

**Purpose**: Create Spring @Scheduled task to replace custom executor tasks

**Status**: ✅ Implemented (2026-01-27)

**What it does**:
- Prompts for task name and purpose
- Prompts for schedule (fixed rate, fixed delay, or cron)
- Prompts for initial delay
- Creates @Service with @Scheduled method:
  - @ConditionalOnProperty for feature flag
  - @Scheduled annotation with configuration properties
  - Dependency injection of required services
  - Error handling and logging
  - Metrics/health tracking
- Adds configuration properties to application.yml
- Compiles and verifies
- Documents in /actuator/scheduledtasks

**Example Usage**:
```
/add-scheduled-task fetch-releases
```

**What gets created**:
```java
@Service
@ConditionalOnProperty(name = "scp.scheduled.enabled", havingValue = "true")
@Profile("!test")
public class FetchReleasesScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(FetchReleasesScheduledService.class);

    @Scheduled(
        fixedRateString = "${scp.scheduled.releases.interval-minutes}",
        timeUnit = TimeUnit.MINUTES,
        initialDelayString = "${scp.scheduled.releases.initial-delay-minutes:2}"
    )
    public void fetchReleases() {
        LOG.info("Fetching GitHub releases");
        try {
            // Fetch and cache releases
            HealthResource.instance().markScheduledWorkerLastSeen("releases");
        } catch (Exception e) {
            LOG.error("Failed to fetch releases", e);
        }
    }
}
```

**Configuration added to application.yml**:
```yaml
scp:
  scheduled:
    releases:
      interval-minutes: 60
      initial-delay-minutes: 2
```

**Time Saved**: 1-2 hours → 5 minutes

**Knowledge Source**: TASK6_SUMMARY.md, JenkinsStatusScheduledService.java, SnykStatusScheduledService.java

---

### 5. `/configure-spring-cache`

**Purpose**: Set up Spring Cache abstraction with Caffeine for a new cache

**Status**: 🆕 New skill based on Task 4 learnings

**What it does**:
- Prompts for cache name and purpose
- Prompts for cache key structure
- Prompts for TTL and max size
- Adds cache name to CacheConfiguration
- Creates @Cacheable service methods
- Creates cache key class if needed
- Adds Micrometer metrics binding
- Compiles and verifies
- Documents cache endpoints

**Example Usage**:
```
/configure-spring-cache pullRequests
```

**What gets modified/created**:

**CacheConfiguration.java** (adds cache name):
```java
@Bean
public CaffeineCacheManager cacheManager() {
    cacheManager.setCacheNames(
        "repositories", "commits", "contents",
        "pullRequests"  // NEW
    );
}
```

**PullRequestService.java** (new service):
```java
@Service
public class PullRequestService {
    @Cacheable(value = "pullRequests", key = "#org + ':' + #repo")
    public List<PullRequest> getPullRequests(String org, String repo) {
        // Fetch from GitHub API
    }

    @CacheEvict(value = "pullRequests", key = "#org + ':' + #repo")
    public void invalidatePullRequests(String org, String repo) {
        // Clear cache
    }
}
```

**Metrics exposed**:
- `/actuator/metrics/cache.size?tag=cache:pullRequests`
- `/actuator/metrics/cache.gets?tag=cache:pullRequests`

**Time Saved**: 1-2 hours → 10 minutes

**Knowledge Source**: TASK4_SUMMARY.md, CacheConfiguration.java, CacheMetricsConfiguration.java

---

### 6. `/add-spring-config`

**Purpose**: Add new type-safe configuration properties

**Status**: 🆕 New skill based on Task 3 learnings

**What it does**:
- Prompts for configuration section name
- Prompts for properties (name, type, default value)
- Adds nested static class to ApplicationProperties.java
- Adds configuration to application.yml
- Adds ConfigurationBridge mapping (for legacy compatibility)
- Validates configuration loads correctly
- Compiles and verifies

**Example Usage**:
```
/add-spring-config gitlab
```

**What gets created**:

**ApplicationProperties.java**:
```java
@Configuration
@ConfigurationProperties(prefix = "scp")
public class ApplicationProperties {
    private GitLab gitlab = new GitLab();

    public static class GitLab {
        private String baseUrl = "https://gitlab.com";
        private String accessToken;
        private int apiVersion = 4;

        // Getters and setters
    }
}
```

**application.yml**:
```yaml
scp:
  gitlab:
    base-url: ${SCP_GITLAB_BASE_URL:https://gitlab.com}
    access-token: ${SCP_GITLAB_ACCESS_TOKEN:}
    api-version: 4
```

**ConfigurationBridge.java** (legacy compatibility):
```java
public String evaluateToString(String key) {
    return switch (key) {
        case "gitlab.base.url" -> properties.getGitlab().getBaseUrl();
        case "gitlab.access.token" -> properties.getGitlab().getAccessToken();
        // ...
    };
}
```

**Time Saved**: 30-60 minutes → 5 minutes

**Knowledge Source**: TASK3_SUMMARY.md, ApplicationProperties.java, ConfigurationBridge.java

---

### 7. `/modernize-dependency`

**Purpose**: Safely update an outdated dependency

**Status**: 📋 Planned (from original skill list)

**What it does**:
- Identifies dependency in pom.xml
- Searches for changelog/migration guide (GitHub releases, website)
- Analyzes breaking changes
- Updates version in pom.xml
- Scans codebase for affected usage patterns
- Suggests code changes if breaking changes detected
- Compiles and runs affected tests
- Creates commit with migration notes

**Example Usage**:
```
/modernize-dependency slf4j
```

**What it does**:
1. Finds `org.slf4j:slf4j-api` in pom.xml (currently 1.8.0-beta4)
2. Fetches latest stable version (2.0.9)
3. Reads migration guide from SLF4J website
4. Identifies breaking changes (none for this upgrade)
5. Updates pom.xml
6. Compiles project
7. Runs tests
8. Creates commit: "chore: upgrade SLF4J 1.8.0-beta4 → 2.0.9 (stable)"

**Time Saved**: 2-4 hours → 10-15 minutes

**Value**: Critical for updating dependencies safely without breaking the application

---

## Integration Skills

### 8. `/add-integration`

**Purpose**: Add new external service integration (GitLab, CircleCI, SonarQube, etc.)

**Status**: 📋 Planned (from original skill list)

**What it does**:
- Prompts for service type:
  - SCM provider (GitHub, GitLab, Bitbucket)
  - CI/CD service (Jenkins, CircleCI, Travis, GitHub Actions)
  - Security service (Snyk, SonarQube, Veracode)
  - Badge service (Shields.io, custom)
- Prompts for service details (base URL, API version, auth method)
- Creates integration components:
  - Command class extending BaseHystrixCommand (or Resilience4j)
  - Domain models with JSON-B annotations
  - Fetch task classes
  - Cache configuration
  - Scheduled fetch service (@Scheduled)
  - Health indicator
  - Configuration properties
  - Link URL generator
  - Tests with mock responses
- Updates repository configuration structure
- Compiles and verifies
- Documents the integration

**Example Usage**:
```
/add-integration gitlab
```

**What gets created** (8-10 files):

1. **GitLabCommand.java** - HTTP client for GitLab API
2. **GitLabRepository.java** - Domain model
3. **GitLabCommit.java** - Domain model
4. **FetchGitLabRepositories.java** - Fetch task
5. **GitLabScheduledService.java** - @Scheduled service
6. **GitLabHealthIndicator.java** - Health check
7. **ApplicationProperties.GitLab** - Configuration
8. **GitLabLinkBuilder.java** - URL generator
9. **GitLabIntegrationTest.java** - Tests
10. Updates to config.json schema

**Time Saved**: 4-6 weeks → 30-60 minutes

**Value**: Dramatically accelerates adding new service integrations

---

### 9. `/add-webhook-handler`

**Purpose**: Add webhook support for new SCM provider

**Status**: 📋 Planned (from original skill list)

**What it does**:
- Prompts for provider name (GitLab, Bitbucket, etc.)
- Prompts for supported events (push, pull request, release)
- Creates webhook handler components:
  - WebhookHandler interface (if not exists)
  - Provider-specific webhook handler implementation
  - Signature verification (HMAC, JWT, etc.)
  - Event payload parsing
  - Event-to-task mapping (update cache, trigger fetch)
  - Controller endpoint (@PostMapping)
  - Tests with sample payloads
- Updates webhook routing configuration
- Adds signature validation configuration
- Compiles and verifies
- Documents webhook setup instructions

**Example Usage**:
```
/add-webhook-handler gitlab
```

**What gets created**:

**GitLabWebhookHandler.java**:
```java
@Component
public class GitLabWebhookHandler implements WebhookHandler {
    @Override
    public String getProvider() {
        return "gitlab";
    }

    @Override
    public boolean verifySignature(String payload, String signature, String secret) {
        // GitLab signature verification (X-Gitlab-Token)
    }

    @Override
    public void handleEvent(String eventType, String payload) {
        switch (eventType) {
            case "Push Hook" -> handlePush(payload);
            case "Merge Request Hook" -> handleMergeRequest(payload);
            case "Release Hook" -> handleRelease(payload);
        }
    }
}
```

**GitLabWebhookController.java**:
```java
@RestController
@RequestMapping("/gitlab/webhook")
public class GitLabWebhookController {
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
        @RequestHeader("X-Gitlab-Event") String event,
        @RequestHeader("X-Gitlab-Token") String token,
        @RequestBody String payload
    ) {
        if (!webhookHandler.verifySignature(payload, token, secret)) {
            return ResponseEntity.status(403).build();
        }
        webhookHandler.handleEvent(event, payload);
        return ResponseEntity.ok().build();
    }
}
```

**Documentation created**: GITLAB_WEBHOOK_SETUP.md

**Time Saved**: 1-2 weeks → 20-30 minutes

**Value**: Enables multi-provider webhook support for real-time updates

---

## Configuration Skills

### 10. `/add-repository-group`

**Purpose**: Add new repository group configuration

**Status**: 📋 Planned (from original skill list)

**What it does**:
- Prompts for group ID (e.g., "whydah", "stingray")
- Prompts for display name (e.g., "Whydah IAM Platform")
- Prompts for description
- Prompts for repository patterns (regex, e.g., "Whydah*", "ConfigService*")
- Prompts for default group repo
- Prompts for external service configurations:
  - Jenkins prefix/suffix patterns
  - Snyk organization/project patterns
  - Custom badge configurations
- Validates regex patterns (compiles them)
- Updates config.json with proper JSON formatting
- Optionally triggers immediate prefetch for new group
- Validates configuration loads correctly

**Example Usage**:
```
/add-repository-group stingray
```

**What gets updated**:

**config.json**:
```json
{
  "groups": [
    {
      "groupId": "stingray",
      "display-name": "Stingray Monitoring",
      "description": "Monitoring and observability tools",
      "defaultGroupRepo": "Stingray-Dashboard",
      "artifactId": ["Stingray*"],
      "jenkins": {
        "prefix": "Stingray-"
      },
      "snyk": {
        "organization": "cantara",
        "projectPrefix": "stingray-"
      }
    }
  ]
}
```

**Validation performed**:
- Regex patterns compile successfully
- Group ID is unique
- Default group repo exists (optional check)
- JSON is valid

**Time Saved**: 15-30 minutes → 2-3 minutes

**Value**: Makes configuration changes trivial, no JSON syntax errors

---

## Skill Implementation Priority

Based on immediate value and Phase 2 completion:

### Tier 1 (Implemented ✅) - **Immediate Value**
1. ✅ `/migrate-controller` - Convert Undertow controllers to Spring MVC (Implemented 2026-01-27)
2. ✅ `/add-health-indicator` - Create custom health indicators (Implemented 2026-01-27)
3. ✅ `/add-scheduled-task` - Create @Scheduled tasks (Implemented 2026-01-27)

### Tier 2 (Implement Next) - **High Value**
4. `/add-repository-group` - Frequent configuration changes
5. `/configure-spring-cache` - New caches for PR, issues, etc.
6. `/modernize-dependency` - Many outdated dependencies

### Tier 3 (Implement Later) - **Strategic Value**
7. `/add-spring-config` - New configuration sections as needed
8. `/add-integration` - Future multi-provider support
9. `/add-webhook-handler` - Multi-provider webhooks
10. `/migrate-to-spring-boot` - General migration orchestrator

---

## Implementation Notes

### Skill Development Guidelines

Each skill should:
1. **Prompt for required information** - Use AskUserQuestion for user input
2. **Validate inputs** - Check patterns compile, files exist, etc.
3. **Generate code** - Create or modify files with proper formatting
4. **Compile and verify** - Run mvn compile to ensure it works
5. **Create tests** - Generate appropriate test cases
6. **Document** - Add comments, README updates
7. **Report results** - Show what was created and where

### Skill Naming Convention

- Use kebab-case: `/add-health-indicator`
- Start with verb: `/add-`, `/migrate-`, `/configure-`
- Be specific: Not `/add-component`, but `/add-health-indicator`

### Skill Testing

Before marking a skill "implemented":
1. Test with real use case from codebase
2. Verify compilation succeeds
3. Verify tests pass
4. Verify documentation is clear
5. Verify time savings vs manual approach

---

## Skills Knowledge Base

### Knowledge Sources by Skill

| Skill | Knowledge Source Files |
|-------|------------------------|
| `/migrate-to-spring-boot` | PHASE2_PROGRESS.md, all TASK*_SUMMARY.md files |
| `/migrate-controller` | TASK5_SUMMARY.md, PingRestController.java, HealthRestController.java, DashboardWebController.java |
| `/add-health-indicator` | TASK7_SUMMARY.md, GitHubHealthIndicator.java, CacheHealthIndicator.java, ExecutorHealthIndicator.java |
| `/add-scheduled-task` | TASK6_SUMMARY.md, JenkinsStatusScheduledService.java, SnykStatusScheduledService.java |
| `/configure-spring-cache` | TASK4_SUMMARY.md, CacheConfiguration.java, CacheMetricsConfiguration.java |
| `/add-spring-config` | TASK3_SUMMARY.md, ApplicationProperties.java, ConfigurationBridge.java |
| `/add-integration` | commands/*.java, fetch/*.java, domain/jenkins/*.java, domain/snyk/*.java |
| `/add-webhook-handler` | controller/GithubWebhookController.java |
| `/add-repository-group` | domain/config/*.java, config.json |

---

## Next Steps

1. ✅ Complete Task 8 (Remove Undertow, finalize Spring Boot) - DONE
2. ✅ Implement Tier 1 skills (`/migrate-controller`, `/add-health-indicator`, `/add-scheduled-task`) - DONE
3. Test skills with real migration scenarios (as needs arise)
4. ✅ Document skill usage in CLAUDE.md - DONE
5. Implement Tier 2 skills as needs arise
6. Gather feedback on skill effectiveness

---

**Status**: ✅ Tier 1 skills implemented and ready to use
**Last Updated**: 2026-01-27
**Next Action**: Use skills for future development, implement Tier 2 skills as needed
