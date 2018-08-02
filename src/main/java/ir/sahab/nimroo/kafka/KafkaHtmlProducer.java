package ir.sahab.nimroo.kafka;

import ir.sahab.nimroo.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

public class KafkaHtmlProducer {

  Producer<String, byte[]> producer;

  public KafkaHtmlProducer() {
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
    props.put("value.serializer", ByteArraySerializer.class.getName());
    producer = new KafkaProducer<String, byte[]>(props);
  }

  public void send(String topic, String key, byte[] value) {
    producer.send(new ProducerRecord<String, byte[]>(topic, key, value));
  }
}
