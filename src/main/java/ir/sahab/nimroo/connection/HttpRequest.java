package ir.sahab.nimroo.connection;

import ir.sahab.nimroo.Config;
import javafx.util.Pair;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.*;


public class HttpRequest {
    private final String url;
    private static AsyncHttpClient asyncHttpClient = asyncHttpClient(
            config()
                    .setMaxConnections(Config.httpRequestMaxConnection)
                    .setMaxConnectionsPerHost(Config.httpRequestMaxConnectionPerHost));

    private BoundRequestBuilder boundRequestBuilder;

    public HttpRequest(String url) {
        this.url = url;
    }

    /**
     * @param httpMethod
     * @throws Exception in case provided method is not defined.
     */
    public void setMethod(HTTP_REQUEST httpMethod) {
        switch (httpMethod) {
            case GET:
                boundRequestBuilder = asyncHttpClient.prepareGet(url);
                break;
        }
    }

    /**
     * @param headers
     * @throws Exception in case of calling without first defining http method
     */
    public void setHeaders(List<Pair<String, String>> headers) throws IllegalStateException {
        if (boundRequestBuilder == null)
            throw new IllegalStateException("Undefined http method");
        headers.forEach((item) -> boundRequestBuilder.setHeader(item.getKey(), item.getValue()));
    }

    public void setRequestTimeout(int timeout) throws IllegalStateException {
        if (boundRequestBuilder == null)
            throw new IllegalStateException("Undefined http method");
        boundRequestBuilder.setRequestTimeout(timeout);
    }

    public CompletableFuture<Response> send() throws IllegalStateException {
        if (boundRequestBuilder == null)
            throw new IllegalStateException("Undefined http method");
        return boundRequestBuilder.execute().toCompletableFuture();
    }

    public enum HTTP_REQUEST {
        GET
    }

}
