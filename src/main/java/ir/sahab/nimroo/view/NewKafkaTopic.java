package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.hbase.HBase;
import ir.sahab.nimroo.kafka.KafkaLinkConsumer;
import ir.sahab.nimroo.kafka.KafkaLinkProducer;
import ir.sahab.nimroo.model.DummyUrlCache;

import java.util.ArrayList;

public class NewKafkaTopic {
  private KafkaLinkConsumer kafkaLinkConsumer;
  private KafkaLinkProducer kafkaLinkProducer;
  private final DummyUrlCache dummyUrlCache = new DummyUrlCache();

  public static void main(String[] args) {
    Config.load();
    NewKafkaTopic newKafkaTopic = new NewKafkaTopic();
    newKafkaTopic.filter();
  }

  public NewKafkaTopic() {
    kafkaLinkConsumer = new KafkaLinkConsumer();
    kafkaLinkProducer = new KafkaLinkProducer();
  }

  private void filter() {
    while (true) {
      ArrayList<String> links = kafkaLinkConsumer.get();
      for (int i = 0; i < links.size(); i++) {
        String link = links.get(i);
        if (!dummyUrlCache.add(link) || HBase.getInstance().isDuplicateUrl(link)) {
          continue;
        }
        kafkaLinkProducer.send("goodlinks", null, link);
      }
    }
  }
}
