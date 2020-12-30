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
        kafkaProps.put("bootstrap.servers", "localhost:9092");
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<String, String>(kafkaProps);
        String topic = "CountryTopic";
        int th = 1;
        while (true) {
            String data = gen_data();
            ProducerRecord<String, String> record = new ProducerRecord<>(topic,"address_"+th,data);
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

    private static String gen_data() {
        Faker faker = new Faker();
        String country = faker.address().country();
        return country;
        
    }
}
