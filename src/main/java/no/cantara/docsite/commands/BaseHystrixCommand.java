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
                                .withExecutionTimeoutInMilliseconds(10000)
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
        for (int statusCode : statusCode) {
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
