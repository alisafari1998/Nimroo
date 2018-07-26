package ir.sahab.nimroo.model;

import org.apache.kafka.clients.producer.KafkaProducer;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaHtmlProducer {

    Producer<String, byte[]> producer;

    public KafkaHtmlProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.122.105:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, byte[]>(props);
    }

    public void send(String topic, String key, byte[] value) {
        producer.send(new ProducerRecord<String, byte[]>(topic, key, value));
    }
}