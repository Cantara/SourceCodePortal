# Dashboard

The Dashboard is the main interface of Source Code Portal, providing a unified view of all repository groups, commit activity, documentation, and status metrics across your GitHub organization.

## Overview

The Dashboard aggregates data from multiple GitHub repositories and presents it in an organized, easy-to-navigate interface. It serves as the central hub for monitoring code activity, viewing documentation, and tracking build/security status.

**Key Capabilities:**
- Display repository groups with commit counts
- Show recent commit activity across all repositories
- Render documentation (Markdown/AsciiDoc) inline
- Display build status badges (Jenkins, GitHub Actions)
- Show security scan results (Snyk)
- Real-time updates via GitHub webhooks

## Dashboard Views

### 1. Main Dashboard (`/dashboard` or `/`)

The main dashboard displays all repository groups configured in `config.json`.

**Features:**
- **Group Cards**: Each repository group is shown as a card with:
  - Group name and description
  - Number of repositories in the group
  - Recent commit count (last 24 hours)
  - Quick links to group details
- **Commit Activity Timeline**: Recent commits across all repositories
- **Status Summary**: Overall health indicators for GitHub API, cache, and executors

**Endpoint**: `http://localhost:9090/dashboard`

**Controller**: `no.cantara.docsite.controller.spring.DashboardWebController`

### 2. Group View (`/group/{groupId}`)

Detailed view of a specific repository group showing all repositories in that group.

**Features:**
- **Repository List**: All repositories matching the group's artifact patterns
- **Per-Repository Cards**: Each repository shows:
  - Repository name, description, and URL
  - Latest commit information (author, message, timestamp)
  - Build status badge (Jenkins/GitHub Actions)
  - Security status badge (Snyk)
  - Stars, forks, and watchers count
  - Primary language and license
- **Group Documentation**: Rendered README from the default group repository
- **Commit History**: Recent commits specific to this group

**Endpoint**: `http://localhost:9090/group/whydah`

**Controller**: `no.cantara.docsite.controller.spring.GroupWebController`

### 3. Repository Contents (`/contents/{org}/{repo}/{branch}`)

View the contents and documentation of a specific repository.

**Features:**
- **Documentation Rendering**:
  - Markdown (.md) files rendered with GitHub-flavored Markdown
  - AsciiDoc (.adoc, .asciidoc) files rendered with AsciiDoctor
  - Syntax highlighting for code blocks
  - Automatic table of contents generation
  - Image embedding support
- **File Browser**: Navigate repository directory structure
- **Branch Selection**: Switch between branches to view different versions
- **Commit Context**: Show which commit the content is from

**Endpoint**: `http://localhost:9090/contents/Cantara/Whydah-UserAdminService/master`

**Controller**: `no.cantara.docsite.controller.spring.ContentsWebController`

### 4. Commit History (`/commits/{org}/{repo}`)

View the commit log for a specific repository.

**Features:**
- **Commit List**: Chronological list of commits with:
  - Commit SHA (short and full)
  - Author name and avatar
  - Commit message
  - Timestamp (relative and absolute)
  - Files changed count
- **Commit Details**: Click to expand and see:
  - Full commit message
  - List of changed files
  - Diff stats (additions/deletions)
- **Pagination**: Navigate through commit history
- **Filtering**: Filter by author, date range, or message content

**Endpoint**: `http://localhost:9090/commits/Cantara/Whydah-UserAdminService`

**Controller**: `no.cantara.docsite.controller.spring.CommitsWebController`

## Documentation Rendering

Source Code Portal can render documentation in multiple formats directly in the dashboard.

### Supported Formats

#### 1. Markdown (.md)

Rendered using a GitHub-flavored Markdown parser with support for:
- Headers (H1-H6)
- Bold, italic, strikethrough
- Ordered and unordered lists
- Code blocks with syntax highlighting
- Tables
- Blockquotes
- Links and images
- Task lists (checkboxes)
- Emoji shortcodes

**Example**:
```markdown
# My Project

## Features
- Feature 1
- Feature 2

## Code Example
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```
```

#### 2. AsciiDoc (.adoc, .asciidoc)

Rendered using AsciiDoctor with support for:
- Document structure (parts, chapters, sections)
- Admonitions (NOTE, TIP, WARNING, IMPORTANT, CAUTION)
- Code blocks with callouts
- Tables with complex formatting
- Include directives
- Conditional text
- Bibliography and footnotes

**Example**:
```asciidoc
= My Project Documentation
:toc:
:icons: font

== Introduction

NOTE: This is an important note.

[source,java]
----
public class Example {
    public void method() {
        // Code here
    }
}
----
```

### Rendering Pipeline

```
1. Fetch content from GitHub API
   ↓
2. Identify file format (.md, .adoc)
   ↓
3. Parse with appropriate renderer
   ↓
4. Apply syntax highlighting (code blocks)
   ↓
5. Inject Thymeleaf template fragments
   ↓
6. Render final HTML
```

**Renderer Classes**:
- `no.cantara.docsite.domain.renderer.MarkdownRenderer` - Markdown processing
- `no.cantara.docsite.domain.renderer.AsciiDocRenderer` - AsciiDoc processing

## Activity Tracking

The Dashboard tracks and displays commit activity across all repositories.

### Real-Time Updates

**GitHub Webhooks** enable real-time updates when events occur:
- **Push Events**: New commits trigger cache refresh
- **Release Events**: New releases update release information
- **Branch/Tag Creation**: New branches/tags appear immediately

**Configuration**: See [Webhooks](webhooks.md) documentation.

### Commit Activity Display

**Recent Commits View**:
- Shows commits from the last 24 hours (configurable)
- Grouped by repository
- Sorted by timestamp (newest first)
- Includes author avatar, name, and commit message
- Direct link to commit on GitHub

**Commit Statistics**:
- Total commits today/this week/this month
- Commits per repository
- Most active repositories
- Top contributors

## Status Badges

The Dashboard integrates with multiple external services to display status badges.

### 1. Build Status (Jenkins)

Shows the current build status for each repository.

**Badge States**:
- **Green (Success)**: Last build passed
- **Yellow (Unstable)**: Build passed with warnings
- **Red (Failure)**: Last build failed
- **Gray (Unknown)**: No build information available
- **Animated (Building)**: Build in progress

**Configuration**: Set Jenkins URL and job prefix in `config.json`:
```json
{
  "groupId": "whydah",
  "jenkins": {
    "prefix": "Whydah-"
  }
}
```

### 2. Security Status (Snyk)

Shows security vulnerability scan results from Snyk.

**Badge States**:
- **Green**: No vulnerabilities found
- **Yellow**: Low/medium severity vulnerabilities
- **Red**: High/critical severity vulnerabilities
- **Gray**: Not scanned or scan failed

**Configuration**: Set Snyk organization and project prefix in `config.json`:
```json
{
  "groupId": "whydah",
  "snyk": {
    "organization": "cantara",
    "projectPrefix": "whydah-"
  }
}
```

### 3. Custom Badges (Shields.io)

Display custom badges from Shields.io for metrics like:
- Code coverage
- License type
- Latest version
- Dependencies status
- Docker pulls

**Configuration**: Add shields to `config.json`:
```json
{
  "groupId": "whydah",
  "shields": [
    {
      "label": "coverage",
      "message": "85%",
      "color": "brightgreen"
    }
  ]
}
```

## Caching Strategy

The Dashboard uses an aggressive caching strategy to minimize GitHub API calls and provide fast page loads.

### Cache Types

1. **Repository Cache**: Basic repository metadata (name, description, URL)
   - TTL: 1 hour
   - Refreshed on webhook push events

2. **Commit Cache**: Commit history per repository
   - TTL: 5 minutes
   - Refreshed on webhook push events

3. **Content Cache**: Documentation content (README files)
   - TTL: 10 minutes
   - Refreshed on webhook push events

4. **Build Status Cache**: Jenkins/GitHub Actions status
   - TTL: 2 minutes
   - Refreshed on Jenkins webhook (if configured)

5. **Snyk Badge Cache**: Security scan results
   - TTL: 15 minutes
   - Refreshed on scheduled task

### Cache Warming

On application startup, the `PreFetchData` service warms up caches by:
1. Loading all repository groups from `config.json`
2. Fetching repository list from GitHub for each group
3. Fetching recent commits for each repository
4. Fetching README content for default group repositories
5. Fetching build status for all repositories
6. Fetching Snyk badges for all repositories

This ensures the first dashboard load is fast.

## Performance Considerations

### GitHub API Rate Limits

The Dashboard is designed to work within GitHub's API rate limits:
- **Authenticated**: 5000 requests/hour
- **Unauthenticated**: 60 requests/hour

**Mitigation Strategies**:
1. Aggressive caching (reduces API calls)
2. Circuit breaker pattern (prevents cascading failures)
3. Conditional requests with ETags (GitHub returns 304 Not Modified when content unchanged)
4. GitHub webhooks (eliminates polling)

**Monitoring**: Check rate limit status at `/actuator/health/github`

### Circuit Breaker

All external API calls use Resilience4j circuit breakers:
- **Failure Threshold**: 50% (opens after 50% of calls fail)
- **Open State Duration**: 60 seconds
- **Half-Open Test Requests**: 3 requests
- **Bulkhead**: Max 25 concurrent calls
- **Timeout**: 75 seconds per call

**Benefits**:
- Prevents cascading failures
- Fails fast when external services are down
- Automatically recovers when services return

## Thymeleaf Templates

The Dashboard is rendered using Thymeleaf server-side templates.

### Template Structure

```
src/main/resources/META-INF/views/
├── template.html           # Base layout (header, footer, navigation)
├── index.html              # Dashboard view
├── group/
│   └── card.html          # Group detail view
├── contents/
│   └── content.html       # Repository content view
├── commits/
│   └── commits.html       # Commit history view
└── fragments/
    ├── repository.html    # Repository card fragment
    ├── commit.html        # Commit list item fragment
    └── badge.html         # Status badge fragment
```

### Template Features

**Thymeleaf Capabilities Used**:
- **Layouts**: `th:replace` for template composition
- **Conditionals**: `th:if`, `th:unless` for conditional rendering
- **Iteration**: `th:each` for lists of repositories/commits
- **Text Interpolation**: `th:text`, `th:utext` for safe/unsafe HTML
- **Attributes**: `th:href`, `th:src` for dynamic URLs
- **Fragments**: `th:fragment` for reusable components

**Example**:
```html
<div th:each="repo : ${repositories}">
    <h3 th:text="${repo.name}">Repository Name</h3>
    <p th:text="${repo.description}">Description</p>
    <a th:href="@{/contents/{org}/{repo}(org=${repo.owner},repo=${repo.name})}">
        View Documentation
    </a>
</div>
```

## REST API Endpoints

While the Dashboard is primarily a web UI, it exposes REST endpoints for programmatic access:

### Health Check
```bash
GET /actuator/health
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
          "remaining": 4850,
          "limit": 5000
        }
      }
    },
    "cache": {
      "status": "UP"
    }
  }
}
```

### Application Info
```bash
GET /actuator/info
```

Response:
```json
{
  "app": {
    "name": "Source Code Portal",
    "version": "0.10.17-SNAPSHOT",
    "java": {
      "version": "21.0.1"
    }
  }
}
```

## Customization

### Custom CSS/Styling

Styles are written in Sass/SCSS and compiled to CSS.

**Source**: `src/main/sass/scss/`
**Output**: `target/classes/META-INF/views/css/`

To customize styling:
1. Edit `.scss` files in `src/main/sass/scss/`
2. Compile with: `sass --watch src/main/sass/scss:target/classes/META-INF/views/css`
3. Changes are reflected immediately (no restart needed)

### Custom Templates

To customize templates:
1. Edit Thymeleaf templates in `src/main/resources/META-INF/views/`
2. Rebuild the project: `mvn clean compile`
3. Restart the application

## Troubleshooting

### Dashboard Not Loading

**Issue**: Dashboard shows loading spinner indefinitely.

**Possible Causes**:
1. GitHub API rate limit exceeded
2. Invalid GitHub access token
3. Network connectivity issues

**Solutions**:
1. Check rate limit: `curl -H "Authorization: token YOUR_TOKEN" https://api.github.com/rate_limit`
2. Verify token: Check `security.properties` or `SCP_GITHUB_ACCESS_TOKEN` env var
3. Check logs: `tail -f logs/application.log`

### Stale Data

**Issue**: Dashboard shows outdated commit information.

**Possible Causes**:
1. Cache not expiring
2. Webhooks not configured
3. Scheduled refresh not running

**Solutions**:
1. Check cache health: `http://localhost:9090/actuator/health/cache`
2. Verify webhook configuration: See [Webhooks](webhooks.md)
3. Check scheduled tasks: `http://localhost:9090/actuator/scheduledtasks`

### Missing Repositories

**Issue**: Some repositories not appearing in groups.

**Possible Causes**:
1. Incorrect regex pattern in `config.json`
2. Repository visibility (private repos require proper token permissions)
3. Organization membership

**Solutions**:
1. Test regex pattern: Use a regex tester to verify pattern matches repository names
2. Verify token has `repo` scope for private repositories
3. Check GitHub organization membership

## Related Documentation

- [Repository Groups](repository-groups.md) - Configure repository grouping
- [Integrations](integrations.md) - Set up Jenkins, Snyk, Shields.io
- [Webhooks](webhooks.md) - Configure real-time updates
- [Observability](observability.md) - Monitor dashboard health
- [Architecture Overview](../architecture/overview.md) - System design
