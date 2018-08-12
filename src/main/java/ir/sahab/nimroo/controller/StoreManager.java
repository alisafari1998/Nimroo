package ir.sahab.nimroo.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.kafka.KafkaHtmlConsumer;
import ir.sahab.nimroo.model.ElasticClient;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class StoreManager {
  private ExecutorService executorService;
  private KafkaHtmlConsumer kafkaHtmlConsumer = new KafkaHtmlConsumer();
  private ElasticClient elasticClient = new ElasticClient();
  private Logger logger = Logger.getLogger(StoreManager.class);

  public StoreManager() {
    executorService =
        new ThreadPoolExecutor(200, 200, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(500));
  }

  public void start() throws InterruptedException, IOException {
    elasticClient.disableSource();
    while (true) {
      ArrayList<PageData> pageDatas = new ArrayList<>();
      ArrayList<byte[]> kafkaPoll = kafkaHtmlConsumer.get();
      for (byte[] temp : kafkaPoll) {
        pageDatas.add(PageDataSerializer.getInstance().deserialize(temp));
      }
      for (PageData pageData : pageDatas) {
        //elasticClient.addToBulkOfElastic(pageData,Config.elasticsearchIndexName);
      }
      try {
        elasticClient.addBulkToElastic();
      } catch (IOException e) {
        logger.error("add bulk to elastic problem: ", e);
      }
    }
  }
}
