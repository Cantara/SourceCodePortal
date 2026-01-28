# Repository Groups

Repository Groups are a core organizational feature of Source Code Portal that allows you to logically group related repositories together, regardless of their naming conventions or GitHub organization structure.

## Overview

Instead of viewing repositories as a flat list, Repository Groups let you organize them into meaningful systems or projects. For example, all repositories related to an "IAM Platform" can be grouped together, even if they have different prefixes or belong to different GitHub organizations.

**Key Benefits:**
- **Logical Organization**: Group repositories by system, product, or team
- **Flexible Matching**: Use regex patterns to automatically include repositories
- **Team Focus**: Each team can focus on their group without noise from other repos
- **Documentation Hub**: Each group has a default repository for documentation
- **Unified Status**: See build/security status for all repos in a group at once

## Configuration

Repository groups are configured in `src/main/resources/conf/config.json`.

### Basic Structure

```json
{
  "github": {
    "organization": "YourOrgName",
    "visibility": "public"
  },
  "groups": [
    {
      "groupId": "group-identifier",
      "display-name": "Human Readable Name",
      "description": "Description of this group",
      "defaultGroupRepo": "DefaultRepositoryName",
      "artifactId": ["Pattern1", "Pattern2*", "Pattern3"]
    }
  ]
}
```

### Configuration Fields

#### Global GitHub Configuration

**`github.organization`** (required)
- The GitHub organization name to fetch repositories from
- Example: `"Cantara"`

**`github.visibility`** (optional, default: `"public"`)
- Repository visibility filter
- Options: `"public"`, `"private"`, `"all"`
- Note: Viewing private repos requires a GitHub token with `repo` scope

#### Group Configuration

**`groupId`** (required)
- Unique identifier for the group (used in URLs)
- Must be URL-safe (lowercase, no spaces)
- Example: `"whydah"`, `"monitoring-tools"`, `"config-service"`

**`display-name`** (required)
- Human-readable name shown in the UI
- Can contain spaces, capitalization, special characters
- Example: `"Whydah IAM Platform"`, `"Monitoring & Observability"`

**`description`** (optional)
- Brief description of the group's purpose
- Shown on group cards and detail pages
- Example: `"Identity and Access Management infrastructure"`

**`defaultGroupRepo`** (required)
- Name of the repository to use for group-level documentation
- This repo's README is displayed on the group page
- Example: `"Whydah-Documentation"`

**`artifactId`** (required)
- Array of repository name patterns to include in this group
- Supports wildcards and regex patterns
- Patterns are case-sensitive
- Example: `["Whydah*", "IAM-*"]`

### Example Configuration

```json
{
  "github": {
    "organization": "Cantara",
    "visibility": "public"
  },
  "groups": [
    {
      "groupId": "whydah",
      "display-name": "Whydah IAM Platform",
      "description": "Identity and Access Management infrastructure",
      "defaultGroupRepo": "Whydah-Documentation",
      "artifactId": [
        "Whydah*"
      ],
      "jenkins": {
        "prefix": "Whydah-"
      },
      "snyk": {
        "organization": "cantara",
        "projectPrefix": "whydah-"
      }
    },
    {
      "groupId": "config-service",
      "display-name": "ConfigService",
      "description": "Configuration management and distribution service",
      "defaultGroupRepo": "ConfigService",
      "artifactId": [
        "ConfigService",
        "ConfigService-*"
      ],
      "jenkins": {
        "prefix": "ConfigService-"
      }
    },
    {
      "groupId": "monitoring",
      "display-name": "Monitoring & Observability",
      "description": "Application monitoring and observability tools",
      "defaultGroupRepo": "Stingray-Documentation",
      "artifactId": [
        "Stingray*",
        "*-Monitor",
        "Grafana-*"
      ]
    }
  ]
}
```

## Pattern Matching

Repository groups use pattern matching to determine which repositories belong to which groups.

### Pattern Types

#### 1. Exact Match

Match a repository name exactly.

**Pattern**: `"ConfigService"`

**Matches**:
- `ConfigService` ✓

**Does Not Match**:
- `ConfigService-Client` ✗
- `configservice` ✗ (case-sensitive)

#### 2. Prefix Wildcard

Match all repositories starting with a prefix.

**Pattern**: `"Whydah*"`

**Matches**:
- `Whydah-UserAdminService` ✓
- `Whydah-SecurityTokenService` ✓
- `Whydah-OAuth2Service` ✓

**Does Not Match**:
- `whydah-client` ✗ (case-sensitive)
- `MyWhydah` ✗ (wildcard only at end)

#### 3. Suffix Wildcard

Match all repositories ending with a suffix.

**Pattern**: `"*-Monitor"`

**Matches**:
- `Application-Monitor` ✓
- `Database-Monitor` ✓
- `Network-Monitor` ✓

**Does Not Match**:
- `Monitor` ✗ (no prefix)
- `Monitor-Tool` ✗ (wildcard only at start)

#### 4. Complex Patterns

Combine multiple patterns to create complex matching rules.

**Pattern**: `["MyApp-*", "MyApp", "*-MyApp-*"]`

**Matches**:
- `MyApp` ✓ (exact match)
- `MyApp-Client` ✓ (prefix)
- `MyApp-Backend` ✓ (prefix)
- `Common-MyApp-Utils` ✓ (contains)

### Pattern Best Practices

1. **Use Consistent Naming**: Establish repository naming conventions in your organization
   - Good: `Platform-Service-Name` (e.g., `IAM-UserService`, `IAM-TokenService`)
   - Bad: Inconsistent naming makes grouping difficult

2. **Test Patterns**: Before deploying, test patterns to ensure they match the right repos
   - Use the Dashboard to verify which repos appear in each group
   - Check for overlap between groups

3. **Order Matters**: If a repository matches multiple groups, it appears in all of them
   - This is by design (a shared library might belong to multiple groups)
   - Use more specific patterns to avoid unintended matches

4. **Case Sensitivity**: Patterns are case-sensitive
   - `"Whydah*"` does NOT match `"whydah-client"`
   - Be consistent with repository naming case

## Group Display

### Group Card (Dashboard)

On the main dashboard, each group is displayed as a card showing:
- **Group name** (from `display-name`)
- **Description** (from `description`)
- **Repository count** (number of repos matching patterns)
- **Recent commits** (commits in the last 24 hours)
- **Quick link** to group detail page

### Group Detail Page

The group detail page (`/group/{groupId}`) shows:

1. **Group Header**:
   - Group name and description
   - Link to default group repository

2. **Repository List**:
   - All repositories matching the group's patterns
   - Each repository card shows:
     - Repository name, description, URL
     - Latest commit (author, message, time)
     - Build status badge (Jenkins/GitHub Actions)
     - Security status badge (Snyk)
     - Repository stats (stars, forks, watchers)
     - Primary language and license

3. **Group Documentation**:
   - Rendered README from the `defaultGroupRepo`
   - Markdown or AsciiDoc format
   - Syntax-highlighted code blocks
   - Embedded images

4. **Recent Activity**:
   - Commit timeline for all repos in the group
   - Filter by repository or author

## Default Group Repository

Each group must specify a `defaultGroupRepo` which serves as the documentation hub for that group.

### Purpose

The default group repository:
- Provides group-level documentation (architecture, getting started, etc.)
- Acts as the landing page for the group detail view
- Should contain a comprehensive README explaining the group

### Best Practices

1. **Create a dedicated documentation repo**:
   - Example: `Whydah-Documentation` for the Whydah group
   - Contains architecture diagrams, API docs, tutorials

2. **Use a main repository**:
   - Example: `ConfigService` for the ConfigService group
   - If the group represents a single project, use the main repo

3. **Keep it updated**:
   - The README is the first thing users see when viewing the group
   - Include links to individual repositories
   - Maintain an up-to-date architecture diagram

### Documentation Structure

Recommended README structure for default group repos:

```markdown
# Group Name

## Overview
Brief description of what this group of repositories does.

## Architecture
High-level architecture diagram and explanation.

## Repositories
List of repositories in this group with descriptions.

## Getting Started
Quick start guide for developers new to this group.

## Documentation
Links to detailed documentation for each repository.

## Support
How to get help (Slack, email, GitHub Discussions).
```

## Multiple Groups

A repository can belong to multiple groups by matching patterns in multiple group configurations.

### Example: Shared Libraries

```json
{
  "groups": [
    {
      "groupId": "platform-core",
      "display-name": "Platform Core",
      "artifactId": ["Platform-*"]
    },
    {
      "groupId": "shared-libraries",
      "display-name": "Shared Libraries",
      "artifactId": ["*-Common", "*-Utils", "*-Shared"]
    }
  ]
}
```

In this example:
- `Platform-Common` matches both groups
- It appears in both "Platform Core" and "Shared Libraries"
- This is intentional and useful for shared components

### Avoiding Unintended Overlap

To prevent unintended overlap:

1. **Use specific patterns**:
   - Instead of `"*"` (matches everything), use `"MyApp-*"`

2. **Test thoroughly**:
   - Review the dashboard to see which repos appear in which groups
   - Use `/actuator/info` to see loaded configuration

3. **Document overlap**:
   - If a repo intentionally appears in multiple groups, document why

## Integration with External Services

Repository groups can include integration configurations for external services.

### Jenkins Integration

Configure Jenkins build status for all repos in a group:

```json
{
  "groupId": "whydah",
  "jenkins": {
    "prefix": "Whydah-"
  }
}
```

This tells SCP to look for Jenkins jobs named:
- `Whydah-UserAdminService`
- `Whydah-SecurityTokenService`
- etc.

See [Integrations](integrations.md#jenkins) for details.

### Snyk Integration

Configure Snyk security scanning for all repos in a group:

```json
{
  "groupId": "whydah",
  "snyk": {
    "organization": "cantara",
    "projectPrefix": "whydah-"
  }
}
```

This tells SCP to look for Snyk projects named:
- `whydah-useradminservice`
- `whydah-securitytokenservice`
- etc.

See [Integrations](integrations.md#snyk) for details.

### Shields.io Badges

Add custom badges to groups:

```json
{
  "groupId": "whydah",
  "shields": [
    {
      "label": "coverage",
      "message": "85%",
      "color": "brightgreen"
    },
    {
      "label": "license",
      "message": "Apache 2.0",
      "color": "blue"
    }
  ]
}
```

See [Integrations](integrations.md#shields-io) for details.

## Dynamic Repository Discovery

When the application starts or when configuration is reloaded, SCP:

1. Reads `config.json`
2. Fetches the repository list from GitHub API
3. Applies pattern matching for each group
4. Populates the cache with repository metadata
5. Prefetches recent commits for each repository

### Refresh Behavior

- **Startup**: All groups are loaded and cached
- **Scheduled Refresh**: Groups are refreshed every 1 hour (configurable)
- **Webhook Trigger**: When a push event is received, the affected repository's cache is updated immediately
- **Manual Refresh**: Restart the application to force a full refresh

### Performance Considerations

- **GitHub API Rate Limit**: Each repository fetch counts against your rate limit
  - With 100 repositories, initial load uses ~100 API calls
  - Use webhooks to minimize subsequent API calls
- **Cache TTL**: Repository metadata is cached for 1 hour to reduce API calls
- **Conditional Requests**: SCP uses ETags to make efficient conditional requests

## Configuration Validation

SCP validates `config.json` on startup and logs errors if issues are found.

### Common Validation Errors

**Missing Required Fields**:
```
ERROR: Group 'whydah' is missing required field 'defaultGroupRepo'
```

**Solution**: Add the missing field to the group configuration.

**Invalid Pattern Syntax**:
```
WARN: Pattern '[Whydah*' in group 'whydah' has invalid regex syntax
```

**Solution**: Fix the regex pattern (remove invalid characters).

**No Matching Repositories**:
```
WARN: Group 'whydah' has no matching repositories (pattern: 'Whydah*')
```

**Solution**: Check the pattern and verify repositories exist in GitHub.

**Duplicate Group IDs**:
```
ERROR: Duplicate group ID 'whydah' found
```

**Solution**: Ensure each group has a unique `groupId`.

## Advanced Configuration

### Conditional Group Display

You can hide groups with no repositories by configuring the dashboard controller.

**Configuration**: `application.yml`
```yaml
scp:
  dashboard:
    hide-empty-groups: true
```

### Custom Group Ordering

Groups are displayed in the order they appear in `config.json`. To reorder:

1. Edit `config.json`
2. Rearrange the groups in the `groups` array
3. Restart the application

### Environment-Specific Configuration

Use Spring profiles to load different configurations per environment:

**application-dev.yml** (development):
```yaml
scp:
  config-file: /path/to/dev-config.json
```

**application-prod.yml** (production):
```yaml
scp:
  config-file: /path/to/prod-config.json
```

Run with: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

## Troubleshooting

### Group Not Appearing

**Issue**: A configured group doesn't appear on the dashboard.

**Possible Causes**:
1. Invalid `config.json` syntax
2. No repositories match the group's patterns
3. GitHub organization name mismatch

**Solutions**:
1. Validate JSON syntax: `cat config.json | jq .`
2. Test patterns against repository list
3. Check GitHub organization name in config matches your actual organization

### Repositories Not Showing in Group

**Issue**: Expected repositories don't appear in a group.

**Possible Causes**:
1. Pattern doesn't match repository name
2. Repository visibility (private repo with insufficient token permissions)
3. Repository recently created (cache not refreshed)

**Solutions**:
1. Test pattern: Use a regex tester to verify pattern matches repo name
2. Verify token has `repo` scope for private repositories
3. Force refresh by restarting application or waiting for scheduled refresh

### Duplicate Repositories

**Issue**: Same repository appears multiple times in a group.

**Possible Causes**:
1. Multiple patterns in `artifactId` array match the same repository
2. Pattern overlap with wildcard

**Solutions**:
1. Remove duplicate patterns from `artifactId` array
2. Use more specific patterns

### Performance Issues

**Issue**: Dashboard loads slowly with many groups/repositories.

**Possible Causes**:
1. Too many repositories in one group
2. GitHub API rate limit reached
3. Network latency to GitHub API

**Solutions**:
1. Split large groups into smaller groups
2. Check rate limit: `/actuator/health/github`
3. Enable GitHub webhooks to reduce API calls

## Related Documentation

- [Dashboard](dashboard.md) - Main dashboard features
- [Integrations](integrations.md) - Jenkins, Snyk, Shields.io configuration
- [Configuration Guide](../getting-started/configuration.md) - Full configuration reference
- [Architecture Overview](../architecture/overview.md) - System design
