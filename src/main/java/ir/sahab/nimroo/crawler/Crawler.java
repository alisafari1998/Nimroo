package ir.sahab.nimroo.crawler;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.crawler.cache.DummyDomainCache;
import ir.sahab.nimroo.crawler.cache.DummyUrlCache;
import ir.sahab.nimroo.connection.HttpRequest;
import ir.sahab.nimroo.hbase.HBase;
import ir.sahab.nimroo.kafka.KafkaHtmlProducer;
import ir.sahab.nimroo.kafka.KafkaLinkConsumer;
import ir.sahab.nimroo.kafka.KafkaLinkProducer;
import ir.sahab.nimroo.kafka.LinkShuffler;
import ir.sahab.nimroo.model.*;
import ir.sahab.nimroo.crawler.parser.HtmlParser;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import ir.sahab.nimroo.crawler.util.Language;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Crawler {


    public Crawler() {
        executorService = new ThreadPoolExecutor(200, 200, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(500));
    }

    private HtmlParser htmlParser;
    private KafkaLinkConsumer kafkaLinkConsumer;
    private KafkaLinkProducer kafkaLinkProducer = new KafkaLinkProducer();
    private KafkaHtmlProducer kafkaHtmlProducer = new KafkaHtmlProducer();
    private final DummyDomainCache dummyDomainCache = new DummyDomainCache(30000);
    private final DummyUrlCache dummyUrlCache = new DummyUrlCache();
    private BlockingQueue<String> linkQueueForShuffle = new ArrayBlockingQueue<>(100000);
    private LinkShuffler linkShuffler;

    private Logger logger = Logger.getLogger(Crawler.class);
    private Long rejectByLRU = 0L;
    private AtomicLong count = new AtomicLong(0L),
            dlCount = new AtomicLong(0L), parseCount = new AtomicLong(0L);
    private ExecutorService executorService;
    private int allLinksCount = 1;
    private double passedDomainCheckCount;
    public void start() throws InterruptedException {
        HttpRequest.init();
        kafkaLinkConsumer = new KafkaLinkConsumer(Config.kafkaLinkTopicName);
        linkShuffler = new LinkShuffler(this);
        Thread shuffleThread = new Thread(linkShuffler);
        shuffleThread.start();
        long time = System.currentTimeMillis(),timeLru, timeProduceBack;
        while (true) {
            logger.info("Start to poll");
            ArrayList<String> links = kafkaLinkConsumer.get();
            logger.info("End to poll");
            logger.info("Summery kafkaConsumedBatchSize: " + links.size());

            for (int i = 0; i < links.size();) {
                String link = links.get(i);
                allLinksCount++;
                timeLru = System.currentTimeMillis();
                if (!dummyDomainCache.add(link, System.currentTimeMillis())) {
                    rejectByLRU++;
                    timeProduceBack = System.currentTimeMillis();
//                    kafkaLinkProducer.send(Config.kafkaLinkTopicName, null, link);
                    produceLink(link);
                    timeProduceBack = System.currentTimeMillis() - timeProduceBack;
                    logger.info("[Timing] TimeProduceBack: " + timeProduceBack);
                    i++;
                    continue;
                }
                passedDomainCheckCount++;
                timeLru = System.currentTimeMillis() - timeLru;
                logger.info("[Timing] TimeLru: " + timeLru);

                try {
                    executorService.submit(()-> crawl(link, "KafkaLinkConsumer"));
                    logger.info("Summery ldCount: " + dlCount + " speedS: " + dlCount.longValue() / ((System.currentTimeMillis() - time) / 1000));
                    logger.info("Summery parseCount: " + parseCount + " speedS: " + parseCount.longValue() / ((System.currentTimeMillis() - time) / 1000));
                    logger.info("Summery finalCount: " + count + " speedM: " + 60 *  count.longValue() / ((System.currentTimeMillis() - time) / 1000));
                    logger.info("Summery finalCount: " + count + " speedS: " + count.longValue() / ((System.currentTimeMillis() - time) / 1000));
                    logger.info("Summery allLinks: " + allLinksCount + " passedDomain: " + passedDomainCheckCount / allLinksCount * 100);
                    logger.info("Summery domains: " + dummyDomainCache.size());
                    logger.info("Summery rejectionsByLRU: " + rejectByLRU);
                    logger.info("Summery blocking queue size:" + linkQueueForShuffle.size());
                }
                catch (RejectedExecutionException e) {
                    Thread.sleep(40);
                    continue;
                }
                catch (Exception e) {
                    logger.error("Bale Bale: ", e);
                }

                i++;
            }
        }

    }

    private void crawl(String link, String info){
        int uniqueLinkProducingCount;
        long timeGet, timeLd, timeParse, timeSerialize, timeProducePageData, timeProduceLinks;
        logger.info("Link: " + link);

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
        httpRequest1.setRequestTimeout(15000);
        try {
            response = httpRequest1.send().get().getResponseBody();
            dlCount.addAndGet(1);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (response == null || response.length() == 0) {
            logger.info("response null");
            return;
        }

        PageData pageData = null;
        timeParse = System.currentTimeMillis();
        htmlParser = new HtmlParser();
        pageData = htmlParser.parse(link, response);
        parseCount.addAndGet(1);
        timeParse = System.currentTimeMillis() - timeParse;
        logger.info("[Timing] TimeParse: " + timeParse);

        timeLd = System.currentTimeMillis();
        if (Language.getInstance().detector(pageData.getText().substring(0,java.lang.Math.min(pageData.getText().length(),1000)))) {
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
        kafkaHtmlProducer.send(Config.kafkaHtmlTopicName, pageData.getUrl(), bytes); //todo topic
//        logger.info("PageData:\t" + pageData.toString());
        timeProducePageData = System.currentTimeMillis() - timeProducePageData;
        logger.info("[Timing] TimeProducePageData : " + timeProducePageData);

        timeProduceLinks = System.currentTimeMillis();
        uniqueLinkProducingCount = 0;
        for (int i = 0; i < pageData.getLinks().size(); i+=10) {
            Link pageDataLink = pageData.getLinks().get(i);
            if (!dummyUrlCache.add(pageDataLink.getLink()) || HBase.getInstance().isDuplicateUrl(pageDataLink.getLink())) {
                continue;
            }
            uniqueLinkProducingCount++;
            produceLink(pageDataLink.getLink());
        }
        logger.info("Producing links:\t" + uniqueLinkProducingCount);
        timeProduceLinks = System.currentTimeMillis() - timeProduceLinks;
        logger.info("[Timing] TimeProduceLinks: " + timeProduceLinks);
        count.addAndGet(1);
    }

    private void produceLink(String link) {
        try {
            linkQueueForShuffle.add(link);
            if(linkQueueForShuffle.size() == 100000) {
                final Object LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING =linkShuffler.getLOCK_FOR_WAIT_AND_NOTIFY_PRODUCING();
                synchronized (LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING) {
                    LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING.notify   ();
                }
            }
        }
	    catch (Exception e) {
            logger.error("ShufferError: ", e);
        }
    }

    public String getFromLinkQueue() {
        try {
            return linkQueueForShuffle.take();
        } catch (InterruptedException e) {
            logger.error("blocking queue interrupted" , e);
        }
        return null;
    }
}
