# External Service Integrations

Source Code Portal integrates with multiple external services to provide comprehensive monitoring and status information for your repositories. This page covers all supported integrations and how to configure them.

## Overview

**Supported Integrations:**
- **Jenkins** - Continuous integration and build status
- **Snyk** - Security vulnerability scanning
- **Shields.io** - Custom badges and metrics
- **GitHub Actions** - Workflow status (planned)

All integrations use the **Circuit Breaker pattern** (Resilience4j) to ensure resilience against service failures and API rate limits.

## Jenkins Integration

Monitor CI/CD build status for all repositories in a group.

### Purpose

Jenkins integration provides:
- Real-time build status badges (success, failure, unstable, building)
- Links to Jenkins job pages
- Build history and trends
- Failed test information

### Configuration

Configure Jenkins integration per repository group in `config.json`:

```json
{
  "groupId": "whydah",
  "display-name": "Whydah IAM Platform",
  "artifactId": ["Whydah*"],
  "jenkins": {
    "prefix": "Whydah-",
    "baseUrl": "https://jenkins.example.com"
  }
}
```

**Configuration Fields:**

- **`prefix`** (required): Jenkins job name prefix
  - For repository `Whydah-UserAdminService`, Jenkins looks for job `Whydah-UserAdminService`
  - Pattern: `{prefix}{repository-name}`

- **`baseUrl`** (optional): Jenkins server URL
  - Default: Configured in `application.yml` under `scp.jenkins.base-url`
  - Override per group if you have multiple Jenkins servers

### Global Jenkins Configuration

Set the default Jenkins URL in `application.yml`:

```yaml
scp:
  jenkins:
    base-url: https://jenkins.example.com
```

Or via environment variable:

```bash
export SCP_JENKINS_BASE_URL=https://jenkins.example.com
```

### Jenkins Job Naming Convention

SCP expects Jenkins jobs to follow this naming pattern:

**Pattern**: `{prefix}{repository-name}`

**Examples:**
- Repository: `Whydah-UserAdminService`
- Prefix: `Whydah-`
- Jenkins Job: `Whydah-UserAdminService`

**Alternatively**, use full repository name:
- Repository: `ConfigService`
- Prefix: (empty or same as repo name)
- Jenkins Job: `ConfigService`

### Build Status Badge

The Jenkins badge displays the status of the last build:

**Badge States:**
- **Green**: Last build successful
- **Yellow**: Build unstable (passed with warnings)
- **Red**: Build failed
- **Blue**: Build in progress
- **Gray**: No build information available

**Badge Location**: Appears on:
- Repository cards on the dashboard
- Group detail page
- Repository detail page

### Jenkins API

SCP uses the Jenkins REST API to fetch build status:

**Endpoint**: `{baseUrl}/job/{jobName}/lastBuild/api/json`

**Response Example**:
```json
{
  "result": "SUCCESS",
  "building": false,
  "timestamp": 1706450000000,
  "url": "https://jenkins.example.com/job/Whydah-UserAdminService/42/"
}
```

### Circuit Breaker Configuration

Jenkins API calls are protected by a circuit breaker with these settings:

- **Failure Threshold**: 50% (opens after 50% of calls fail)
- **Open State Duration**: 60 seconds
- **Half-Open Test Requests**: 3 requests
- **Bulkhead**: Max 25 concurrent calls
- **Timeout**: 75 seconds per call

**When Circuit Opens:**
- SCP stops making requests to Jenkins
- Badge shows "Unknown" status (gray)
- After 60 seconds, circuit enters "Half-Open" state and tries 3 test requests
- If test requests succeed, circuit closes and normal operation resumes

### Troubleshooting Jenkins Integration

**Issue: Badge shows "Unknown" status**

Possible causes:
1. Jenkins URL unreachable
2. Jenkins job doesn't exist
3. Incorrect job name pattern
4. Circuit breaker open due to repeated failures

Solutions:
1. Verify Jenkins URL: `curl https://jenkins.example.com/api/json`
2. Check job exists: Visit `{baseUrl}/job/{jobName}/`
3. Review prefix configuration in `config.json`
4. Check circuit breaker status: `/actuator/health`

**Issue: Badge shows wrong build status**

Possible causes:
1. Cache not expired
2. Jenkins webhook not configured

Solutions:
1. Wait for cache expiry (2 minutes) or restart application
2. Configure Jenkins webhook to trigger immediate updates

## Snyk Integration

Monitor security vulnerabilities for all repositories in a group.

### Purpose

Snyk integration provides:
- Real-time vulnerability count badges
- Severity levels (high, medium, low)
- Links to Snyk project pages
- Scheduled vulnerability scanning

### Configuration

Configure Snyk integration per repository group in `config.json`:

```json
{
  "groupId": "whydah",
  "display-name": "Whydah IAM Platform",
  "artifactId": ["Whydah*"],
  "snyk": {
    "organization": "cantara",
    "projectPrefix": "whydah-",
    "apiVersion": "2024-01-01"
  }
}
```

**Configuration Fields:**

- **`organization`** (required): Your Snyk organization slug
  - Find in Snyk URL: `https://app.snyk.io/org/{organization}/projects`

- **`projectPrefix`** (optional): Snyk project name prefix
  - For repository `Whydah-UserAdminService`, Snyk looks for project `whydah-useradminservice`
  - Pattern: `{projectPrefix}{repository-name-lowercase}`
  - If omitted, uses repository name as-is

- **`apiVersion`** (optional): Snyk API version
  - Default: `2024-01-01`
  - See [Snyk API Versioning](https://docs.snyk.io/snyk-api-info/api-versioning)

### Global Snyk Configuration

Set the Snyk API token in `application.yml`:

```yaml
scp:
  snyk:
    api-token: your_snyk_api_token_here
```

Or via environment variable (recommended):

```bash
export SCP_SNYK_API_TOKEN=your_snyk_api_token_here
```

**Obtaining a Snyk API Token:**

1. Log in to [Snyk](https://app.snyk.io/)
2. Go to **Account Settings** → **General**
3. Copy your **API Token**
4. Set the `SCP_SNYK_API_TOKEN` environment variable

### Snyk Project Naming Convention

SCP expects Snyk projects to follow this naming pattern:

**Pattern**: `{projectPrefix}{repository-name-lowercase}`

**Examples:**
- Repository: `Whydah-UserAdminService`
- Prefix: `whydah-`
- Snyk Project: `whydah-useradminservice`

**Case Sensitivity:**
- Repository names are converted to lowercase
- Hyphens in repository names are preserved
- Example: `ConfigService-Client` → `configservice-client`

### Vulnerability Badge

The Snyk badge displays the vulnerability count and severity:

**Badge States:**
- **Green**: 0 vulnerabilities
- **Yellow**: Low/medium severity vulnerabilities
- **Orange**: High severity vulnerabilities
- **Red**: Critical severity vulnerabilities
- **Gray**: No scan data available or scan failed

**Badge Location**: Appears on:
- Repository cards on the dashboard
- Group detail page
- Repository detail page

### Snyk API

SCP uses the Snyk REST API to fetch vulnerability data:

**Endpoint**: `https://api.snyk.io/v1/org/{orgId}/projects`

**Response Example**:
```json
{
  "projects": [
    {
      "name": "whydah-useradminservice",
      "issueCountsBySeverity": {
        "low": 2,
        "medium": 1,
        "high": 0,
        "critical": 0
      }
    }
  ]
}
```

### Badge Caching

Snyk badges are cached to reduce API calls:

- **Cache TTL**: 15 minutes
- **Refresh Strategy**: Scheduled task fetches badges every 15 minutes
- **On-Demand Refresh**: Snyk webhook (if configured) triggers immediate update

**Why Cache?**
- Snyk API rate limits (1000 requests/month for free tier)
- Badge rendering is expensive (SVG parsing)
- Vulnerabilities don't change frequently

See [Snyk Integration Details](snyk-integration.md) for more information on caching strategy.

### Circuit Breaker Configuration

Snyk API calls are protected by a circuit breaker with the same settings as Jenkins:

- **Failure Threshold**: 50%
- **Open State Duration**: 60 seconds
- **Bulkhead**: Max 25 concurrent calls
- **Timeout**: 75 seconds per call

### Troubleshooting Snyk Integration

**Issue: Badge shows "Unknown" status**

Possible causes:
1. Snyk API token invalid or missing
2. Snyk project doesn't exist
3. Incorrect project naming pattern
4. Rate limit exceeded

Solutions:
1. Verify API token: `curl -H "Authorization: token YOUR_TOKEN" https://api.snyk.io/v1/user/me`
2. Check project exists in Snyk dashboard
3. Review `projectPrefix` configuration in `config.json`
4. Upgrade Snyk plan or reduce API call frequency

**Issue: Vulnerabilities not updating**

Possible causes:
1. Cache not expired
2. Snyk scan not run recently

Solutions:
1. Wait 15 minutes for cache expiry or restart application
2. Trigger Snyk scan manually in Snyk dashboard

## Shields.io Integration

Display custom badges for metrics like code coverage, version, license, etc.

### Purpose

Shields.io integration provides:
- Custom badges for any metric
- Static or dynamic badges
- Consistent badge styling
- Support for popular services (Codecov, npm, PyPI, etc.)

### Configuration

Configure Shields.io badges per repository group in `config.json`:

```json
{
  "groupId": "whydah",
  "display-name": "Whydah IAM Platform",
  "artifactId": ["Whydah*"],
  "shields": [
    {
      "label": "coverage",
      "message": "85%",
      "color": "brightgreen",
      "style": "flat"
    },
    {
      "label": "license",
      "message": "Apache 2.0",
      "color": "blue"
    }
  ]
}
```

**Configuration Fields:**

- **`label`** (required): Badge label (left side)
- **`message`** (required): Badge message (right side)
- **`color`** (required): Badge color (right side)
  - Options: `brightgreen`, `green`, `yellowgreen`, `yellow`, `orange`, `red`, `blue`, `lightgrey`, `gray`
  - Hex colors: `#ff6900`
- **`style`** (optional): Badge style
  - Options: `flat`, `flat-square`, `plastic`, `for-the-badge`, `social`
  - Default: `flat`

### Dynamic Badges

Use Shields.io's dynamic badge endpoints for live data:

**Example: GitHub Release**
```json
{
  "label": "release",
  "message": "dynamic",
  "color": "blue",
  "url": "https://img.shields.io/github/v/release/Cantara/Whydah-UserAdminService"
}
```

**Example: Code Coverage (Codecov)**
```json
{
  "label": "coverage",
  "message": "dynamic",
  "color": "brightgreen",
  "url": "https://img.shields.io/codecov/c/github/Cantara/Whydah-UserAdminService"
}
```

**Example: npm Version**
```json
{
  "label": "npm",
  "message": "dynamic",
  "color": "blue",
  "url": "https://img.shields.io/npm/v/my-package"
}
```

### Badge Caching

Shields.io badges are cached to improve performance:

- **Static Badges**: Cached indefinitely (don't change)
- **Dynamic Badges**: Cached for 5 minutes
- **Refresh Strategy**: Badges are refetched when cache expires

### Circuit Breaker Configuration

Shields.io API calls use a dedicated circuit breaker:

- **Failure Threshold**: 50%
- **Open State Duration**: 30 seconds (faster recovery than Jenkins/Snyk)
- **Bulkhead**: Max 50 concurrent calls (higher than Jenkins/Snyk)
- **Timeout**: 30 seconds per call (faster than Jenkins/Snyk)

**Why Different Settings?**
- Shields.io is more reliable than Jenkins/Snyk
- Faster timeout and recovery improve user experience

### Troubleshooting Shields.io Integration

**Issue: Badge not displaying**

Possible causes:
1. Invalid Shields.io URL
2. Network connectivity issues
3. Circuit breaker open

Solutions:
1. Test badge URL in browser: Visit the `url` directly
2. Check network connectivity: `curl https://img.shields.io/badge/test-message-blue`
3. Wait 30 seconds for circuit breaker to recover

**Issue: Badge shows wrong data**

Possible causes:
1. Cache not expired
2. Shields.io data source out of date

Solutions:
1. Wait 5 minutes for cache expiry
2. Check the source service (Codecov, GitHub, npm, etc.)

## Circuit Breaker Pattern

All external service integrations use the **Resilience4j Circuit Breaker** pattern to protect against failures.

### How Circuit Breaker Works

```
┌─────────────────────────────────────────────────────┐
│                   Circuit States                     │
├─────────────────────────────────────────────────────┤
│                                                       │
│   CLOSED (Normal Operation)                          │
│   ┌──────────────────────┐                          │
│   │ All requests pass    │                          │
│   │ through normally     │                          │
│   └──────┬───────────────┘                          │
│          │                                           │
│          │ >50% failures                             │
│          ▼                                           │
│   OPEN (Failing Fast)                                │
│   ┌──────────────────────┐                          │
│   │ All requests fail    │                          │
│   │ immediately (no API  │                          │
│   │ calls made)          │                          │
│   └──────┬───────────────┘                          │
│          │                                           │
│          │ After 60 seconds                          │
│          ▼                                           │
│   HALF_OPEN (Testing)                                │
│   ┌──────────────────────┐                          │
│   │ Allow 3 test         │                          │
│   │ requests through     │                          │
│   └──────┬───────────────┘                          │
│          │                                           │
│          │ If successful                             │
│          ▼                                           │
│   Back to CLOSED                                     │
│                                                       │
└─────────────────────────────────────────────────────┘
```

### Circuit Breaker Configuration

**Global Settings** (all integrations):
- Failure Rate Threshold: 50%
- Slow Call Rate Threshold: 50%
- Slow Call Duration: 30 seconds
- Minimum Number of Calls: 5 (before calculating failure rate)
- Sliding Window Size: 10 calls
- Permitted Calls in Half-Open State: 3

**Integration-Specific Settings:**

| Integration | Open Duration | Timeout | Bulkhead |
|-------------|---------------|---------|----------|
| Jenkins     | 60 seconds    | 75s     | 25       |
| Snyk        | 60 seconds    | 75s     | 25       |
| Shields.io  | 30 seconds    | 30s     | 50       |
| GitHub      | 60 seconds    | 75s     | 25       |

### Monitoring Circuit Breakers

Check circuit breaker status via Spring Boot Actuator:

**Endpoint**: `/actuator/health`

**Response Example**:
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "jenkins": {
          "state": "CLOSED",
          "failureRate": "10.0%",
          "slowCallRate": "5.0%"
        },
        "snyk": {
          "state": "OPEN",
          "failureRate": "60.0%",
          "slowCallRate": "30.0%"
        }
      }
    }
  }
}
```

**Interpreting States:**
- **CLOSED**: Normal operation, all systems healthy
- **OPEN**: Service failing, requests are blocked
- **HALF_OPEN**: Testing service recovery

### Benefits of Circuit Breaker

1. **Fail Fast**: Don't wait for timeouts when service is down
2. **Resource Protection**: Prevent thread pool exhaustion
3. **Automatic Recovery**: Automatically test service recovery
4. **Cascading Failure Prevention**: Prevent one service failure from taking down the entire application
5. **Better User Experience**: Show cached data or fallback UI instead of hanging

### Implementation

Circuit breakers are implemented in the `no.cantara.docsite.commands` package:

**Key Classes:**
- `BaseResilientCommand` - Base class with circuit breaker, bulkhead, timeout
- `GetJenkinsCommand` - Jenkins API calls
- `GetSnykCommand` - Snyk API calls
- `GetShieldsCommand` - Shields.io API calls
- `GetGitHubCommand` - GitHub API calls

**Example Usage**:
```java
@Service
public class JenkinsService {
    public BuildStatus getBuildStatus(String jobName) {
        return new GetJenkinsCommand(jobName)
            .execute()
            .map(this::parseBuildStatus)
            .orElse(BuildStatus.UNKNOWN);
    }
}
```

## Adding New Integrations

To add a new external service integration:

### 1. Create Command Class

Create a new command class extending `BaseResilientCommand`:

```java
public class GetNewServiceCommand extends BaseResilientCommand<String> {
    private final String resourceUrl;

    public GetNewServiceCommand(String resourceUrl) {
        super("newservice");
        this.resourceUrl = resourceUrl;
    }

    @Override
    protected String run() throws Exception {
        // Make HTTP request to external service
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(resourceUrl))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        return response.body();
    }

    @Override
    protected String getFallback() {
        // Return cached data or default value
        return "UNKNOWN";
    }
}
```

### 2. Add Configuration

Add configuration fields to `config.json`:

```json
{
  "groupId": "example",
  "newservice": {
    "apiKey": "your-api-key",
    "projectPrefix": "example-"
  }
}
```

### 3. Create Service Class

Create a service class to interact with the new integration:

```java
@Service
public class NewServiceService {
    private final String apiKey;

    public NewServiceService(ApplicationProperties properties) {
        this.apiKey = properties.getNewService().getApiKey();
    }

    public ServiceData fetchData(String projectName) {
        return new GetNewServiceCommand(buildUrl(projectName))
            .execute()
            .map(this::parseResponse)
            .orElse(ServiceData.UNKNOWN);
    }
}
```

### 4. Add Health Indicator

Create a custom health indicator for monitoring:

```java
@Component
public class NewServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check service connectivity
        boolean isUp = checkServiceHealth();

        return isUp
            ? Health.up().build()
            : Health.down().withDetail("reason", "Service unreachable").build();
    }
}
```

### 5. Update Documentation

Update this file with configuration and usage instructions for the new integration.

## Related Documentation

- [Snyk Integration Details](snyk-integration.md) - Detailed Snyk integration documentation
- [Repository Groups](repository-groups.md) - Configure repository groups
- [Dashboard](dashboard.md) - View integration status on dashboard
- [Observability](observability.md) - Monitor integration health
- [Architecture: Circuit Breaker](../architecture/overview.md#circuit-breaker-pattern) - Circuit breaker architecture
