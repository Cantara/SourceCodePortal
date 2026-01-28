# Architecture Documentation

Technical architecture documentation for Source Code Portal.

## Overview

Source Code Portal is a Spring Boot 3.2.2 application built on Java 21 LTS. It follows modern architectural patterns including:
- **Spring MVC** for web controllers
- **Caffeine cache** with Spring Cache abstraction
- **Resilience4j** circuit breaker for external calls
- **Virtual threads** for I/O operations
- **Actuator** for observability

## Architecture Guides

### System Design

- **[Overview](overview.md)** - System architecture, capabilities, and technology stack
- **[Spring Boot](spring-boot.md)** - Spring Boot initialization and application startup
- **[Controllers](controllers.md)** - Request flow and Spring MVC architecture
- **[Caching](caching.md)** - Cache strategy with Caffeine and Spring Cache
- **[Packages](packages.md)** - Package structure and responsibilities

### Key Concepts

| Concept | Description | Guide |
|---------|-------------|-------|
| **Request Flow** | How HTTP requests are processed | [Controllers](controllers.md) |
| **Cache Population** | Pre-fetch, scheduled refresh, webhooks | [Caching](caching.md) |
| **Circuit Breaker** | Resilience4j for external calls | [Overview](overview.md) |
| **Spring Boot Init** | Application startup sequence | [Spring Boot](spring-boot.md) |
| **Package Organization** | Code structure and responsibilities | [Packages](packages.md) |

## System Diagrams

### High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                   GitHub Organization                │
│              (Repositories, Commits, etc.)           │
└─────────────────────────────────────────────────────┘
                          │
                          │ REST API (with Circuit Breaker)
                          ↓
┌─────────────────────────────────────────────────────┐
│              Source Code Portal (Spring Boot)        │
│                                                      │
│  ┌─────────────┐  ┌──────────┐  ┌───────────────┐  │
│  │  Controllers │→│ Services │→│ CacheStore    │  │
│  │  (Spring MVC)│  │          │  │ (Caffeine)    │  │
│  └─────────────┘  └──────────┘  └───────────────┘  │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │  Scheduled Tasks (Pre-fetch, Refresh)       │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          │ HTTP/HTML
                          ↓
                    ┌──────────┐
                    │ Browsers │
                    └──────────┘
```

### Request Processing Flow

See [Controllers Guide](controllers.md) for detailed request flow diagrams.

## Architecture Decisions

### Why Spring Boot?

**Chosen**: Spring Boot 3.2.2
**Alternative**: Standalone Undertow (legacy, now deprecated)

**Reasons**:
- ✅ Modern Spring ecosystem features
- ✅ Dependency injection and auto-configuration
- ✅ Actuator for observability
- ✅ Better testability
- ✅ Industry-standard patterns

See [Spring Boot Guide](spring-boot.md) for migration details.

### Why Caffeine Cache?

**Chosen**: Caffeine with Spring Cache abstraction
**Alternative**: JSR-107 JCache (still supported)

**Reasons**:
- ✅ High performance (best-in-class)
- ✅ Spring Cache integration
- ✅ Statistics and metrics support
- ✅ Size-based and time-based eviction

See [Caching Guide](caching.md) for details.

### Why Resilience4j?

**Chosen**: Resilience4j circuit breaker pattern
**Alternative**: Manual retry logic

**Reasons**:
- ✅ Protects against GitHub API failures
- ✅ Circuit breaker, bulkhead, time limiter
- ✅ Spring Boot integration
- ✅ Metrics support

See [Overview](overview.md) for circuit breaker configuration.

### Why Java 21?

**Chosen**: Java 21 LTS
**Previous**: Java 17 LTS

**Reasons**:
- ✅ Virtual threads (better I/O performance)
- ✅ Latest LTS release
- ✅ Pattern matching improvements
- ✅ String templates (preview)

## Technology Stack Summary

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Language** | Java | 21 LTS | Runtime platform |
| **Framework** | Spring Boot | 3.2.2 | Application framework |
| **Server** | Undertow | 2.3.17 | Embedded web server |
| **View Engine** | Thymeleaf | 3.1.2 | Server-side templating |
| **Cache** | Caffeine | (via Spring) | In-memory caching |
| **Resilience** | Resilience4j | 2.2.0 | Circuit breaker |
| **Observability** | Spring Actuator | (via Spring) | Metrics and health |
| **Testing** | JUnit | 5.11.3 | Test framework |
| **Build** | Maven | 3.6+ | Build automation |

## Design Patterns

### Circuit Breaker Pattern

All external HTTP calls (GitHub API, Jenkins, Snyk, Shields.io) use Resilience4j circuit breaker:

```java
@Override
public String execute() {
    return circuitBreaker.executeSupplier(() -> {
        // HTTP call to external service
        return httpClient.get(url);
    });
}
```

See [Overview](overview.md#circuit-breaker-pattern) for configuration.

### Cache-Aside Pattern

Data is fetched from GitHub API and cached:

```java
public Repository getRepository(String org, String repo) {
    // Check cache first
    Repository cached = cache.get(key);
    if (cached != null) {
        return cached;
    }

    // Fetch from GitHub
    Repository fresh = githubApi.getRepository(org, repo);

    // Store in cache
    cache.put(key, fresh);
    return fresh;
}
```

See [Caching Guide](caching.md) for details.

### Configuration-Driven Pattern

Repository groups are defined in `config.json`, not hardcoded:

```json
{
  "groups": [
    {
      "groupId": "backend",
      "github-repositories": ["*-service"]
    }
  ]
}
```

See [Features - Repository Groups](../features/repository-groups.md) for configuration.

## Performance Characteristics

### Cache Hit Rates

- **Expected**: >95% cache hit rate after warmup
- **Warmup time**: ~30-60 seconds (depends on repo count)
- **Refresh interval**: 15 minutes (configurable)

### Response Times

- **Cached requests**: <50ms (p99)
- **Cache miss**: 200-500ms (depends on GitHub API)
- **Initial load**: 2-5s (pre-fetch GitHub data)

### Scalability Limits

- **Organizations**: Unlimited
- **Repositories per org**: <500 recommended
- **Commit throughput**: <2500 commits/hour
- **Concurrent users**: 100+ (limited by cache size and memory)

### Memory Usage

- **Base**: ~200-300 MB
- **Per repository**: ~1-2 MB (cached data)
- **Recommended**: 1-2 GB heap for medium orgs

## Code Organization Principles

1. **Package by feature**: Group related code together
2. **Dependency injection**: Use Spring beans
3. **Separation of concerns**: Controllers, services, domain
4. **Testability**: Mock external dependencies
5. **Configuration over code**: Use property files

See [Packages Guide](packages.md) for detailed package structure.

## Security Considerations

1. **GitHub token security**: Never commit `security.properties`
2. **Webhook verification**: HMAC signature validation
3. **Rate limit protection**: Circuit breaker prevents abuse
4. **Input validation**: Validate user input in controllers
5. **CORS configuration**: Configure allowed origins

## Further Reading

- **[Getting Started](../getting-started/)** - Run the application
- **[Features](../features/)** - Understand capabilities
- **[Operations](../operations/)** - Deploy and monitor
- **[Development](../development/)** - Contribute to the project

## Related Documentation

- [CLAUDE.md](../../CLAUDE.md) - Claude Code guidance (includes architecture overview)
- [LEARNINGS.md](../../LEARNINGS.md) - Architecture-related gotchas
- [CHANGELOG.md](../../CHANGELOG.md) - Architecture evolution over time
