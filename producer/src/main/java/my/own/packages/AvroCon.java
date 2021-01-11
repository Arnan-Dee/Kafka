package my.own.packages;

import static java.lang.String.join;
import java.util.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import java.time.Duration;


public class AvroCon {

    // list of Kafka servers
    private static List<String> servers = Arrays.asList("localhost:9092");
    private static String joinServer =  join(",",servers);
    private static final String schemaRegistry = "http://localhost:8081";

    public static void main( String[] args ) {

        Properties consumerConf = genConf();
        String topic = "GoTCharacter";
        
        final KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<String, GenericRecord>(consumerConf);
        consumer.subscribe(Arrays.asList(topic));

        try {
            while (true) {
              ConsumerRecords<String, GenericRecord> records = consumer.poll(Duration.ofMillis(100));
              for (ConsumerRecord<String, GenericRecord> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s \n", record.offset(), record.key(), record.value());
                System.out.printf("------------------------------------------------------------\n");
                }
            }
        } finally {
            consumer.close();
        }

    }

    private static Properties genConf() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, joinServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");


        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;

    }
}
