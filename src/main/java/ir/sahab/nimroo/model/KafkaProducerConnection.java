package ir.sahab.nimroo.model;

import org.apache.kafka.clients.producer.KafkaProducer;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaProducerConnection {

  public static void main(String[] args) {
    KafkaProducerConnection mm= new KafkaProducerConnection();
    mm.send("topic-name","1","salam");
  }

  public void send(String topic, String key, String value) {
    // create instance for properties to access producer configs
    Properties props = new Properties();
    // Assign localhost id
    props.put("bootstrap.servers", "192.168.122.105:9092");
    // Set acknowledgements for producer requests.
    props.put("acks", "all");
    // If the request fails, the producer can automatically retry,
    props.put("retries", 0);
    // Specify buffer size in config
    props.put("batch.size", 16384);
    // Reduce the no of requests less than 0
    props.put("linger.ms", 1);
    // The buffer.memory controls the total amount of memory available to the producer for buffering.
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    try (Producer<String, String> producer = new KafkaProducer<String, String>(props)) {
      producer.send(new ProducerRecord<String, String>(topic, key, value));
    }
  }
}
