# Features Documentation

This directory contains detailed documentation for all major features of Source Code Portal.

## Overview

Source Code Portal provides a comprehensive dashboard for managing and monitoring GitHub organizations. The key features are organized into the following areas:

## Feature Guides

### Core Features

1. **[Dashboard](dashboard.md)** - Main dashboard interface
   - Repository group displays
   - Commit activity views
   - Documentation rendering (Markdown/AsciiDoc)
   - Status badge aggregation
   - Real-time updates

2. **[Repository Groups](repository-groups.md)** - Logical repository organization
   - Configuration-driven grouping
   - Regex pattern matching
   - Group-level views and navigation
   - Default repository selection
   - Multi-group management

### External Integrations

3. **[Integrations](integrations.md)** - External service integrations
   - Jenkins build status integration
   - Snyk security scanning
   - Shields.io badge integration
   - Circuit breaker patterns for resilience
   - Configuration examples

4. **[Snyk Integration](snyk-integration.md)** - Security vulnerability scanning
   - Badge caching and display
   - Scheduled fetching
   - Slack webhook integration
   - SVG parsing for vulnerability data

5. **[Webhooks](webhooks.md)** - Real-time GitHub integration
   - GitHub webhook setup and configuration
   - Supported webhook events
   - HMAC signature validation
   - Cache invalidation triggers
   - Local development with ngrok

### Observability

6. **[Observability](observability.md)** - Monitoring and health checks
   - Spring Boot Actuator endpoints
   - Custom health indicators
   - Prometheus metrics
   - Logging configuration
   - Performance monitoring

## Quick Reference

### Dashboard Access

- Main Dashboard: http://localhost:9090/dashboard
- Group View: http://localhost:9090/group/{groupId}
- Repository Contents: http://localhost:9090/contents/{org}/{repo}/{branch}
- Commit History: http://localhost:9090/commits/{org}/{repo}

### Health & Monitoring

- Overall Health: http://localhost:9090/actuator/health
- GitHub Status: http://localhost:9090/actuator/health/github
- Cache Status: http://localhost:9090/actuator/health/cache
- Metrics: http://localhost:9090/actuator/metrics
- Prometheus: http://localhost:9090/actuator/prometheus

### Configuration Files

- Repository Groups: `src/main/resources/conf/config.json`
- Application Settings: `src/main/resources/application.yml`
- Security Credentials: `security.properties`
- Property Overrides: `application_override.properties`

## Feature Capabilities Summary

| Feature | Purpose | Configuration File |
|---------|---------|-------------------|
| Dashboard | Centralized view of all repositories and activity | `application.yml` |
| Repository Groups | Organize repos into logical systems | `config.json` |
| Jenkins Integration | Build status and CI/CD monitoring | `config.json` (per-group) |
| Snyk Integration | Security vulnerability scanning | `config.json` (per-group) |
| Shields.io Badges | Custom badges and metrics | `config.json` |
| GitHub Webhooks | Real-time push notifications | `security.properties` |
| Actuator Endpoints | Health checks and metrics | `application.yml` |

## Feature Dependencies

```
Dashboard
  ├─ Repository Groups (required)
  ├─ GitHub API (required)
  ├─ Cache Store (required)
  └─ Integrations (optional)
      ├─ Jenkins
      ├─ Snyk
      └─ Shields.io

Repository Groups
  ├─ config.json (required)
  └─ GitHub API (required)

Webhooks
  ├─ GitHub Setup (required)
  ├─ HMAC Secret (required)
  └─ Cache Store (required)

Observability
  ├─ Spring Boot Actuator (required)
  └─ Custom Health Indicators (optional)
```

## Getting Started

To enable specific features:

1. **Dashboard** - Enabled by default, no configuration needed
2. **Repository Groups** - Edit `src/main/resources/conf/config.json`
3. **Jenkins** - Add Jenkins URL and job patterns to group config
4. **Snyk** - Set `SCP_SNYK_API_TOKEN` environment variable
5. **Webhooks** - Configure webhook URL and secret in GitHub
6. **Observability** - Enabled by default with Spring Boot Actuator

## Further Reading

- [Architecture Overview](../architecture/overview.md)
- [Configuration Guide](../getting-started/configuration.md)
- [Development Guide](../development/contributing.md)
- [Operations Guide](../operations/deployment.md)
