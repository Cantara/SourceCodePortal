package no.cantara.docsite.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.net.http.HttpResponse;

abstract public class BaseHystrixCommand<R> extends HystrixCommand<R> {

    protected BaseHystrixCommand(String groupKey) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey)));
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
