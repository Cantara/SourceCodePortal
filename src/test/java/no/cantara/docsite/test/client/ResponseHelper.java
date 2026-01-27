package no.cantara.docsite.test.client;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseHelper<T> {

    private final HttpResponse<T> response;
    private final T body;

    public ResponseHelper(HttpResponse<T> response) {
        this.response = response;
        this.body = response.body();
    }

    public HttpResponse<T> response() {
        return response;
    }

    public T body() {
        return body;
    }

    public ResponseHelper<T> expectAnyOf(int... anyOf) {
        int matchingStatusCode = -1;
        for (int statusCode : anyOf) {
            if (response.statusCode() == statusCode) {
                matchingStatusCode = statusCode;
            }
        }
        assertTrue(matchingStatusCode != -1, "Actual statusCode was " + response.statusCode() + " message: " + String.valueOf(body));
        return this;
    }

    public ResponseHelper<T> expect400BadRequest() {
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_BAD_REQUEST, String.valueOf(body));
        return this;
    }

    public ResponseHelper<T> expect404NotFound() {
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NOT_FOUND, String.valueOf(body));
        return this;
    }

    public ResponseHelper<T> expect200Ok() {
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_OK, String.valueOf(body));
        return this;
    }

    public ResponseHelper<T> expect201Created() {
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_CREATED, String.valueOf(body));
        return this;
    }

    public ResponseHelper<T> expect204NoContent() {
        assertEquals(response.statusCode(), HttpURLConnection.HTTP_NO_CONTENT, String.valueOf(body));
        return this;
    }
}
