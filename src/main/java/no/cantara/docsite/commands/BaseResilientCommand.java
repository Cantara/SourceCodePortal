package no.cantara.docsite.commands;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import no.cantara.docsite.util.CommonUtil;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for resilient HTTP commands using Resilience4j.
 * Replaces the deprecated Hystrix implementation with Circuit Breaker, Bulkhead, and TimeLimiter patterns.
 */
abstract public class BaseResilientCommand<R> {

    private static final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final TimeLimiter timeLimiter;
    protected final String groupKey;

    protected BaseResilientCommand(String groupKey) {
        this.groupKey = groupKey;

        // Circuit breaker configuration (similar to Hystrix settings)
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(30))
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(100)
                .minimumNumberOfCalls(10)
                .build();

        this.circuitBreaker = CircuitBreaker.of(groupKey, circuitBreakerConfig);

        // Bulkhead configuration (similar to Hystrix semaphore isolation)
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(25)
                .maxWaitDuration(Duration.ofMillis(100))
                .build();

        this.bulkhead = Bulkhead.of(groupKey + "-bulkhead", bulkheadConfig);

        // Time limiter configuration (similar to Hystrix execution timeout)
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(75))
                .build();

        this.timeLimiter = TimeLimiter.of(groupKey + "-timelimiter", timeLimiterConfig);
    }

    /**
     * Execute the command synchronously with circuit breaker, bulkhead, and time limiter protection.
     */
    public R execute() {
        try {
            Callable<R> decoratedSupplier = CircuitBreaker.decorateCallable(
                    circuitBreaker,
                    Bulkhead.decorateCallable(
                            bulkhead,
                            () -> executeWithTimeout()
                    )
            );

            return decoratedSupplier.call();
        } catch (Exception e) {
            return handleFallback(e);
        }
    }

    /**
     * Execute the command asynchronously, returning a Future.
     * This replaces Hystrix's queue() method.
     */
    public CompletableFuture<R> queue() {
        return CompletableFuture.supplyAsync(this::execute, executorService);
    }

    private R executeWithTimeout() {
        try {
            CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executorService);

            return timeLimiter.executeFutureSupplier(() -> future);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Implement this method with the actual command logic.
     */
    protected abstract R run() throws Exception;

    /**
     * Implement this method to provide fallback behavior.
     */
    protected abstract R handleFallback(Exception e);

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public Bulkhead getBulkhead() {
        return bulkhead;
    }

    public static <R> boolean anyOf(HttpResponse<R> response, int... statusCode) {
        if (response == null) return false;
        int matchingStatusCode = -1;
        for (int sc : statusCode) {
            if (response.statusCode() == sc) {
                matchingStatusCode = sc;
            }
        }
        return (matchingStatusCode != -1);
    }

    public static <R> HttpResponse<R> expectAnyOf(HttpResponse<R> response, int... anyOf) {
        if (response == null) return null;
        int matchingStatusCode = -1;
        for (int statusCode : anyOf) {
            if (response.statusCode() == statusCode) {
                matchingStatusCode = statusCode;
            }
        }
        if (matchingStatusCode != -1) {
            throw new RuntimeException("Expected statusCode: " + anyOf + " but actual statusCode was " + response.statusCode());
        }
        return response;
    }

    static void ifDumpToFile(String url, HttpResponse<String> response) throws IOException {
        Path dataPath = CommonUtil.getCurrentPath().resolve("target/data/");
        Files.createDirectories(dataPath);
        url = new URL(url).getFile();
        System.out.println(url);
        String[] urlPath = url.split("/");
        String filename = urlPath[urlPath.length - 1];
        String relativePath = url.replace(filename, "").substring(1);
        Files.createDirectories(dataPath.resolve(relativePath));
        Path writeToFile = dataPath.resolve(relativePath + filename);
        String body = response.body();
        Files.write(writeToFile, body.getBytes());
    }

    public static <R> HttpResponse<R> getNullResponse(String url) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 500;
            }

            @Override
            public HttpRequest request() {
                return HttpRequest.newBuilder().build();
            }

            @Override
            public Optional<HttpResponse<R>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(new HashMap<>(), (s1, s2) -> false);
            }

            @Override
            public R body() {
                return null;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                try {
                    return new URI(url);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_2;
            }
        };
    }
}
