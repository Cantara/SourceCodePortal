# Troubleshooting Guide

This guide helps diagnose and resolve common issues with Source Code Portal.

## Table of Contents

- [Build Issues](#build-issues)
- [Configuration Issues](#configuration-issues)
- [GitHub API Issues](#github-api-issues)
- [Webhook Issues](#webhook-issues)
- [Performance Issues](#performance-issues)
- [Memory Issues](#memory-issues)
- [Cache Issues](#cache-issues)
- [Deployment Issues](#deployment-issues)
- [Network Issues](#network-issues)
- [Diagnostic Tools](#diagnostic-tools)

## Build Issues

### Maven Build Fails with "Cannot find symbol"

**Symptom**:
```
[ERROR] cannot find symbol: method findElementById(String)
```

**Cause**: API changes in dependencies (e.g., Selenium 4 removed convenience methods)

**Solution**:

```java
// Wrong (Selenium 3)
driver.findElementById("login_field");

// Correct (Selenium 4)
driver.findElement(By.id("login_field"));
```

**Reference**: [LEARNINGS.md - Selenium 4 API Changes](../../LEARNINGS.md#2-selenium-4-api-changes)

---

### Maven Build Fails with "Could not find artifact"

**Symptom**:
```
[ERROR] Could not find artifact com.atlassian.commonmark:commonmark:jar:0.22.0
```

**Cause**: Dependency moved to different group ID

**Solution**:

Check if the library changed group ID. Example for Commonmark:

```xml
<!-- Wrong -->
<groupId>com.atlassian.commonmark</groupId>

<!-- Correct -->
<groupId>org.commonmark</groupId>
```

**Reference**: [LEARNINGS.md - Commonmark Group ID Change](../../LEARNINGS.md#1-commonmark-group-id-change)

---

### Tests Fail After Migration to JUnit 5

**Symptom**: Assertions pass when they should fail, or vice versa

**Cause**: TestNG and JUnit 5 have reversed parameter order

**Solution**:

```java
// TestNG (actual, expected)
Assert.assertEquals(actualValue, expectedValue);

// JUnit 5 (expected, actual) - REVERSED!
Assertions.assertEquals(expectedValue, actualValue);
```

**Critical**: This is a logic error, not a compilation error. Review all assertions carefully.

**Reference**: [LEARNINGS.md - Assertion Parameter Order](../../LEARNINGS.md#3-testng--junit-5-assertion-parameter-order)

---

### Maven Package Fails with NoSuchMethodError

**Symptom**:
```
[ERROR] NoSuchMethodError: 'org.codehaus.plexus.archiver.util.DefaultFileSet...'
```

**Cause**: Maven plugin version incompatibility

**Workaround**:

```bash
# Don't use this (fails)
mvn package

# Use Spring Boot plugin instead (works)
mvn spring-boot:run
# or
mvn clean install -DskipTests
```

**Reference**: [LEARNINGS.md - Maven Plugin Compatibility](../../LEARNINGS.md#5-maven-plugin-compatibility)

---

## Configuration Issues

### Application Fails to Start: "GitHub access token not configured"

**Symptom**:
```
ERROR: GitHub access token not configured. Set SCP_GITHUB_ACCESS_TOKEN environment variable.
```

**Cause**: Missing GitHub authentication

**Solution**:

Option 1: Environment variable (recommended)
```bash
export SCP_GITHUB_ACCESS_TOKEN=ghp_your_token
java -jar source-code-portal.jar
```

Option 2: Security properties file
```properties
# security.properties
github.client.accessToken=ghp_your_token
```

Option 3: Generate token
```bash
# Using Docker
docker run -it \
  -e SCP_github.oauth2.client.clientId=CLIENT_ID \
  -e SCP_github.oauth2.client.clientSecret=CLIENT_SECRET \
  cantara/sourcecodeportal /github-access-token
```

**Reference**: [Configuration Guide](../getting-started/configuration.md#github-authentication)

---

### Configuration File Not Found

**Symptom**:
```
WARN: Configuration file not found: config.json. Using defaults.
```

**Cause**: Missing repository configuration file

**Solution**:

1. Create `config.json`:
```json
{
  "githubOrganizationName": "YourOrg",
  "groups": [
    {
      "groupId": "core",
      "display-name": "Core Services",
      "description": "Core application services",
      "repos": ["repo1", "repo2"]
    }
  ]
}
```

2. Place in one of these locations:
   - `src/main/resources/conf/config.json` (built-in)
   - `./config_override/conf/config.json` (runtime override)
   - `/home/sourcecodeportal/config_override/conf/config.json` (Docker)

**Reference**: [Configuration Guide](../getting-started/configuration.md#repository-configuration)

---

### Environment Variables Not Loading

**Symptom**: Configuration from environment variables is ignored

**Cause**: Incorrect prefix or format

**Solution**:

Use `SCP_` prefix and correct format:

```bash
# Wrong
export GITHUB_ACCESS_TOKEN=token

# Correct
export SCP_GITHUB_ACCESS_TOKEN=token

# For nested properties, use underscores or dots
export SCP_GITHUB_ORGANIZATION=Cantara
export SCP_github.organization=Cantara  # Both work
```

**Reference**: [Configuration Guide](../getting-started/configuration.md#environment-variables)

---

## GitHub API Issues

### GitHub Rate Limit Exceeded

**Symptom**:
```
ERROR: GitHub API rate limit exceeded. Reset at: 2026-01-28T15:30:00Z
```

**Cause**: Too many API calls, exceeding GitHub's rate limit

**GitHub Rate Limits**:
- Unauthenticated: 60 requests/hour
- Authenticated: 5000 requests/hour
- GitHub Enterprise: 15000 requests/hour

**Immediate Solutions**:

1. Check rate limit status:
```bash
curl http://localhost:9090/actuator/health/github
```

2. Wait for rate limit reset (shown in error message)

3. Use authenticated requests (much higher limit):
```bash
export SCP_GITHUB_ACCESS_TOKEN=ghp_your_token
```

**Long-term Solutions**:

1. Increase cache TTL to reduce API calls:
```yaml
cache:
  ttl: 60  # Cache for 60 minutes instead of 30
```

2. Reduce scheduled task frequency:
```yaml
fetch:
  schedule:
    repositories: 600000  # 10 minutes instead of 5
```

3. Use webhooks for real-time updates instead of polling:
```yaml
github:
  webhook:
    enabled: true
    secret: your_webhook_secret
```

**Monitoring**:

Set up alerts for rate limit:
```yaml
# Prometheus alert
- alert: GitHubRateLimitLow
  expr: github_rate_limit_remaining < 500
  for: 5m
```

**Reference**: [Monitoring Guide](monitoring.md#github-api)

---

### GitHub API Unreachable

**Symptom**:
```
ERROR: Failed to connect to GitHub API: Connection refused
```

**Cause**: Network connectivity issue or firewall blocking

**Diagnosis**:

1. Test GitHub API connectivity:
```bash
curl -H "Authorization: token ghp_your_token" https://api.github.com/rate_limit
```

2. Check DNS resolution:
```bash
nslookup api.github.com
```

3. Check firewall rules:
```bash
telnet api.github.com 443
```

**Solutions**:

1. Configure proxy if behind firewall:
```yaml
spring:
  proxy:
    host: proxy.example.com
    port: 8080
```

2. Check network connectivity
3. Verify GitHub API status: https://www.githubstatus.com/

---

### GitHub Authentication Fails

**Symptom**:
```
ERROR: GitHub authentication failed: 401 Unauthorized
```

**Cause**: Invalid or expired access token

**Solutions**:

1. Verify token is valid:
```bash
curl -H "Authorization: token ghp_your_token" https://api.github.com/user
```

2. Check token has required scopes:
   - `repo` - Full repository access
   - `read:org` - Read organization data
   - `read:user` - Read user profile

3. Generate new token: https://github.com/settings/tokens

4. Verify token is not expired (check GitHub settings)

---

## Webhook Issues

### Webhook Delivery Fails

**Symptom**: GitHub shows webhook delivery failed (red X)

**Diagnosis**:

1. Check webhook endpoint is accessible:
```bash
curl https://your-server.com/github/webhook
```

2. View webhook delivery in GitHub:
   - Go to repository → Settings → Webhooks
   - Click on webhook
   - View "Recent Deliveries"

**Common Causes**:

1. **Server not accessible from internet**
   - Solution: Use ngrok for local development
   ```bash
   ngrok http 9090
   # Use ngrok URL: https://xxxxx.ngrok.io/github/webhook
   ```

2. **SSL certificate invalid**
   - GitHub requires valid SSL certificate
   - Solution: Use Let's Encrypt or disable SSL verification (dev only)

3. **Webhook secret mismatch**
   - Solution: Verify secret matches in both places
   ```bash
   export SCP_GITHUB_WEBHOOK_SECRET=your_secret
   # Same secret in GitHub webhook settings
   ```

---

### Webhook Authenticated but Cache Not Updated

**Symptom**: Webhook delivered successfully but dashboard not updating

**Diagnosis**:

1. Check application logs:
```bash
tail -f logs/application.log | grep "webhook"
```

2. Check webhook is calling cache eviction:
```java
// Should see this in logs
INFO: Webhook received: push event for repo xyz
INFO: Evicting cache for repository xyz
```

**Solutions**:

1. Verify webhook controller is processing events:
```bash
curl -X POST http://localhost:9090/github/webhook \
  -H "Content-Type: application/json" \
  -H "X-Hub-Signature-256: sha256=..." \
  -d @webhook-payload.json
```

2. Manually trigger cache refresh:
```bash
# Via actuator
curl -X DELETE http://localhost:9090/actuator/caches/repositories
```

---

## Performance Issues

### Slow Dashboard Load Times

**Symptom**: Dashboard takes > 5 seconds to load

**Diagnosis**:

1. Check health endpoint:
```bash
curl http://localhost:9090/actuator/health
```

2. Check cache hit rate:
```bash
curl http://localhost:9090/actuator/health/cache
```

**Common Causes**:

1. **Cache not populated**
   - Solution: Wait for initial data fetch (60 seconds after startup)
   - Or manually trigger: Restart application

2. **Low cache hit rate**
   - Solution: Increase cache TTL
   ```yaml
   cache:
     ttl: 60  # Increase from 30 to 60 minutes
     max-size: 20000  # Increase cache size
   ```

3. **Too many repositories**
   - Solution: Use pagination or reduce repository count

4. **GitHub API slow**
   - Check GitHub status: https://www.githubstatus.com/
   - Solution: Increase timeouts
   ```yaml
   github:
     timeout: 120  # Increase from 75 to 120 seconds
   ```

---

### High CPU Usage

**Symptom**: CPU usage constantly above 80%

**Diagnosis**:

1. Check thread pool:
```bash
curl http://localhost:9090/actuator/health/executor
```

2. Check metrics:
```bash
curl http://localhost:9090/actuator/metrics/system.cpu.usage
```

**Common Causes**:

1. **Too many concurrent API calls**
   - Solution: Reduce bulkhead limit
   ```yaml
   resilience4j:
     bulkhead:
       instances:
         github:
           maxConcurrentCalls: 15  # Reduce from 25
   ```

2. **Scheduled tasks running too frequently**
   - Solution: Increase intervals
   ```yaml
   fetch:
     schedule:
       repositories: 600000  # 10 minutes instead of 5
   ```

3. **Inefficient code**
   - Use Java profiler (VisualVM, YourKit)
   - Identify hot spots

---

### High Response Times

**Symptom**: API endpoints taking > 2 seconds

**Diagnosis**:

1. Check response times:
```bash
curl -w "@curl-format.txt" http://localhost:9090/dashboard
```

Create `curl-format.txt`:
```
time_namelookup:  %{time_namelookup}\n
time_connect:  %{time_connect}\n
time_appconnect:  %{time_appconnect}\n
time_pretransfer:  %{time_pretransfer}\n
time_redirect:  %{time_redirect}\n
time_starttransfer:  %{time_starttransfer}\n
time_total:  %{time_total}\n
```

2. Check circuit breaker status:
```bash
curl http://localhost:9090/actuator/circuitbreakers
```

**Solutions**:

1. **Circuit breaker open**
   - Too many failures caused circuit breaker to open
   - Wait for circuit breaker to reset (60 seconds)
   - Fix underlying issue (GitHub API, network)

2. **External service slow**
   - Check GitHub, Jenkins, Snyk status
   - Increase timeouts or disable integration

3. **Enable virtual threads** (Java 21)
   ```yaml
   spring:
     threads:
       virtual:
         enabled: true
   ```

---

## Memory Issues

### OutOfMemoryError

**Symptom**:
```
java.lang.OutOfMemoryError: Java heap space
```

**Immediate Solution**:

1. Restart with more memory:
```bash
java -Xmx2g -Xms1g -jar source-code-portal.jar
```

**Diagnosis**:

1. Check memory usage:
```bash
curl http://localhost:9090/actuator/metrics/jvm.memory.used
```

2. Generate heap dump:
```bash
jmap -dump:format=b,file=heap.bin <PID>
```

3. Analyze with Eclipse MAT or VisualVM

**Common Causes**:

1. **Cache too large**
   - Solution: Reduce cache size
   ```yaml
   cache:
     max-size: 5000  # Reduce from 10000
   ```

2. **Memory leak**
   - Use heap dump analysis to identify
   - Check for unclosed resources (streams, connections)

3. **Too many threads**
   - Solution: Reduce thread pool size
   ```yaml
   executor:
     core-pool-size: 5
     max-pool-size: 10
   ```

---

### High Memory Usage

**Symptom**: Memory usage growing over time (not yet OOM)

**Diagnosis**:

1. Monitor memory trend:
```bash
watch -n 5 'curl -s http://localhost:9090/actuator/metrics/jvm.memory.used | jq'
```

2. Check garbage collection:
```bash
curl http://localhost:9090/actuator/metrics/jvm.gc.pause
```

**Solutions**:

1. **Tune garbage collector**:
```bash
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -jar source-code-portal.jar
```

2. **Enable heap dump on OOM**:
```bash
java -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/tmp/heap-dump.hprof \
     -jar source-code-portal.jar
```

3. **Reduce cache TTL** to allow more evictions

---

## Cache Issues

### Cache Not Working

**Symptom**: Cache hit rate is 0% or cache always misses

**Diagnosis**:

1. Check cache health:
```bash
curl http://localhost:9090/actuator/health/cache
```

2. Check cache configuration:
```bash
curl http://localhost:9090/actuator/caches
```

**Common Causes**:

1. **Cache disabled**
   - Solution: Enable cache
   ```yaml
   spring:
     cache:
       type: caffeine
   ```

2. **Cache keys changing**
   - Verify cache keys are consistent
   - Check `CacheKey`, `CacheRepositoryKey` implementations

3. **TTL too short**
   - Solution: Increase TTL
   ```yaml
   cache:
     ttl: 60  # Increase from 30
   ```

---

### Cache Eviction Too Frequent

**Symptom**: Cache evictions very high

**Diagnosis**:

Check eviction count:
```bash
curl http://localhost:9090/actuator/metrics/cache.evictions
```

**Causes**:

1. **Cache size too small**
   - Solution: Increase max size
   ```yaml
   cache:
     max-size: 20000
   ```

2. **Memory pressure**
   - Increase JVM heap size
   - Reduce other memory consumers

---

### Stale Cache Data

**Symptom**: Dashboard shows outdated information

**Solutions**:

1. **Manual cache clear**:
```bash
curl -X DELETE http://localhost:9090/actuator/caches/repositories
```

2. **Reduce TTL**:
```yaml
cache:
  ttl: 15  # Reduce from 30 to 15 minutes
```

3. **Enable webhooks** for real-time updates

4. **Force refresh** via API:
```bash
curl -X POST http://localhost:9090/api/refresh
```

---

## Deployment Issues

### Docker Container Won't Start

**Diagnosis**:

Check logs:
```bash
docker logs scp
docker logs -f scp  # Follow logs
```

**Common Causes**:

1. **Port already in use**:
```bash
# Check what's using port 9090
lsof -i :9090

# Use different port
docker run -p 8080:9090 cantara/sourcecodeportal
```

2. **Missing environment variables**:
```bash
docker run \
  -e SCP_GITHUB_ACCESS_TOKEN=token \
  cantara/sourcecodeportal
```

3. **Volume mount permission issues**:
```bash
# Fix permissions
chmod 644 config.json
docker run -v $(pwd)/config.json:/home/sourcecodeportal/config_override/conf/config.json:ro cantara/sourcecodeportal
```

**Reference**: [Docker Guide - Troubleshooting](docker.md#troubleshooting)

---

### Kubernetes Pod CrashLoopBackOff

**Diagnosis**:

```bash
# Check pod status
kubectl describe pod <pod-name> -n sourcecodeportal

# Check logs
kubectl logs <pod-name> -n sourcecodeportal --previous
```

**Common Causes**:

1. **Health check failing too early**:
```yaml
livenessProbe:
  initialDelaySeconds: 120  # Increase from 60
```

2. **Missing secrets**:
```bash
# Verify secrets exist
kubectl get secrets -n sourcecodeportal
kubectl describe secret scp-secrets -n sourcecodeportal
```

3. **Resource limits too low**:
```yaml
resources:
  limits:
    memory: "2Gi"  # Increase from 1Gi
```

---

## Network Issues

### Cannot Reach GitHub API

**Diagnosis**:

1. Test from within container:
```bash
docker exec scp curl https://api.github.com/rate_limit
```

2. Check DNS resolution:
```bash
docker exec scp nslookup api.github.com
```

**Solutions**:

1. **Configure DNS**:
```bash
docker run --dns 8.8.8.8 cantara/sourcecodeportal
```

2. **Configure proxy**:
```yaml
spring:
  http:
    proxy:
      host: proxy.example.com
      port: 8080
```

---

## Diagnostic Tools

### Health Check Script

Create `health-check.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:9090"

echo "=== Overall Health ==="
curl -s $BASE_URL/actuator/health | jq

echo -e "\n=== GitHub Health ==="
curl -s $BASE_URL/actuator/health/github | jq

echo -e "\n=== Cache Health ==="
curl -s $BASE_URL/actuator/health/cache | jq

echo -e "\n=== Executor Health ==="
curl -s $BASE_URL/actuator/health/executor | jq

echo -e "\n=== Memory Usage ==="
curl -s $BASE_URL/actuator/metrics/jvm.memory.used | jq

echo -e "\n=== Thread Count ==="
curl -s $BASE_URL/actuator/metrics/jvm.threads.live | jq
```

Run:
```bash
chmod +x health-check.sh
./health-check.sh
```

---

### Log Analysis

```bash
# Find errors
tail -1000 logs/application.log | grep ERROR

# Find GitHub API errors
grep "GitHub API" logs/application.log | grep ERROR

# Find slow queries (> 1 second)
grep "took [0-9]\{4,\}ms" logs/application.log

# Count error types
grep ERROR logs/application.log | cut -d: -f4 | sort | uniq -c | sort -rn
```

---

### Performance Profiling

```bash
# Enable JMX
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar source-code-portal.jar

# Connect with JConsole
jconsole localhost:9010

# Or use VisualVM
jvisualvm
```

---

## Getting Help

If you're still stuck:

1. **Check documentation**:
   - [Configuration Guide](../getting-started/configuration.md)
   - [Monitoring Guide](monitoring.md)
   - [Deployment Guide](deployment.md)

2. **Check logs**: `logs/application.log`

3. **Check health endpoints**: `/actuator/health`

4. **Review learnings**: [LEARNINGS.md](../../LEARNINGS.md)

5. **Open an issue**: Provide:
   - Error message
   - Logs (relevant sections)
   - Configuration (sanitized)
   - Health endpoint output
   - Steps to reproduce

## Next Steps

- [Monitoring Guide](monitoring.md) - Set up monitoring to catch issues early
- [Deployment Guide](deployment.md) - Best practices for production
- [Docker Guide](docker.md) - Container-specific troubleshooting
