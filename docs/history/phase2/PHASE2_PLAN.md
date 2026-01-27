# Phase 2: Spring Boot Migration - Implementation Plan

**Status**: 🚧 IN PROGRESS
**Started**: 2026-01-27
**Estimated Duration**: 4-6 weeks
**Approach**: Incremental migration with dual-mode support

---

## 🎯 Objectives

Transform Source Code Portal from custom Undertow setup to Spring Boot framework:
- ✅ Maintain all existing functionality
- ✅ Keep system running at each step
- ✅ Improve maintainability and developer experience
- ✅ Add observability (Actuator)
- ✅ Simplify configuration and deployment

---

## 📋 Migration Strategy

### Incremental Approach

We'll run **both systems in parallel** during migration:
1. Add Spring Boot alongside Undertow
2. Migrate components one by one
3. Test each component thoroughly
4. Remove Undertow only when everything works

**Why**: Zero downtime, easy rollback, lower risk

---

## 🗺️ Phase 2 Roadmap

### Week 1: Foundation (Tasks 1-2)
**Goal**: Get Spring Boot running alongside Undertow

#### Task 1: Add Spring Boot Dependencies ⏳
**What**: Add Spring Boot BOM and core dependencies
**Impact**: No code changes, just dependency additions
**Risk**: Low - nothing breaks, just adds JARs

**Dependencies to add**:
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.2</version>
    <relativePath/>
</parent>

<!-- Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<!-- Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Caffeine Cache -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

**Verification**:
```bash
mvn clean compile
# Should succeed with no errors
```

---

#### Task 2: Create Spring Boot Application Class ⏳
**What**: Create `@SpringBootApplication` that can coexist with Server.java

**New file**: `SpringBootServer.java`
```java
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SpringBootServer {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootServer.class, args);
    }
}
```

**Dual-mode support**: Add flag to choose which to run
```properties
# application.properties
server.mode=undertow  # or 'spring-boot'
```

**Verification**:
```bash
# Run Spring Boot mode
mvn spring-boot:run
# Should start (may not serve pages yet, that's OK)
```

---

### Week 2: Configuration & Cache (Tasks 3-4)
**Goal**: Migrate configuration and caching to Spring

#### Task 3: Migrate Configuration ⏳
**What**: Convert DynamicConfiguration to Spring @ConfigurationProperties

**Create**: `ApplicationConfig.java`
```java
@Configuration
@ConfigurationProperties(prefix = "scp")
public class ApplicationConfig {
    private String githubOrganization;
    private GitHub github = new GitHub();
    private Jenkins jenkins = new Jenkins();

    // Getters/setters

    @Data
    public static class GitHub {
        private String clientId;
        private String clientSecret;
        private String accessToken;
    }
}
```

**Convert**: `application-defaults.properties` → `application.yml`
```yaml
scp:
  github:
    organization: Cantara
    clientId: ${GITHUB_CLIENT_ID}
    clientSecret: ${GITHUB_CLIENT_SECRET}
    accessToken: ${GITHUB_ACCESS_TOKEN}
```

**Verification**:
```bash
mvn test -Dtest=*Config*
# Configuration loads correctly
```

---

#### Task 4: Convert CacheStore to Spring Cache ⏳
**What**: Replace JSR-107 JCache with Spring Cache + Caffeine

**Before** (CacheStore.java):
```java
public class CacheStore {
    private Cache<CacheKey, ScmRepository> repositoryCache;

    public ScmRepository getRepository(CacheKey key) {
        return repositoryCache.get(key);
    }
}
```

**After** (Spring Cache):
```java
@Service
public class RepositoryService {
    @Cacheable(value = "repositories", key = "#key")
    public ScmRepository getRepository(CacheKey key) {
        return fetchFromGitHub(key);
    }
}
```

**Cache Config**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "repositories", "commits", "contents", "buildStatus"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30)));
        return cacheManager;
    }
}
```

**Verification**:
```bash
mvn test -Dtest=*Cache*
# Cache works with Spring
```

---

### Week 3: Controllers (Task 5)
**Goal**: Migrate HTTP controllers to Spring MVC

#### Task 5: Migrate Controllers ⏳
**What**: Convert Undertow handlers to Spring @Controller/@RestController

**Before** (WebController.java):
```java
public class WebController {
    public void handleRequest(HttpServerExchange exchange) {
        String path = exchange.getRequestPath();
        // Manual routing
    }
}
```

**After** (Spring MVC):
```java
@Controller
public class DashboardController {
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("repos", repositoryService.getAll());
        return "index";  // Thymeleaf template
    }

    @GetMapping("/group/{groupId}")
    public String groupView(@PathVariable String groupId, Model model) {
        model.addAttribute("group", groupService.getGroup(groupId));
        return "group/card";
    }
}

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        return ResponseEntity.ok(healthService.getStatus());
    }
}
```

**Thymeleaf integration**:
```java
@Configuration
public class ThymeleafConfig {
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.addDialect(new LayoutDialect());
        return engine;
    }
}
```

**Verification**:
```bash
mvn spring-boot:run
curl http://localhost:9090/dashboard
# Should return HTML
```

---

### Week 4: Scheduling & Actuator (Tasks 6-7)
**Goal**: Replace custom executors and add observability

#### Task 6: Replace Custom Executors ⏳
**What**: Convert ScheduledExecutorService to @Scheduled

**Before** (ScheduledFetchData.java):
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
scheduler.scheduleAtFixedRate(
    () -> fetchRepositories(),
    0, 30, TimeUnit.MINUTES
);
```

**After** (Spring @Scheduled):
```java
@Service
public class ScheduledFetchService {
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    public void fetchRepositories() {
        // Same logic
    }

    @Scheduled(cron = "0 0 * * * *")  // Every hour
    public void refreshCache() {
        // Cache refresh
    }
}
```

**Configuration**:
```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("scp-scheduled-");
        scheduler.setVirtualThreads(true);  // Java 21!
        return scheduler;
    }
}
```

**Verification**:
```bash
mvn spring-boot:run
# Check logs for scheduled task execution
```

---

#### Task 7: Add Spring Boot Actuator ⏳
**What**: Add health checks, metrics, and monitoring

**Configuration**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

**Custom Health Indicators**:
```java
@Component
public class GitHubHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            RateLimit rateLimit = githubClient.getRateLimit();
            return Health.up()
                .withDetail("rateLimit", rateLimit.getRemaining())
                .withDetail("resetTime", rateLimit.getReset())
                .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}

@Component
public class CacheHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        long cacheSize = cacheManager.getCacheNames().stream()
            .mapToLong(name -> getCacheSize(name))
            .sum();
        return Health.up()
            .withDetail("cacheSize", cacheSize)
            .withDetail("caches", cacheManager.getCacheNames())
            .build();
    }
}
```

**Verification**:
```bash
curl http://localhost:9090/actuator/health
# Should return health status with details

curl http://localhost:9090/actuator/metrics
# Should return available metrics
```

---

### Week 5-6: Finalization (Task 8)
**Goal**: Complete migration and remove Undertow

#### Task 8: Remove Undertow and Finalize ⏳
**What**: Remove old Undertow code and dependencies

**Steps**:
1. Verify all functionality works with Spring Boot
2. Remove Undertow dependencies from pom.xml
3. Delete Server.java and Undertow controllers
4. Update documentation
5. Final testing

**Remove**:
```xml
<!-- Remove these -->
<dependency>
    <groupId>io.undertow</groupId>
    <artifactId>undertow-core</artifactId>
</dependency>
```

**Verification**:
```bash
# Full build
mvn clean install

# Run application
java -jar target/source-code-portal-*.jar

# Test all endpoints
./test-all-endpoints.sh

# Run full test suite
mvn test
```

---

## 🎯 Success Criteria

### Functional Requirements
- [ ] All endpoints work identically
- [ ] Same URL structure maintained
- [ ] Configuration loading works
- [ ] Cache behavior unchanged
- [ ] Scheduled tasks run correctly
- [ ] GitHub webhooks work
- [ ] Documentation rendering works
- [ ] Static assets served correctly

### Non-Functional Requirements
- [ ] Startup time < 15 seconds
- [ ] Response time same or better
- [ ] Memory usage reasonable
- [ ] Health checks working
- [ ] Metrics exposed
- [ ] Easy to deploy
- [ ] Tests pass

### Developer Experience
- [ ] Simpler configuration
- [ ] Better error messages
- [ ] Hot reload works
- [ ] IDE integration good
- [ ] Documentation updated

---

## 📊 Migration Progress Tracking

| Component | Status | Notes |
|-----------|--------|-------|
| Dependencies | ⏳ Not Started | Task 1 |
| Spring Boot App | ⏳ Not Started | Task 2 |
| Configuration | ⏳ Not Started | Task 3 |
| Cache | ⏳ Not Started | Task 4 |
| Controllers | ⏳ Not Started | Task 5 |
| Scheduling | ⏳ Not Started | Task 6 |
| Actuator | ⏳ Not Started | Task 7 |
| Finalization | ⏳ Not Started | Task 8 |

Legend: ⏳ Not Started | 🚧 In Progress | ✅ Complete | ❌ Blocked

---

## 🚨 Risk Mitigation

### Risk 1: Breaking Changes
**Mitigation**: Dual-mode operation, feature flags, gradual rollout

### Risk 2: Performance Regression
**Mitigation**: Benchmark each component, keep monitoring

### Risk 3: Configuration Issues
**Mitigation**: Support both old and new config during transition

### Risk 4: Testing Gaps
**Mitigation**: Add integration tests for each migrated component

---

## 🔄 Rollback Plan

At any point, can rollback to Undertow:
```bash
# Revert to Undertow mode
java -jar app.jar --server.mode=undertow

# Or rollback code
git revert <commit-range>
mvn clean install
```

---

## 📚 Documentation Updates Needed

- [ ] Update CLAUDE.md with Spring Boot info
- [ ] Create Spring Boot gotchas doc
- [ ] Update README with new startup
- [ ] Document Actuator endpoints
- [ ] Update Docker configuration
- [ ] Create Spring Boot skill

---

## 🎓 Learning Opportunities

This phase will create new skills:
- `/migrate-to-spring-boot` - Full migration guide
- `/add-actuator-health` - Custom health indicators
- `/spring-cache-setup` - Cache configuration
- `/spring-mvc-controller` - Controller patterns

---

## 📅 Timeline

**Week 1**: Foundation (Tasks 1-2)
**Week 2**: Config & Cache (Tasks 3-4)
**Week 3**: Controllers (Task 5)
**Week 4**: Scheduling & Actuator (Tasks 6-7)
**Week 5-6**: Finalization (Task 8)

**Total**: 4-6 weeks

---

## 🎯 Next Steps

Starting with Task 1: Add Spring Boot dependencies to pom.xml

---

*This is a living document. Update as tasks complete and new insights emerge.*
