# GitHub Webhooks

GitHub webhooks enable real-time updates in Source Code Portal by notifying the application when events occur in your repositories. This eliminates the need for polling and provides instant cache updates.

## Overview

Without webhooks, SCP must poll the GitHub API periodically to check for new commits, releases, and other changes. This:
- Wastes API rate limit quota
- Introduces latency (updates only as frequent as polling interval)
- Increases server load

With webhooks, GitHub pushes notifications to SCP immediately when events occur, providing:
- **Real-time updates**: See new commits within seconds
- **Reduced API calls**: Only fetch data when changes occur
- **Lower latency**: No polling interval delay
- **Better user experience**: Dashboard always shows latest information

## Supported Events

SCP supports the following GitHub webhook events:

### 1. Push Events

Triggered when commits are pushed to a repository.

**Use Case**: Update commit history and documentation immediately after push.

**What SCP Does**:
1. Invalidates commit cache for the affected repository
2. Fetches latest commits from GitHub
3. Updates documentation if README changed
4. Refreshes repository metadata

**Event Payload** (abbreviated):
```json
{
  "ref": "refs/heads/master",
  "repository": {
    "name": "Whydah-UserAdminService",
    "full_name": "Cantara/Whydah-UserAdminService"
  },
  "commits": [
    {
      "id": "abc123...",
      "message": "Fix authentication bug",
      "author": {
        "name": "John Doe",
        "email": "john@example.com"
      }
    }
  ]
}
```

### 2. Release Events

Triggered when a release is created, edited, or deleted.

**Use Case**: Update release information on repository cards.

**What SCP Does**:
1. Invalidates release cache
2. Fetches latest release information
3. Updates version badges

**Event Payload** (abbreviated):
```json
{
  "action": "published",
  "release": {
    "tag_name": "v1.2.3",
    "name": "Version 1.2.3",
    "body": "Release notes..."
  },
  "repository": {
    "name": "Whydah-UserAdminService",
    "full_name": "Cantara/Whydah-UserAdminService"
  }
}
```

### 3. Branch or Tag Creation

Triggered when a branch or tag is created.

**Use Case**: Update branch list for documentation browsing.

**What SCP Does**:
1. Invalidates branch cache
2. Fetches updated branch list
3. Makes new branch available for content viewing

**Event Payload** (abbreviated):
```json
{
  "ref": "refs/heads/feature/new-feature",
  "ref_type": "branch",
  "repository": {
    "name": "Whydah-UserAdminService",
    "full_name": "Cantara/Whydah-UserAdminService"
  }
}
```

## Configuration

### Step 1: Configure Webhook Secret

Set a webhook secret for HMAC signature validation.

**In `security.properties`:**
```properties
github.webhook.securityAccessToken=your-secret-token-here
```

**Or via environment variable:**
```bash
export SCP_GITHUB_WEBHOOK_SECURITY_ACCESS_TOKEN=your-secret-token-here
```

**Generating a Strong Secret:**
```bash
# Generate a random 32-character secret
openssl rand -hex 32
```

### Step 2: Configure Webhook in GitHub

1. Go to your GitHub organization settings:
   ```
   https://github.com/organizations/YOUR_ORG/settings/hooks
   ```

2. Click **Add webhook**

3. Configure webhook:

   **Payload URL**:
   ```
   https://your-server.com/github/webhook
   ```

   **Content type**:
   ```
   application/json
   ```

   **Secret**:
   ```
   your-secret-token-here
   ```
   (Must match the secret configured in Step 1)

   **SSL verification**:
   ```
   ☑ Enable SSL verification (recommended)
   ```

4. Select events to trigger webhook:
   - ☑ Branch or tag creation
   - ☑ Pushes
   - ☑ Releases

5. Click **Add webhook**

### Step 3: Verify Configuration

After adding the webhook, GitHub will send a `ping` event.

**Check webhook status**:
1. Go to webhook settings in GitHub
2. Click on the webhook
3. Check "Recent Deliveries" tab
4. Verify `ping` event shows ✓ (green checkmark)

**Check SCP logs**:
```bash
tail -f logs/application.log | grep "webhook"
```

You should see:
```
INFO  GitHubWebhookRestController - Received webhook ping event
INFO  GitHubWebhookRestController - Webhook ping successful
```

## Webhook Endpoint

SCP exposes a webhook endpoint at `/github/webhook`.

**Endpoint**: `POST /github/webhook`

**Controller**: `no.cantara.docsite.controller.spring.GitHubWebhookRestController`

**Request Headers**:
- `X-GitHub-Event`: Event type (e.g., `push`, `release`, `create`)
- `X-Hub-Signature-256`: HMAC SHA256 signature for payload verification
- `Content-Type`: `application/json`

**Response**:
- `200 OK`: Webhook processed successfully
- `400 Bad Request`: Invalid payload or missing headers
- `401 Unauthorized`: Invalid signature (secret mismatch)
- `500 Internal Server Error`: Processing error

## Security

### HMAC Signature Validation

SCP validates webhook payloads using HMAC SHA256 signatures to ensure they originate from GitHub.

**How It Works**:
1. GitHub signs the payload with your secret using HMAC SHA256
2. GitHub sends the signature in the `X-Hub-Signature-256` header
3. SCP computes the signature using the same secret
4. SCP compares the computed signature with the received signature
5. If signatures match, payload is authentic; otherwise, request is rejected

**Implementation**:
```java
public boolean isValidSignature(String payload, String signature, String secret) {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
    byte[] computedHash = mac.doFinal(payload.getBytes());
    String computedSignature = "sha256=" + bytesToHex(computedHash);
    return computedSignature.equals(signature);
}
```

**Security Benefits**:
- Prevents unauthorized webhook calls
- Ensures payload integrity (not tampered with)
- Protects against replay attacks (signature tied to specific payload)

### Best Practices

1. **Use HTTPS**: Always use HTTPS for webhook URLs
   - Encrypts payload in transit
   - Prevents man-in-the-middle attacks

2. **Strong Secret**: Use a long, random secret
   - Generate with `openssl rand -hex 32`
   - Don't use predictable secrets like "password123"

3. **Environment Variables**: Store secret in environment variables, not in code
   - Use `SCP_GITHUB_WEBHOOK_SECURITY_ACCESS_TOKEN`
   - Don't commit `security.properties` to git

4. **Enable SSL Verification**: Always enable SSL verification in GitHub webhook settings
   - Verifies your server's SSL certificate is valid
   - Prevents impersonation attacks

5. **Monitor Webhook Deliveries**: Regularly check GitHub's "Recent Deliveries" tab
   - Look for failed deliveries (red X)
   - Investigate failed deliveries promptly

## Local Development with ngrok

For local development, use [ngrok](https://ngrok.com/) to expose your local server to GitHub.

### Setup

1. **Install ngrok**:
   ```bash
   # macOS (Homebrew)
   brew install ngrok

   # Linux (download binary)
   wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz
   tar -xzf ngrok-v3-stable-linux-amd64.tgz
   sudo mv ngrok /usr/local/bin/
   ```

2. **Start SCP locally**:
   ```bash
   mvn spring-boot:run
   ```

3. **Start ngrok tunnel**:
   ```bash
   ngrok http 9090
   ```

   Output:
   ```
   Forwarding   https://abc123.ngrok.io -> http://localhost:9090
   ```

4. **Configure webhook in GitHub**:
   - Payload URL: `https://abc123.ngrok.io/github/webhook`
   - Secret: Your configured secret
   - Events: Push, Release, Branch/Tag creation

5. **Test webhook**:
   - Push a commit to a repository
   - Check ngrok console for incoming webhook request
   - Check SCP logs for webhook processing

### ngrok Features

**Web Interface**: http://localhost:4040
- View all HTTP requests
- Inspect request/response headers and bodies
- Replay requests for testing

**Persistent URLs**: Upgrade to ngrok paid plan for persistent URLs
- Free tier: URLs change on every restart
- Paid tier: Reserve a custom subdomain (e.g., `your-app.ngrok.io`)

### Alternative: localtunnel

[localtunnel](https://localtunnel.github.io/www/) is a free alternative to ngrok:

```bash
# Install
npm install -g localtunnel

# Start tunnel
lt --port 9090 --subdomain your-subdomain
```

## Cache Invalidation

When a webhook event is received, SCP invalidates relevant caches to ensure fresh data.

### Cache Invalidation Strategy

**Push Event** → Invalidates:
- Commit cache for the repository
- Content cache (if README changed)
- Repository metadata cache

**Release Event** → Invalidates:
- Release cache for the repository
- Repository metadata cache

**Branch/Tag Creation** → Invalidates:
- Branch list cache
- Repository metadata cache

### Cache Invalidation Implementation

```java
@RestController
@RequestMapping("/github/webhook")
public class GitHubWebhookRestController {

    private final CacheStore cacheStore;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("X-GitHub-Event") String event,
        @RequestHeader("X-Hub-Signature-256") String signature
    ) {
        // Validate signature
        if (!isValidSignature(payload, signature)) {
            return ResponseEntity.status(401).build();
        }

        // Parse payload
        WebhookPayload webhookPayload = parsePayload(payload);
        String repoName = webhookPayload.getRepository().getName();

        // Invalidate caches based on event type
        switch (event) {
            case "push":
                cacheStore.invalidateCommits(repoName);
                cacheStore.invalidateContent(repoName);
                break;
            case "release":
                cacheStore.invalidateReleases(repoName);
                break;
            case "create":
                cacheStore.invalidateBranches(repoName);
                break;
        }

        return ResponseEntity.ok().build();
    }
}
```

## Monitoring Webhooks

### GitHub Webhook Dashboard

GitHub provides a dashboard to monitor webhook deliveries:

1. Go to webhook settings:
   ```
   https://github.com/organizations/YOUR_ORG/settings/hooks/{webhook-id}
   ```

2. Click **Recent Deliveries** tab

3. Review deliveries:
   - ✓ Green checkmark: Delivery successful
   - ✗ Red X: Delivery failed

4. Click on a delivery to see:
   - Request headers and body
   - Response headers and body
   - Response time

### SCP Health Indicator

SCP provides a custom health indicator for webhook status:

**Endpoint**: `/actuator/health/webhook`

**Response**:
```json
{
  "status": "UP",
  "details": {
    "lastWebhookReceived": "2024-01-28T10:30:00Z",
    "webhookCount": 42,
    "failedWebhooks": 0
  }
}
```

**Status**:
- **UP**: Webhooks received in last 24 hours
- **DEGRADED**: No webhooks received in last 24 hours (possible configuration issue)
- **DOWN**: Webhook endpoint unreachable or signature validation failing

### Application Logs

Check application logs for webhook activity:

```bash
# View recent webhook events
tail -f logs/application.log | grep "webhook"

# Count webhook events by type
grep "Received webhook" logs/application.log | awk '{print $5}' | sort | uniq -c
```

**Example Output**:
```
INFO  GitHubWebhookRestController - Received webhook push event for Whydah-UserAdminService
INFO  GitHubWebhookRestController - Invalidated commit cache for Whydah-UserAdminService
INFO  GitHubWebhookRestController - Webhook processing completed in 45ms
```

## Troubleshooting

### Webhook Delivery Failures

**Issue**: GitHub shows red X for webhook deliveries.

**Possible Causes**:
1. SCP server unreachable (firewall, DNS, or server down)
2. Invalid SSL certificate (GitHub SSL verification enabled)
3. Webhook endpoint returns error (4xx or 5xx)

**Solutions**:
1. Verify server is reachable: `curl https://your-server.com/github/webhook`
2. Check SSL certificate: `curl -v https://your-server.com/github/webhook`
3. Check SCP logs for errors: `tail -f logs/application.log`

### Signature Validation Failures

**Issue**: SCP logs show "Invalid webhook signature" errors.

**Possible Causes**:
1. Secret mismatch between GitHub and SCP
2. Payload modified in transit (HTTPS required)
3. Wrong signature algorithm (must be SHA256)

**Solutions**:
1. Verify secret matches:
   - GitHub: Check webhook settings
   - SCP: Check `security.properties` or `SCP_GITHUB_WEBHOOK_SECURITY_ACCESS_TOKEN`
2. Use HTTPS for webhook URL
3. Verify GitHub sends `X-Hub-Signature-256` header (not `X-Hub-Signature`)

### Webhooks Not Triggering Updates

**Issue**: Webhooks are delivered successfully, but dashboard doesn't update.

**Possible Causes**:
1. Cache invalidation not working
2. Scheduled refresh overwriting webhook updates
3. Browser cache showing stale data

**Solutions**:
1. Check cache invalidation logs: `grep "Invalidated" logs/application.log`
2. Verify cache TTL settings: `/actuator/health/cache`
3. Hard refresh browser: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (macOS)

### ngrok Issues

**Issue**: ngrok URL changes on every restart.

**Solution**: Upgrade to ngrok paid plan for persistent URLs, or use localtunnel.

**Issue**: ngrok tunnel shows "502 Bad Gateway".

**Possible Causes**:
1. SCP not running
2. SCP running on different port

**Solutions**:
1. Start SCP: `mvn spring-boot:run`
2. Verify port: `lsof -i :9090`

## Performance Considerations

### Webhook Processing Time

Webhook processing should be fast to avoid timeouts:

**Target**: <100ms for webhook processing

**Current Performance**:
- Signature validation: <1ms
- Payload parsing: <5ms
- Cache invalidation: <10ms
- Total: <20ms

**GitHub Timeout**: 10 seconds (plenty of headroom)

### High-Volume Scenarios

For organizations with high commit volume (>100 commits/hour):

1. **Batch Cache Invalidation**: Invalidate multiple repositories in a single operation
2. **Async Processing**: Process webhook events asynchronously (fire and forget)
3. **Rate Limiting**: Limit webhook processing to prevent overload

**Implementation**:
```java
@Async
public void processWebhookAsync(WebhookPayload payload) {
    // Process webhook in background thread
    cacheStore.invalidate(payload.getRepository().getName());
}
```

## Advanced Configuration

### Webhook Retry

GitHub automatically retries failed webhook deliveries:

- **Retry Count**: Up to 3 retries
- **Retry Delay**: Exponential backoff (5s, 15s, 45s)
- **Retry Conditions**: 5xx errors or network failures

**No retry for**:
- 4xx errors (client errors, like invalid signature)
- Successful deliveries (2xx)

### Webhook Filtering

GitHub allows filtering which repositories trigger webhooks:

**Organization-Level Webhook**: Receives events from all repositories

**Repository-Level Webhook**: Receives events from a specific repository

**Recommendation**: Use organization-level webhook for SCP (monitors all repos).

### Custom Webhook Events

To add support for additional webhook events:

1. Update event handling in `GitHubWebhookRestController`
2. Add cache invalidation logic for the new event
3. Update this documentation

**Example: Pull Request Events**
```java
case "pull_request":
    String action = webhookPayload.getAction(); // "opened", "closed", "merged"
    if ("merged".equals(action)) {
        cacheStore.invalidateCommits(repoName);
    }
    break;
```

## Related Documentation

- [Dashboard](dashboard.md) - View real-time updates on dashboard
- [Repository Groups](repository-groups.md) - Configure repository groups
- [Integrations](integrations.md) - External service integrations
- [Observability](observability.md) - Monitor webhook health
- [Architecture: Caching](../architecture/caching.md) - Cache invalidation strategy
