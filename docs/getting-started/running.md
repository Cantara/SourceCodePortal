# Running Source Code Portal

Comprehensive guide to running the application in different modes.

## Overview

Source Code Portal supports two execution modes:
- **Spring Boot Mode** (Recommended) - Modern Spring ecosystem with actuator endpoints
- **Legacy Undertow Mode** (Deprecated) - Standalone Undertow server

**Recommendation**: Use Spring Boot mode for all new deployments.

## Spring Boot Mode (Recommended)

Spring Boot is the recommended mode for running the application. It provides modern Spring ecosystem features including dependency injection, auto-configuration, actuator endpoints, and better testability.

### Basic Execution

#### Run with Maven

```bash
mvn spring-boot:run
```

**Startup time**: ~10-15 seconds
**Default URL**: http://localhost:9090

**What happens**:
1. Spring Boot initializes Spring context
2. Loads configuration from `application.yml` and property files
3. Creates beans (CacheStore, ExecutorService, etc.)
4. Loads repository configuration from `config.json`
5. Pre-fetches GitHub data (initial cache population)
6. Starts embedded Undertow server

#### Run with Maven in Dev Mode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Dev profile enables**:
- Debug logging (more verbose)
- Spring Boot DevTools (hot reload)
- Detailed error pages
- Cache statistics in logs

#### Run JAR After Building

```bash
# Build first
mvn clean package

# Run the JAR
java -jar target/source-code-portal-*.jar
```

**Pros**:
- Faster startup (~5-7 seconds)
- No Maven overhead
- Production-ready artifact

**Cons**:
- Requires rebuild after code changes

#### Run with IntelliJ IDEA

1. Open project in IntelliJ
2. Navigate to `src/main/java/no/cantara/docsite/SpringBootServer.java`
3. Right-click → **Run 'SpringBootServer.main()'**

**Or create Run Configuration**:
1. **Run** → **Edit Configurations**
2. Click **+** → **Spring Boot**
3. **Main class**: `no.cantara.docsite.SpringBootServer`
4. **Active profiles**: `dev` (optional)
5. Click **OK**

### Execution Profiles

Spring Boot supports different profiles for different environments:

#### Development Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Configuration**: `application-dev.yml` or `application-dev.properties`

**Features**:
- Debug logging
- Hot reload with DevTools
- Detailed error pages
- SQL logging (if using databases)
- Cache statistics

#### Production Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**Configuration**: `application-prod.yml` or `application-prod.properties`

**Features**:
- Optimized logging (INFO level)
- Error handling for production
- Performance optimizations
- Security hardening

#### Test Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

**Used for**:
- Integration testing
- CI/CD pipelines
- Staging environments

#### Multiple Profiles

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev,local
```

Profiles are applied in order (later overrides earlier).

### Environment Variables

You can override any configuration property using environment variables with the `SCP_` prefix:

```bash
# Set GitHub access token
SCP_GITHUB_ACCESS_TOKEN=your_token mvn spring-boot:run

# Set server port
SERVER_PORT=8080 mvn spring-boot:run

# Set multiple variables
SCP_GITHUB_ACCESS_TOKEN=token \
SERVER_PORT=8080 \
SPRING_PROFILES_ACTIVE=dev \
mvn spring-boot:run
```

**Naming convention**:
- Spring Boot properties: Use standard names (e.g., `SERVER_PORT`)
- Custom properties: Prefix with `SCP_` (e.g., `SCP_GITHUB_ACCESS_TOKEN`)

### JVM Arguments

Pass JVM arguments for memory, debugging, etc.:

```bash
# Increase heap memory
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2g"

# Enable remote debugging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Multiple arguments
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2g -XX:+UseG1GC -Duser.timezone=UTC"
```

### Application Arguments

Pass arguments to the application:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080 --spring.profiles.active=dev"
```

## Spring Boot Endpoints

### Actuator Endpoints

Spring Boot Actuator provides production-ready features for monitoring and management:

**Health Endpoints**:
- `/actuator/health` - Overall application health
- `/actuator/health/github` - GitHub API rate limit status
- `/actuator/health/cache` - Cache health and statistics
- `/actuator/health/executor` - Thread pool health

**Information Endpoints**:
- `/actuator/info` - Application information (version, runtime, configuration)
- `/actuator/metrics` - Application metrics (memory, CPU, HTTP requests)
- `/actuator/prometheus` - Prometheus-format metrics
- `/actuator/caches` - Cache manager details
- `/actuator/scheduledtasks` - Scheduled task list

**Example usage**:
```bash
# Check overall health
curl http://localhost:9090/actuator/health

# Check GitHub rate limit
curl http://localhost:9090/actuator/health/github

# View metrics
curl http://localhost:9090/actuator/metrics

# View specific metric
curl http://localhost:9090/actuator/metrics/jvm.memory.used
```

### Application Endpoints

**Web Pages**:
- `/` or `/dashboard` - Main dashboard
- `/group/{groupId}` - Repository group view
- `/contents/{org}/{repo}/{branch}` - Repository contents
- `/commits/{org}/{repo}` - Commit history
- `/docs` - Documentation portal

**API Endpoints**:
- `/ping` - Simple health check (returns "pong")
- `/health` - Legacy health endpoint (prefer `/actuator/health`)
- `/github/webhook` - GitHub webhook receiver

**Resource Endpoints**:
- `/badge/{org}/{repo}` - Repository badges
- `/css/*`, `/js/*`, `/images/*` - Static resources

## Legacy Undertow Mode (Deprecated)

The legacy Undertow mode is still available but deprecated. It will be removed in a future version.

⚠️ **Deprecation Notice**: Use Spring Boot mode for all new deployments.

### Run Legacy Mode

```bash
# Build first
mvn clean package

# Run with standalone Undertow
java -cp target/source-code-portal-*.jar no.cantara.docsite.Server
```

### Run with IntelliJ (Legacy)

1. Navigate to `src/main/java/no/cantara/docsite/Server.java`
2. Right-click → **Run 'Server.main()'**

### Limitations

- ❌ No Spring Boot Actuator endpoints
- ❌ No Spring Boot DevTools (hot reload)
- ❌ No auto-configuration
- ❌ Manual dependency wiring
- ❌ Limited observability

**Available endpoints**:
- `/health` - Basic health check (no detailed indicators)
- Application endpoints (same as Spring Boot mode)

## Configuration Files

### Configuration Loading Order

Configuration is loaded in this order (later overrides earlier):

1. `src/main/resources/application-defaults.properties` - Default values
2. `application.properties` - Custom overrides
3. `security.properties` - Credentials (GitHub tokens)
4. `application_override.properties` - Final overrides
5. Environment variables with `SCP_` prefix
6. System properties (`-D` flags)

### Example Configuration

**application.properties**:
```properties
# Server configuration
server.port=9090
server.compression.enabled=true

# Logging
logging.level.no.cantara.docsite=INFO
logging.level.org.springframework=WARN

# Cache configuration
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m

# GitHub configuration
github.repository.visibility=public
```

**security.properties** (not in version control):
```properties
github.oauth2.client.clientId=YOUR_CLIENT_ID
github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET
github.client.accessToken=YOUR_ACCESS_TOKEN
```

See [Configuration Guide](configuration.md) for detailed options.

## Common Run Scenarios

### Development with Hot Reload

```bash
# Terminal 1: Watch Sass files
sass --watch src/main/sass/scss:target/classes/META-INF/views/css

# Terminal 2: Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Changes to templates and CSS will reload automatically with Spring Boot DevTools.

### Production-Like Local Run

```bash
mvn clean package
java -jar target/source-code-portal-*.jar --spring.profiles.active=prod
```

### Custom Port

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
# or
SERVER_PORT=8080 mvn spring-boot:run
```

### Remote Debugging

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

Then attach your IDE debugger to `localhost:5005`.

### Multiple Instances

Run multiple instances on different ports:

```bash
# Terminal 1
SERVER_PORT=9090 mvn spring-boot:run

# Terminal 2
SERVER_PORT=9091 mvn spring-boot:run
```

## Startup Verification

### Check Application Started

**Look for log message**:
```
Started SpringBootServer in X.XXX seconds
```

**Check endpoints**:
```bash
# Ping check
curl http://localhost:9090/ping
# Expected: pong

# Health check
curl http://localhost:9090/actuator/health
# Expected: {"status":"UP"}
```

### Verify GitHub Integration

```bash
curl http://localhost:9090/actuator/health/github
```

**Successful response**:
```json
{
  "status": "UP",
  "details": {
    "rateLimit": {
      "limit": 5000,
      "remaining": 4999,
      "reset": "2025-01-28T15:00:00Z"
    }
  }
}
```

**Failed response** (missing token):
```json
{
  "status": "DOWN",
  "details": {
    "error": "Unauthorized"
  }
}
```

### View Logs

**Console output**: Most important messages shown in terminal

**Log levels**:
- **ERROR**: Critical failures
- **WARN**: Potential issues (rate limits, timeouts)
- **INFO**: Normal operations (startup, cache updates)
- **DEBUG**: Detailed operations (dev profile only)

**Enable debug logging**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.no.cantara.docsite=DEBUG"
```

## Troubleshooting

### Port Already in Use

```
Web server failed to start. Port 9090 was already in use.
```

**Solution 1**: Change port
```bash
SERVER_PORT=8080 mvn spring-boot:run
```

**Solution 2**: Kill process using port
```bash
# Find process
lsof -i :9090
# Kill it
kill -9 <PID>
```

### Slow Startup

**Symptom**: Startup takes >30 seconds

**Causes**:
- GitHub API slow/timing out
- Large number of repositories to fetch
- Network issues

**Solution**: Check GitHub health
```bash
curl http://localhost:9090/actuator/health/github
```

### Out of Memory

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution**: Increase heap size
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2g"
```

Or for JAR:
```bash
java -Xmx2g -jar target/source-code-portal-*.jar
```

### GitHub Rate Limit Exceeded

Check rate limit status:
```bash
curl http://localhost:9090/actuator/health/github
```

**Solution**: Ensure GitHub access token is configured (see [Configuration Guide](configuration.md)).

### Cache Not Populating

**Symptom**: Dashboard shows no data

**Check**:
1. GitHub token configured: `/actuator/health/github`
2. Config.json correct: `src/main/resources/conf/config.json`
3. Cache health: `/actuator/health/cache`

**Enable debug logging**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.no.cantara.docsite.fetch=DEBUG"
```

## Performance Tuning

### JVM Flags

```bash
java \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -jar target/source-code-portal-*.jar
```

### Virtual Threads

Java 21 virtual threads are enabled by default, improving I/O performance for GitHub API calls.

### Cache Configuration

Tune cache sizes in `application.properties`:
```properties
spring.cache.caffeine.spec=maximumSize=2000,expireAfterWrite=15m
```

## Next Steps

- **[Configuration Guide](configuration.md)** - Detailed configuration options
- **[Operations - Monitoring](../operations/monitoring.md)** - Production monitoring
- **[Operations - Deployment](../operations/deployment.md)** - Deploy to production
- **[Architecture - Spring Boot](../architecture/spring-boot.md)** - Understand initialization

## See Also

- Spring Boot documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring Boot Actuator: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- [LEARNINGS.md](../../LEARNINGS.md) - Runtime gotchas and solutions
