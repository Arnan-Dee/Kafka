package my.own.packages;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.lang.String;


/*
the input stream reads from a topic named "streams-plaintext-input", where the values of
messages represent lines of text; and the histogram output is written to topic
"streams-wordcount-output", where each record is an updated count of a single word, i.e. {@code word (String) -> currentCount (Long)}.

Note: Before running this example you must 
1) create the source topic (e.g. via {@code kafka-topics --create ...}), then 
2) start this example and 
3) write some data to the source topic (e.g. via {@code kafka-console-producer}).
Otherwise you won't see any data arriving in the output topic.
Inspect the resulting data in the output topic, e.g. via {@code kafka-console-consumer}.

$ /bin/kafka-topics --create --topic streams-plaintext-input \
        --bootstrap-server kafka:29092 --partitions 1 --replication-factor 1
$ /bin/kafka-topics --create --topic streams-wordcount-output \
        --bootstrap-server kafka:29092 --partitions 1 --replication-factor 1

$ /bin/kafka-console-producer --bootstrap-server kafka:29092 --topic streams-plaintext-input

$ /bin/kafka-console-consumer --topic streams-wordcount-output --from-beginning \
                            --bootstrap-server kafka:29092 \
                            --property print.key=true \
                            --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

Note: The output you see is indeterminate the exact output sequence will depend on how fast you
type the input massages. This is because Kafka Streams use Record caches to store records in memory
to reduce the number of requests going to a state store. The Record caches evolve `CACHE_MAX_BYTES_BUFFERING_CONFIG`
*/

public class  streamWordCount{

    static final String inputTopic = "streams-plaintext-input";
    static final String outputTopic = "streams-wordcount-output";

    // list of Kafka servers
    static final List<String> servers = Arrays.asList("localhost:9092");
    static final String bootstrapServers =  String.join(",",servers);

    public static void main(final String[] args) {
    
        // Configure the Streams application.
        final Properties streamsConfiguration = getStreamsConfiguration();
        
        // StreamsBuilder to be defined logical tolopogy.
        final StreamsBuilder builder = new StreamsBuilder();
        // Define logical tolopogy.
        createWordCountStream(builder);
        // KafkaStreams is the unit of performing continuous computation. It can take 2 parameter
        // a Tolopogy object, Configuration.
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        streams.cleanUp();

        // Now run the processing topology via `start()` to begin processing its input data.
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close the Streams application.
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }



    static Properties getStreamsConfiguration() {

        final Properties streamsConfiguration = new Properties();

        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-tiw");
        // Client ID for this application instance.
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "wordcount-machine1");
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // Records should be flushed every 10 seconds. This is less than the default
        // in order to keep this example interactive.
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        

        return streamsConfiguration;
    }

    static void createWordCountStream(final StreamsBuilder builder) {

        // Construct a `KStream` from the input topic "streams-plaintext-input", where message values
        // represent lines of text (for the sake of this example, we ignore whatever may be stored
        // in the message keys).  The default key and value serdes will be used.
        final KStream<String, String> textLines = builder.stream(inputTopic);
        
        final KTable<String, Long> wordCounts = textLines
        // Split each text line, by whitespace, into words.  The text lines are the record
        // values.
            .flatMapValues(value -> Arrays.asList(value.toLowerCase().split(" ")))
        // Group the split data by word so that we can subsequently count the occurrences per word.
        // This step re-keys (re-partitions) the input data, with the new record key being the words.
        // Note: No need to specify explicit serdes because the resulting key and value types
        // (String and String) match the application's default serdes.
            .groupBy((keyIgnored, word) -> word)
        // Count the occurrences of each word (record key).
            .count();
        
        // Write the `KTable<String, Long>` to the output topic. Produced class is used to provide
        // the optional parameters when producing to new topics.
        wordCounts.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));

    }
}

