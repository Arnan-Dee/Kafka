/*
V 1.0
Simple Producer 
*/

package my.own.packages;

import com.github.javafaker.Faker;
import java.lang.*;
import java.util.*;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import java.util.concurrent.TimeUnit;

public class MyProducer {

    public static void main( String[] args ){

        Properties kafkaProps = new Properties();

        // list of Kafka servers
        List<String> servers = Arrays.asList("localhost:9092");
        String joinServer =  String.join(",",servers);

        kafkaProps.put("bootstrap.servers", joinServer);
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(kafkaProps);
        String topic = "CountryTopic";
        int th = 1;
        while (true) {
            String data = GenData();
            ProducerRecord<String, String> record = new ProducerRecord<>(topic,"Address"+th,data);
            th ++;
            try {
                producer.send(record);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try{
                TimeUnit.SECONDS.sleep(2L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        

    }

    private static String GenData() {
        Faker faker = new Faker();
        String country = faker.address().country();
        return country;
        
    }
}
