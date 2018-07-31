package ir.sahab.nimroo.connection;

import io.netty.handler.codec.http.HttpHeaders;
import ir.sahab.nimroo.Config;
import javafx.util.Pair;
import org.asynchttpclient.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.*;


public class HttpRequest {
    private final String url;
    public static AsyncHttpClient asyncHttpClient = asyncHttpClient(
            config()
                    .setMaxConnections(Config.httpRequestMaxConnection)
                    .setMaxConnectionsPerHost(Config.httpRequestMaxConnectionPerHost)
                    .setConnectTimeout(5000)
                    .setRequestTimeout(5000));
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
        return boundRequestBuilder.execute(new AsyncCompletionHandler<Response>() {
            @Override
            public State onHeadersReceived(HttpHeaders headers) throws Exception {
                State state = super.onHeadersReceived(headers);
                if (headers.contains("Content-Type") && headers.get("Content-Type").contains("text/html")) {
                    return state;
                }
                return State.CONTINUE;
            }

            @Override
            public Response onCompleted(Response response) throws Exception {
                return response;
            }
        }).toCompletableFuture();
    }

    public enum HTTP_REQUEST {
        GET
    }

}
