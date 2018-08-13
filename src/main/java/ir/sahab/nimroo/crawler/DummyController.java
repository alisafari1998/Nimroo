package ir.sahab.nimroo.crawler;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.crawler.cache.DummyDomainCache;
import ir.sahab.nimroo.connection.HttpRequest;
import ir.sahab.nimroo.kafka.KafkaHtmlProducer;
import ir.sahab.nimroo.kafka.KafkaLinkConsumer;
import ir.sahab.nimroo.kafka.KafkaLinkProducer;
import ir.sahab.nimroo.model.*;
import ir.sahab.nimroo.crawler.parser.HtmlParser;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import ir.sahab.nimroo.crawler.util.Language;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.asynchttpclient.Response;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class DummyController extends Crawler {

        private long testArmin = System.currentTimeMillis();
        private HtmlParser htmlParser;
        private KafkaLinkConsumer kafkaLinkConsumer = new KafkaLinkConsumer();
        private KafkaLinkProducer kafkaLinkProducer = new KafkaLinkProducer();
        private KafkaHtmlProducer kafkaHtmlProducer = new KafkaHtmlProducer();
        private DummyDomainCache dummyDomainCache = new DummyDomainCache(30000);
        private PriorityQueue<Pair<Long, String>> priorityQueue = new PriorityQueue<>(Comparator.comparing(Pair::getKey));
        private Logger logger = Logger.getLogger(Crawler.class);
        private long count = 0, rejectByLRU = 0;

        public void start() {
            while (true) {
                while (!priorityQueue.isEmpty() && System.currentTimeMillis() - priorityQueue.peek().getKey() >= 30000) {
                    try {
                        crawl(priorityQueue.poll().getValue(), "PriorityQueue");
                    }
                    catch (Exception e) {
                        logger.error("Bale Bale: ", e);
                    }
                }
                logger.info("Start to poll");
                ArrayList<String> links = kafkaLinkConsumer.get();
                logger.info("End to poll");

                for (String link : links) {
                    try {
                        crawl(link, "KafkaLinkConsumer");
                    }
                    catch (Exception e) {
                        logger.error("Bale Bale: ", e);
                    }
                }
            }

        }
        static class stats  {
            public static long req, ld, parse, seri, produce, lru;
        }

        private void crawl(String link, String info) throws ExecutionException, InterruptedException {
            long req, ld, parse, seri, produce, lru;
            logger.info("before lru");
            lru = System.currentTimeMillis();
            boolean ifbool = dummyDomainCache.add(link, System.currentTimeMillis());
            lru = System.currentTimeMillis() - lru;
            stats.lru += lru;
            logger.info("after lru " + lru);

            if (!ifbool) {
//                 logger.info(info);
                rejectByLRU++;
                priorityQueue.add(new Pair<>(System.currentTimeMillis() + 30000, link));
                return;
            }
            long test2Armin = System.currentTimeMillis();
            test2Armin -=testArmin;
            logger.info("Armin " + test2Armin);
            testArmin = System.currentTimeMillis();

            logger.info("Before connection");
            req = System.currentTimeMillis();
            HttpRequest httpRequest = new HttpRequest(link);
            httpRequest.setMethod(HttpRequest.HTTP_REQUEST.GET);
            httpRequest.setRequestTimeout(15000); //TODO
            List<Pair<String, String>> headers = new ArrayList<>();
            headers.add(new Pair<>("accept", "text/html,application/xhtml+xml,application/xml"));
            headers.add(new Pair<>("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36"));
            httpRequest.setHeaders(headers);

            Response response = httpRequest.send().get();

            req = System.currentTimeMillis() - req;
            stats.req += req;
            logger.info("After connection \t " + req);

            String html = response.getResponseBody();
            count++;
            logger.info("Summery count: " + count + " rejectionsByLRU: " + rejectByLRU + " lruSize: " + dummyDomainCache.size());
            logger.info("before LD");
            ld = System.currentTimeMillis();
            PageData pageData = null;
            if (Language.getInstance().detector(html)) { //todo optimize
                ld = System.currentTimeMillis() - ld;
                logger.info("after LD " + ld);

                logger.info("before parser");
                parse = System.currentTimeMillis();
                htmlParser = new HtmlParser();
                pageData = htmlParser.parse(link, html);
                parse = System.currentTimeMillis() - parse;
                logger.info("after parser " + parse);
                stats.ld += ld;
                stats.parse+=parse;
            }

            if (pageData == null) {
                return;
            }
            logger.info("before serializer");
            seri = System.currentTimeMillis();
            byte[] bytes = PageDataSerializer.getInstance().serialize(pageData);
            seri = System.currentTimeMillis() - seri;
            stats.seri += seri;
            logger.info("after serializer " + seri);

            logger.info("before producing");
            produce = System.currentTimeMillis();
            kafkaHtmlProducer.send(Config.kafkaHtmlTopicName, "", bytes); //todo topic
            logger.info("Producing links:\t" + pageData.getLinks().size());
            for (Link pageDataLink: pageData.getLinks()) {
                kafkaLinkProducer.send(Config.kafkaLinkTopicName, "", pageDataLink.getLink());
            }
            produce = System.currentTimeMillis() - produce;
            stats.produce += produce;
            logger.info("after producing " + produce);
        }
}