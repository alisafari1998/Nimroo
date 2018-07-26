package ir.sahab.nimroo.controller;

import ir.sahab.nimroo.connection.HttpRequest;
import ir.sahab.nimroo.model.KafkaLinkConsumer;
import ir.sahab.nimroo.model.KafkaLinkProducer;
import ir.sahab.nimroo.model.Language;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.parser.HtmlParser;
import org.asynchttpclient.Response;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Controller {
    HtmlParser htmlParser = new HtmlParser(); //todo consider single
    KafkaLinkConsumer kafkaConsumerConnection = new KafkaLinkConsumer();
    KafkaLinkProducer kafkaProducerConnection = new KafkaLinkProducer();

    public void start() {

        for (int i = 0; i < 20; i++) {
            String link = kafkaConsumerConnection.get(); //todo get 1
            HttpRequest httpRequest = new HttpRequest(link);
            httpRequest.setMethod(HttpRequest.HTTP_REQUEST.GET);
            httpRequest.setRequestTimeout(100); //TODO
            CompletableFuture<Response> completableFuture = httpRequest.send();
            CompletableFuture<PageData> p = completableFuture.thenApply(response -> {
                String html = response.getResponseBody();
                if (Language.getInstance().detector(html)) { //todo optimize
                    return htmlParser.parse(link, html);
                }
                throw new RuntimeException("bad language"); //todo
            });
//            p.thenApply(pageData -> {
//                kafkaProducerConnection.send("link", (new Random().nextInt(2)) + "", pageData.getUrl()); // todo
//            });
        }


    }
}
