package ir.sahab.nimroo.connection;

import io.netty.handler.codec.http.HttpHeaders;
import ir.sahab.nimroo.Config;
import javafx.util.Pair;
import org.asynchttpclient.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.*;


public class HttpRequest {
    private final String url;
    private static long c = 0;
    private static DefaultAsyncHttpClientConfig.Builder config =
            config().setFollowRedirect(true).setMaxConnections(Config.httpRequestMaxConnection)
            .setMaxConnectionsPerHost(Config.httpRequestMaxConnectionPerHost)
            .setConnectTimeout(Config.httpSocketTimeout)
            .setRequestTimeout(Config.httpRequestTimeout);

    private BoundRequestBuilder boundRequestBuilder;
    private static ArrayList<AsyncHttpClient> clients;

    public static void init() {
        HttpRequest.clients = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            clients.add(asyncHttpClient(config));
        }
    }

    public HttpRequest(String url) {
        this.url = url;
    }

    /**
     * @param httpMethod
     * @throws Exception in case provided method is not defined.
     */
    public void setMethod(HTTP_REQUEST httpMethod) {
        c++;
        AsyncHttpClient client = null;
        client = clients.get((int) (c%6));

        switch (httpMethod) {
            case GET:
                boundRequestBuilder = client.prepareGet(url);
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
                return State.ABORT;
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
