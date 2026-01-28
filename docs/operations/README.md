# Operations Documentation

This section provides comprehensive guides for deploying, monitoring, and troubleshooting the Source Code Portal in production environments.

## Overview

Source Code Portal is a GitHub-integrated dashboard for monitoring and aggregating repository information. It's designed for small to medium-sized organizations and can be deployed as a JAR file, Docker container, or Kubernetes deployment.

## Quick Links

### Deployment

- **[Deployment Guide](deployment.md)** - Production deployment options, configuration, and best practices
- **[Docker Guide](docker.md)** - Building and running Docker containers
- **[Configuration Reference](../getting-started/configuration.md)** - Complete configuration guide

### Monitoring & Maintenance

- **[Monitoring Guide](monitoring.md)** - Spring Boot Actuator, metrics, Prometheus, and Grafana
- **[Troubleshooting Guide](troubleshooting.md)** - Common issues and solutions

### Related Documentation

- **[Getting Started](../getting-started/)** - Initial setup and development
- **[Architecture](../architecture/)** - System design and patterns
- **[Features](../features/)** - Detailed feature documentation

## Deployment Options

Source Code Portal can be deployed in several ways:

### 1. JAR Deployment (Spring Boot)

Simplest option for single server deployment.

```bash
# Build and run
mvn clean package
java -jar target/source-code-portal-*.jar
```

**Pros**: Simple, direct, minimal overhead
**Cons**: Manual process management, no orchestration

See: [Deployment Guide - JAR Deployment](deployment.md#jar-deployment)

### 2. Docker Container

Containerized deployment for consistency and portability.

```bash
# Build and run
docker build -t cantara/sourcecodeportal .
docker run -p 9090:9090 cantara/sourcecodeportal
```

**Pros**: Consistent environments, easy to version, portable
**Cons**: Additional Docker layer

See: [Docker Guide](docker.md)

### 3. Kubernetes

Production-grade orchestration for high availability.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: source-code-portal
```

**Pros**: High availability, auto-scaling, service discovery
**Cons**: Higher complexity, requires Kubernetes cluster

See: [Deployment Guide - Kubernetes](deployment.md#kubernetes-deployment)

## Production Checklist

Before deploying to production, ensure:

- [ ] GitHub OAuth credentials configured (`security.properties`)
- [ ] Repository configuration (`config.json`) prepared
- [ ] Environment variables set (`SCP_*` prefix)
- [ ] Health checks configured (`/actuator/health`)
- [ ] Monitoring enabled (Prometheus metrics)
- [ ] Resource limits set (CPU, memory)
- [ ] SSL/TLS certificates configured
- [ ] Backup strategy for configuration files
- [ ] Log aggregation configured
- [ ] Alert thresholds defined

See: [Deployment Guide - Production Checklist](deployment.md#production-checklist)

## Key Endpoints

### Application

- `/` or `/dashboard` - Main dashboard
- `/group/{groupId}` - Group view
- `/contents/{org}/{repo}/{branch}` - Repository contents
- `/commits/{org}/{repo}` - Commit history
- `/github/webhook` - GitHub webhook receiver

### Health & Monitoring

- `/actuator/health` - Overall health status
- `/actuator/health/github` - GitHub API rate limit
- `/actuator/health/cache` - Cache health
- `/actuator/health/executor` - Thread pool health
- `/actuator/metrics` - Micrometer metrics
- `/actuator/prometheus` - Prometheus metrics export
- `/actuator/info` - Application info

See: [Monitoring Guide](monitoring.md)

## System Requirements

### Minimum Requirements

- **Java**: JDK 21 (LTS)
- **Memory**: 512MB RAM
- **CPU**: 1 core
- **Disk**: 500MB

### Recommended for Production

- **Java**: JDK 21 (LTS)
- **Memory**: 2GB RAM
- **CPU**: 2 cores
- **Disk**: 2GB (for logs and cache)

### External Dependencies

- **GitHub API**: Internet access required
- **Optional**: Jenkins, Snyk, Shields.io for badge integration

See: [Deployment Guide - System Requirements](deployment.md#system-requirements)

## Support & Troubleshooting

### Common Issues

- GitHub API rate limit exceeded
- Configuration file not found
- Webhook authentication failures
- Cache performance issues
- Memory leaks

See: [Troubleshooting Guide](troubleshooting.md)

### Getting Help

1. Check the [Troubleshooting Guide](troubleshooting.md)
2. Review logs: `logs/application.log`
3. Check health endpoints: `/actuator/health`
4. Open an issue on GitHub

## Next Steps

- **New Deployment**: Start with [Deployment Guide](deployment.md)
- **Existing Deployment**: Review [Monitoring Guide](monitoring.md)
- **Issues**: Check [Troubleshooting Guide](troubleshooting.md)
