package ir.sahab.nimroo.model;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class KafkaLinkConsumer {

    KafkaConsumer<String, String> consumer;

    public KafkaLinkConsumer() {
        String topicName = "topic-name";
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.122.105:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", "1");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topicName));
    }

    public String get() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            if (!records.isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    consumer.commitSync();
                    return record.value();
                }
                break;
            }
        }
        return null;
    }
}

