package ir.sahab.nimroo.kafka;

import ir.sahab.nimroo.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class KafkaLinkConsumer {

  KafkaConsumer<String, String> consumer;

  public KafkaLinkConsumer() {
    String topicName = Config.kafkaLinkTopicName;
    Properties props = new Properties();
    // props.put("bootstrap.servers", Config.server1Address + ":" + Config.kafka1Port);
    props.put(
        "bootstrap.servers",
        Config.server1Address
            + ":"
            + Config.kafka1Port
            + ","
            + Config.server2Address
            + ":"
            + Config.kafka2Port
            + ","
            + Config.server2Address
            + ":"
            + Config.kafka3Port);
    props.put("group.id", Config.kafkaConsumerGroupId);
    props.put("enable.auto.commit", "true");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", Config.kafkaConsumerSessionTimeoutsMS);
    props.put("max.poll.records", Config.kafkaConsumerMaxPollRecords);
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumer = new KafkaConsumer<String, String>(props);
    consumer.subscribe(Arrays.asList(topicName));
  }

  public ArrayList<String> get() {
    ArrayList<String> pollValues = new ArrayList<>();
    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(100);
      if (!records.isEmpty()) {
        for (ConsumerRecord<String, String> record : records) {
          pollValues.add(record.value());
        }
        break;
      }
    }
    consumer.commitSync();
    return pollValues;
  }
}
