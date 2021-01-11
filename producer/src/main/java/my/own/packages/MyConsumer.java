/*
V 1.0
Simple Consumer 
*/

package my.own.packages;

import static java.lang.String.join;

import java.util.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import java.time.Duration;

public class MyConsumer {
    public static void main( String[] args ){
        Properties props = new Properties();

        // list of Kafka servers
        List<String> servers = Arrays.asList("localhost:9092");
        String joinServer =  join(",",servers);

        
        props.put("bootstrap.servers", joinServer);
        props.put("group.id", "AddressConsumer");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        // client.id
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        // list of subscribe topics
        List<String> topics = Arrays.asList("CountryTopic");
        consumer.subscribe(topics);

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
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