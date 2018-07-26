package ir.sahab.nimroo.model;

import ir.sahab.nimroo.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class KafkaHtmlConsumer {

  KafkaConsumer<String, byte[]> consumer;

  public KafkaHtmlConsumer() {
    String topicName = Config.kafkaHtmlTopicName;
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
    consumer = new KafkaConsumer<String, byte[]>(props);
    consumer.subscribe(Arrays.asList(topicName));
  }

  public byte[] get() {
    while (true) {
      ConsumerRecords<String, byte[]> records = consumer.poll(100);
      if (!records.isEmpty()) {
        for (ConsumerRecord<String, byte[]> record : records) {
          consumer.commitSync();
          return record.value();
        }
        break;
      }
    }
    return null;
  }
}
