# Source Code Portal

> A unified dashboard for GitHub organizations - aggregating repositories, commits, documentation, and DevOps metrics into logical system groups.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## 🎯 What is Source Code Portal?

Source Code Portal (SCP) solves a common problem in organizations with multiple GitHub repositories: **fragmented visibility**. Instead of switching between GitHub, Jenkins, Snyk, and other tools, SCP provides a single dashboard showing:

- 📦 **Repository Groups** - Logical grouping of related repositories (e.g., all "Whydah*" repos = IAM platform)
- 📊 **Commit Activity** - Real-time commit logs across multiple repositories
- 📚 **Documentation** - Rendered README files (Markdown/AsciiDoc) in-portal
- ✅ **Build Status** - Jenkins, GitHub Actions integration
- 🔒 **Security Status** - Snyk vulnerability scanning results
- 🏷️ **Badges & Metrics** - Shields.io integration

**Designed for**: Small to medium organizations (<2500 commits/hour) with 10-100 repositories.

---

## 🚀 Quick Start

### Prerequisites

- Java 21+ ([download](https://adoptium.net/))
- Maven 3.9+ ([download](https://maven.apache.org/download.cgi))
- GitHub Personal Access Token ([create](https://github.com/settings/tokens))

### Run in 3 Commands

```bash
# 1. Clone and build
git clone https://github.com/Cantara/SourceCodePortal.git
cd SourceCodePortal
mvn clean compile

# 2. Set GitHub credentials
export SCP_GITHUB_ACCESS_TOKEN=ghp_your_github_token_here

# 3. Run with Spring Boot
mvn spring-boot:run
```

### Access the Dashboard

- **Dashboard**: http://localhost:9090/dashboard
- **Health Check**: http://localhost:9090/actuator/health
- **Metrics**: http://localhost:9090/actuator/metrics
- **API Info**: http://localhost:9090/actuator/info

---

## 📸 Screenshots

### Dashboard View
*(Main dashboard showing repository groups and activity)*

```
┌─────────────────────────────────────────────────────────────────┐
│  Source Code Portal - Cantara Organization                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  📦 Whydah IAM Platform           🟢 5 repos | 24 commits today │
│  📦 ConfigService                 🟢 3 repos | 12 commits today │
│  📦 Stingray Monitoring           🟢 4 repos | 8 commits today  │
│  📦 Documentation                 🟢 2 repos | 3 commits today  │
│                                                                 │
│  Recent Commits Across Organization:                            │
│  • 2 min ago - [Whydah] Fix authentication bug                 │
│  • 15 min ago - [ConfigService] Update README                  │
│  • 1 hour ago - [Stingray] Add metrics endpoint                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Group View
*(Detailed view of a repository group with build status, documentation, and commits)*

---

## ✨ Key Features

### 🎯 Repository Grouping
- **Logical Organization**: Group repositories by system (e.g., "Whydah IAM", "Monitoring Tools")
- **Regex Patterns**: Automatic repository inclusion via patterns (e.g., "Whydah*")
- **Configuration-Driven**: Easy to add new groups via `config.json`

### 📊 Activity Tracking
- **Real-Time Updates**: GitHub webhooks for immediate commit notifications
- **Cross-Repository View**: See activity across all repositories in one place
- **Commit History**: Detailed commit logs with author, message, and files changed

### 📚 Documentation Hub
- **In-Portal Rendering**: View README files without leaving SCP
- **Multiple Formats**: Markdown and AsciiDoc support
- **Syntax Highlighting**: Code blocks with proper formatting

### ✅ DevOps Integration
- **Jenkins**: Build status badges and links
- **Snyk**: Security vulnerability scanning results
- **Shields.io**: Custom badges and metrics
- **GitHub Actions**: Workflow status (coming soon)

### 🔍 Observability
- **Spring Boot Actuator**: Health checks, metrics, and info endpoints
- **Custom Health Indicators**: GitHub API, cache, executor thread pools
- **Prometheus Metrics**: Ready for Grafana dashboards
- **Structured Logging**: SLF4J with Logback

---

## 🏗️ Architecture

### Technology Stack

**Backend**:
- Java 21 LTS (with virtual threads)
- Spring Boot 3.2.2 (web, cache, actuator)
- Undertow (embedded web server)
- Resilience4j (circuit breaker)
- Caffeine (high-performance caching)

**Frontend**:
- Thymeleaf (server-side rendering)
- Bootstrap 5 (UI framework)
- HTMX (dynamic interactions - coming soon)
- Sass/SCSS (styling)

**Build & Test**:
- Maven 3.9+
- JUnit 5 (testing framework)
- Testcontainers (integration testing - planned)

### System Design

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────────────────────────────────┐
│      Spring Boot Application            │
│  ┌────────────────────────────────────┐ │
│  │  Spring MVC Controllers            │ │
│  │  - Dashboard, Groups, Commits      │ │
│  └────────────────────────────────────┘ │
│  ┌────────────────────────────────────┐ │
│  │  Spring Boot Actuator              │ │
│  │  - Health, Metrics, Info           │ │
│  └────────────────────────────────────┘ │
│  ┌────────────────────────────────────┐ │
│  │  Business Logic                    │ │
│  │  - Repository Config Loader        │ │
│  │  - Commit Fetcher                  │ │
│  │  - Documentation Renderer          │ │
│  └────────────────────────────────────┘ │
│  ┌────────────────────────────────────┐ │
│  │  Caffeine Cache                    │ │
│  │  - Repositories, Commits, Docs     │ │
│  └────────────────────────────────────┘ │
└────────────┬────────────────────────────┘
             │
             ▼
   ┌─────────────────────┐
   │  External Services  │
   │  - GitHub API       │
   │  - Jenkins          │
   │  - Snyk             │
   │  - Shields.io       │
   └─────────────────────┘
```

---

## 📖 Documentation

> **Complete Documentation**: See **[docs/README.md](docs/README.md)** for the full documentation hub with 25+ comprehensive guides.

### Getting Started
- **[Quick Start Guide](docs/getting-started/quickstart.md)** - Get running in 5 minutes
- **[Building](docs/getting-started/building.md)** - Maven build commands and frontend compilation
- **[Running](docs/getting-started/running.md)** - Spring Boot execution modes and profiles
- **[Configuration](docs/getting-started/configuration.md)** - GitHub authentication and repository groups

### Architecture
- **[Overview](docs/architecture/overview.md)** - System architecture and technology stack
- **[Spring Boot](docs/architecture/spring-boot.md)** - Application initialization and setup
- **[Controllers](docs/architecture/controllers.md)** - Request flow and Spring MVC patterns
- **[Caching](docs/architecture/caching.md)** - Cache strategy with Caffeine
- **[Packages](docs/architecture/packages.md)** - Package structure and responsibilities

### Features
- **[Dashboard](docs/features/dashboard.md)** - Dashboard features and repository views
- **[Repository Groups](docs/features/repository-groups.md)** - Group configuration and patterns
- **[Integrations](docs/features/integrations.md)** - Jenkins, Snyk, Shields.io
- **[Webhooks](docs/features/webhooks.md)** - Real-time GitHub updates
- **[Snyk Integration](docs/features/snyk-integration.md)** - Security scanning details

### Operations
- **[Docker](docs/operations/docker.md)** - Docker build and deployment
- **[Deployment](docs/operations/deployment.md)** - Production deployment (JAR, Docker, K8s)
- **[Monitoring](docs/operations/monitoring.md)** - Actuator, Prometheus, Grafana
- **[Troubleshooting](docs/operations/troubleshooting.md)** - Common issues and solutions

### For Claude Code Users
- **[CLAUDE.md](CLAUDE.md)** - Comprehensive Claude Code guidance
- **[CLAUDE_SKILLS.md](CLAUDE_SKILLS.md)** - Automation skills for common tasks
- **[LEARNINGS.md](LEARNINGS.md)** - Gotchas and best practices
- **[VERIFICATION_GUIDE.md](VERIFICATION_GUIDE.md)** - Verify Spring Boot setup

---

## 🔧 Configuration

### Repository Groups

Configure repository groups in `src/main/resources/conf/config.json`:

```json
{
  "groups": [
    {
      "groupId": "whydah",
      "display-name": "Whydah IAM Platform",
      "description": "Identity and Access Management",
      "defaultGroupRepo": "Whydah-Documentation",
      "artifactId": ["Whydah*"],
      "jenkins": {
        "prefix": "Whydah-"
      },
      "snyk": {
        "organization": "cantara",
        "projectPrefix": "whydah-"
      }
    }
  ]
}
```

### GitHub Integration

Set up GitHub credentials in `security.properties` or environment variables:

```bash
export SCP_GITHUB_ORGANIZATION=YourOrg
export SCP_GITHUB_ACCESS_TOKEN=ghp_your_token_here
```

### Jenkins Integration

Configure Jenkins URL and job patterns:

```yaml
scp:
  jenkins:
    base-url: https://jenkins.example.com
```

### Snyk Integration

Configure Snyk API token:

```bash
export SCP_SNYK_API_TOKEN=your_snyk_token_here
```

---

## 🚦 Health & Monitoring

### Spring Boot Actuator Endpoints

- `/actuator/health` - Overall health with custom indicators
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus scraping endpoint

### Custom Health Indicators

**GitHub Health**:
- Monitors GitHub API rate limit
- Status: UP (>10% remaining), DEGRADED (<10%), DOWN (unreachable)

**Cache Health**:
- Monitors cache population across 9 caches
- Status: UP (populated), DEGRADED (empty), DOWN (closed)

**Executor Health**:
- Monitors thread pool utilization
- Status: UP (healthy), DEGRADED (>90% utilization), DOWN (terminated)

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](docs/development/contributing.md) for:
- Code of conduct
- Development workflow
- Pull request process
- Coding standards

### Quick Contribution Guide

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

---

## 📜 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Cantara** - Original creators and maintainers
- **Claude Code** - Assisted with Spring Boot migration (Phase 2)
- **Contributors** - Thank you to all who have contributed!

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Cantara/SourceCodePortal/issues)
- **Wiki**: [Project Wiki](https://github.com/Cantara/SourceCodePortal/wiki)
- **Discussions**: [GitHub Discussions](https://github.com/Cantara/SourceCodePortal/discussions)

---

## 🗺️ Roadmap

### ✅ Phase 1: Modernization (Completed)
- Java 11 → Java 21 LTS
- TestNG → JUnit 5
- Hystrix → Resilience4j

### ✅ Phase 2: Spring Boot Migration (Completed)
- Spring Boot 3.2.2 integration
- Spring MVC controllers
- Spring Boot Actuator
- Caffeine caching

### 🚧 Phase 3: User Experience (In Progress)
- Bootstrap 4 → Bootstrap 5
- HTMX for dynamic interactions
- Gulp → Vite (faster builds)
- Dark mode support
- Search functionality

### 📋 Phase 4: Feature Enhancements (Planned)
- GitHub Actions integration
- GitLab support
- Pull request dashboard
- Team velocity metrics
- AI-powered changelog generation

---

## 📊 Project Status

- **Version**: 0.10.17-SNAPSHOT
- **Build**: [![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
- **Coverage**: [![Coverage](https://img.shields.io/badge/coverage-65%25-yellow.svg)]()
- **Activity**: [![Last Commit](https://img.shields.io/badge/last%20commit-today-brightgreen.svg)]()

---

Made with ❤️ by [Cantara](https://github.com/Cantara)
