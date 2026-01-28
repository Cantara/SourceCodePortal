# Docker Deployment Guide

This guide covers building, running, and managing Source Code Portal Docker containers.

## Table of Contents

- [Building Docker Images](#building-docker-images)
- [Running Containers](#running-containers)
- [Configuration](#configuration)
- [Docker Compose](#docker-compose)
- [Advanced Topics](#advanced-topics)
- [Troubleshooting](#troubleshooting)

## Building Docker Images

### Build Latest Version

Build from the official Maven repository:

```bash
docker build -t cantara/sourcecodeportal .
```

This downloads the latest released version from Maven Central.

### Build Specific Version

Specify a version using the `DOCKER_TAG` build argument:

```bash
docker build --build-arg DOCKER_TAG=0.10.17 -t cantara/sourcecodeportal:0.10.17 .
```

### Build from Local Source

To build from local source code instead of Maven:

```bash
# Build JAR first
mvn clean package -DskipTests

# Build Docker image with local JAR
docker build -f Dockerfile.local -t cantara/sourcecodeportal:local .
```

Create `Dockerfile.local`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine

# Create app user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create app directory
WORKDIR /home/sourcecodeportal
RUN chown appuser:appgroup /home/sourcecodeportal

# Copy local JAR
COPY --chown=appuser:appgroup target/source-code-portal-*.jar app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 9090

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Verify Build

```bash
# Check image exists
docker images | grep sourcecodeportal

# Inspect image
docker inspect cantara/sourcecodeportal
```

## Running Containers

### Basic Run

```bash
docker run -p 9090:9090 cantara/sourcecodeportal
```

Access the application at `http://localhost:9090`

### Run with GitHub OAuth Credentials

```bash
docker run \
  --env github.oauth2.client.clientId=YOUR_CLIENT_ID \
  --env github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET \
  --rm -p 9090:9090 \
  cantara/sourcecodeportal
```

### Run with Environment File

Create `.env` file:

```bash
# GitHub Configuration
github.oauth2.client.clientId=your_client_id
github.oauth2.client.clientSecret=your_client_secret
github.client.accessToken=ghp_your_access_token
github.organization=YourOrg

# Server Configuration
server.port=9090
```

Run with environment file:

```bash
docker run --env-file .env -p 9090:9090 cantara/sourcecodeportal
```

### Run in Background (Detached)

```bash
docker run -d \
  --name scp \
  --env-file .env \
  -p 9090:9090 \
  --restart unless-stopped \
  cantara/sourcecodeportal
```

### Run with Volume Mounts

Mount configuration files:

```bash
docker run -d \
  --name scp \
  -v $(pwd)/config.json:/home/sourcecodeportal/config_override/conf/config.json:ro \
  -v $(pwd)/security.properties:/home/sourcecodeportal/config_override/security.properties:ro \
  -v $(pwd)/logs:/home/sourcecodeportal/logs \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

## Configuration

### Environment Variables

All configuration properties can be set via environment variables with `SCP_` prefix:

```bash
docker run \
  -e SCP_GITHUB_ACCESS_TOKEN=ghp_token \
  -e SCP_GITHUB_ORGANIZATION=Cantara \
  -e SCP_SERVER_PORT=9090 \
  -e SCP_CACHE_TTL=30 \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

### Configuration File Override

#### Method 1: Volume Mount (Recommended)

```bash
docker run \
  -v $(pwd)/config.json:/home/sourcecodeportal/config_override/conf/config.json:ro \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

#### Method 2: Copy to Running Container

```bash
# Find container ID
docker ps
CONTAINER_ID=abc123def456

# Copy config file
docker cp config.json $CONTAINER_ID:/home/sourcecodeportal/config_override/conf/config.json

# Restart container to pick up changes
docker restart $CONTAINER_ID
```

#### Method 3: Copy Using Inspect (Legacy)

```bash
# Get full container ID
FULL_ID=$(docker inspect -f '{{.Id}}' cantara/sourcecodeportal)

# Copy config (requires root or Docker volume access)
sudo cp config.json /var/lib/docker/aufs/mnt/$FULL_ID/home/sourcecodeportal/config_override/conf/config.json
```

**Note**: This method is deprecated and may not work with newer Docker storage drivers.

### Health Checks

Add health check to Docker run:

```bash
docker run -d \
  --name scp \
  --health-cmd="curl -f http://localhost:9090/actuator/health || exit 1" \
  --health-interval=30s \
  --health-timeout=10s \
  --health-retries=3 \
  --health-start-period=60s \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

Check health status:

```bash
docker ps  # Shows health status
docker inspect scp | grep -A 10 Health
```

## Docker Compose

### Basic Compose File

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  sourcecodeportal:
    image: cantara/sourcecodeportal:latest
    container_name: scp
    ports:
      - "9090:9090"
    environment:
      - github.oauth2.client.clientId=${GITHUB_CLIENT_ID}
      - github.oauth2.client.clientSecret=${GITHUB_CLIENT_SECRET}
      - github.client.accessToken=${GITHUB_ACCESS_TOKEN}
      - github.organization=${GITHUB_ORGANIZATION}
    volumes:
      - ./config.json:/home/sourcecodeportal/config_override/conf/config.json:ro
      - ./logs:/home/sourcecodeportal/logs
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9090/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

### Production Compose File

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  sourcecodeportal:
    image: cantara/sourcecodeportal:0.10.17
    container_name: scp-prod
    ports:
      - "80:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - github.oauth2.client.clientId=${GITHUB_CLIENT_ID}
      - github.oauth2.client.clientSecret=${GITHUB_CLIENT_SECRET}
      - github.client.accessToken=${GITHUB_ACCESS_TOKEN}
      - github.organization=${GITHUB_ORGANIZATION}
    volumes:
      - ./config.json:/home/sourcecodeportal/config_override/conf/config.json:ro
      - ./security.properties:/home/sourcecodeportal/config_override/security.properties:ro
      - scp-logs:/home/sourcecodeportal/logs
    restart: always
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9090/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

volumes:
  scp-logs:
    driver: local
```

### Using Docker Compose

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Restart services
docker-compose restart

# Pull latest images
docker-compose pull

# Recreate containers with new config
docker-compose up -d --force-recreate
```

### Docker Compose with Build

Create `docker-compose.build.yml`:

```yaml
version: '3.8'

services:
  sourcecodeportal:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        DOCKER_TAG: 0.10.17
    container_name: scp-built
    ports:
      - "9090:9090"
    env_file:
      - .env
    volumes:
      - ./config.json:/home/sourcecodeportal/config_override/conf/config.json:ro
    restart: unless-stopped
```

Build and run:

```bash
docker-compose -f docker-compose.build.yml build
docker-compose -f docker-compose.build.yml up -d
```

## Advanced Topics

### Multi-Stage Build

Optimize Docker image size with multi-stage build:

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /home/sourcecodeportal
COPY --from=builder --chown=appuser:appgroup /app/target/source-code-portal-*.jar app.jar
USER appuser
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Resource Limits

Limit container resources:

```bash
docker run -d \
  --name scp \
  --cpus="2" \
  --memory="2g" \
  --memory-reservation="1g" \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

### Logging Configuration

#### JSON File Logging

```bash
docker run -d \
  --name scp \
  --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

#### Syslog Logging

```bash
docker run -d \
  --name scp \
  --log-driver syslog \
  --log-opt syslog-address=tcp://192.168.1.100:514 \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

### Network Configuration

#### Create Custom Network

```bash
# Create network
docker network create scp-network

# Run container on network
docker run -d \
  --name scp \
  --network scp-network \
  -p 9090:9090 \
  cantara/sourcecodeportal
```

#### Use Host Network

```bash
docker run -d \
  --name scp \
  --network host \
  cantara/sourcecodeportal
```

**Note**: With host network, the container shares the host's network namespace.

### Generating GitHub Access Token

Generate GitHub access token using Docker:

```bash
docker run -it \
  -e SCP_github.oauth2.client.clientId=YOUR_CLIENT_ID \
  -e SCP_github.oauth2.client.clientSecret=YOUR_CLIENT_SECRET \
  cantara/sourcecodeportal /github-access-token
```

This runs the OAuth flow to obtain a GitHub personal access token.

## Troubleshooting

### Container Won't Start

Check logs:

```bash
docker logs scp
docker logs -f scp  # Follow logs
```

Common issues:

- Port 9090 already in use: Use `-p 8080:9090` to map to different port
- Missing configuration: Ensure environment variables are set
- Permission issues: Check volume mount permissions

### Container Keeps Restarting

Check restart policy and logs:

```bash
docker inspect scp | grep -A 5 RestartPolicy
docker logs --tail 100 scp
```

Disable restart temporarily:

```bash
docker update --restart=no scp
docker restart scp
docker logs -f scp
```

### Can't Access Application

Check if container is running:

```bash
docker ps
```

Check port mapping:

```bash
docker port scp
```

Test from within container:

```bash
docker exec scp curl -f http://localhost:9090/actuator/health
```

Test from host:

```bash
curl http://localhost:9090/actuator/health
```

### High Memory Usage

Check memory stats:

```bash
docker stats scp
```

Limit memory:

```bash
docker update --memory="2g" scp
docker restart scp
```

### Configuration Not Loading

Verify volume mounts:

```bash
docker inspect scp | grep -A 10 Mounts
```

Verify file inside container:

```bash
docker exec scp cat /home/sourcecodeportal/config_override/conf/config.json
```

### GitHub API Rate Limit

Check rate limit status:

```bash
docker exec scp curl http://localhost:9090/actuator/health/github
```

## Best Practices

1. **Use specific version tags** in production (not `latest`)
2. **Set resource limits** to prevent resource exhaustion
3. **Configure health checks** for automatic restart on failure
4. **Use volume mounts** for configuration files
5. **Store secrets in environment variables**, not in images
6. **Enable logging** with rotation to prevent disk fill
7. **Use Docker Compose** for multi-container setups
8. **Regularly update base images** for security patches
9. **Monitor container health** via `/actuator/health`
10. **Backup configuration files** before updates

## Next Steps

- [Deployment Guide](deployment.md) - Full deployment options
- [Monitoring Guide](monitoring.md) - Set up monitoring and metrics
- [Troubleshooting Guide](troubleshooting.md) - Detailed troubleshooting
