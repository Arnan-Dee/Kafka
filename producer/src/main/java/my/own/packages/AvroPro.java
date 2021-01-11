package my.own.packages;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import com.github.javafaker.Faker;
import com.github.javafaker.GameOfThrones;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AvroPro {

    // list of Kafka servers
    private static final List<String> servers = Arrays.asList("localhost:9092");
    private static final String joinServer = String.join(",", servers);
    // schema registry server
    private static final String schemaRegistry = "http://localhost:8081";

    private static Properties getConf() {

        final Properties props = new Properties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, joinServer);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);

        return props;
    }

    public static void main(String[] args) {

        Properties producerConf = getConf();
        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<String, GenericRecord>(producerConf);

        String topic = "GoTCharacter";
        int th = 1;
        
        String userSchema = "{\"namespace\": \"namespace.avro\", " +
            "\"type\": \"record\"," +
            "\"name\": \"GoTCharacter\"," +
            "\"fields\": [" +
                "{\"name\": \"name\", \"type\": \"string\", \"doc\": \"a GameofTrone character's name\", \"default\": \"UNKNOWN\"}," +
                "{\"name\": \"house\", \"type\": [\"null\",\"string\"], \"default\": null}," +
                "{\"name\": \"quote\", \"type\": [\"null\",\"string\"], \"default\": null}" +
            "]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);

        while (true) {

            GameOfThrones fakeChar = new Faker().gameOfThrones();
            GenericRecord myGotAvro = new GenericData.Record(schema);
            myGotAvro.put("name", fakeChar.character());
            myGotAvro.put("house", fakeChar.house());
            myGotAvro.put("quote", fakeChar.quote());
            
            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic,"record"+th,myGotAvro);
            th ++;
            try {
                producer.send(record);
            } catch (Exception e) {
                e.printStackTrace();
            } 

            try {
                TimeUnit.SECONDS.sleep(2L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        }                
        
    }

}
