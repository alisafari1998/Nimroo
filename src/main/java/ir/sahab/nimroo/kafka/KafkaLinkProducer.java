package ir.sahab.nimroo.kafka;

import ir.sahab.nimroo.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaLinkProducer {

  private Producer<String, String> producer;

  public KafkaLinkProducer() {
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
    props.put("acks", Config.kafkaProducerAcks);
    props.put("retries", 0);
    props.put("batch.size", Config.kafkaProducerBatchSize);
    props.put("linger.ms", Config.kafkaProducerLingerMS);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producer = new KafkaProducer<String, String>(props);
  }

  public void send(String topic, String key, String value) {
    producer.send(new ProducerRecord<String, String>(topic, key, value));
  }
}
