# Quick Start Guide

Get Source Code Portal running in 5 minutes.

## Prerequisites

- **Java 21 LTS** installed and available on your PATH
- **Maven 3.6+** installed
- **GitHub Access Token** (see [Configuration Guide](configuration.md) for details)

Check your versions:
```bash
java -version  # Should show Java 21
mvn -version   # Should show Maven 3.6+
```

## 3-Command Quick Start

### 1. Clone and Build

```bash
git clone https://github.com/Cantara/SourceCodePortal.git
cd SourceCodePortal
mvn clean install -DskipTests
```

**Build time**: ~2-3 minutes on first run (downloads dependencies)

### 2. Configure GitHub Authentication

Create `security.properties` in the project root:

```properties
github.oauth2.client.clientId=YOUR_CLIENT_ID
github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET
github.client.accessToken=YOUR_ACCESS_TOKEN
```

**Don't have a token?** See [Configuration Guide](configuration.md#generating-access-tokens) for instructions.

### 3. Run the Application

```bash
mvn spring-boot:run
```

**Startup time**: ~10-15 seconds

## Access the Application

Once started, open your browser to:

- **Dashboard**: http://localhost:9090/
- **Health Check**: http://localhost:9090/actuator/health
- **Documentation Portal**: http://localhost:9090/docs

You should see the main dashboard with repository groups from your configured GitHub organization.

## First Steps

### Verify It's Working

1. **Check health status**:
   ```bash
   curl http://localhost:9090/actuator/health
   ```

   You should see:
   ```json
   {
     "status": "UP",
     "components": {
       "github": {"status": "UP"},
       "cache": {"status": "UP"},
       "executor": {"status": "UP"}
     }
   }
   ```

2. **View the dashboard** at http://localhost:9090/

3. **Check GitHub rate limit**:
   ```bash
   curl http://localhost:9090/actuator/health/github
   ```

### Configure Repository Groups

Edit `src/main/resources/conf/config.json` to customize which repositories are displayed:

```json
{
  "github": {
    "organization": "YourOrganization"
  },
  "groups": [
    {
      "groupId": "frontend",
      "display-name": "Frontend Applications",
      "description": "React and Vue applications",
      "github-repositories": ["frontend-*", "web-app"]
    }
  ]
}
```

See [Configuration Guide](configuration.md) for detailed config options.

## Quick Development Workflow

### Frontend Changes (Live Reload)

If you're working on CSS/SCSS:

```bash
# Terminal 1: Watch Sass files
sass --watch src/main/sass/scss:target/classes/META-INF/views/css

# Terminal 2: Run application with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Backend Changes

For Java code changes:
1. Make your changes
2. Rebuild: `mvn clean install -DskipTests`
3. Restart: `mvn spring-boot:run`

Or use IntelliJ with Spring Boot DevTools for hot reload.

## Common Issues

### Port 9090 Already in Use

Change the port in `application.properties`:
```properties
server.port=8080
```

Or via environment variable:
```bash
SERVER_PORT=8080 mvn spring-boot:run
```

### GitHub API Rate Limit

If you see "rate limit exceeded" errors:
1. Ensure you have a valid GitHub access token configured
2. Check rate limit status: http://localhost:9090/actuator/health/github
3. Authenticated requests have higher rate limits (5,000/hour vs 60/hour)

### Build Failures

```bash
# Clean Maven cache and rebuild
mvn clean
rm -rf ~/.m2/repository/no/cantara/
mvn install -DskipTests
```

See [Troubleshooting Guide](../operations/troubleshooting.md) for more solutions.

## Next Steps

Now that you're up and running:

1. **[Building Guide](building.md)** - Learn about build commands and options
2. **[Running Guide](running.md)** - Understand execution modes and profiles
3. **[Configuration Guide](configuration.md)** - Detailed configuration options
4. **[Architecture Overview](../architecture/overview.md)** - Understand the system

## Alternative: Docker Quick Start

If you prefer Docker:

```bash
docker pull cantara/sourcecodeportal
docker run -p 9090:9090 \
  -e github.oauth2.client.clientId=YOUR_CLIENT_ID \
  -e github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET \
  cantara/sourcecodeportal
```

See [Docker Guide](../operations/docker.md) for more details.

## Getting Help

- **Logs**: Check console output for errors
- **Health checks**: http://localhost:9090/actuator/health
- **Metrics**: http://localhost:9090/actuator/metrics
- **Documentation**: http://localhost:9090/docs

For troubleshooting, see:
- [Operations - Troubleshooting](../operations/troubleshooting.md)
- [LEARNINGS.md](../../LEARNINGS.md) - Common gotchas and solutions
