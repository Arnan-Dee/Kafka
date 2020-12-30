// package my.own.packages;

// import java.util.Collections;
// import java.util.*;

// public class Consumer {
//     public static void main( String[] args ){
//         Properties props = new Properties();
//         props.put("bootstrap.servers", "broker1:9092,broker2:9092");
//         props.put("group.id", "AddressConsumer");
//         props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
//         props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
//         KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

//         String topic = "AddressTopic";
//         consumer.subscribe(Collections.singletonList(topic));

//         try {
//             while (true) {
//                 ConsumerRecords<String, String> records = consumer.poll(100);
//                 for (ConsumerRecord<String, String> record : records) {
//                     System.out.println("topic = %s, partition = %d, offset = %d, key = %s, value = %s\n",
//                         record.topic(), record.partition(), record.offset(), record.key(), record.value());
//                 }
//             }
//         } finally {
//             consumer.close();
//         }
//     }
// }