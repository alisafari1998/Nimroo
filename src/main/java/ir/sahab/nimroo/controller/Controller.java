package ir.sahab.nimroo.controller;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.connection.NewHttpRequest;
import ir.sahab.nimroo.kafka.KafkaHtmlProducer;
import ir.sahab.nimroo.kafka.KafkaLinkConsumer;
import ir.sahab.nimroo.kafka.KafkaLinkProducer;
import ir.sahab.nimroo.model.*;
import ir.sahab.nimroo.parser.HtmlParser;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;

public class Controller {


    public Controller() {
        executorService = new ThreadPoolExecutor(650, 650, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5000));
    }

    private HtmlParser htmlParser;
    private KafkaLinkConsumer kafkaLinkConsumer = new KafkaLinkConsumer();
    private KafkaLinkProducer kafkaLinkProducer = new KafkaLinkProducer();
    private KafkaHtmlProducer kafkaHtmlProducer = new KafkaHtmlProducer();
    private final DummyDomainCache dummyDomainCache = new DummyDomainCache(30000);
    private Logger logger = Logger.getLogger(Controller.class);
    private Long count = 0L, rejectByLRU = 0L;
    private ExecutorService executorService;

    public void start() throws InterruptedException {
        long time = System.currentTimeMillis();
        while (true) {
            logger.info("Start to poll");
            ArrayList<String> links = kafkaLinkConsumer.get();
            logger.info("End to poll");

            for (String link : links) {
                try {
                    executorService.submit(()-> {
                        crawl(link, "KafkaLinkConsumer");
                    });
                    logger.info("Summery count: " + count + " speed: " + 60 *  count / ((System.currentTimeMillis() - time) / 1000) + " rejectionsByLRU: " + rejectByLRU + " lruSize: " + dummyDomainCache.size());
                }
                catch (RejectedExecutionException e) {
                    Thread.sleep(100);
                }
                catch (Exception e) {
                    logger.error("Bale Bale: ", e);
                }
            }
        }

    }

    private void crawl(String link, String info) {
        logger.info("Link: " + link);
        boolean boolif = false;
        synchronized (dummyDomainCache) {
            boolif = dummyDomainCache.add(link, System.currentTimeMillis());
        }
        if (!boolif) {
            rejectByLRU++;
            kafkaLinkProducer.send(Config.kafkaLinkTopicName, "", link);
            return;
        }

        NewHttpRequest httpRequest = new NewHttpRequest();
        String response = httpRequest.get(link);

        if (response == null) {
            logger.info("response null");
            return;
        }

        logger.info("before LD");

        PageData pageData = null;
        if (Language.getInstance().detector(response)) { //todo optimize
            logger.info("after LD");
            htmlParser = new HtmlParser();
            pageData = htmlParser.parse(link, response);
            logger.info("after parser");
        }
        else {
            return;
        }

        byte[] bytes = PageDataSerializer.getInstance().serialize(pageData);
        kafkaHtmlProducer.send(Config.kafkaHtmlTopicName, "", bytes); //todo topic
        logger.info("Producing links:\t" + pageData.getLinks().size());
        for (Link pageDataLink: pageData.getLinks()) {
            kafkaLinkProducer.send(Config.kafkaLinkTopicName, "", pageDataLink.getLink());
        }
        synchronized (count) {
            count++;
        }
    }
}
