# Configuration Guide

Complete guide to configuring Source Code Portal for your organization.

## Overview

Source Code Portal uses a layered configuration approach:
- **Repository groups**: `config.json` - Which repositories to display
- **GitHub authentication**: `security.properties` - API credentials
- **Application settings**: `application.properties` - Server and behavior
- **Environment variables**: `SCP_*` - Runtime overrides

## Configuration Loading Order

Configuration is loaded in this order (later overrides earlier):

1. **`src/main/resources/application-defaults.properties`** - Built-in defaults
2. **`application.properties`** - Custom overrides (project root)
3. **`security.properties`** - Credentials (project root)
4. **`application_override.properties`** - Final overrides (project root)
5. **Environment variables** - Variables with `SCP_` prefix
6. **System properties** - JVM `-D` flags

**Example**: If `server.port=9090` in `application-defaults.properties` but `SERVER_PORT=8080` environment variable is set, the app will use port 8080.

## GitHub Authentication

### Access Token (Required)

Source Code Portal requires a GitHub access token for API access. Without it, you'll hit rate limits quickly (60 requests/hour vs 5,000 with authentication).

### Step 1: Generate Access Token

#### Option A: GitHub Web Interface (Easiest)

1. Go to https://github.com/settings/tokens
2. Click **Generate new token** → **Generate new token (classic)**
3. Set token name: `SourceCodePortal`
4. Set expiration: Your choice (recommend 90 days)
5. Select scopes:
   - ✅ `repo` (Full control of private repositories)
   - ✅ `read:org` (Read org and team membership)
   - ✅ `read:user` (Read user profile data)
6. Click **Generate token**
7. **Copy the token immediately** (you won't see it again!)

#### Option B: ObtainGitHubAccessTokenTestTool (Automated)

Use the included test tool:

1. Create `security.properties` with OAuth credentials:
   ```properties
   github.oauth2.client.clientId=YOUR_CLIENT_ID
   github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET
   ```

2. Run the tool:
   ```bash
   mvn test -Dtest=ObtainGitHubAccessTokenTestTool
   ```

3. Follow OAuth flow in browser
4. Token will be printed to console

**Note**: Requires ChromeDriver for Selenium automation.

#### Option C: Docker Method

```bash
docker run -it \
  -e SCP_github.oauth2.client.clientId=YOUR_CLIENT_ID \
  -e SCP_github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET \
  cantara/sourcecodeportal /github-access-token
```

### Step 2: Configure Token

Create or edit `security.properties` in project root:

```properties
# GitHub OAuth credentials (for token generation)
github.oauth2.client.clientId=YOUR_CLIENT_ID
github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET

# GitHub access token (for API access) - REQUIRED
github.client.accessToken=YOUR_ACCESS_TOKEN
```

**Important**:
- ⚠️ **Never commit `security.properties` to version control**
- Add `security.properties` to `.gitignore`
- Store tokens securely (use environment variables in production)

### Step 3: Verify Token

Start the application and check:

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
      "remaining": 4998,
      "reset": "2025-01-28T15:00:00Z"
    }
  }
}
```

**Failed response**:
```json
{
  "status": "DOWN",
  "details": {
    "error": "Bad credentials"
  }
}
```

### Environment Variable Method (Recommended for Production)

Instead of `security.properties`, use environment variables:

```bash
# Set token via environment variable
export SCP_GITHUB_CLIENT_ACCESSTOKEN=your_token_here

# Or inline
SCP_GITHUB_CLIENT_ACCESSTOKEN=your_token mvn spring-boot:run
```

**Docker**:
```bash
docker run -p 9090:9090 \
  -e SCP_GITHUB_CLIENT_ACCESSTOKEN=your_token \
  cantara/sourcecodeportal
```

## Repository Configuration

### config.json Structure

The repository configuration defines which GitHub repositories to display and how they're grouped.

**Location**: `src/main/resources/conf/config.json`

**Basic structure**:
```json
{
  "github": {
    "organization": "YourOrganization",
    "repository.visibility": "public"
  },
  "groups": [
    {
      "groupId": "backend",
      "display-name": "Backend Services",
      "description": "Microservices and APIs",
      "default-group-repo": "main-api",
      "github-repositories": [
        "user-service",
        "order-service",
        "payment-service"
      ]
    }
  ]
}
```

### GitHub Organization

```json
{
  "github": {
    "organization": "Cantara"
  }
}
```

**Required**: Specifies which GitHub organization to fetch repositories from.

### Repository Visibility

```json
{
  "github": {
    "repository.visibility": "public"
  }
}
```

**Options**:
- `public` - Public repositories only
- `private` - Private repositories only
- `all` - Both public and private

**Default**: `public`

### Repository Groups

Groups organize repositories into logical categories:

```json
{
  "groups": [
    {
      "groupId": "frontend",
      "display-name": "Frontend Applications",
      "description": "User-facing web applications",
      "default-group-repo": "web-app",
      "github-repositories": [
        "web-app",
        "mobile-app",
        "admin-portal"
      ]
    }
  ]
}
```

**Fields**:
- **`groupId`** (required) - Unique identifier, used in URLs (`/group/{groupId}`)
- **`display-name`** (required) - Human-readable name shown in UI
- **`description`** (optional) - Group description
- **`default-group-repo`** (optional) - Default repository for group navigation
- **`github-repositories`** (required) - List of repository names or patterns

### Repository Patterns

Use glob patterns to match multiple repositories:

```json
{
  "github-repositories": [
    "Whydah*",           // All repos starting with "Whydah"
    "*-service",         // All repos ending with "-service"
    "lib-*",             // All repos starting with "lib-"
    "specific-repo"      // Exact match
  ]
}
```

**Examples**:
- `"Whydah*"` matches: WhydahGally, WhydahSSOLoginWebApp, Whydah-UserAdminService
- `"*-api"` matches: user-api, order-api, payment-api
- `"frontend-*"` matches: frontend-web, frontend-mobile

### Multiple Groups Example

```json
{
  "github": {
    "organization": "Cantara"
  },
  "groups": [
    {
      "groupId": "whydah",
      "display-name": "Whydah Identity",
      "description": "Whydah SSO and identity management",
      "default-group-repo": "Whydah-UserAdminService",
      "github-repositories": ["Whydah*"]
    },
    {
      "groupId": "microservices",
      "display-name": "Microservices",
      "description": "Domain microservices",
      "github-repositories": ["*-service", "!Whydah*"]
    },
    {
      "groupId": "frontend",
      "display-name": "Frontend",
      "description": "User interfaces",
      "github-repositories": ["*-web", "*-ui", "*-app"]
    },
    {
      "groupId": "libraries",
      "display-name": "Libraries",
      "description": "Shared libraries",
      "github-repositories": ["lib-*", "common-*"]
    }
  ]
}
```

**Note**: Use `!pattern` to exclude matches (e.g., `"!Whydah*"` excludes Whydah repos).

### External Service Integrations

Add Jenkins, Snyk, and Shields.io integrations per repository:

```json
{
  "github-repositories": [
    {
      "name": "my-service",
      "jenkins": {
        "url": "https://jenkins.example.com/job/my-service",
        "badges": ["build", "coverage"]
      },
      "snyk": {
        "enabled": true,
        "project": "my-org/my-service"
      },
      "shields": {
        "badges": [
          {
            "label": "version",
            "url": "https://img.shields.io/github/v/release/Cantara/my-service"
          }
        ]
      }
    }
  ]
}
```

See [Features - Integrations](../features/integrations.md) for detailed integration configuration.

## Application Settings

### Server Configuration

**application.properties**:
```properties
# Server port
server.port=9090

# Server compression
server.compression.enabled=true
server.compression.mime-types=text/html,text/css,application/javascript,application/json

# Context path (if running at subpath)
server.servlet.context-path=/sourcecodeportal
```

### Logging Configuration

```properties
# Log levels
logging.level.root=INFO
logging.level.no.cantara.docsite=INFO
logging.level.org.springframework=WARN

# Log file
logging.file.name=logs/source-code-portal.log
logging.file.max-size=10MB
logging.file.max-history=30

# Log pattern
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### Cache Configuration

```properties
# Caffeine cache specification
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m

# Cache statistics (dev profile)
spring.cache.cache-names=repositories,commits,contents,badges
```

### GitHub API Configuration

```properties
# Repository visibility (public, private, all)
github.repository.visibility=public

# API timeout (milliseconds)
github.api.timeout=30000

# Retry configuration
github.api.retry.max-attempts=3
github.api.retry.backoff=1000
```

### Scheduled Tasks

```properties
# Data refresh interval (cron expression)
# Default: Every 15 minutes
scheduled.fetch-data.cron=0 */15 * * * *

# Repository refresh interval
# Default: Every hour
scheduled.refresh-repos.cron=0 0 * * * *
```

**Cron syntax**: `second minute hour day month weekday`

**Examples**:
- `0 */15 * * * *` - Every 15 minutes
- `0 0 * * * *` - Every hour
- `0 0 0 * * *` - Every day at midnight
- `0 0 */6 * * *` - Every 6 hours

## Environment Variables

Override any property using environment variables:

### Standard Spring Boot Properties

```bash
# Server port
SERVER_PORT=8080

# Active profiles
SPRING_PROFILES_ACTIVE=prod

# Log level
LOGGING_LEVEL_NO_CANTARA_DOCSITE=DEBUG
```

### Custom Properties (SCP_ prefix)

```bash
# GitHub token
SCP_GITHUB_CLIENT_ACCESSTOKEN=your_token

# Organization
SCP_GITHUB_ORGANIZATION=YourOrg

# Repository visibility
SCP_GITHUB_REPOSITORY_VISIBILITY=all
```

### Property Name Mapping

**Property file format** → **Environment variable format**:
- Dots (`.`) → Underscores (`_`)
- Dashes (`-`) → Underscores (`_`)
- Lowercase → UPPERCASE
- Add `SCP_` prefix for custom properties

**Examples**:
- `github.client.accessToken` → `SCP_GITHUB_CLIENT_ACCESSTOKEN`
- `server.port` → `SERVER_PORT`
- `spring.profiles.active` → `SPRING_PROFILES_ACTIVE`

## Docker Configuration

### Using environment variables:

```bash
docker run -p 9090:9090 \
  -e SCP_GITHUB_CLIENT_ACCESSTOKEN=your_token \
  -e SCP_GITHUB_ORGANIZATION=YourOrg \
  -e SERVER_PORT=9090 \
  cantara/sourcecodeportal
```

### Using config files:

```bash
# Create config directory
mkdir -p config_override/conf

# Copy your config
cp config.json config_override/conf/
cp security.properties config_override/

# Run with volume mount
docker run -p 9090:9090 \
  -v $(pwd)/config_override:/home/sourcecodeportal/config_override \
  cantara/sourcecodeportal
```

### Docker Compose:

```yaml
version: '3.8'
services:
  sourcecodeportal:
    image: cantara/sourcecodeportal
    ports:
      - "9090:9090"
    environment:
      - SCP_GITHUB_CLIENT_ACCESSTOKEN=${GITHUB_TOKEN}
      - SCP_GITHUB_ORGANIZATION=Cantara
      - SPRING_PROFILES_ACTIVE=prod
    volumes:
      - ./config.json:/home/sourcecodeportal/config_override/conf/config.json
```

Run with:
```bash
GITHUB_TOKEN=your_token docker-compose up
```

## Configuration Profiles

### Development Profile (dev)

**application-dev.properties**:
```properties
# Debug logging
logging.level.no.cantara.docsite=DEBUG
logging.level.org.springframework.cache=TRACE

# Cache statistics
spring.cache.caffeine.spec=recordStats,maximumSize=100,expireAfterWrite=5m

# Disable security (local dev only)
management.security.enabled=false
```

**Activate**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Profile (prod)

**application-prod.properties**:
```properties
# Production logging
logging.level.root=WARN
logging.level.no.cantara.docsite=INFO

# Larger cache, longer expiry
spring.cache.caffeine.spec=maximumSize=5000,expireAfterWrite=30m

# Production actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

**Activate**:
```bash
java -jar target/source-code-portal-*.jar --spring.profiles.active=prod
```

### Test Profile (test)

**application-test.properties**:
```properties
# Test configuration
github.api.timeout=5000
spring.cache.caffeine.spec=maximumSize=10,expireAfterWrite=1m

# H2 database for tests (if using DB)
spring.datasource.url=jdbc:h2:mem:testdb
```

## Configuration Best Practices

### Security

1. **Never commit credentials**: Add to `.gitignore`
   ```
   security.properties
   application_override.properties
   ```

2. **Use environment variables in production**: Avoid file-based secrets

3. **Rotate tokens regularly**: GitHub tokens should be rotated every 90 days

4. **Minimal scopes**: Only grant required GitHub token scopes

### Performance

1. **Tune cache sizes**: Based on number of repositories
   ```properties
   # Small org (<50 repos)
   spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=15m

   # Medium org (50-200 repos)
   spring.cache.caffeine.spec=maximumSize=2000,expireAfterWrite=30m

   # Large org (>200 repos)
   spring.cache.caffeine.spec=maximumSize=5000,expireAfterWrite=1h
   ```

2. **Adjust fetch intervals**: Balance freshness vs API usage
   ```properties
   # High activity: Refresh every 10 minutes
   scheduled.fetch-data.cron=0 */10 * * * *

   # Low activity: Refresh every hour
   scheduled.fetch-data.cron=0 0 * * * *
   ```

3. **Enable compression**: Reduces bandwidth
   ```properties
   server.compression.enabled=true
   ```

### Maintainability

1. **Use profiles**: Separate dev/prod configs
2. **Document custom properties**: Add comments in config files
3. **Version config.json**: Commit to version control (no secrets!)
4. **Validate config on startup**: Check required properties

## Troubleshooting

### "Bad credentials" error

**Problem**: Invalid or expired GitHub token

**Solution**:
1. Verify token in `security.properties` or environment variable
2. Check token scopes at https://github.com/settings/tokens
3. Generate new token if expired

### "Rate limit exceeded"

**Problem**: Too many GitHub API requests

**Check current limit**:
```bash
curl http://localhost:9090/actuator/health/github
```

**Solutions**:
1. Add GitHub access token (5000/hour vs 60/hour)
2. Increase cache expiry time
3. Reduce fetch frequency

### Repositories not showing

**Problem**: No repositories visible on dashboard

**Check**:
1. **Organization correct**: Verify `github.organization` in config.json
2. **Token has access**: Check token scopes
3. **Visibility setting**: Try `"repository.visibility": "all"`
4. **Pattern matching**: Check regex patterns in `github-repositories`

**Debug**:
```bash
# Enable debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.no.cantara.docsite.domain.config=DEBUG"
```

### Configuration not loading

**Problem**: Changes to properties not taking effect

**Solutions**:
1. **Restart application**: Configuration is loaded at startup
2. **Check override order**: Later sources override earlier (see loading order above)
3. **Verify syntax**: Check for typos in property names
4. **Check profile**: Ensure correct profile active

## Next Steps

- **[Running Guide](running.md)** - Learn how to run with different configs
- **[Features - Repository Groups](../features/repository-groups.md)** - Advanced group configuration
- **[Features - Integrations](../features/integrations.md)** - Jenkins, Snyk, Shields.io
- **[Operations - Deployment](../operations/deployment.md)** - Production deployment

## See Also

- Spring Boot Configuration: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
- GitHub API: https://docs.github.com/en/rest
- [LEARNINGS.md](../../LEARNINGS.md) - Configuration gotchas
