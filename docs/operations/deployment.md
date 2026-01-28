# Deployment Guide

This guide covers deploying Source Code Portal in production environments.

## Table of Contents

- [Deployment Options](#deployment-options)
- [System Requirements](#system-requirements)
- [JAR Deployment](#jar-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Configuration](#configuration)
- [Security](#security)
- [SSL/TLS Configuration](#ssltls-configuration)
- [Production Checklist](#production-checklist)
- [Best Practices](#best-practices)

## Deployment Options

Source Code Portal supports multiple deployment options:

| Method | Best For | Complexity | High Availability |
|--------|----------|------------|-------------------|
| **JAR** | Single server, simple setups | Low | No |
| **Docker** | Containerized environments | Medium | With orchestration |
| **Kubernetes** | Production, cloud-native | High | Yes |

## System Requirements

### Minimum Requirements

- **Java**: OpenJDK 21 LTS or Eclipse Temurin 21
- **Memory**: 512MB RAM (1GB recommended)
- **CPU**: 1 core (2 cores recommended)
- **Disk**: 500MB for application + 1GB for logs and cache
- **Network**: Internet access for GitHub API

### Recommended Production Configuration

- **Java**: Eclipse Temurin 21 (LTS)
- **Memory**: 2GB RAM
- **CPU**: 2 cores
- **Disk**: 5GB (2GB for application, 3GB for logs)
- **Network**: Dedicated outbound connection to GitHub API

### External Dependencies

#### Required

- **GitHub API**: `https://api.github.com`
  - Rate limits: 5000 requests/hour (authenticated)
  - Ensure firewall allows HTTPS to GitHub

#### Optional

- **Jenkins**: For build status badges
- **Snyk**: For security test badges
- **Shields.io**: For custom badges

## JAR Deployment

### Building the JAR

```bash
# Build production JAR
mvn clean package -DskipTests

# JAR location
ls target/source-code-portal-*.jar
```

### Running as Spring Boot Application

```bash
# Run directly
java -jar target/source-code-portal-*.jar

# Run with profile
java -jar target/source-code-portal-*.jar --spring.profiles.active=prod

# Run with JVM options
java -Xmx2g -Xms1g -jar target/source-code-portal-*.jar
```

### Recommended JVM Options

```bash
java \
  -Xmx2g \
  -Xms1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dspring.profiles.active=prod \
  -Dserver.port=9090 \
  -jar target/source-code-portal-*.jar
```

### Running as Systemd Service

Create `/etc/systemd/system/sourcecodeportal.service`:

```ini
[Unit]
Description=Source Code Portal
After=network.target

[Service]
Type=simple
User=scp
WorkingDirectory=/opt/sourcecodeportal
ExecStart=/usr/bin/java \
  -Xmx2g \
  -Xms1g \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  -jar /opt/sourcecodeportal/source-code-portal.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
# Create user
sudo useradd -r -s /bin/false scp

# Create directory
sudo mkdir -p /opt/sourcecodeportal
sudo cp target/source-code-portal-*.jar /opt/sourcecodeportal/source-code-portal.jar
sudo chown -R scp:scp /opt/sourcecodeportal

# Install service
sudo systemctl daemon-reload
sudo systemctl enable sourcecodeportal
sudo systemctl start sourcecodeportal

# Check status
sudo systemctl status sourcecodeportal

# View logs
sudo journalctl -u sourcecodeportal -f
```

## Kubernetes Deployment

### Basic Deployment

Create `deployment.yaml`:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: sourcecodeportal

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: scp-config
  namespace: sourcecodeportal
data:
  application.yml: |
    spring:
      profiles:
        active: prod
    server:
      port: 9090
    github:
      organization: YourOrg

---
apiVersion: v1
kind: Secret
metadata:
  name: scp-secrets
  namespace: sourcecodeportal
type: Opaque
stringData:
  github-client-id: YOUR_CLIENT_ID
  github-client-secret: YOUR_CLIENT_SECRET
  github-access-token: ghp_YOUR_ACCESS_TOKEN

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sourcecodeportal
  namespace: sourcecodeportal
spec:
  replicas: 2
  selector:
    matchLabels:
      app: sourcecodeportal
  template:
    metadata:
      labels:
        app: sourcecodeportal
    spec:
      containers:
      - name: sourcecodeportal
        image: cantara/sourcecodeportal:0.10.17
        ports:
        - containerPort: 9090
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: github.oauth2.client.clientId
          valueFrom:
            secretKeyRef:
              name: scp-secrets
              key: github-client-id
        - name: github.oauth2.client.clientSecret
          valueFrom:
            secretKeyRef:
              name: scp-secrets
              key: github-client-secret
        - name: github.client.accessToken
          valueFrom:
            secretKeyRef:
              name: scp-secrets
              key: github-access-token
        volumeMounts:
        - name: config
          mountPath: /home/sourcecodeportal/config_override
          readOnly: true
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 9090
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 9090
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
      volumes:
      - name: config
        configMap:
          name: scp-config

---
apiVersion: v1
kind: Service
metadata:
  name: sourcecodeportal
  namespace: sourcecodeportal
spec:
  selector:
    app: sourcecodeportal
  ports:
  - port: 80
    targetPort: 9090
    name: http
  type: ClusterIP

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: sourcecodeportal
  namespace: sourcecodeportal
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - scp.example.com
    secretName: scp-tls
  rules:
  - host: scp.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: sourcecodeportal
            port:
              number: 80
```

Deploy:

```bash
# Apply configuration
kubectl apply -f deployment.yaml

# Check deployment
kubectl get pods -n sourcecodeportal
kubectl get svc -n sourcecodeportal
kubectl get ingress -n sourcecodeportal

# View logs
kubectl logs -f deployment/sourcecodeportal -n sourcecodeportal

# Scale deployment
kubectl scale deployment/sourcecodeportal --replicas=3 -n sourcecodeportal
```

### Health Probes

Spring Boot Actuator provides health endpoints for Kubernetes probes:

- **Liveness**: `/actuator/health/liveness` - Is the application running?
- **Readiness**: `/actuator/health/readiness` - Can the application accept traffic?

Configure in `application.yml`:

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

## Configuration

### Configuration File Hierarchy

Configuration is loaded in this order (later overrides earlier):

1. `src/main/resources/application-defaults.properties` (built-in defaults)
2. `application.properties` (custom overrides)
3. `security.properties` (credentials)
4. `application_override.properties` (final overrides)
5. Environment variables with `SCP_` prefix
6. System properties (`-D` flags)

### Environment Variables

All configuration properties can be set via environment variables:

```bash
# GitHub configuration
export SCP_GITHUB_ACCESS_TOKEN=ghp_your_token
export SCP_GITHUB_ORGANIZATION=YourOrg

# Server configuration
export SCP_SERVER_PORT=9090

# Cache configuration
export SCP_CACHE_TTL=30

# Run application
java -jar source-code-portal.jar
```

### Production Configuration File

Create `application-prod.yml`:

```yaml
spring:
  profiles:
    active: prod
  threads:
    virtual:
      enabled: true  # Enable virtual threads for better I/O performance

server:
  port: 9090
  shutdown: graceful
  undertow:
    threads:
      io: 4
      worker: 20

github:
  organization: ${GITHUB_ORGANIZATION:Cantara}
  access-token: ${GITHUB_ACCESS_TOKEN:}
  repository:
    visibility: all  # all, public, or private
  oauth2:
    client:
      clientId: ${GITHUB_CLIENT_ID:}
      clientSecret: ${GITHUB_CLIENT_SECRET:}

cache:
  ttl: 30  # minutes
  max-size: 10000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    no.cantara: INFO
    org.springframework: WARN
  file:
    name: logs/application.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 7
```

Run with production profile:

```bash
java -jar source-code-portal.jar --spring.profiles.active=prod
```

## Security

### GitHub Authentication

#### Personal Access Token (Recommended)

Generate token at: https://github.com/settings/tokens

Required scopes:

- `repo` (full repository access)
- `read:org` (read organization data)
- `read:user` (read user profile)

Configure:

```bash
export SCP_GITHUB_ACCESS_TOKEN=ghp_your_token
```

#### OAuth Application

1. Create OAuth app: https://github.com/settings/developers
2. Set callback URL: `http://your-server:9090/oauth/callback`
3. Configure:

```bash
export SCP_GITHUB_CLIENT_ID=your_client_id
export SCP_GITHUB_CLIENT_SECRET=your_client_secret
```

### Webhook Security

Secure webhook endpoint with secret:

```bash
export SCP_GITHUB_WEBHOOK_SECRET=your_webhook_secret
```

Configure in GitHub webhook settings:

- URL: `https://your-server.com/github/webhook`
- Content type: `application/json`
- Secret: Same as `SCP_GITHUB_WEBHOOK_SECRET`

### Secrets Management

#### Using Environment Variables (Simple)

```bash
# Load secrets from file
source /etc/scp/secrets.env
java -jar source-code-portal.jar
```

#### Using Kubernetes Secrets (Recommended)

```bash
# Create secret
kubectl create secret generic scp-secrets \
  --from-literal=github-access-token=ghp_token \
  -n sourcecodeportal

# Reference in deployment
# (See Kubernetes deployment section)
```

#### Using HashiCorp Vault (Enterprise)

```bash
# Fetch secrets from Vault
export SCP_GITHUB_ACCESS_TOKEN=$(vault kv get -field=token secret/scp/github)
java -jar source-code-portal.jar
```

## SSL/TLS Configuration

### Using Reverse Proxy (Recommended)

Use Nginx or similar as SSL termination:

```nginx
server {
    listen 443 ssl http2;
    server_name scp.example.com;

    ssl_certificate /etc/ssl/certs/scp.crt;
    ssl_certificate_key /etc/ssl/private/scp.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://localhost:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Using Spring Boot SSL (Alternative)

Configure in `application-prod.yml`:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.jks
    key-store-password: changeit
    key-store-type: JKS
    key-alias: tomcat
```

Generate keystore:

```bash
keytool -genkeypair \
  -alias tomcat \
  -keyalg RSA \
  -keysize 2048 \
  -storetype JKS \
  -keystore keystore.jks \
  -validity 3650 \
  -storepass changeit
```

## Production Checklist

Before deploying to production:

### Configuration

- [ ] GitHub OAuth credentials configured
- [ ] GitHub access token generated
- [ ] Repository groups defined in `config.json`
- [ ] Environment variables set
- [ ] SSL/TLS certificates configured
- [ ] Webhook secret configured

### Security

- [ ] Secrets stored securely (not in code)
- [ ] GitHub token has minimum required scopes
- [ ] Firewall rules configured
- [ ] HTTPS enabled
- [ ] Webhook endpoint secured

### Monitoring

- [ ] Health checks configured
- [ ] Prometheus metrics enabled
- [ ] Log aggregation configured
- [ ] Alert thresholds defined
- [ ] On-call rotation established

### Performance

- [ ] Resource limits set (CPU, memory)
- [ ] Cache TTL configured appropriately
- [ ] Virtual threads enabled
- [ ] Connection pooling configured
- [ ] Rate limiting implemented

### Backup & Recovery

- [ ] Configuration files backed up
- [ ] Disaster recovery plan documented
- [ ] Rollback procedure tested
- [ ] Database backups scheduled (if using DB)

### Testing

- [ ] Load testing completed
- [ ] Failover testing completed
- [ ] Health check endpoints tested
- [ ] Webhook delivery tested
- [ ] Rate limit handling tested

## Best Practices

### High Availability

1. **Run multiple replicas** (Kubernetes: `replicas: 3`)
2. **Use health checks** for automatic restart
3. **Configure graceful shutdown** (`server.shutdown: graceful`)
4. **Implement circuit breakers** (already done via Resilience4j)
5. **Use load balancer** for traffic distribution

### Performance Optimization

1. **Enable virtual threads** for better I/O performance
2. **Configure cache appropriately** (TTL, max size)
3. **Set JVM heap size** based on memory available
4. **Use G1GC** for better garbage collection
5. **Monitor thread pool usage** via actuator

### Monitoring & Observability

1. **Enable Spring Boot Actuator**
2. **Export metrics to Prometheus**
3. **Set up Grafana dashboards**
4. **Configure log aggregation** (ELK, Splunk)
5. **Set up alerts** for critical issues

### Security Hardening

1. **Use minimal container images** (Alpine-based)
2. **Run as non-root user**
3. **Scan images for vulnerabilities** (Trivy, Snyk)
4. **Keep dependencies updated**
5. **Implement rate limiting** on public endpoints

### Cost Optimization

1. **Right-size resources** (don't over-provision)
2. **Use caching effectively** to reduce API calls
3. **Implement request batching** where possible
4. **Monitor GitHub API usage** to avoid rate limits
5. **Use spot instances** for non-critical environments

## Next Steps

- [Docker Guide](docker.md) - Container deployment
- [Monitoring Guide](monitoring.md) - Set up monitoring
- [Troubleshooting Guide](troubleshooting.md) - Common issues
