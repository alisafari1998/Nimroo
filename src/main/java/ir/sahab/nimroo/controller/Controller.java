package ir.sahab.nimroo.controller;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.connection.HttpRequest;
import ir.sahab.nimroo.model.*;
import ir.sahab.nimroo.parser.HtmlParser;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.asynchttpclient.Response;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Controller {
    private HtmlParser htmlParser = new HtmlParser();
    private KafkaLinkConsumer kafkaLinkConsumer = new KafkaLinkConsumer();
    private KafkaLinkProducer kafkaLinkProducer = new KafkaLinkProducer();
    private KafkaHtmlProducer kafkaHtmlProducer = new KafkaHtmlProducer();
    private DummyDomainCache dummyDomainCache = new DummyDomainCache(30000);
    private PriorityQueue<Pair<Long, String>> priorityQueue = new PriorityQueue<>(Comparator.comparing(Pair::getKey));
    private Logger logger = Logger.getLogger(Controller.class);
    private int count = 0;

    public void start() {
        while (true) {
            while (!priorityQueue.isEmpty() && System.currentTimeMillis() - priorityQueue.peek().getKey() >= 30000) {
                crawl(priorityQueue.poll().getValue(), "PriorityQueue");
            }

            ArrayList<String> links = kafkaLinkConsumer.get();
            for (String link : links) {
                crawl(link, "KafkaLinkConsumer");
            }
        }

    }

    private void crawl(String link, String debug) {
        if (!dummyDomainCache.add(link, System.currentTimeMillis())){
            logger.debug(debug);
            priorityQueue.add(new Pair<>(System.currentTimeMillis(), link));
            return;
        }
        HttpRequest httpRequest = new HttpRequest(link);
        httpRequest.setMethod(HttpRequest.HTTP_REQUEST.GET);
        httpRequest.setRequestTimeout(30000); //TODO
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("accept", "text/html,application/xhtml+xml,application/xml"));
        headers.add(new Pair<>("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36"));

        httpRequest.setHeaders(headers);


        CompletableFuture<Response> completableFuture = httpRequest.send();
        completableFuture.exceptionally((throwable -> {
            logger.error("HttpRequestFailed: ", throwable);
            return null;
        }));

        CompletableFuture<PageData> p = completableFuture.thenApply(response -> {
            String html = response.getResponseBody();
            count++;
            logger.debug(count);
            logger.debug("before LD");
            if (Language.getInstance().detector(html)) { //todo optimize
                logger.debug("after LD");
                return htmlParser.parse(link, html);
            }
            throw new RuntimeException("bad language"); //todo catch
        });
        p.thenAccept(pageData -> {
            byte[] bytes = PageDataSerializer.getInstance().serialize(pageData);
            kafkaHtmlProducer.send(Config.kafkaHtmlTopicName, (new Random().nextInt(2)) + "", bytes); //todo topic
            logger.debug("Producing links:\t" + pageData.getLinks().size());
            for (Link pageDataLink: pageData.getLinks()) {
                kafkaLinkProducer.send(Config.kafkaLinkTopicName, (new Random().nextInt(2)) + "", pageDataLink.getLink());
            }
        });
    }
}
