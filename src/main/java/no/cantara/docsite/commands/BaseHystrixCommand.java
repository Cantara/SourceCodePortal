package no.cantara.docsite.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
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
import java.util.HashMap;
import java.util.Optional;

abstract public class BaseHystrixCommand<R> extends HystrixCommand<R> {

    protected BaseHystrixCommand(String groupKey) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                                .withExecutionTimeoutInMilliseconds(75 * 1000)
                                .withExecutionIsolationSemaphoreMaxConcurrentRequests(25))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withMaxQueueSize(100)
                        .withQueueSizeRejectionThreshold(2500)
                        .withCoreSize(8))
        );
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
            throw new RuntimeException("Excepted statusCode: " + anyOf + " but actual statusCode was " + response.statusCode());
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
