package ir.sahab.nimroo.controller;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.connection.HttpRequest;
import ir.sahab.nimroo.model.*;
import ir.sahab.nimroo.parser.HtmlParser;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.asynchttpclient.Response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Controller {
    private HtmlParser htmlParser = new HtmlParser();
    private KafkaLinkConsumer kafkaLinkConsumer = new KafkaLinkConsumer();
    private KafkaLinkProducer kafkaLinkProducer = new KafkaLinkProducer();
    private KafkaHtmlProducer kafkaHtmlProducer = new KafkaHtmlProducer();
    private DummyDomainCache dummyDomainCache = new DummyDomainCache(30000);
    private PriorityQueue<Pair<Long, String>> priorityQueue = new PriorityQueue<>(Comparator.comparing(Pair::getKey));
    private Logger logger = Logger.getLogger(Controller.class);

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
        httpRequest.setRequestTimeout(100); //TODO
        CompletableFuture<Response> completableFuture = httpRequest.send();
        CompletableFuture<PageData> p = completableFuture.thenApply(response -> {
            String html = response.getResponseBody();
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
            for (Link pageDataLink: pageData.getLinks()) {
                kafkaLinkProducer.send(Config.kafkaLinkTopicName, (new Random().nextInt(2)) + "", pageDataLink.getLink());
            }
        });
    }
}
