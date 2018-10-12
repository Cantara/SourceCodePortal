package no.cantara.docsite.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import java.net.http.HttpResponse;

abstract public class BaseHystrixCommand<R> extends HystrixCommand<R> {

    protected BaseHystrixCommand(String groupKey) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                                .withExecutionTimeoutInMilliseconds(2500)
                                .withExecutionIsolationSemaphoreMaxConcurrentRequests(25))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withMaxQueueSize(100)
                        .withQueueSizeRejectionThreshold(100)
                        .withCoreSize(4))
        );
    }

    public static <R> boolean anyOf(HttpResponse<R> response, int... anyOf) {
        if (response == null) return false;
        int matchingStatusCode = -1;
        for (int statusCode : anyOf) {
            if (response.statusCode() == statusCode) {
                matchingStatusCode = statusCode;
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

}
