package ir.sahab.nimroo.connection;

import javafx.util.Pair;
import org.asynchttpclient.Response;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TestHttpRequest {

    private static String blankPage;

    @BeforeClass
    public static void setUp() throws IOException {
        blankPage = new String(Files.readAllBytes(Paths.get("TestResources/blankpage.txt")));
        HttpRequest.init();
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        HttpRequest httpRequest = new HttpRequest("http://example.com/");
        httpRequest.setMethod(HttpRequest.HTTP_REQUEST.GET);
        Response response = httpRequest.send().get();
        Assert.assertEquals(response.getResponseBody(), blankPage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithWrongUrl() throws ExecutionException, InterruptedException {
        HttpRequest httpRequest = new HttpRequest("blankwebsite");
        httpRequest.setMethod(HttpRequest.HTTP_REQUEST.GET);
        httpRequest.send().get();
    }

    @Test(expected = IllegalStateException.class)
    public void testSetHeaderBeforeSettingMethod() {
        HttpRequest httpRequest = new HttpRequest("blankwebsite");
        List<Pair<String, String>> list = new ArrayList<>();
        httpRequest.setHeaders(list);
    }

    @Test(expected = IllegalStateException.class)
    public void testSetRequestTimeoutBeforeSettingMethod() {
        HttpRequest httpRequest = new HttpRequest("blankwebsite");
        httpRequest.setRequestTimeout(1000);
    }
}
