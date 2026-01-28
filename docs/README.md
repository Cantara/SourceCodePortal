# Source Code Portal Documentation

Welcome to the Source Code Portal documentation. This guide will help you understand, deploy, and contribute to the project.

## 🚀 Getting Started

New to Source Code Portal? Start here:

- **[Quick Start](getting-started/quickstart.md)** - Get the application running in 5 minutes
- **[Building](getting-started/building.md)** - Maven build commands and frontend compilation
- **[Running](getting-started/running.md)** - Spring Boot execution modes and profiles
- **[Configuration](getting-started/configuration.md)** - GitHub authentication and repository groups

**Time to first run**: ~5-10 minutes

## 🏗️ Architecture

Understand the system design and technical architecture:

- **[Overview](architecture/overview.md)** - System architecture, capabilities, and technology stack
- **[Spring Boot](architecture/spring-boot.md)** - Application initialization and Spring Boot setup
- **[Controllers](architecture/controllers.md)** - Request flow and Spring MVC architecture
- **[Caching](architecture/caching.md)** - Cache strategy with Caffeine and Spring Cache
- **[Packages](architecture/packages.md)** - Package structure and code organization

**Technology stack**: Java 21, Spring Boot 3.2.2, Caffeine Cache, Resilience4j

## ✨ Features

Explore what Source Code Portal can do:

- **[Dashboard](features/dashboard.md)** - Main dashboard features and repository views
- **[Repository Groups](features/repository-groups.md)** - Organize repositories with config.json
- **[Integrations](features/integrations.md)** - Jenkins, Snyk, and Shields.io badges
- **[Webhooks](features/webhooks.md)** - Real-time updates from GitHub
- **[Snyk Integration](features/snyk-integration.md)** - Security scanning and badge caching

**Key capabilities**: GitHub aggregation, commit logs, documentation rendering, build status

## 🔧 Operations

Deploy and maintain in production:

- **[Docker](operations/docker.md)** - Docker build, run, and Docker Compose
- **[Deployment](operations/deployment.md)** - Production deployment (JAR, Docker, Kubernetes)
- **[Monitoring](operations/monitoring.md)** - Spring Boot Actuator, Prometheus, Grafana
- **[Troubleshooting](operations/troubleshooting.md)** - Common issues and solutions

**Production checklist**: See [Deployment Guide](operations/deployment.md#production-checklist)

## 👨‍💻 Development

Contribute to the project:

- **[Contributing](development/contributing.md)** - How to contribute and coding standards
- **[Testing](development/testing.md)** - Test framework (JUnit 5) and patterns
- **[Skills](development/skills.md)** - Claude Code automation skills

**Test framework**: JUnit 5 (Jupiter) with TestServerExtension

## 📚 Historical Documentation

Archived documentation from previous phases:

### Phase 1: Java 21 & JUnit 5 Migration
- [Phase 1 Complete](history/phase1/PHASE1_COMPLETE.md) - Migration summary
- [Modernization Phase 1](history/phase1/MODERNIZATION_PHASE1.md) - Technical details
- [JUnit 5 Migration](history/phase1/MIGRATION_JUNIT5_SUMMARY.md) - Test framework migration

### Phase 2: Spring Boot Migration
- [Phase 2 Plan](history/phase2/PHASE2_PLAN.md) - Migration planning
- [Phase 2 Learnings](history/phase2/LEARNINGS_PHASE2.md) - Lessons learned
- [Task Summaries](history/phase2/tasks/) - Detailed task documentation

### Phase 3: Frontend Improvements
- [Phase 3 Complete](history/phase3/PHASE3_COMPLETE.md) - Frontend work summary
- [HTMX Complete](history/phase3/PHASE3_HTMX_COMPLETE.md) - HTMX variant completion
- [Dark Mode Complete](history/phase3/DARK_MODE_COMPLETE.md) - Dark mode implementation

### Additional Archives
- [Testing](history/testing/) - Test completion reports
- [Sessions](history/sessions/) - Session summaries
- [Planning](history/planning/) - Historical planning documents
- [Skills](history/skills/) - Skills proposals and completion
- [Deprecated](history/deprecated/) - Deprecated controller migration guides
- [Migration Notes](history/migration-notes/) - Phase-specific gotchas

## 📖 Quick Reference

### Essential Files

| File | Purpose |
|------|---------|
| [README.md](../README.md) | Project overview and quick links |
| [CLAUDE.md](../CLAUDE.md) | Claude Code guidance (comprehensive overview) |
| [CHANGELOG.md](../CHANGELOG.md) | Version history and changes |
| [LEARNINGS.md](../LEARNINGS.md) | Gotchas, best practices, and lessons learned |
| [VERIFICATION_GUIDE.md](../VERIFICATION_GUIDE.md) | Verify Spring Boot migration works |
| [CLAUDE_SKILLS.md](../CLAUDE_SKILLS.md) | Claude Code automation skills reference |
| [TODO.md](../TODO.md) | Current work tracking |

### Configuration Files

| File | Purpose | Location |
|------|---------|----------|
| `config.json` | Repository groups | `src/main/resources/conf/` |
| `security.properties` | GitHub credentials | Project root (not in git) |
| `application.properties` | App settings | Project root |
| `application.yml` | Spring Boot config | `src/main/resources/` |

### Key Endpoints

| Endpoint | Purpose |
|----------|---------|
| `http://localhost:9090/` | Main dashboard |
| `/actuator/health` | Health status |
| `/actuator/health/github` | GitHub API rate limit |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus metrics |

## 🎯 Common Tasks

### First-Time Setup
1. [Clone and build](getting-started/quickstart.md#3-command-quick-start)
2. [Configure GitHub authentication](getting-started/configuration.md#github-authentication)
3. [Run the application](getting-started/running.md#spring-boot-mode-recommended)

### Adding a Repository Group
1. Edit `src/main/resources/conf/config.json`
2. See [Repository Groups Guide](features/repository-groups.md)

### Setting Up Webhooks
1. Follow [Webhook Setup Guide](features/webhooks.md)
2. Configure webhook secret in `security.properties`

### Deploying to Production
1. Review [Deployment Guide](operations/deployment.md)
2. Check [Production Checklist](operations/deployment.md#production-checklist)
3. Set up [Monitoring](operations/monitoring.md)

### Troubleshooting Issues
1. Check [Troubleshooting Guide](operations/troubleshooting.md)
2. Review [LEARNINGS.md](../LEARNINGS.md) for known gotchas
3. Check [GitHub health endpoint](operations/monitoring.md#health-endpoints)

## 🔍 Finding What You Need

### By Role

**New Developer**:
1. [Quick Start](getting-started/quickstart.md)
2. [Architecture Overview](architecture/overview.md)
3. [Contributing Guide](development/contributing.md)

**Operations Engineer**:
1. [Deployment](operations/deployment.md)
2. [Monitoring](operations/monitoring.md)
3. [Troubleshooting](operations/troubleshooting.md)

**System Architect**:
1. [Architecture Overview](architecture/overview.md)
2. [Spring Boot Architecture](architecture/spring-boot.md)
3. [Caching Strategy](architecture/caching.md)

**Claude Code User**:
1. [CLAUDE.md](../CLAUDE.md) - Comprehensive guidance
2. [CLAUDE_SKILLS.md](../CLAUDE_SKILLS.md) - Automation skills
3. [LEARNINGS.md](../LEARNINGS.md) - Gotchas and best practices

### By Task

| Task | Guide |
|------|-------|
| Build the project | [Building Guide](getting-started/building.md) |
| Run locally | [Running Guide](getting-started/running.md) |
| Configure GitHub | [Configuration Guide](getting-started/configuration.md) |
| Add repository group | [Repository Groups](features/repository-groups.md) |
| Set up Jenkins badges | [Integrations](features/integrations.md) |
| Enable webhooks | [Webhooks Guide](features/webhooks.md) |
| Deploy with Docker | [Docker Guide](operations/docker.md) |
| Monitor production | [Monitoring Guide](operations/monitoring.md) |
| Debug issues | [Troubleshooting](operations/troubleshooting.md) |

### By Topic

| Topic | Guide |
|-------|-------|
| **Spring Boot** | [Architecture - Spring Boot](architecture/spring-boot.md) |
| **Caching** | [Architecture - Caching](architecture/caching.md) |
| **GitHub API** | [Features - Integrations](features/integrations.md) |
| **Health Checks** | [Operations - Monitoring](operations/monitoring.md) |
| **Testing** | [Development - Testing](development/testing.md) |
| **Docker** | [Operations - Docker](operations/docker.md) |
| **Kubernetes** | [Operations - Deployment](operations/deployment.md) |

## 🆘 Getting Help

### Documentation Navigation

- **Getting Started**: For setup and basic usage
- **Architecture**: For understanding system design
- **Features**: For learning capabilities
- **Operations**: For deployment and maintenance
- **Development**: For contributing

### Troubleshooting Resources

1. **[Troubleshooting Guide](operations/troubleshooting.md)** - Common issues and solutions
2. **[LEARNINGS.md](../LEARNINGS.md)** - Known gotchas and best practices
3. **[Health Endpoints](operations/monitoring.md#health-endpoints)** - Check system status

### External Resources

- **GitHub Repository**: https://github.com/Cantara/SourceCodePortal
- **Issue Tracker**: https://github.com/Cantara/SourceCodePortal/issues
- **Docker Hub**: https://hub.docker.com/r/cantara/sourcecodeportal
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/

## 📝 Documentation Standards

This documentation follows these principles:

1. **Comprehensive**: Each guide is self-contained
2. **Practical**: Includes real-world examples
3. **Cross-referenced**: Links to related documentation
4. **Up-to-date**: Reflects current codebase (Phase 2 complete)
5. **Structured**: Consistent format across guides

### Contributing to Documentation

See [Contributing Guide](development/contributing.md) for documentation standards and how to submit improvements.

## 🎓 Learning Path

Recommended reading order for new developers:

1. **Week 1**: Getting started and basic understanding
   - [Quick Start](getting-started/quickstart.md)
   - [Architecture Overview](architecture/overview.md)
   - [Dashboard Features](features/dashboard.md)

2. **Week 2**: Deep dive into architecture
   - [Spring Boot Architecture](architecture/spring-boot.md)
   - [Controllers](architecture/controllers.md)
   - [Caching Strategy](architecture/caching.md)

3. **Week 3**: Advanced topics and operations
   - [Repository Groups](features/repository-groups.md)
   - [Integrations](features/integrations.md)
   - [Deployment](operations/deployment.md)

4. **Week 4**: Contributing and best practices
   - [Contributing Guide](development/contributing.md)
   - [Testing](development/testing.md)
   - [LEARNINGS.md](../LEARNINGS.md)

## 📊 Documentation Statistics

- **Total guides**: 25+ comprehensive guides
- **Lines of documentation**: ~15,000 lines
- **Code examples**: 200+ code snippets
- **Diagrams**: 10+ architecture and flow diagrams
- **Last updated**: Phase 2 completion (Spring Boot migration)

## 🚀 What's New

- ✅ **Phase 2 Complete**: Spring Boot 3.2.2 migration finished
- ✅ **Phase 3 Complete**: Frontend improvements (dark mode, HTMX)
- ✅ **Controller Migration**: All Undertow controllers migrated to Spring MVC
- ✅ **Comprehensive Documentation**: 25+ guides covering all aspects
- 📅 **Next Phase**: Performance optimization and observability enhancements

---

**Version**: Phase 2 Complete (Spring Boot Migration)
**Last Updated**: January 2026
**Documentation Status**: ✅ Comprehensive and up-to-date
