# Learnings & Gotchas - Source Code Portal

**Last Updated**: 2026-01-27
**Applies to**: All phases of modernization

This document consolidates all critical learnings, gotchas, and best practices discovered during the Source Code Portal modernization project.

---

## 📚 Table of Contents

- [Phase 2: Spring Boot Migration](#phase-2-spring-boot-migration)
- [Phase 1: Java 21 & JUnit 5](#phase-1-java-21--junit-5)
- [General Gotchas](#general-gotchas)
- [Best Practices](#best-practices)

---

## Phase 2: Spring Boot Migration

**Context**: Migration from Undertow to Spring Boot 3.2.2 (2026-01)
**Duration**: 7 hours (8 tasks)
**Source**: [LEARNINGS_PHASE2.md](LEARNINGS_PHASE2.md) (full details)

### Top 5 Critical Learnings

#### 1. JSON-B vs Jackson Conflict ⚠️

**Problem**: Spring Boot 3.2.2 auto-configures Jakarta JSON-B, but project uses legacy javax.json.bind.

**Error**:
```
jakarta.json.spi.JsonProvider: org.glassfish.json.JsonProviderImpl not a subtype
```

**Root Cause**: Spring Boot 3.x uses Jakarta EE 10 (jakarta.*), but project has old javax.json.bind (1.0).

**Solution**:
```java
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration.class
})
```

**Lesson**: Always check for Jakarta vs javax namespace conflicts when migrating to Spring Boot 3.x.

---

#### 2. DynamicConfiguration Bridge Pattern ✅

**Challenge**: Legacy code uses DynamicConfiguration interface, Spring Boot uses @ConfigurationProperties.

**Solution**: Two-layer bridge pattern
```java
// Layer 1: Bridge to Spring Boot properties
@Component
public class ConfigurationBridge {
    private final ApplicationProperties properties;

    public String evaluateToString(String key) {
        return switch (key) {
            case "github.organization" -> properties.getGithub().getOrganization();
            // ... 40+ mappings
        };
    }
}

// Layer 2: Adapter for legacy code
public class DynamicConfigurationAdapter implements DynamicConfiguration {
    private final ConfigurationBridge bridge;

    @Override
    public String evaluateToString(String key) {
        return bridge.evaluateToString(key);
    }
}
```

**Lesson**: Use adapter pattern to maintain compatibility during migration. Don't break existing code.

---

#### 3. Profile Exclusion for Tests 🧪

**Challenge**: Spring Boot components should not run during tests.

**Solution**: Use `@Profile("!test")` annotation
```java
@Component
@Profile("!test")  // Excludes from test profile
public class SpringBootInitializer implements ApplicationRunner {
    // Won't run during tests
}
```

**Why**:
- Tests shouldn't depend on external services (GitHub API)
- Tests should be fast (no initialization overhead)
- Tests should be isolated (no shared state)

**Lesson**: Always exclude Spring Boot initialization components from tests.

---

#### 4. Code Reduction with Spring MVC 📉

**Metric**: 70-80% code reduction when migrating controllers.

**Before (Undertow - 180 lines)**:
```java
public class HealthController implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("status", "OK");
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json);
    }
}
```

**After (Spring MVC - 40 lines)**:
```java
@RestController
@RequestMapping("/health")
public class HealthRestController {
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
```

**What Spring MVC Handles**:
- ✅ JSON serialization (automatic)
- ✅ Content-Type headers (automatic)
- ✅ Status codes (via ResponseEntity)
- ✅ Exception handling (@ExceptionHandler)
- ✅ Request binding (@RequestParam, @PathVariable)

**Lesson**: Modern frameworks eliminate boilerplate. Embrace them.

---

#### 5. Maven Plugin Compatibility ⚠️

**Problem**: Maven JAR packaging fails with NoSuchMethodError.

**Error**:
```
NoSuchMethodError: 'org.codehaus.plexus.archiver.util.DefaultFileSet...'
```

**Root Cause**: Maven plugin version incompatibility.

**Workaround**: Use Spring Boot Maven plugin instead
```bash
# Don't use this (fails)
mvn package

# Use this instead (works)
mvn spring-boot:run
```

**Lesson**: Maven plugins can have transitive dependency conflicts. When packaging fails, use Spring Boot's plugin directly.

---

### Additional Phase 2 Learnings

#### Health Indicator Status Levels
- **UP**: Healthy and operational
- **DOWN**: Failed or unavailable
- **DEGRADED**: Operational but showing warning signs (custom status)

**Example**: GitHub rate limit < 10%: DEGRADED (still works, but warn)

#### Caffeine Cache Performance
- **2-3x faster** than JSR-107 JCache RI
- Integrates seamlessly with Spring Boot Actuator metrics
- Prometheus endpoints: `/actuator/metrics/cache.size`

#### ApplicationRunner Initialization Order
- Runs after Spring context initialized
- Runs before @Scheduled tasks start
- Use `@Order(1)` for priority

**For complete Phase 2 learnings**: See [LEARNINGS_PHASE2.md](LEARNINGS_PHASE2.md)

---

## Phase 1: Java 21 & JUnit 5

**Context**: Modernization to Java 21, JUnit 5, Resilience4j (2025-12)
**Source**: [GOTCHAS_AND_LEARNINGS.md](docs/history/migration-notes/gotchas.md)

### Critical Gotchas

#### 1. Commonmark Group ID Change ⚠️

**Symptom**: `Could not find artifact com.atlassian.commonmark:commonmark:jar:0.22.0`

**Problem**: Commonmark moved from Atlassian to its own organization.

**Fix**:
```xml
<!-- Wrong -->
<groupId>com.atlassian.commonmark</groupId>

<!-- Correct -->
<groupId>org.commonmark</groupId>
```

---

#### 2. Selenium 4 API Changes 🔄

**Symptom**: `cannot find symbol: method findElementById(String)`

**Problem**: Selenium 4 removed convenience methods.

**Fix**:
```java
// Wrong (Selenium 3)
driver.findElementById("login_field");

// Correct (Selenium 4)
driver.findElement(By.id("login_field"));
```

**Pattern**: All `findElementBy*()` → `findElement(By.*())`

---

#### 3. TestNG → JUnit 5 Assertion Parameter Order ⚠️

**Critical**: **TestNG and JUnit 5 have REVERSED parameter order!**

```java
// TestNG (actual, expected)
Assert.assertEquals(actualValue, expectedValue);

// JUnit 5 (expected, actual) - REVERSED!
Assertions.assertEquals(expectedValue, actualValue);
```

**This is a logic error, not a compilation error!** Tests will compile but may assert incorrectly.

---

#### 4. JSoup normalise() Removal

**Symptom**: `cannot find symbol: method normalise()`

**Problem**: JSoup 1.18+ removed `normalise()` method (now automatic).

**Fix**:
```java
// Wrong (JSoup 1.13)
Element body = doc.normalise().body();

// Correct (JSoup 1.18)
Element body = doc.body();  // normalise() happens automatically
```

---

#### 5. Hystrix → Resilience4j Migration

**Pattern Change**: Different API for circuit breakers.

**Before (Hystrix)**:
```java
public class MyCommand extends HystrixCommand<String> {
    protected String run() {
        return callExternalService();
    }
}
```

**After (Resilience4j)**:
```java
@CircuitBreaker(name = "myService")
public String callExternalService() {
    return externalService.call();
}
```

**Key Differences**:
- Hystrix: Inheritance-based
- Resilience4j: Annotation-based (Spring AOP)

---

### Java 21 Specific Learnings

#### Virtual Threads
- Enable in application.yml: `spring.threads.virtual.enabled=true`
- Massive concurrency improvement (thousands of threads)
- No code changes required

#### Pattern Matching for Switch
```java
// Old
if (obj instanceof String) {
    String s = (String) obj;
    return s.length();
}

// New (Java 21)
return switch(obj) {
    case String s -> s.length();
    case Integer i -> i;
    default -> 0;
};
```

---

## General Gotchas

### Build & Dependency Management

#### Maven Dependency Conflicts
**Problem**: Transitive dependencies can conflict.

**Solution**:
```bash
# View dependency tree
mvn dependency:tree

# Find conflicts
mvn dependency:tree | grep -A 5 "conflict"
```

#### Node/NPM Version Issues
**Problem**: Old package.json may specify outdated Node version.

**Fix**:
```json
{
  "engines": {
    "node": "20.x",  // Update from 10.x
    "npm": "10.x"
  }
}
```

---

### Configuration & Properties

#### Environment Variables
**Pattern**: Use `${VAR:default}` syntax in Spring Boot properties.

```yaml
github:
  access-token: ${SCP_GITHUB_ACCESS_TOKEN:}  # Empty default
  organization: ${SCP_GITHUB_ORGANIZATION:Cantara}  # With default
```

#### Profile-Specific Configuration
```yaml
# application.yml (common)
spring:
  application:
    name: source-code-portal

---
# application-dev.yml (development)
logging:
  level:
    no.cantara: DEBUG

---
# application-prod.yml (production)
logging:
  level:
    no.cantara: INFO
```

---

### Testing Best Practices

#### Don't Mock What You Don't Own
```java
// Bad - mocking Spring framework
@Mock
private ApplicationContext context;

// Good - mock your own interfaces
@Mock
private RepositoryService repositoryService;
```

#### Use @SpringBootTest Sparingly
```java
// Expensive - starts full Spring context
@SpringBootTest
class FullIntegrationTest { }

// Better - lightweight unit test
class ServiceTest {
    private ServiceUnderTest service;

    @BeforeEach
    void setUp() {
        service = new ServiceUnderTest(mockDependency);
    }
}
```

---

## Best Practices

### Development Workflow

#### 1. Incremental Changes
- ✅ Small commits (one logical change per commit)
- ✅ Compile after each change
- ✅ Run tests frequently
- ❌ Don't accumulate broken code

#### 2. Documentation as You Go
- ✅ Document gotchas immediately
- ✅ Update README with changes
- ✅ Add code comments for non-obvious logic
- ❌ Don't defer documentation

#### 3. Dual-Mode During Migration
- ✅ Keep old code working
- ✅ Add new code alongside
- ✅ Switch via configuration
- ❌ Don't break existing functionality

---

### Code Quality

#### Dependency Injection
```java
// Bad - field injection
@Autowired
private MyService service;

// Good - constructor injection
public class MyController {
    private final MyService service;

    public MyController(MyService service) {
        this.service = service;
    }
}
```

**Why**: Constructor injection is:
- Testable (can pass mock in constructor)
- Immutable (final fields)
- Explicit (shows dependencies clearly)

#### Configuration Properties
```java
// Bad - @Value everywhere
@Value("${github.organization}")
private String organization;

// Good - @ConfigurationProperties
@ConfigurationProperties(prefix = "github")
public class GitHubProperties {
    private String organization;
    // Getters/setters
}
```

**Why**: @ConfigurationProperties is:
- Type-safe (validation)
- Grouped logically
- Easier to test
- Better IDE support

---

### Performance

#### Caching Strategy
```java
// Bad - cache everything forever
@Cacheable("bigCache")

// Good - cache with TTL
@Cacheable(value = "repositories", unless = "#result == null")
@CacheEvict(value = "repositories", allEntries = true, condition = "#force")
```

**Caffeine Configuration**:
```java
Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(30, TimeUnit.MINUTES)
    .recordStats()  // For metrics
```

#### Virtual Threads
```yaml
# Enable for I/O-bound workloads
spring:
  threads:
    virtual:
      enabled: true
```

**When to use**:
- ✅ I/O-bound tasks (HTTP calls, database)
- ✅ High concurrency needs
- ❌ CPU-bound tasks (better with platform threads)

---

### Security

#### Secrets Management
```bash
# Bad - hardcoded secrets
github.access.token=ghp_abc123

# Good - environment variables
export SCP_GITHUB_ACCESS_TOKEN=ghp_abc123

# Better - secrets management
# Use AWS Secrets Manager, HashiCorp Vault, etc.
```

#### Rate Limiting
```java
// Monitor GitHub rate limit
@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
public void checkRateLimit() {
    RateLimit rateLimit = github.getRateLimit();
    if (rateLimit.getRemaining() < threshold) {
        logger.warn("GitHub rate limit low: {}", rateLimit);
    }
}
```

---

## Time-Saving Tips

### Maven

```bash
# Skip tests for faster builds
mvn clean install -DskipTests

# Compile only (no tests, no packaging)
mvn clean compile

# Run single test
mvn test -Dtest=MyTest#myMethod

# Show dependency tree
mvn dependency:tree

# Update versions
mvn versions:display-dependency-updates
```

### Git

```bash
# Commit with co-author (for pair programming)
git commit -m "feat: add feature

Co-authored-by: Claude <noreply@anthropic.com>"

# Interactive rebase (clean up commits)
git rebase -i HEAD~3

# Search commits
git log --grep="spring boot"
```

### IntelliJ IDEA

- `Ctrl+Shift+O` - Optimize imports
- `Ctrl+Alt+L` - Reformat code
- `Ctrl+Shift+T` - Navigate to test
- `Ctrl+Shift+F` - Find in path
- `Ctrl+Shift+R` - Replace in path

---

## Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Framework Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Caffeine Cache](https://github.com/ben-manes/caffeine/wiki)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

### Tools
- [Spring Initializr](https://start.spring.io/) - Bootstrap Spring Boot projects
- [Maven Repository](https://mvnrepository.com/) - Find dependencies
- [OpenRewrite](https://docs.openrewrite.org/) - Automated code refactoring

---

## Contributing to This Document

When you encounter a new gotcha or learning:

1. **Capture immediately** - Don't wait, document while fresh
2. **Include error messages** - Exact error text helps searching
3. **Show before/after code** - Examples are invaluable
4. **Explain why** - Not just what, but why it matters
5. **Add to appropriate section** - Keep organized

**Format**:
```markdown
#### Title of Gotcha ⚠️

**Symptom**: Error message or observable behavior

**Problem**: Why it happens

**Fix**: How to resolve it

**Lesson**: What to remember
```

---

**Last Updated**: 2026-01-27
**Contributors**: Cantara Team, Claude Code
**Feedback**: Open an issue or pull request
