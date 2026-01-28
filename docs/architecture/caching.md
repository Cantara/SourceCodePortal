# Caching Architecture

This document describes the caching architecture of Source Code Portal, including cache strategy, implementation, and performance optimization.

## Table of Contents

- [Overview](#overview)
- [Cache Strategy](#cache-strategy)
- [Cache Implementation](#cache-implementation)
- [Cache Keys](#cache-keys)
- [Cache Population](#cache-population)
- [Cache Invalidation](#cache-invalidation)
- [Metrics and Monitoring](#metrics-and-monitoring)
- [Performance Tuning](#performance-tuning)

## Overview

Source Code Portal employs aggressive caching to minimize GitHub API calls and provide fast response times. The caching layer is critical because:

1. **GitHub API Rate Limits**: 5000 requests/hour (authenticated)
2. **Network Latency**: External API calls add significant latency
3. **Data Freshness**: Repository data changes infrequently
4. **User Experience**: Fast page loads require cached data

### Caching Modes

The application supports two caching implementations:

- **Spring Cache + Caffeine** (Recommended, Spring Boot mode)
- **JSR-107 JCache** (Legacy, Undertow mode, deprecated)

## Cache Strategy

### High-Level Strategy

```
┌─────────────────────────────────────────────────────────┐
│                   Request Arrives                        │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
              ┌───────────────┐
              │  Check Cache  │
              └───────┬───────┘
                      │
         ┌────────────┴────────────┐
         │                         │
    Cache Hit                 Cache Miss
         │                         │
         ▼                         ▼
  ┌─────────────┐         ┌──────────────┐
  │ Return Data │         │ Fetch GitHub │
  └─────────────┘         └──────┬───────┘
                                 │
                                 ▼
                          ┌──────────────┐
                          │  Store Cache │
                          └──────┬───────┘
                                 │
                                 ▼
                          ┌─────────────┐
                          │ Return Data │
                          └─────────────┘
```

### Cache-First Approach

**Principle**: Always check cache before calling external APIs.

**Benefits**:
- Reduced API calls (90%+ cache hit rate)
- Faster response times (< 10ms vs 500ms+)
- Protection against rate limits
- Graceful degradation during GitHub outages

**Trade-offs**:
- Stale data (acceptable for most use cases)
- Memory consumption (manageable with TTL)
- Cache invalidation complexity (mitigated by webhooks)

### Cache Layers

The system uses a single in-memory cache layer with multiple caches:

```
┌──────────────────────────────────────────────────────────┐
│                  Application Layer                        │
│  (Controllers, Services)                                  │
└─────────────────────────┬────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────┐
│                     CacheStore                            │
│  (Facade for all caches)                                  │
└─────────────────────────┬────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
    ┌──────────┐    ┌──────────┐   ┌──────────┐
    │  Repos   │    │ Commits  │   │ Contents │
    │  Cache   │    │  Cache   │   │  Cache   │
    └──────────┘    └──────────┘   └──────────┘
          │               │               │
          ▼               ▼               ▼
    ┌──────────────────────────────────────────┐
    │      Caffeine In-Memory Cache            │
    │  (or JCache Reference Implementation)    │
    └──────────────────────────────────────────┘
```

## Cache Implementation

### Spring Cache + Caffeine (Current)

**Configuration**: `src/main/java/no/cantara/docsite/config/CacheConfiguration.java`

```java
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Configure cache manager with multiple named caches.
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Create individual caches
        List<CaffeineCache> caches = Arrays.asList(
            buildCache("repositories", 500, Duration.ofMinutes(30)),
            buildCache("commits", 1000, Duration.ofMinutes(15)),
            buildCache("contents", 500, Duration.ofMinutes(30)),
            buildCache("buildStatus", 200, Duration.ofMinutes(10)),
            buildCache("badges", 100, Duration.ofMinutes(60)),
            buildCache("releases", 200, Duration.ofMinutes(30)),
            buildCache("groupRepos", 50, Duration.ofMinutes(30))
        );

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * Build a Caffeine cache with specified parameters.
     */
    private CaffeineCache buildCache(String name, int maxSize, Duration ttl) {
        return new CaffeineCache(name, Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(ttl)
            .recordStats()  // Enable metrics
            .build());
    }

    /**
     * CacheStore facade bean.
     */
    @Bean
    public CacheStore cacheStore(CacheManager cacheManager) {
        return new CacheStore(cacheManager);
    }
}
```

**Cache Characteristics:**

- **Repositories Cache**: 500 entries, 30min TTL
  - Stores repository metadata
  - Updated on push webhooks

- **Commits Cache**: 1000 entries, 15min TTL
  - Stores commit history per repository
  - Updated frequently, shorter TTL

- **Contents Cache**: 500 entries, 30min TTL
  - Stores README and documentation files
  - Larger entries, longer TTL

- **Build Status Cache**: 200 entries, 10min TTL
  - Jenkins/Snyk build status
  - Shorter TTL for fresher build data

- **Badges Cache**: 100 entries, 60min TTL
  - SVG badge images from Shields.io
  - Rarely changes, longest TTL

### CacheStore Facade

**File**: `src/main/java/no/cantara/docsite/cache/CacheStore.java`

```java
/**
 * Central cache store providing typed access to all caches.
 *
 * <p>This class acts as a facade over Spring CacheManager, providing:
 * <ul>
 *   <li>Type-safe cache operations</li>
 *   <li>Consistent cache key generation</li>
 *   <li>Null-safe cache access</li>
 *   <li>Metrics and statistics</li>
 * </ul>
 */
@Component
public class CacheStore {

    private static final Logger log = LoggerFactory.getLogger(CacheStore.class);

    private final CacheManager cacheManager;

    public CacheStore(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get repository from cache.
     *
     * @param key Cache key (org/repo)
     * @return Repository or null if not cached
     */
    public Repository getRepository(CacheRepositoryKey key) {
        Cache cache = getCache("repositories");
        return cache.get(key, Repository.class);
    }

    /**
     * Put repository into cache.
     */
    public void putRepository(CacheRepositoryKey key, Repository repo) {
        Cache cache = getCache("repositories");
        cache.put(key, repo);
        log.debug("Cached repository: {}", key);
    }

    /**
     * Get commits from cache.
     *
     * @param key Cache key (org/repo/branch)
     * @return List of commits or null if not cached
     */
    @SuppressWarnings("unchecked")
    public List<Commit> getCommits(CacheKey key) {
        Cache cache = getCache("commits");
        return cache.get(key, List.class);
    }

    /**
     * Put commits into cache.
     */
    public void putCommits(CacheKey key, List<Commit> commits) {
        Cache cache = getCache("commits");
        cache.put(key, commits);
        log.debug("Cached {} commits for {}", commits.size(), key);
    }

    /**
     * Get repository contents from cache.
     *
     * @param key Cache key (org/repo/branch/path)
     * @return Contents or null if not cached
     */
    public Contents getContents(CacheKey key) {
        Cache cache = getCache("contents");
        return cache.get(key, Contents.class);
    }

    /**
     * Put contents into cache.
     */
    public void putContents(CacheKey key, Contents contents) {
        Cache cache = getCache("contents");
        cache.put(key, contents);
        log.debug("Cached contents: {}", key);
    }

    /**
     * Invalidate cache entry.
     */
    public void invalidate(String cacheName, Object key) {
        Cache cache = getCache(cacheName);
        cache.evict(key);
        log.info("Invalidated cache entry: {} / {}", cacheName, key);
    }

    /**
     * Clear entire cache.
     */
    public void clearCache(String cacheName) {
        Cache cache = getCache(cacheName);
        cache.clear();
        log.info("Cleared cache: {}", cacheName);
    }

    /**
     * Get cache statistics.
     */
    public CacheStats getStats(String cacheName) {
        Cache cache = getCache(cacheName);
        if (cache instanceof CaffeineCache caffeineCache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                caffeineCache.getNativeCache();
            return nativeCache.stats();
        }
        return null;
    }

    /**
     * Get cache by name, creating if missing.
     */
    private Cache getCache(String name) {
        Cache cache = cacheManager.getCache(name);
        if (cache == null) {
            throw new IllegalStateException("Cache not configured: " + name);
        }
        return cache;
    }

    /**
     * Get all cache names.
     */
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}
```

## Cache Keys

### Key Design Principles

**1. Strongly Typed Keys**: Each cache uses specific key types for type safety.

**2. Immutable Keys**: Keys are immutable records to prevent modification.

**3. Composite Keys**: Keys combine multiple attributes (org, repo, branch, etc.).

**4. Consistent Hashing**: Keys implement `hashCode()` and `equals()` properly.

### Key Types

#### CacheKey (Base Key)

**File**: `src/main/java/no/cantara/docsite/cache/CacheKey.java`

```java
/**
 * Base cache key for organization/repository/branch.
 *
 * <p>Used for: commits, contents, build status, releases.
 */
public record CacheKey(
    String organization,
    String repository,
    String branch
) {
    public CacheKey {
        Objects.requireNonNull(organization, "organization");
        Objects.requireNonNull(repository, "repository");
        Objects.requireNonNull(branch, "branch");
    }

    public static CacheKey of(String org, String repo, String branch) {
        return new CacheKey(org, repo, branch);
    }

    @Override
    public String toString() {
        return organization + "/" + repository + "/" + branch;
    }
}
```

#### CacheRepositoryKey

```java
/**
 * Cache key for repository metadata.
 *
 * <p>Includes groupId for multi-group repository support.
 */
public record CacheRepositoryKey(
    String groupId,
    String organization,
    String repository
) {
    public CacheRepositoryKey {
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(organization, "organization");
        Objects.requireNonNull(repository, "repository");
    }

    public static CacheRepositoryKey of(String groupId, String org, String repo) {
        return new CacheRepositoryKey(groupId, org, repo);
    }

    @Override
    public String toString() {
        return groupId + ":" + organization + "/" + repository;
    }
}
```

#### CacheGroupKey

```java
/**
 * Cache key for repository group data.
 */
public record CacheGroupKey(String groupId) {
    public CacheGroupKey {
        Objects.requireNonNull(groupId, "groupId");
    }

    public static CacheGroupKey of(String groupId) {
        return new CacheGroupKey(groupId);
    }
}
```

#### CacheShaKey

```java
/**
 * Cache key for commit SHA lookup.
 *
 * <p>Used for webhook invalidation by commit SHA.
 */
public record CacheShaKey(
    String organization,
    String repository,
    String sha
) {
    // Similar to CacheKey but with SHA instead of branch
}
```

### Key Usage Examples

```java
// Cache repository metadata
CacheRepositoryKey repoKey = CacheRepositoryKey.of("security", "cantara", "STS");
cacheStore.putRepository(repoKey, repository);

// Cache commits for master branch
CacheKey commitKey = CacheKey.of("cantara", "STS", "master");
cacheStore.putCommits(commitKey, commits);

// Cache README contents
CacheKey contentKey = CacheKey.of("cantara", "STS", "master");
cacheStore.putContents(contentKey, readme);

// Cache by commit SHA
CacheShaKey shaKey = CacheShaKey.of("cantara", "STS", "abc123");
cacheStore.putCommit(shaKey, commit);
```

## Cache Population

### Startup Prefetch

**File**: `src/main/java/no/cantara/docsite/fetch/PreFetchData.java`

```java
/**
 * Prefetch critical data on application startup.
 *
 * <p>Runs once after Spring context initialization to populate caches
 * with repository metadata, recent commits, and README files.
 */
@Component
public class PreFetchData implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PreFetchData.class);

    private final RepositoryConfigLoader configLoader;
    private final GitHubCommands gitHubCommands;
    private final CacheStore cacheStore;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting data prefetch...");

        List<RepositoryGroup> groups = configLoader.getRepositoryGroups();

        for (RepositoryGroup group : groups) {
            prefetchGroup(group);
        }

        log.info("Data prefetch complete");
    }

    private void prefetchGroup(RepositoryGroup group) {
        for (Repository repo : group.getRepositories()) {
            try {
                // Prefetch repository metadata
                prefetchRepository(repo);

                // Prefetch recent commits (last 50)
                prefetchCommits(repo);

                // Prefetch README
                prefetchReadme(repo);

                // Prefetch build status
                prefetchBuildStatus(repo);

            } catch (Exception e) {
                log.error("Failed to prefetch data for {}", repo.getName(), e);
            }
        }
    }

    private void prefetchRepository(Repository repo) {
        CacheRepositoryKey key = CacheRepositoryKey.of(
            repo.getGroupId(),
            repo.getOrganization(),
            repo.getName()
        );

        Repository fullRepo = gitHubCommands.getRepository(
            repo.getOrganization(),
            repo.getName()
        );

        cacheStore.putRepository(key, fullRepo);
        log.debug("Prefetched repository: {}", repo.getName());
    }

    private void prefetchCommits(Repository repo) {
        CacheKey key = CacheKey.of(
            repo.getOrganization(),
            repo.getName(),
            "master"
        );

        List<Commit> commits = gitHubCommands.getCommits(
            repo.getOrganization(),
            repo.getName(),
            "master",
            50  // Last 50 commits
        );

        cacheStore.putCommits(key, commits);
        log.debug("Prefetched {} commits for {}", commits.size(), repo.getName());
    }

    private void prefetchReadme(Repository repo) {
        CacheKey key = CacheKey.of(
            repo.getOrganization(),
            repo.getName(),
            "master"
        );

        try {
            Contents readme = gitHubCommands.getReadme(
                repo.getOrganization(),
                repo.getName(),
                "master"
            );

            cacheStore.putContents(key, readme);
            log.debug("Prefetched README for {}", repo.getName());

        } catch (NotFoundException e) {
            log.debug("No README found for {}", repo.getName());
        }
    }
}
```

### Scheduled Refresh

**File**: `src/main/java/no/cantara/docsite/scheduled/ScheduledFetchData.java`

```java
/**
 * Periodically refresh cached data.
 *
 * <p>Runs in background to keep caches fresh without waiting for TTL expiration.
 */
@Component
public class ScheduledFetchData {

    private static final Logger log = LoggerFactory.getLogger(ScheduledFetchData.class);

    private final CacheStore cacheStore;
    private final RepositoryConfigLoader configLoader;
    private final GitHubCommands gitHubCommands;

    /**
     * Refresh all repositories every 15 minutes.
     */
    @Scheduled(fixedDelay = 900000, initialDelay = 60000)  // 15min delay, 1min initial
    public void refreshRepositories() {
        log.info("Starting scheduled repository refresh");

        List<RepositoryGroup> groups = configLoader.getRepositoryGroups();
        int refreshed = 0;

        for (RepositoryGroup group : groups) {
            for (Repository repo : group.getRepositories()) {
                try {
                    refreshRepository(repo);
                    refreshed++;
                } catch (Exception e) {
                    log.error("Failed to refresh {}", repo.getName(), e);
                }
            }
        }

        log.info("Refreshed {} repositories", refreshed);
    }

    /**
     * Refresh commits every 10 minutes.
     */
    @Scheduled(cron = "0 */10 * * * *")  // Every 10 minutes
    public void refreshCommits() {
        log.info("Starting scheduled commit refresh");

        // Refresh commits for active repositories only
        List<Repository> activeRepos = getActiveRepositories();

        for (Repository repo : activeRepos) {
            try {
                refreshCommitsForRepo(repo);
            } catch (Exception e) {
                log.error("Failed to refresh commits for {}", repo.getName(), e);
            }
        }
    }

    private void refreshRepository(Repository repo) {
        CacheRepositoryKey key = CacheRepositoryKey.of(
            repo.getGroupId(),
            repo.getOrganization(),
            repo.getName()
        );

        Repository updated = gitHubCommands.getRepository(
            repo.getOrganization(),
            repo.getName()
        );

        cacheStore.putRepository(key, updated);
    }
}
```

## Cache Invalidation

### Webhook-Triggered Invalidation

**File**: `src/main/java/no/cantara/docsite/controller/spring/GitHubWebhookRestController.java`

```java
@RestController
@RequestMapping("/github/webhook")
public class GitHubWebhookRestController {

    private final CacheStore cacheStore;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody GitHubWebhookPayload payload,
            @RequestHeader("X-Hub-Signature-256") String signature) {

        // Verify webhook signature
        if (!verifySignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Handle push event
        if ("push".equals(payload.getEvent())) {
            handlePushEvent(payload);
        }

        return ResponseEntity.ok().build();
    }

    private void handlePushEvent(GitHubWebhookPayload payload) {
        String org = payload.getRepository().getOrganization();
        String repo = payload.getRepository().getName();
        String branch = extractBranch(payload.getRef());

        log.info("Push event received: {}/{}/{}", org, repo, branch);

        // Invalidate commits cache
        CacheKey commitKey = CacheKey.of(org, repo, branch);
        cacheStore.invalidate("commits", commitKey);

        // Invalidate contents cache (README might have changed)
        cacheStore.invalidate("contents", commitKey);

        // Invalidate repository metadata
        cacheStore.invalidate("repositories",
            CacheRepositoryKey.of("*", org, repo));

        log.info("Invalidated caches for {}/{}/{}", org, repo, branch);
    }
}
```

### Manual Invalidation

**Admin Endpoint** (for manual cache clearing):

```java
@RestController
@RequestMapping("/admin/cache")
public class CacheAdminController {

    private final CacheStore cacheStore;

    /**
     * Clear specific cache.
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Void> clearCache(@PathVariable String cacheName) {
        cacheStore.clearCache(cacheName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Clear all caches.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearAllCaches() {
        for (String cacheName : cacheStore.getCacheNames()) {
            cacheStore.clearCache(cacheName);
        }
        return ResponseEntity.noContent().build();
    }
}
```

## Metrics and Monitoring

### Cache Statistics

**Actuator Endpoint**: `/actuator/health/cache`

```json
{
  "status": "UP",
  "details": {
    "repositories": {
      "size": 245,
      "hitRate": 0.92,
      "missRate": 0.08,
      "evictions": 12
    },
    "commits": {
      "size": 876,
      "hitRate": 0.95,
      "missRate": 0.05,
      "evictions": 45
    },
    "contents": {
      "size": 312,
      "hitRate": 0.89,
      "missRate": 0.11,
      "evictions": 8
    }
  }
}
```

### Prometheus Metrics

**Metrics Exported**:

- `cache_size{cache="repositories"}` - Current cache size
- `cache_hit_rate{cache="repositories"}` - Cache hit rate (0-1)
- `cache_miss_rate{cache="repositories"}` - Cache miss rate (0-1)
- `cache_evictions_total{cache="repositories"}` - Total evictions
- `cache_load_time_seconds{cache="repositories"}` - Cache load time

**Example Queries**:

```promql
# Cache hit rate over time
rate(cache_hit_total[5m]) / rate(cache_requests_total[5m])

# Cache size by cache name
cache_size

# Eviction rate
rate(cache_evictions_total[5m])
```

### Logging

**Cache Events Logged**:

- Cache hits/misses (DEBUG level)
- Cache evictions (INFO level)
- Cache errors (ERROR level)
- Prefetch progress (INFO level)

**Example Log Output**:

```
2024-01-28 10:15:23 DEBUG CacheStore - Cache hit: repositories / cantara:STS
2024-01-28 10:15:45 DEBUG CacheStore - Cache miss: commits / cantara:STS:master
2024-01-28 10:15:46 INFO  CacheStore - Cached 50 commits for cantara:STS:master
2024-01-28 10:16:00 INFO  ScheduledFetchData - Refreshed 245 repositories
2024-01-28 10:20:00 INFO  CacheStore - Invalidated cache entry: commits / cantara:STS:master
```

## Performance Tuning

### Cache Size Tuning

**Guidelines**:

1. **Monitor Memory Usage**: Check heap size and cache occupancy
2. **Adjust Max Size**: Increase for higher hit rates, decrease for memory constraints
3. **Balance TTL**: Longer TTL = fewer API calls, but staler data

**Configuration Example**:

```yaml
scp:
  cache:
    repositories:
      max-size: 1000  # Increase if many repos
      ttl: 30m
    commits:
      max-size: 2000  # Larger for commit history
      ttl: 15m
    contents:
      max-size: 500   # Smaller for large README files
      ttl: 30m
```

### TTL Optimization

**Recommendations by Cache Type**:

- **Repositories**: 30-60min (metadata changes rarely)
- **Commits**: 10-15min (active development)
- **Contents**: 30min (documentation updates infrequent)
- **Build Status**: 5-10min (fast feedback on builds)
- **Badges**: 60min (rarely changes)

### Prefetch Optimization

**Strategies**:

1. **Selective Prefetch**: Only prefetch frequently accessed repos
2. **Parallel Prefetch**: Use virtual threads for concurrent fetching
3. **Lazy Loading**: Prefetch metadata only, lazy-load contents
4. **Smart Refresh**: Refresh based on activity (e.g., skip archived repos)

### Memory Profiling

**Monitor These Metrics**:

- Heap usage percentage
- Cache occupancy (entries vs max size)
- Eviction rate (high = cache too small)
- Hit rate (low = cache not effective)

**Target Metrics**:

- Hit rate: > 90%
- Heap usage: < 80%
- Eviction rate: < 5% of requests
- Miss penalty: < 500ms

## Related Documentation

- [Spring Boot Architecture](spring-boot.md) - Cache configuration and beans
- [Controller Architecture](controllers.md) - How controllers use cache
- [Configuration Guide](../getting-started/configuration.md) - Cache configuration options
- [Monitoring Guide](../operations/monitoring.md) - Cache metrics and alerts

---

**Next Steps**: Read the [Package Structure](packages.md) document to understand how cache-related code is organized.
