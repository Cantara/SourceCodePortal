# Monitoring Guide

This guide covers monitoring Source Code Portal using Spring Boot Actuator, Prometheus, and Grafana.

## Table of Contents

- [Spring Boot Actuator](#spring-boot-actuator)
- [Custom Health Indicators](#custom-health-indicators)
- [Prometheus Integration](#prometheus-integration)
- [Grafana Dashboards](#grafana-dashboards)
- [Log Aggregation](#log-aggregation)
- [Alerting](#alerting)
- [Metrics Reference](#metrics-reference)

## Spring Boot Actuator

Spring Boot Actuator provides production-ready features for monitoring and managing the application.

### Enabling Actuator

Actuator is enabled by default in Spring Boot mode. Configure in `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,caches,scheduledtasks
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

### Available Endpoints

#### Health Endpoints

**Overall Health**: `/actuator/health`

Returns overall application health status.

```bash
curl http://localhost:9090/actuator/health
```

Response:

```json
{
  "status": "UP",
  "components": {
    "github": {
      "status": "UP",
      "details": {
        "rateLimit": {
          "limit": 5000,
          "remaining": 4523,
          "reset": "2026-01-28T15:30:00Z"
        }
      }
    },
    "cache": {
      "status": "UP",
      "details": {
        "caches": {
          "repositories": {
            "size": 42,
            "hitRate": 0.87
          }
        }
      }
    },
    "executor": {
      "status": "UP",
      "details": {
        "activeThreads": 5,
        "queueSize": 0
      }
    }
  }
}
```

**GitHub Health**: `/actuator/health/github`

Checks GitHub API connectivity and rate limits.

```bash
curl http://localhost:9090/actuator/health/github
```

Response:

```json
{
  "status": "UP",
  "details": {
    "rateLimit": {
      "limit": 5000,
      "remaining": 4523,
      "reset": "2026-01-28T15:30:00Z",
      "percentageRemaining": 90.46
    }
  }
}
```

Status levels:

- `UP`: Healthy, rate limit > 10%
- `DEGRADED`: Warning, rate limit between 5-10%
- `DOWN`: Critical, rate limit < 5% or API unreachable

**Cache Health**: `/actuator/health/cache`

Monitors cache health and statistics.

```bash
curl http://localhost:9090/actuator/health/cache
```

Response:

```json
{
  "status": "UP",
  "details": {
    "caches": {
      "repositories": {
        "size": 42,
        "hitRate": 0.87,
        "missRate": 0.13,
        "evictionCount": 0
      },
      "commits": {
        "size": 156,
        "hitRate": 0.92,
        "missRate": 0.08,
        "evictionCount": 2
      }
    }
  }
}
```

**Executor Health**: `/actuator/health/executor`

Monitors thread pool health.

```bash
curl http://localhost:9090/actuator/health/executor
```

Response:

```json
{
  "status": "UP",
  "details": {
    "activeThreads": 5,
    "poolSize": 10,
    "queueSize": 0,
    "completedTaskCount": 1523
  }
}
```

**Liveness Probe**: `/actuator/health/liveness`

Kubernetes liveness probe endpoint. Returns `UP` if application is running.

```bash
curl http://localhost:9090/actuator/health/liveness
```

**Readiness Probe**: `/actuator/health/readiness`

Kubernetes readiness probe endpoint. Returns `UP` if application can accept traffic.

```bash
curl http://localhost:9090/actuator/health/readiness
```

#### Info Endpoint

**Application Info**: `/actuator/info`

Returns application metadata.

```bash
curl http://localhost:9090/actuator/info
```

Response:

```json
{
  "app": {
    "name": "Source Code Portal",
    "version": "0.10.17-SNAPSHOT",
    "description": "GitHub organization dashboard"
  },
  "build": {
    "artifact": "source-code-portal",
    "group": "no.cantara.docsite",
    "version": "0.10.17-SNAPSHOT"
  },
  "runtime": {
    "javaVersion": "21.0.1",
    "javaVendor": "Eclipse Adoptium",
    "osName": "Linux",
    "osVersion": "6.17.0"
  },
  "configuration": {
    "githubOrganization": "Cantara",
    "repositoryCount": 42,
    "groupCount": 5,
    "cacheEnabled": true
  }
}
```

#### Metrics Endpoint

**All Metrics**: `/actuator/metrics`

Lists all available metrics.

```bash
curl http://localhost:9090/actuator/metrics
```

Response:

```json
{
  "names": [
    "jvm.memory.used",
    "jvm.threads.live",
    "cache.size",
    "cache.gets",
    "http.server.requests",
    "system.cpu.usage"
  ]
}
```

**Specific Metric**: `/actuator/metrics/{metricName}`

```bash
curl http://localhost:9090/actuator/metrics/jvm.memory.used
```

Response:

```json
{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 536870912
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

#### Cache Endpoint

**Cache Details**: `/actuator/caches`

Shows cache manager details.

```bash
curl http://localhost:9090/actuator/caches
```

Response:

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

#### Scheduled Tasks Endpoint

**Scheduled Tasks**: `/actuator/scheduledtasks`

Lists all scheduled tasks.

```bash
curl http://localhost:9090/actuator/scheduledtasks
```

Response:

```json
{
  "cron": [],
  "fixedDelay": [],
  "fixedRate": [
    {
      "runnable": {
        "target": "no.cantara.docsite.fetch.ScheduledFetchData.fetchRepositories"
      },
      "initialDelay": 60000,
      "interval": 300000
    }
  ]
}
```

## Custom Health Indicators

Source Code Portal includes three custom health indicators:

### GitHubHealthIndicator

Monitors GitHub API connectivity and rate limits.

**Location**: `no.cantara.docsite.actuator.GitHubHealthIndicator`

**Checks**:

- GitHub API reachability
- Rate limit remaining
- Rate limit reset time

**Status Logic**:

```java
if (remaining < limit * 0.05) {
    return Health.down()
        .withDetail("message", "GitHub rate limit critically low")
        .build();
} else if (remaining < limit * 0.10) {
    return Health.status("DEGRADED")
        .withDetail("message", "GitHub rate limit low")
        .build();
} else {
    return Health.up().build();
}
```

### CacheHealthIndicator

Monitors cache performance and health.

**Location**: `no.cantara.docsite.actuator.CacheHealthIndicator`

**Checks**:

- Cache size
- Hit rate
- Miss rate
- Eviction count

**Status Logic**:

```java
if (hitRate < 0.50) {
    return Health.status("DEGRADED")
        .withDetail("message", "Cache hit rate below 50%")
        .build();
} else {
    return Health.up().build();
}
```

### ExecutorHealthIndicator

Monitors thread pool health.

**Location**: `no.cantara.docsite.actuator.ExecutorHealthIndicator`

**Checks**:

- Active thread count
- Pool size
- Queue size
- Completed task count

**Status Logic**:

```java
if (queueSize > 100) {
    return Health.down()
        .withDetail("message", "Executor queue size too high")
        .build();
} else if (activeThreads >= poolSize) {
    return Health.status("DEGRADED")
        .withDetail("message", "All threads busy")
        .build();
} else {
    return Health.up().build();
}
```

## Prometheus Integration

### Enabling Prometheus

Prometheus metrics are automatically exposed at `/actuator/prometheus`.

```bash
curl http://localhost:9090/actuator/prometheus
```

Output (Prometheus format):

```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 5.36870912E8

# HELP cache_size The number of entries in the cache
# TYPE cache_size gauge
cache_size{cache="repositories",cacheManager="cacheManager",} 42.0

# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",outcome="SUCCESS",status="200",uri="/dashboard",} 1523.0
```

### Prometheus Configuration

Create `prometheus.yml`:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'sourcecodeportal'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:9090']
        labels:
          application: 'source-code-portal'
          environment: 'production'
```

Run Prometheus:

```bash
docker run -d \
  --name prometheus \
  -p 9091:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

Access Prometheus UI: `http://localhost:9091`

### Key Metrics to Monitor

#### Application Health

- `health_status{component="github"}` - GitHub health (1=UP, 0=DOWN)
- `health_status{component="cache"}` - Cache health
- `health_status{component="executor"}` - Executor health

#### Performance

- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Total request time
- `cache_gets_total{result="hit"}` - Cache hits
- `cache_gets_total{result="miss"}` - Cache misses

#### Resource Usage

- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_threads_live` - Active threads
- `system_cpu_usage` - CPU usage
- `process_cpu_usage` - Process CPU usage

#### GitHub API

- `github_rate_limit_remaining` - Remaining API calls
- `github_rate_limit_limit` - Total API calls allowed
- `github_api_calls_total` - Total API calls made

## Grafana Dashboards

### Setting Up Grafana

```bash
docker run -d \
  --name grafana \
  -p 3000:3000 \
  grafana/grafana
```

Access Grafana: `http://localhost:3000` (admin/admin)

### Adding Prometheus Data Source

1. Navigate to **Configuration → Data Sources**
2. Click **Add data source**
3. Select **Prometheus**
4. Set URL: `http://prometheus:9091`
5. Click **Save & Test**

### Dashboard JSON

Create `sourcecodeportal-dashboard.json`:

```json
{
  "dashboard": {
    "title": "Source Code Portal",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ],
        "type": "graph"
      },
      {
        "title": "Response Time (95th percentile)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))"
          }
        ],
        "type": "graph"
      },
      {
        "title": "Cache Hit Rate",
        "targets": [
          {
            "expr": "rate(cache_gets_total{result=\"hit\"}[5m]) / rate(cache_gets_total[5m])"
          }
        ],
        "type": "graph"
      },
      {
        "title": "GitHub Rate Limit",
        "targets": [
          {
            "expr": "github_rate_limit_remaining"
          }
        ],
        "type": "graph"
      },
      {
        "title": "JVM Memory Usage",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area=\"heap\"}"
          }
        ],
        "type": "graph"
      }
    ]
  }
}
```

Import dashboard:

1. Navigate to **Dashboards → Import**
2. Upload `sourcecodeportal-dashboard.json`
3. Select Prometheus data source
4. Click **Import**

### Recommended Panels

1. **Request Rate**: `rate(http_server_requests_seconds_count[5m])`
2. **Response Time P95**: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
3. **Cache Hit Rate**: `rate(cache_gets_total{result="hit"}[5m]) / rate(cache_gets_total[5m])`
4. **GitHub Rate Limit**: `github_rate_limit_remaining`
5. **JVM Memory**: `jvm_memory_used_bytes{area="heap"}`
6. **Active Threads**: `jvm_threads_live`
7. **CPU Usage**: `system_cpu_usage`
8. **Error Rate**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m])`

## Log Aggregation

### Logback Configuration

Configure in `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- Console appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- File appender with rolling -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- JSON appender for log aggregation -->
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>

    <logger name="no.cantara" level="DEBUG"/>
    <logger name="org.springframework" level="WARN"/>
</configuration>
```

### ELK Stack Integration

#### Filebeat Configuration

Create `filebeat.yml`:

```yaml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /var/log/sourcecodeportal/application.log
    fields:
      application: sourcecodeportal
      environment: production

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "sourcecodeportal-%{+yyyy.MM.dd}"

setup.kibana:
  host: "kibana:5601"
```

#### Docker Compose with ELK

```yaml
version: '3.8'

services:
  sourcecodeportal:
    image: cantara/sourcecodeportal
    volumes:
      - scp-logs:/home/sourcecodeportal/logs

  filebeat:
    image: docker.elastic.co/beats/filebeat:8.11.0
    volumes:
      - scp-logs:/var/log/sourcecodeportal:ro
      - ./filebeat.yml:/usr/share/filebeat/filebeat.yml:ro
    depends_on:
      - elasticsearch

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch

volumes:
  scp-logs:
```

## Alerting

### Prometheus Alertmanager

Create `alert.rules.yml`:

```yaml
groups:
  - name: sourcecodeportal
    interval: 30s
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} requests/sec"

      # GitHub rate limit low
      - alert: GitHubRateLimitLow
        expr: github_rate_limit_remaining < 500
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "GitHub rate limit low"
          description: "Only {{ $value }} API calls remaining"

      # Cache hit rate low
      - alert: CacheHitRateLow
        expr: rate(cache_gets_total{result="hit"}[5m]) / rate(cache_gets_total[5m]) < 0.5
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Cache hit rate below 50%"

      # High memory usage
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
          description: "Memory usage is {{ $value | humanizePercentage }}"

      # Application down
      - alert: ApplicationDown
        expr: up{job="sourcecodeportal"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Application is down"
```

### Alertmanager Configuration

Create `alertmanager.yml`:

```yaml
global:
  slack_api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'

route:
  group_by: ['alertname']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h
  receiver: 'slack-notifications'

receivers:
  - name: 'slack-notifications'
    slack_configs:
      - channel: '#alerts'
        text: '{{ range .Alerts }}{{ .Annotations.summary }}\n{{ .Annotations.description }}\n{{ end }}'
```

## Metrics Reference

### JVM Metrics

- `jvm.memory.used` - Memory usage (heap/nonheap)
- `jvm.memory.max` - Maximum memory
- `jvm.threads.live` - Live threads
- `jvm.threads.daemon` - Daemon threads
- `jvm.gc.pause` - Garbage collection pause time

### HTTP Metrics

- `http.server.requests` - Request count, duration
- `http.server.requests.active` - Active requests

### Cache Metrics

- `cache.size` - Cache size
- `cache.gets` - Cache gets (hit/miss)
- `cache.puts` - Cache puts
- `cache.evictions` - Cache evictions

### System Metrics

- `system.cpu.usage` - System CPU usage
- `process.cpu.usage` - Process CPU usage
- `system.load.average.1m` - System load average

### Custom Metrics

- `github.rate.limit.remaining` - GitHub API rate limit
- `github.api.calls.total` - Total GitHub API calls
- `repository.count` - Number of repositories monitored

## Best Practices

1. **Monitor health endpoints regularly** (every 30 seconds)
2. **Set up alerts for critical metrics** (error rate, rate limit)
3. **Use Grafana dashboards** for visualization
4. **Aggregate logs centrally** (ELK stack)
5. **Monitor GitHub rate limit** closely
6. **Track cache performance** to optimize TTL
7. **Set up on-call rotation** for alerts
8. **Review metrics trends** weekly
9. **Test alerts** to ensure they fire correctly
10. **Document runbooks** for common issues

## Next Steps

- [Troubleshooting Guide](troubleshooting.md) - Resolve common issues
- [Deployment Guide](deployment.md) - Production deployment
- [Docker Guide](docker.md) - Container deployment
