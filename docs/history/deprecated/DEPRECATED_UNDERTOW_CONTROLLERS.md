# Deprecated Undertow Controllers

**Status**: ⚠️ Deprecated as of 0.10.17-SNAPSHOT
**Removal**: Planned for future version (Phase 3+)

---

## Overview

The following Undertow controllers and handlers are deprecated and will be removed in a future version. All functionality has been migrated to Spring MVC controllers.

**Recommended**: Use Spring Boot mode (`mvn spring-boot:run`) instead of Undertow standalone mode.

---

## Deprecated Controllers

### Main Routing Controllers

| Class | Replacement | Status |
|-------|-------------|--------|
| `ApplicationController` | Spring Boot routing | ⚠️ Deprecated |
| `WebController` | Spring MVC @RequestMapping | ⚠️ Deprecated |

### Undertow-Specific Controllers (Kept for Undertow mode)

| Class | Spring MVC Replacement | Status |
|-------|----------------------|--------|
| `CORSController` | `CorsConfiguration` | ⚠️ Deprecated |
| `EchoController` | `EchoRestController` | ⚠️ Deprecated |
| `PingController` | `PingRestController` | ⚠️ Deprecated |
| `HealthController` | `HealthRestController` | ⚠️ Deprecated |
| `GithubWebhookController` | `GitHubWebhookRestController` | ⚠️ Deprecated |
| `ImageResourceController` | Spring Boot static resources | ⚠️ Deprecated |
| `StaticContentController` | Spring Boot static resources | ⚠️ Deprecated |

### Web Handlers (Undertow)

| Handler | Spring MVC Controller | Status |
|---------|---------------------|--------|
| `DashboardHandler` | `DashboardWebController` | ⚠️ Deprecated |
| `CardHandler` | `GroupWebController` | ⚠️ Deprecated |
| `CommitsHandler` | `CommitsWebController` | ⚠️ Deprecated |
| `ContentsHandler` | `ContentsWebController` | ⚠️ Deprecated |
| `CantaraWikiHandler` | `WikiWebController` | ⚠️ Deprecated |
| `BadgeResourceHandler` | `BadgeResourceController` | ⚠️ Deprecated |

---

## Migration Path

### For Users

**Old way (Undertow standalone - deprecated)**:
```bash
java -jar target/source-code-portal-*.jar
# or
mvn exec:java -Dexec.mainClass="no.cantara.docsite.Server"
```

**New way (Spring Boot - recommended)**:
```bash
mvn spring-boot:run
```

### For Developers

If you're adding new endpoints, use Spring MVC patterns:

**REST Endpoints**:
```java
@RestController
@RequestMapping("/api/resource")
public class MyRestController {
    @GetMapping
    public ResponseEntity<Data> getData() {
        return ResponseEntity.ok(data);
    }
}
```

**Web Pages**:
```java
@Controller
@RequestMapping("/page")
public class MyWebController {
    @GetMapping
    public String showPage(Model model) {
        model.addAttribute("data", data);
        return "template-name";
    }
}
```

---

## Why Deprecated?

### Problems with Undertow Mode
- Manual HTTP handling (boilerplate code)
- No integration with Spring Boot Actuator
- Harder to test (no @WebMvcTest)
- Non-standard patterns (harder to hire for)
- Manual routing (error-prone)

### Benefits of Spring MVC
- ✅ 50-70% less code
- ✅ Industry-standard patterns
- ✅ Better testability
- ✅ Spring Boot Actuator integration
- ✅ Automatic content negotiation
- ✅ Better IDE support
- ✅ Declarative routing

---

## Timeline

| Version | Status |
|---------|--------|
| 0.10.17-SNAPSHOT | Deprecated (current) |
| 0.11.0 (Phase 3) | Still available but discouraged |
| 0.12.0 (Phase 4) | Planned removal |

---

## Support

If you need to continue using Undertow mode for any reason, please:
1. Open an issue explaining your use case
2. We may extend the deprecation period
3. Consider migrating to Spring Boot mode

---

## Technical Details

### Dual-Mode Support

Currently, both modes work:

**Spring Boot Mode** (Recommended):
- Starts with: `mvn spring-boot:run`
- Port: 9090 (configurable via `server.port`)
- All Spring MVC controllers active
- Spring Boot Actuator available at `/actuator/*`
- CORS via Spring Boot configuration

**Undertow Mode** (Legacy):
- Starts with: `java -jar ...` or `Server.main()`
- Port: 9090 (configurable via `http.port`)
- Uses deprecated controllers
- No Actuator endpoints
- CORS via CORSController

### Configuration Differences

**Spring Boot Mode**:
- Uses `application.yml`
- Type-safe with `@ConfigurationProperties`
- Environment variable support: `SCP_*`
- Profile-based configuration (dev, prod, test)

**Undertow Mode**:
- Uses `application.properties`
- String-based configuration
- Manual property loading
- No profile support

---

## Questions?

See:
- `README.md` - Project overview
- `CLAUDE.md` - Developer guide
- `VERIFICATION_GUIDE.md` - How to verify Spring Boot mode
- `WEEK2-3_PROGRESS.md` - Migration details

---

**Last Updated**: 2026-01-27
**Applies to**: Version 0.10.17-SNAPSHOT and later
