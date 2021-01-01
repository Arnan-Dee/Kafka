/*
V 1.0
Simple Admin 
*/

package my.own.packages;

import java.util.*;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.AdminClient;

public class MyAdmin {

    public static void main( String[] args ){

        // list of Kafka servers
        List<String> servers = Arrays.asList("localhost:9092");
        String joinServer =  String.join(",",servers);

        Properties properties = new Properties();
        properties.put("bootstrap.servers", joinServer);

        // topics to be created
        List<NewTopic> topics = new ArrayList<NewTopic>();

        // topic 1
        String topicName1 = "demo";
        Optional<Integer> o1 = Optional.empty();
        Optional<Short> o2 = Optional.empty();
        NewTopic topic1 = new NewTopic(topicName1, o1, o2);
        topics.add(topic1);

        AdminClient admin =  KafkaAdminClient.create(properties);
        CreateTopicsResult success = admin.createTopics(topics);
        try {
            success.all().get();
            System.out.println("success");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("errr");
        }


    }
}

