# Architecture Overview

This document provides a high-level overview of the Source Code Portal architecture, including its key capabilities, technology stack, and design constraints.

## Table of Contents

- [System Overview](#system-overview)
- [Key Capabilities](#key-capabilities)
- [Technology Stack](#technology-stack)
- [Design Constraints](#design-constraints)
- [Deployment Modes](#deployment-modes)
- [Related Documentation](#related-documentation)

## System Overview

Source Code Portal (SCP) is an organizational dashboard that aggregates and displays status and documentation from GitHub repositories. It serves as a central hub for software development teams to monitor and navigate their codebase ecosystem.

### Target Audience

The system is designed for small to medium-sized organizations with:
- Multiple GitHub repositories
- Need for centralized documentation
- Build status monitoring requirements
- Commit activity tracking
- Security vulnerability monitoring

### Scale Characteristics

- **Target Scale**: Organizations with < 2500 commits/hour
- **Repository Support**: Unlimited repositories per organization
- **Group Support**: Logical grouping of repositories into systems
- **Update Mechanism**: Real-time via GitHub webhooks + periodic background refresh

## Key Capabilities

### 1. Repository Aggregation

**Data Collection:**
- Fetches repository metadata from GitHub organizations
- Aggregates commit history across all repositories
- Collects release information and tags
- Retrieves repository contents (README, documentation)

**Configuration-Driven:**
- Define repository groups via `config.json`
- Support for regex patterns to match repositories dynamically
- Example: "Whydah*" matches all repositories starting with "Whydah"

### 2. Logical System Grouping

**Group Organization:**
- Multiple repositories can be grouped into logical systems
- Each group has:
  - Unique `groupId` (URL-safe identifier)
  - Human-readable `display-name`
  - Descriptive `description`
  - Default repository for navigation
  - List of repository patterns to include

**Use Case Example:**
```json
{
  "groupId": "security-platform",
  "display-name": "Security Platform",
  "description": "Authentication and authorization services",
  "default-group-repo": "SecurityTokenService",
  "repoSearch": [
    "SecurityTokenService",
    "UserIdentityBackend",
    "SSOLoginWebApp"
  ]
}
```

### 3. Documentation Display

**Supported Formats:**
- Markdown (.md) - Primary format
- AsciiDoc (.adoc, .asciidoc) - Full support
- Plain text (.txt) - Fallback

**Rendering Features:**
- Server-side rendering via Thymeleaf
- Syntax highlighting for code blocks
- Image embedding from repository
- Link resolution within repository

### 4. Build Status Integration

**Jenkins Integration:**
- Displays build status badges
- Links to Jenkins build pages
- Configurable per repository group

**Snyk Integration:**
- Security vulnerability scanning results
- Security test status badges
- Configurable per repository

**Shields.io Integration:**
- Custom badge generation
- Metrics display (coverage, versions, etc.)

### 5. Real-Time Updates

**GitHub Webhooks:**
- Push events trigger immediate cache updates
- Branch creation/deletion notifications
- Release events
- Configurable webhook secret for security

**Background Refresh:**
- Scheduled periodic updates for all repositories
- Configurable refresh intervals
- Rate-limit aware (respects GitHub API limits)

### 6. Commit History Tracking

**Features:**
- Per-repository commit logs
- Commit author information
- Commit message display
- Link to GitHub for full details
- Configurable commit history depth

## Technology Stack

### Backend Stack

**Core Platform:**
- **Java 21 LTS** - Long-term support version
  - Virtual threads enabled for better I/O performance
  - Modern language features (records, pattern matching, etc.)
  - JDK requirement: Minimum Java 21

**Application Framework:**
- **Spring Boot 3.2.2** - Primary deployment mode (recommended)
  - Spring MVC for web layer
  - Spring Cache abstraction
  - Spring Boot Actuator for observability
  - Embedded Undertow server (2.3.17)
- **Standalone Undertow** - Legacy deployment mode (deprecated)
  - Direct Undertow integration
  - Custom routing layer
  - Scheduled for removal in future version

**Templating Engine:**
- **Thymeleaf 3.1.2** - Server-side HTML rendering
  - Natural templating (valid HTML)
  - Fragment support for modular UI
  - Spring integration for model binding

### Caching Layer

**Current (Spring Boot Mode):**
- **Spring Cache Abstraction** - Primary caching interface
- **Caffeine Cache** - High-performance in-memory cache backend
  - Metrics via Micrometer
  - Prometheus export support

**Legacy (Undertow Mode):**
- **JSR-107 JCache** - Standard cache API
- **Reference Implementation** - Default provider
- Note: Deprecated with Undertow mode removal

### Resilience & Circuit Breaking

**Resilience4j 2.2.0** - Fault tolerance library
- **Circuit Breaker**: 50% failure threshold, 60s open state
- **Bulkhead**: 25 max concurrent external calls
- **Time Limiter**: 75s timeout for external calls
- **Use Case**: All GitHub API calls, Jenkins/Snyk/Shields.io requests

### Observability

**Spring Boot Actuator:**
- Health checks (application, GitHub API, cache, thread pools)
- Metrics collection (JVM, HTTP, custom)
- Prometheus metrics export
- Application info endpoint

**Custom Health Indicators:**
- GitHub API rate limit monitoring
- Cache health and statistics
- Executor service health (thread pools)

### Testing Framework

**JUnit 5.11.3** (Jupiter):
- Modern assertion API
- Parameterized tests
- Extension model (TestServerExtension)
- Integration with Spring Boot Test

### Frontend Stack

**Styling:**
- **Sass/SCSS** - CSS preprocessing
- **Bootstrap 5** - UI framework (dark mode support)
- Live compilation support for development

**Build Tool:**
- **Maven** - Dependency management and build automation
- Maven Shade plugin for fat JAR creation
- libsass-maven-plugin for Sass compilation

## Design Constraints

### 1. Performance Constraints

**Caching Strategy:**
- All GitHub data must be cached
- Cache-first approach to minimize API calls
- Prefetch on startup for critical data
- Periodic background refresh

**Rationale**: GitHub API rate limits (5000 requests/hour authenticated)

### 2. Thread Model

**Java 21 Virtual Threads:**
- All I/O operations use virtual threads
- High concurrency for external API calls
- Minimal resource overhead

**Executor Services:**
- Fixed thread pool for scheduled tasks
- Virtual thread executor for async operations

### 3. External API Protection

**Circuit Breaker Pattern:**
- Prevents cascading failures
- Automatic recovery from transient errors
- Fallback mechanisms for degraded operation

**Rate Limiting:**
- Respects GitHub API rate limits
- Backoff strategy for rate-limited requests

### 4. Configuration Flexibility

**Multi-Layer Configuration:**
1. Default properties (baked into JAR)
2. Custom overrides (file-based)
3. Security properties (credentials)
4. Environment variables (`SCP_*` prefix)
5. System properties (runtime overrides)

**Rationale**: Support for different deployment environments (dev, test, prod)

### 5. Backward Compatibility

**Dual-Mode Support:**
- Spring Boot mode (recommended)
- Legacy Undertow mode (deprecated)
- Graceful migration path

**API Stability:**
- All URLs remain unchanged during migration
- JSON response formats preserved
- Webhook contracts maintained

## Deployment Modes

### Spring Boot Mode (Recommended)

**Characteristics:**
- Modern Spring ecosystem features
- Dependency injection throughout
- Auto-configuration for common patterns
- Actuator endpoints for monitoring
- Better testability with Spring Test
- Industry-standard practices

**Entry Point:**
```java
no.cantara.docsite.SpringBootServer
```

**Execution:**
```bash
mvn spring-boot:run
# or
java -jar target/source-code-portal-*.jar
```

**Configuration:**
```yaml
scp:
  server:
    mode: spring-boot  # Default
```

### Legacy Undertow Mode (Deprecated)

**Characteristics:**
- Custom Undertow integration
- Manual dependency wiring
- Custom routing layer
- Limited observability
- Scheduled for removal

**Entry Point:**
```java
no.cantara.docsite.Server
```

**Execution:**
```bash
java -cp target/source-code-portal-*.jar no.cantara.docsite.Server
```

**Configuration:**
```yaml
scp:
  server:
    mode: undertow  # Deprecated
```

**Deprecation Notice**: This mode will be removed in version 1.0.0

## Related Documentation

### Architecture Deep-Dives

- [Spring Boot Architecture](spring-boot.md) - Spring Boot initialization and configuration
- [Controller Architecture](controllers.md) - Request flow and controller patterns
- [Caching Architecture](caching.md) - Cache strategy and implementation
- [Package Structure](packages.md) - Code organization and responsibilities

### Getting Started

- [Quick Start Guide](../getting-started/quick-start.md)
- [Building the Application](../getting-started/building.md)
- [Configuration Guide](../getting-started/configuration.md)

### Operations

- [Running the Application](../operations/running.md)
- [Monitoring and Observability](../operations/monitoring.md)
- [Docker Deployment](../operations/docker.md)

### Development

- [Development Setup](../development/setup.md)
- [Testing Guide](../development/testing.md)
- [Contributing Guidelines](../development/contributing.md)

---

**Next Steps**: Read the [Spring Boot Architecture](spring-boot.md) document to understand the application initialization flow and Spring integration.
