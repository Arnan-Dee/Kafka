package my.own.packages;

/**
 * Demonstrates how to count things over time, using time windows. In this specific example we
 * read from a user click stream and detect any such users as anomalous that have appeared more
 * than twice in the click stream during one minute.
 * 
 * 1) Start Zookeeper and Kafka.
 * 
 * 2) Create the input and output topics used by this example.
 * $ /bin/kafka-topics --create --topic UserClicks \
 *                    --bootstrap-server kafka:29092 --partitions 1 --replication-factor 1
 * $ /bin/kafka-topics --create --topic AnomalousUsers \
 *                    --bootstrap-server kafka:29092 --partitions 1 --replication-factor 1
 * 
 * 3) Start this example application either in your IDE or on the command line.
 * 
 * 4) Write some input data to the source topic (e.g. via {@code kafka-console-producer}. The already
 * running example application (step 3) will automatically process this input data and write the
 * results to the output topic.
 * 
 * # Start the console producer. You can then enter input data by writing some line of text,
 * # followed by ENTER.  The input data you enter should be some example usernames; and because
 * # this example is set to detect only such users as "anomalous" that appear at least three times
 * # during a 1-minute time window, you should enter at least one username three times;otherwise
 * # this example won't produce any output data (cf. step 5).
 * #
 * #   alice<ENTER>
 * #   alice<ENTER>
 * #   bob<ENTER>
 * #   alice<ENTER>
 * #   alice<ENTER>
 * #   charlie<ENTER>
 * #
 * # Every line you enter will become the value of a single Kafka message.
 * $ bin/kafka-console-producer --bootstrap-server kafka:29092 --topic UserClicks
 * 
 * 5) Inspect the resulting data in the output topic, e.g. via {@code kafka-console-consumer}.
 * 
 * $ /bin/kafka-console-consumer --topic AnomalousUsers --from-beginning \
 *                              --bootstrap-server kafka:29092 \
 *                              --property print.key=true \
 *                              --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
 * 
 * You should see output data similar to:
 *
 * [alice@1466521140000]	4
 * 
 * Here, the output format is "[USER@WINDOW_START_TIME] COUNT".
 *
 * 6) Once you're done with your experiments, you can stop this example via {@code Ctrl-C}.  If needed,
 * also stop the Kafka broker ({@code Ctrl-C}), and only then stop the ZooKeeper instance ({@code Ctrl-C}).
 */
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

public class WindowStream {

    // list of Kafka servers
    private static List<String> servers = Arrays.asList("localhost:9092");
    private static String joinServer =  String.join(",",servers);


    private static Properties genConf() {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "anomaly-detection-lambda-example");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "anomaly-detection-lambda-example-client");
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, joinServer);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // Set the commit interval to 500ms so that any changes are flushed frequently. The low latency
        // would be important for anomaly detection.
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500);

        return streamsConfiguration;
    }

    public static void main(final String[] args) {

        final StreamsBuilder builder = new StreamsBuilder();
        final Properties streamsConfiguration = genConf();

        // Read the source stream.  In this example, we ignore whatever is stored in the record key and
        // assume the record value contains the username (and each record would represent a single
        // click by the corresponding user).
        final KStream<String, String> views = builder.stream("UserClicks");
        // 1 minute tumbling window
        final long windowTime = 1L;

        final KTable<Windowed<String>, Long> anomalousUsers = views
            // map the user name as key, because the subsequent counting is performed based on the key
            .map((ignoredKey, username) -> new KeyValue<>(username, username))
            // count users, using one-minute tumbling windows-- the advance interval is equal to the window size;
            // no need to specify explicit serdes because the resulting key and value types match our default serde settings
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(windowTime)))
            .count()
            // get users whose one-minute count is >= 3
            .filter((windowedUserId, count) -> count >= 3);


        // Note: The following operations would NOT be needed for the actual anomaly detection,
        // which would normally stop at the filter() above.  We use the operations below only to
        // "massage" the output data so it is easier to inspect on the console via
        // kafka-console-consumer.
        final KStream<String, Long> anomalousUsersForConsole = anomalousUsers
            // get rid of windows (and the underlying KTable) by transforming the KTable to a KStream
            .toStream()
            // sanitize the output by removing null record values (again, we do this only so that the
            // output is easier to read via kafka-console-consumer combined with LongDeserializer
            // because LongDeserializer fails on null values, and even though we could configure
            // kafka-console-consumer to skip messages on error, the output still wouldn't look pretty)
            .filter((windowedUserId, count) -> count != null)
            .map((windowedUserId, count) -> new KeyValue<>(windowedUserId.toString(), count));

        // write to the result topic
        anomalousUsersForConsole.to("AnomalousUsers", Produced.with(Serdes.String(), Serdes.Long()));

        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        streams.cleanUp();
        streams.start();
    
        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        
    }
    
}
