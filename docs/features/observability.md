# Observability

Source Code Portal provides comprehensive observability features through Spring Boot Actuator, custom health indicators, and structured logging. This enables effective monitoring, troubleshooting, and performance optimization.

## Overview

**Observability Components:**
- **Spring Boot Actuator** - Health checks, metrics, and application info
- **Custom Health Indicators** - GitHub, Cache, and Executor monitoring
- **Prometheus Metrics** - Ready for Grafana dashboards
- **Structured Logging** - SLF4J with Logback
- **Application Info** - Runtime and configuration details

## Spring Boot Actuator Endpoints

Spring Boot Actuator provides production-ready endpoints for monitoring and managing the application.

### Base URL

All actuator endpoints are available at:
```
http://localhost:9090/actuator
```

### Available Endpoints

#### 1. Health Endpoint

**URL**: `/actuator/health`

Shows overall application health and custom health indicators.

**Response Example**:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 125829394432,
        "threshold": 10485760,
        "exists": true
      }
    },
    "github": {
      "status": "UP",
      "details": {
        "rateLimit": {
          "limit": 5000,
          "remaining": 4850,
          "reset": "2024-01-28T11:00:00Z"
        },
        "organization": "Cantara"
      }
    },
    "cache": {
      "status": "UP",
      "details": {
        "cacheManager": "caffeine",
        "caches": {
          "repositories": {
            "size": 42,
            "hitRate": 0.85
          },
          "commits": {
            "size": 156,
            "hitRate": 0.92
          }
        }
      }
    },
    "executor": {
      "status": "UP",
      "details": {
        "activeThreads": 5,
        "poolSize": 25,
        "queueSize": 0,
        "utilization": 0.20
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Status Values**:
- **UP**: Component is healthy
- **DOWN**: Component is unhealthy (application may not function properly)
- **DEGRADED**: Component is operational but performance is degraded
- **UNKNOWN**: Component health cannot be determined

#### 2. Health Details (Individual Components)

**GitHub Health**: `/actuator/health/github`
```json
{
  "status": "UP",
  "details": {
    "rateLimit": {
      "limit": 5000,
      "remaining": 4850,
      "reset": "2024-01-28T11:00:00Z"
    },
    "organization": "Cantara",
    "lastApiCall": "2024-01-28T10:45:32Z"
  }
}
```

**Cache Health**: `/actuator/health/cache`
```json
{
  "status": "UP",
  "details": {
    "cacheManager": "caffeine",
    "caches": {
      "repositories": {
        "size": 42,
        "hitRate": 0.85,
        "evictions": 12
      },
      "commits": {
        "size": 156,
        "hitRate": 0.92,
        "evictions": 45
      }
    }
  }
}
```

**Executor Health**: `/actuator/health/executor`
```json
{
  "status": "UP",
  "details": {
    "activeThreads": 5,
    "poolSize": 25,
    "queueSize": 0,
    "utilization": 0.20,
    "completedTasks": 1234
  }
}
```

#### 3. Application Info

**URL**: `/actuator/info`

Shows application metadata, runtime information, and configuration.

**Response Example**:
```json
{
  "app": {
    "name": "Source Code Portal",
    "version": "0.10.17-SNAPSHOT",
    "description": "GitHub organization dashboard"
  },
  "build": {
    "version": "0.10.17-SNAPSHOT",
    "artifact": "source-code-portal",
    "group": "no.cantara.docsite"
  },
  "java": {
    "version": "21.0.1",
    "vendor": "Eclipse Adoptium",
    "runtime": {
      "name": "OpenJDK Runtime Environment",
      "version": "21.0.1+12-LTS"
    },
    "jvm": {
      "name": "OpenJDK 64-Bit Server VM",
      "vendor": "Eclipse Adoptium",
      "version": "21.0.1+12-LTS"
    }
  },
  "os": {
    "name": "Linux",
    "version": "5.15.0-89-generic",
    "arch": "amd64"
  },
  "git": {
    "branch": "master",
    "commit": {
      "id": "abc123...",
      "time": "2024-01-28T09:00:00Z"
    }
  }
}
```

#### 4. Metrics

**URL**: `/actuator/metrics`

Lists all available metrics.

**Response Example**:
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "jvm.threads.live",
    "http.server.requests",
    "cache.gets",
    "cache.puts",
    "cache.evictions"
  ]
}
```

**Individual Metric**: `/actuator/metrics/{metricName}`

Example: `/actuator/metrics/jvm.memory.used`
```json
{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 524288000
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    }
  ]
}
```

#### 5. Prometheus Metrics

**URL**: `/actuator/prometheus`

Exposes metrics in Prometheus format for scraping.

**Response Example**:
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space"} 1.048576E8
jvm_memory_used_bytes{area="heap",id="G1 Old Gen"} 4.194304E8

# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",uri="/dashboard",status="200"} 42
http_server_requests_seconds_sum{method="GET",uri="/dashboard",status="200"} 1.25

# HELP cache_gets_total Cache gets
# TYPE cache_gets_total counter
cache_gets_total{cache="repositories",result="hit"} 850
cache_gets_total{cache="repositories",result="miss"} 150
```

#### 6. Caches

**URL**: `/actuator/caches`

Shows cache manager and cache details.

**Response Example**:
```json
{
  "cacheManagers": {
    "cacheManager": {
      "caches": {
        "repositories": {
          "target": "com.github.benmanes.caffeine.cache.BoundedLocalCache"
        },
        "commits": {
          "target": "com.github.benmanes.caffeine.cache.BoundedLocalCache"
        }
      }
    }
  }
}
```

#### 7. Scheduled Tasks

**URL**: `/actuator/scheduledtasks`

Shows scheduled tasks and their intervals.

**Response Example**:
```json
{
  "cron": [],
  "fixedDelay": [],
  "fixedRate": [
    {
      "runnable": {
        "target": "no.cantara.docsite.fetch.ScheduledFetchData.fetchRepositories"
      },
      "initialDelay": 0,
      "interval": 3600000
    }
  ]
}
```

## Custom Health Indicators

SCP provides custom health indicators for monitoring critical components.

### 1. GitHub Health Indicator

**Class**: `no.cantara.docsite.actuator.GitHubHealthIndicator`

**Monitors**:
- GitHub API connectivity
- API rate limit status
- Organization accessibility

**Health Status Logic**:
- **UP**: Rate limit remaining > 10%, API accessible
- **DEGRADED**: Rate limit remaining < 10% but > 0%
- **DOWN**: Rate limit exhausted or API unreachable

**Details Provided**:
```json
{
  "rateLimit": {
    "limit": 5000,
    "remaining": 4850,
    "reset": "2024-01-28T11:00:00Z"
  },
  "organization": "Cantara",
  "lastApiCall": "2024-01-28T10:45:32Z"
}
```

**Use Cases**:
- Alert when rate limit is low
- Detect GitHub API outages
- Monitor API usage patterns

### 2. Cache Health Indicator

**Class**: `no.cantara.docsite.actuator.CacheHealthIndicator`

**Monitors**:
- Cache manager status
- Cache population (number of entries)
- Cache hit rate
- Cache evictions

**Health Status Logic**:
- **UP**: All caches operational, hit rate > 70%
- **DEGRADED**: Hit rate < 70% or high eviction rate
- **DOWN**: Cache manager closed or unavailable

**Details Provided**:
```json
{
  "cacheManager": "caffeine",
  "caches": {
    "repositories": {
      "size": 42,
      "hitRate": 0.85,
      "evictions": 12,
      "hitCount": 850,
      "missCount": 150,
      "loadSuccessCount": 100,
      "loadFailureCount": 0
    }
  }
}
```

**Use Cases**:
- Detect cache thrashing (high eviction rate)
- Monitor cache effectiveness (hit rate)
- Identify memory issues (cache size growing indefinitely)

### 3. Executor Health Indicator

**Class**: `no.cantara.docsite.actuator.ExecutorHealthIndicator`

**Monitors**:
- Thread pool utilization
- Active threads
- Queue size
- Completed tasks

**Health Status Logic**:
- **UP**: Utilization < 90%, queue size reasonable
- **DEGRADED**: Utilization > 90% or large queue backlog
- **DOWN**: Executor terminated or all threads busy for extended period

**Details Provided**:
```json
{
  "activeThreads": 5,
  "poolSize": 25,
  "queueSize": 0,
  "utilization": 0.20,
  "completedTasks": 1234,
  "largestPoolSize": 25
}
```

**Use Cases**:
- Detect thread pool exhaustion
- Monitor async task performance
- Identify resource bottlenecks

## Prometheus Integration

SCP exposes metrics in Prometheus format for integration with monitoring systems.

### Setup Prometheus Scraping

**prometheus.yml**:
```yaml
scrape_configs:
  - job_name: 'source-code-portal'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

### Key Metrics

#### JVM Metrics

- `jvm.memory.used` - JVM memory usage
- `jvm.memory.max` - Maximum JVM memory
- `jvm.threads.live` - Number of live threads
- `jvm.gc.pause` - Garbage collection pause time

#### HTTP Metrics

- `http.server.requests` - HTTP request count and duration
- `http.server.requests.max` - Maximum request duration

#### Cache Metrics

- `cache.gets` - Cache get operations (hit/miss)
- `cache.puts` - Cache put operations
- `cache.evictions` - Cache evictions

#### Custom Metrics

- `github.api.calls` - GitHub API call count
- `github.rate.limit.remaining` - GitHub rate limit remaining
- `webhook.events.received` - Webhook events received

### Grafana Dashboard

Create a Grafana dashboard to visualize SCP metrics:

**Example Dashboard Panels**:
1. **GitHub Rate Limit**: Gauge showing remaining API calls
2. **Cache Hit Rate**: Time series showing cache effectiveness
3. **HTTP Request Rate**: Requests per second
4. **JVM Memory**: Memory usage over time
5. **Thread Pool Utilization**: Percentage of threads in use
6. **Response Time**: P50, P95, P99 latency

## Logging

SCP uses SLF4J with Logback for structured logging.

### Log Configuration

**Location**: `src/main/resources/logback.xml` (or `logback-spring.xml`)

**Default Configuration**:
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### Log Levels

Set log levels in `application.yml`:

```yaml
logging:
  level:
    root: INFO
    no.cantara.docsite: DEBUG
    org.springframework: WARN
    com.github.benmanes.caffeine: DEBUG
```

Or via environment variables:

```bash
export LOGGING_LEVEL_NO_CANTARA_DOCSITE=DEBUG
```

### Log Categories

**Application Logs**:
- `no.cantara.docsite.controller` - Controller requests/responses
- `no.cantara.docsite.fetch` - Data fetching operations
- `no.cantara.docsite.cache` - Cache operations
- `no.cantara.docsite.commands` - External API calls

**Framework Logs**:
- `org.springframework.web` - Spring MVC
- `org.springframework.cache` - Spring Cache
- `com.github.benmanes.caffeine` - Caffeine cache

### Log Examples

**Controller Request**:
```
2024-01-28 10:30:15 [http-nio-9090-exec-1] INFO  DashboardWebController - Received request for dashboard
2024-01-28 10:30:15 [http-nio-9090-exec-1] DEBUG DashboardWebController - Fetching repository groups
2024-01-28 10:30:15 [http-nio-9090-exec-1] INFO  DashboardWebController - Dashboard request completed in 45ms
```

**GitHub API Call**:
```
2024-01-28 10:30:20 [scheduled-task-1] DEBUG GetGitHubCommand - Executing GitHub API call: GET /orgs/Cantara/repos
2024-01-28 10:30:21 [scheduled-task-1] INFO  GetGitHubCommand - GitHub API call successful, rate limit remaining: 4850/5000
```

**Cache Operation**:
```
2024-01-28 10:30:25 [scheduled-task-2] DEBUG CacheStore - Cache miss for key: repositories:Cantara
2024-01-28 10:30:25 [scheduled-task-2] INFO  CacheStore - Loaded 42 repositories into cache
2024-01-28 10:30:26 [http-nio-9090-exec-2] DEBUG CacheStore - Cache hit for key: repositories:Cantara
```

**Webhook Event**:
```
2024-01-28 10:30:30 [http-nio-9090-exec-3] INFO  GitHubWebhookRestController - Received webhook push event for Whydah-UserAdminService
2024-01-28 10:30:30 [http-nio-9090-exec-3] INFO  GitHubWebhookRestController - Invalidated commit cache for Whydah-UserAdminService
2024-01-28 10:30:30 [http-nio-9090-exec-3] INFO  GitHubWebhookRestController - Webhook processing completed in 15ms
```

## Monitoring Best Practices

### 1. Set Up Alerts

Configure alerts for critical conditions:

**GitHub Rate Limit Alert**:
- Trigger: Rate limit remaining < 10%
- Action: Send Slack/email notification

**Cache Health Alert**:
- Trigger: Cache hit rate < 70% for 10 minutes
- Action: Investigate cache configuration

**Executor Health Alert**:
- Trigger: Thread pool utilization > 90% for 5 minutes
- Action: Scale up or optimize async tasks

### 2. Monitor Key Metrics

**Essential Metrics to Monitor**:
- GitHub rate limit remaining
- Cache hit rate
- HTTP request rate and latency
- JVM memory usage
- Thread pool utilization

### 3. Regular Health Checks

Configure external health checks:

**Kubernetes Liveness Probe**:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 9090
  initialDelaySeconds: 30
  periodSeconds: 10
```

**Kubernetes Readiness Probe**:
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 9090
  initialDelaySeconds: 10
  periodSeconds: 5
```

### 4. Log Analysis

Use log aggregation tools to analyze logs:

**Tools**:
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- **Splunk**
- **Datadog**
- **Grafana Loki**

**Key Queries**:
- Error rate per endpoint
- Slowest API calls
- Most frequent cache misses
- Webhook processing time

## Troubleshooting

### High Response Time

**Symptoms**: Dashboard loads slowly, `/actuator/health` shows high latency.

**Investigation**:
1. Check cache hit rate: `/actuator/health/cache`
2. Check thread pool utilization: `/actuator/health/executor`
3. Review slow queries in logs: `grep "completed in" logs/application.log | sort`

**Solutions**:
- Increase cache TTL to reduce API calls
- Scale up thread pool size
- Optimize slow queries

### GitHub Rate Limit Exhausted

**Symptoms**: Dashboard shows stale data, `/actuator/health/github` shows DOWN.

**Investigation**:
1. Check rate limit: `/actuator/health/github`
2. Review API call frequency in logs

**Solutions**:
- Increase cache TTL to reduce API calls
- Configure GitHub webhooks to eliminate polling
- Wait for rate limit reset

### Cache Not Populating

**Symptoms**: Cache hit rate is 0%, `/actuator/health/cache` shows empty caches.

**Investigation**:
1. Check cache configuration: `/actuator/caches`
2. Review cache loading logs: `grep "Loaded" logs/application.log`

**Solutions**:
- Verify scheduled tasks are running: `/actuator/scheduledtasks`
- Check GitHub API connectivity
- Restart application

## Related Documentation

- [Dashboard](dashboard.md) - Main dashboard features
- [Integrations](integrations.md) - External service integrations
- [Webhooks](webhooks.md) - GitHub webhook setup
- [Operations: Monitoring](../operations/monitoring.md) - Production monitoring guide
- [Architecture: Caching](../architecture/caching.md) - Cache architecture
