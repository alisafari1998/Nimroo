package ir.sahab.nimroo.controller;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.connection.HttpRequest;
import ir.sahab.nimroo.connection.NewHttpRequest;
import ir.sahab.nimroo.kafka.KafkaHtmlProducer;
import ir.sahab.nimroo.kafka.KafkaLinkConsumer;
import ir.sahab.nimroo.kafka.KafkaLinkProducer;
import ir.sahab.nimroo.model.*;
import ir.sahab.nimroo.parser.HtmlParser;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;

public class Controller {


    public Controller() {
        executorService = new ThreadPoolExecutor(950, 950, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(500));
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
        HttpRequest.init();
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

                    logger.info("Summery count: " + count + " speedM: " + 60 *  count / ((System.currentTimeMillis() - time) / 1000));
                    logger.info("Summery count: " + count + " speedS: " + count / ((System.currentTimeMillis() - time) / 1000));
                    logger.info("domains: " + dummyDomainCache.size());
                    logger.info("rejectionsByLRU: " + rejectByLRU);

                }
                catch (RejectedExecutionException e) {
                    Thread.sleep(40);
                }
                catch (Exception e) {
                    logger.error("Bale Bale: ", e);
                }
            }
        }

    }

    private void crawl(String link, String info) {
        long timeLru, timeProduceBack, timeGet, timeLd, timeParse, timeSerialize, timeProducePageData, timeProduceLinks;
        logger.info("Link: " + link);
        boolean boolif = false;
        synchronized (dummyDomainCache) {
            timeLru = System.currentTimeMillis();
            boolif = dummyDomainCache.add(link, System.currentTimeMillis());
            timeLru = System.currentTimeMillis() - timeLru;
            logger.info("[Timing] TimeLru: " + timeLru);
        }
        if (!boolif) {
            rejectByLRU++;
            timeProduceBack = System.currentTimeMillis();
            kafkaLinkProducer.send(Config.kafkaLinkTopicName, "", link);
            timeProduceBack = System.currentTimeMillis() - timeProduceBack;
            logger.info("[Timing] TimeProduceBack: " + timeProduceBack);
            return;
        }

        timeGet = System.currentTimeMillis();
//        NewHttpRequest httpRequest = new NewHttpRequest();
//        String response = httpRequest.get(link);
        timeGet = System.currentTimeMillis() - timeGet;
        logger.info("[Timing] TimeGet: " + timeGet);
        String response = null;

        HttpRequest httpRequest1 = new HttpRequest(link);
        httpRequest1.setMethod(HttpRequest.HTTP_REQUEST.GET);
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("accept", "text/html,application/xhtml+xml,application/xml"));
        headers.add(new Pair<>("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36"));
        httpRequest1.setHeaders(headers);
        try {
            response = httpRequest1.send().get().getResponseBody();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (response == null) {
            logger.info("response null");
            return;
        }




        PageData pageData = null;
        timeParse = System.currentTimeMillis();
        htmlParser = new HtmlParser();
        pageData = htmlParser.parse(link, response);
        timeParse = System.currentTimeMillis() - timeParse;
        logger.info("[Timing] TimeParse: " + timeParse);

        timeLd = System.currentTimeMillis();
        if (Language.getInstance().detector(pageData.getText().substring(0,java.lang.Math.max(pageData.getText().length(),1000)))) {
            timeLd = System.currentTimeMillis() - timeLd;
            logger.info("[Timing] TimeLanguageDetector Text: " + timeLd);
        }
        else {
            timeLd = System.currentTimeMillis() - timeLd;
            logger.info("[Timing] TimeLanguageDetector NotEnglish: " + timeLd);
            return;
        }

        timeSerialize = System.currentTimeMillis();
        byte[] bytes = PageDataSerializer.getInstance().serialize(pageData);
        timeSerialize = System.currentTimeMillis() - timeSerialize;
        logger.info("[Timing] TimeSerialize: " + timeSerialize);

        timeProducePageData = System.currentTimeMillis();
        kafkaHtmlProducer.send(Config.kafkaHtmlTopicName, "", bytes); //todo topic
        timeProducePageData = System.currentTimeMillis() - timeProducePageData;
        logger.info("[Timing] TimeProducePageData : " + timeProducePageData);

        timeProduceLinks = System.currentTimeMillis();
        logger.info("Producing links:\t" + pageData.getLinks().size());
        for (Link pageDataLink: pageData.getLinks()) {
            kafkaLinkProducer.send(Config.kafkaLinkTopicName, "", pageDataLink.getLink());
        }
        timeProduceLinks = System.currentTimeMillis() - timeProduceLinks;
        logger.info("[Timing] TimeProduceLinks: " + timeProduceLinks);
        synchronized (count) {
            count++;
        }
    }
}
