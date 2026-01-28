# Week 2-3: Complete Spring Boot Migration

**Status**: 📋 Planning Complete - Ready to Execute
**Duration**: 8-10 hours (estimated)
**Goal**: Migrate all remaining Undertow controllers to Spring MVC

---

## Current State

### ✅ Already Migrated (Spring MVC)
- `PingRestController` - Simple health check (`/actuator/ping`)
- `HealthRestController` - Health endpoints (`/actuator/health`)
- `DashboardWebController` - Main dashboard (`/dashboard`)

### 🔄 To Be Migrated (Undertow)

**Controllers (8 files)**:
1. `ApplicationController` - Main routing controller (117 lines)
2. `GithubWebhookController` - GitHub webhooks (179 lines)
3. `HealthController` - Legacy health endpoints (180 lines)
4. `ImageResourceController` - Image/favicon serving
5. `EchoController` - Echo endpoint
6. `PingController` - Legacy ping endpoint
7. `StaticContentController` - CSS/JS serving
8. `CORSController` - CORS handling
9. `WebController` - Web page routing (72 lines)

**Handlers (6 files)**:
1. `DashboardHandler` - Dashboard page logic
2. `CardHandler` - Group view page (60 lines)
3. `CommitsHandler` - Commit history page (130 lines)
4. `ContentsHandler` - Repository contents page (50 lines)
5. `CantaraWikiHandler` - Wiki page
6. `BadgeResourceHandler` - Badge serving

**Total**: 14 files to migrate

---

## Migration Strategy

### Phase 1: Resource Serving (Low Risk, 2 hours)
**Goal**: Migrate static resource controllers to Spring Boot conventions

1. **StaticContentController** → Spring Boot Static Resources
   - Move CSS/JS to `src/main/resources/static/`
   - Configure Spring Boot static resource handling
   - Remove custom controller

2. **ImageResourceController** → Spring Boot Static Resources
   - Move images to `src/main/resources/static/img/`
   - Configure favicon handling
   - Remove custom controller

3. **CORSController** → Spring Boot CORS Configuration
   - Create `@Configuration` class with CORS setup
   - Use `WebMvcConfigurer.addCorsMappings()`
   - Remove custom CORS handling

**Outcome**: Simplified resource serving, ~3 files deleted

---

### Phase 2: Simple Endpoints (Low Risk, 1 hour)

4. **EchoController** → `EchoRestController`
   - Simple echo endpoint for testing
   - Migrate to `@RestController`
   - ~20 lines

5. **PingController (legacy)** → Consolidate with `PingRestController`
   - Already have Spring Boot version
   - Just remove old Undertow version
   - Update tests if needed

**Outcome**: Cleaner endpoint structure, ~2 files deleted/merged

---

### Phase 3: Webhook Integration (Medium Risk, 2 hours)

6. **GithubWebhookController** → `GitHubWebhookRestController`
   - Complex signature verification logic
   - Handles push, ping, create, release events
   - Queue tasks to ExecutorService
   - 179 lines → ~80 lines (Spring MVC simplifies HTTP handling)

**Complexity**:
- Signature verification logic stays the same
- Use `@PostMapping` with `@RequestBody` and `@RequestHeader`
- Spring Boot handles JSON deserialization automatically
- Keep task queueing logic identical

**Outcome**: Modern webhook endpoint with cleaner code

---

### Phase 4: Legacy Health Endpoints (Low-Medium Risk, 1.5 hours)

7. **HealthController (legacy)** → Consolidate with `HealthRestController`
   - Already have modern `/actuator/health` endpoints
   - Legacy `/health` endpoints for backward compatibility
   - Add routes to existing `HealthRestController`
   - Keep JSON response format identical

**Outcome**: Unified health endpoint controller

---

### Phase 5: Web Page Handlers (Medium Risk, 3-4 hours)

**Goal**: Migrate WebController + all handlers to Spring MVC controllers

8. **WebController** → Remove (routing now via Spring MVC)

9. **DashboardHandler** → Merge with existing `DashboardWebController`
   - Already have Spring Boot version
   - Verify logic is identical
   - Remove old handler

10. **CardHandler** → `GroupWebController`
    - Group view page (`/group/{groupId}`)
    - Uses Thymeleaf to render `group/card.html`
    - 60 lines → ~30 lines

11. **CommitsHandler** → `CommitsWebController`
    - Commit history page (`/commits/*`)
    - Complex logic for filtering commits
    - 130 lines → ~60 lines

12. **ContentsHandler** → `ContentsWebController`
    - Repository contents page (`/contents/{org}/{repo}/{branch}`)
    - Renders README/documentation
    - 50 lines → ~25 lines

13. **CantaraWikiHandler** → `WikiWebController`
    - Wiki page (`/wiki`)
    - Simple handler
    - ~40 lines → ~20 lines

14. **BadgeResourceHandler** → `BadgeResourceController`
    - Badge serving (`/badge/*`)
    - Returns images/JSON
    - ~80 lines → ~40 lines

**Outcome**: Modern Spring MVC web controllers, cleaner routing

---

### Phase 6: Main Routing (Low Risk, 1 hour)

15. **ApplicationController** → Remove entirely
    - Spring Boot handles routing via annotations
    - No need for manual dispatcher
    - All routing now via `@RequestMapping` annotations

**Outcome**: Spring Boot convention-based routing, simplified architecture

---

## Migration Order (Priority)

### Week 2 (4-5 hours)
**Priority 1: Quick Wins**
1. StaticContentController → Spring Boot static resources (30 min)
2. ImageResourceController → Spring Boot static resources (30 min)
3. EchoController → EchoRestController (20 min)
4. PingController → Remove (already have Spring version) (10 min)
5. CORSController → Spring Boot CORS config (30 min)
6. HealthController → Merge with HealthRestController (1 hour)
7. GithubWebhookController → GitHubWebhookRestController (2 hours)

**Checkpoints**: After each migration, compile and verify endpoint works

---

### Week 3 (4-5 hours)
**Priority 2: Web Pages**
8. CardHandler → GroupWebController (1 hour)
9. CommitsHandler → CommitsWebController (1.5 hours)
10. ContentsHandler → ContentsWebController (45 min)
11. CantaraWikiHandler → WikiWebController (30 min)
12. BadgeResourceHandler → BadgeResourceController (1 hour)
13. DashboardHandler → Merge with DashboardWebController (30 min)
14. WebController → Remove (routing via Spring MVC) (15 min)
15. ApplicationController → Remove (Spring Boot routing) (15 min)

**Checkpoints**: After each migration, test page renders correctly

---

## Testing Strategy

### Unit Tests
- Each new controller gets JUnit 5 tests
- Use `@WebMvcTest` for Spring MVC controllers
- Mock CacheStore and other dependencies
- Verify response status, content type, body

### Integration Tests
- `@SpringBootTest` for full application context
- Test actual HTTP requests to endpoints
- Verify Thymeleaf rendering works
- Test webhook signature verification

### Manual Testing
- Start application: `mvn spring-boot:run`
- Visit each migrated endpoint
- Verify pages render identically
- Test webhook with sample payload

---

## Success Criteria

### Technical
- ✅ All Undertow controllers removed
- ✅ All endpoints work via Spring MVC
- ✅ Tests pass (compile + JUnit 5)
- ✅ No breaking changes (URLs stay the same)
- ✅ Performance maintained or improved

### Documentation
- ✅ Update CLAUDE.md with new controller structure
- ✅ Update VERIFICATION_GUIDE.md with new endpoints
- ✅ Document any URL changes (if any)
- ✅ Add migration notes to CHANGELOG.md

### Code Quality
- ✅ 50-70% code reduction (Spring MVC vs Undertow)
- ✅ Consistent naming: `*RestController` (APIs), `*WebController` (pages)
- ✅ Constructor injection for dependencies
- ✅ Proper error handling with `@ExceptionHandler`

---

## Code Patterns

### Undertow → Spring MVC Translation

**Before (Undertow HttpHandler)**:
```java
public class MyController implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        String param = exchange.getQueryParameters().get("key").getFirst();

        JsonObject response = Json.createObjectBuilder()
            .add("result", "value")
            .build();

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(response.toString());
    }
}
```

**After (Spring MVC)**:
```java
@RestController
@RequestMapping("/my-endpoint")
public class MyRestController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> handle(@RequestParam String key) {
        return ResponseEntity.ok(Map.of("result", "value"));
    }
}
```

**Code Reduction**: ~50-70%

**What Spring Handles Automatically**:
- ✅ Thread dispatch (handled by servlet container)
- ✅ Query parameter extraction (`@RequestParam`)
- ✅ JSON serialization (Jackson auto-configured)
- ✅ Content-Type headers (automatic)
- ✅ Status codes (via `ResponseEntity`)

---

### Web Page Controller Pattern

**Before (Undertow Handler)**:
```java
public class MyHandler implements WebHandler {
    @Override
    public boolean handleRequest(DynamicConfiguration config,
                                  CacheStore cache,
                                  ResourceContext resourceContext,
                                  WebContext webContext,
                                  HttpServerExchange exchange) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("data", getData(cache));

        return ThymeleafViewEngineProcessor.processView(
            exchange, cache, webContext.asTemplateResource(), variables
        );
    }
}
```

**After (Spring MVC)**:
```java
@Controller
@RequestMapping("/my-page")
public class MyWebController {

    private final CacheStore cacheStore;

    public MyWebController(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @GetMapping
    public String showPage(Model model) {
        model.addAttribute("data", getData(cacheStore));
        return "my-template";  // resolves to templates/my-template.html
    }

    private Object getData(CacheStore cache) {
        // business logic
    }
}
```

**Code Reduction**: ~60-70%

**What Spring Handles Automatically**:
- ✅ Template resolution (Thymeleaf auto-configured)
- ✅ Model injection (Spring `Model`)
- ✅ View rendering (returns template name)
- ✅ Dependency injection (constructor injection)

---

## Risk Mitigation

### Risk 1: Breaking URL Changes
**Mitigation**: Keep all URLs identical to maintain backward compatibility
- `/dashboard` stays `/dashboard`
- `/group/{groupId}` stays `/group/{groupId}`
- `/health` stays `/health` (legacy endpoints)
- `/actuator/*` is new (Spring Boot Actuator)

### Risk 2: Thymeleaf Template Rendering Issues
**Mitigation**:
- Test each page after migration
- Verify template variables match
- Keep template files unchanged (only controller logic changes)

### Risk 3: Webhook Signature Verification
**Mitigation**:
- Keep signature verification logic identical (copy/paste)
- Add comprehensive tests with sample payloads
- Test with real GitHub webhook events (use ngrok)

### Risk 4: Performance Degradation
**Mitigation**:
- Spring Boot has efficient request handling
- Virtual threads (Java 21) improve concurrency
- Benchmark before/after if concerned

---

## Tools & Skills to Create

### `/migrate-controller` Skill
**Purpose**: Automate controller migration from Undertow to Spring MVC

**What it does**:
1. Prompts for controller name
2. Analyzes Undertow controller code
3. Generates Spring MVC controller
4. Creates JUnit 5 tests
5. Updates documentation

**Time Saved**: 30-45 minutes per controller

### `/test-endpoint` Skill
**Purpose**: Test migrated endpoint works correctly

**What it does**:
1. Starts Spring Boot (if not running)
2. Sends HTTP request to endpoint
3. Verifies response status, headers, body
4. Compares with expected output
5. Reports success/failure

**Time Saved**: 10-15 minutes per endpoint

---

## Rollback Plan

If migration fails or causes issues:

1. **Git**: All changes in feature branch, can revert
2. **Dual Mode**: Undertow mode still works until fully removed
3. **Incremental**: Each controller migration is independent

---

## Next Steps After Week 2-3

Once all controllers are migrated:

### Week 4: Cleanup & Skills (2 hours)
- Remove Undertow startup code entirely
- Create `/migrate-controller` skill
- Create `/test-endpoint` skill
- Update all documentation

### Week 5+: Phase 3 (User Experience)
- Bootstrap 5 upgrade
- HTMX integration
- Vite build system

---

## Summary

**Before Migration**:
- 14 Undertow controllers/handlers
- ~1,200 lines of controller code
- Manual HTTP handling
- Custom routing logic

**After Migration**:
- 14 Spring MVC controllers
- ~500 lines of controller code (60% reduction)
- Automatic HTTP handling
- Convention-based routing

**Benefits**:
- ✅ Simpler code (Spring Boot conventions)
- ✅ Better testability (`@WebMvcTest`)
- ✅ Industry-standard patterns (easier maintenance)
- ✅ Improved observability (Spring Boot Actuator)
- ✅ Zero breaking changes (backward compatible)

---

**Status**: 📋 Ready to execute - Starting with Phase 1 (Resource Serving)
