package my.own.packages;

/*
perform simple, state-less transformations via map functions.
1) Start Zookeeper and Kafka.
2) Create the input and output topics used by this example.

$ /bin/kafka-topics --create --topic TextLinesTopic \
                   --bootstrap-server kafka:29092 --partitions 1 --replication-factor 1
$ /bin/kafka-topics --create --topic UppercasedTextLinesTopic \
                   --bootstrap-server kafka:29092 --partitions 1 --replication-factor 1

3) Start this example application either in your IDE or on the command line.
4) Write some input data to the source topic (e.g. via {@code kafka-console-producer}). The already
running example application (step 3) will automatically process this input data and write the
results to the output topics.
$   /bin/kafka-console-producer --bootstrap-server kafka:29092 --topic TextLinesTopic

Start the console producer. You can then enter input data by writing some line of text, followed by ENTER:
#
#   hello kafka streams<ENTER>
#   all streams lead to kafka<ENTER>
#
# Every line you enter will become the value of a single Kafka message.
$ bin/kafka-console-producer --broker-list localhost:9092 --topic TextLinesTopic
5) Inspect the resulting data in the output topics, e.g. via {@code kafka-console-consumer}.

$ /bin/kafka-console-consumer --topic UppercasedTextLinesTopic --from-beginning \
                             --bootstrap-server kafka:29092

You should see output data similar to:
#   HELLO KAFKA STREAMS
#   ALL STREAMS LEAD TO KAFKA
*/

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.lang.String;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

public class streamMap {

    static final String inputTopic = "TextLinesTopic";
    static final String outputTopic = "UppercasedTextLinesTopic";

    // list of Kafka servers
    static final List<String> servers = Arrays.asList("localhost:9092");
    static final String bootstrapServers =  String.join(",",servers);

    public static void main(final String[] args) {

        // Configure the Streams application.
        final Properties streamsConfiguration = getStreamsConfiguration();
        
        // StreamsBuilder to be defined logical tolopogy.
        final StreamsBuilder builder = new StreamsBuilder();
        // Define logical tolopogy.
        mapTopology(builder);

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
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-tiw");
        // Client ID for this application instance.
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "map-machine1");
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        
        return streamsConfiguration;
    }

    static void mapTopology(final StreamsBuilder builder) {

        KStream<String,String> inputMass = builder.stream(inputTopic);
        // Map value to Uppercase
        KStream<String,String> outputMass = inputMass.flatMapValues((value) -> Arrays.asList(value.toUpperCase()));
        
        // Produce to outputTopic
        outputMass.to(outputTopic, Produced.with(Serdes.String(),Serdes.String()));
    }
    
}
