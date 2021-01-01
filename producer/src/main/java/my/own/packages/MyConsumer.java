/*
V 1.0
Simple Consumer 
*/

package my.own.packages;

import java.util.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class MyConsumer {
    public static void main( String[] args ){
        Properties props = new Properties();

        // list of Kafka servers
        List<String> servers = Arrays.asList("localhost:9092");
        String joinServer =  String.join(",",servers);

        
        props.put("bootstrap.servers", joinServer);
        props.put("group.id", "AddressConsumer");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        // list of subscribe topics
        List<String> topics = Arrays.asList("CountryTopic");
        consumer.subscribe(topics);

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partition = %d, offset = %d, key = %s, value = %s\n",
                        record.topic(), record.partition(), record.offset(), record.key(), record.value());
                    System.out.printf("------------------------------------------------------------\n");
                }
            }
        } finally {
            consumer.close();
        }
    }
}