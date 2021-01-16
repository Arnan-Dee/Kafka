# Setup and Run
Assumed that this repo was cloned to your local computer.
1. In your root project dir. Start docker runtime. Create all required containers by 
```
$ docker-compose -f docker-compose-kafka.yml up
```
2. Start the appication you want either in your IDE or on the command line. k

If via IDE execute mvn commands: 
```
$ mvn clean compile
$ mvn exec:java -Dexec.mainClass=my.own.packages.${AppClass}
```
Note that you must already have maven installed. 

Via command line make sure you are in the maven package dir--`producer`. Create a standalone jar ("fat jar"). If needed, you can disable the test suite during packaging: 
```
$ mvn clean package
or
$ mvn -DskipTests=true clean package
```
Note that there is no test appication written in this repo. This creates `target/producer-1.0.jar`. You can now run the application examples as follows:
```
$ java -cp target/producer-1.0.jar my.own.packages.${AppClass}
```
3. In some examples you may first need to create topic and add some records manually. To create a topic, first open bash shell in `kafka` container then run commands to create a topic as follows:
```
$ /bin/kafka-topics --create --topic ${topicName} --bootstrap-server kafka:29092 
```
Note that in MyProducer/MyConsumer appication, we produce records to topic named `CountryTopic` and consume records from the same topic. So, in order to have the appication run you have to create topic named `CountryTopic`. In other example appications you may have to create topic with the corresponding names.

To consume records from a topic use the below command
```
$ /bin/kafka-console-consumer --topic ${topicName} --from-beginning --bootstrap-server kafka:29092
```
or in some examples you may have to specify a proper deserializer: use
```
$ /bin/kafka-console-consumer --topic ${topicName} --from-beginning \
                            --bootstrap-server kafka:29092 \
                            --property print.key=true \
                            --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

```
in above command we use LongDeserializer provided by `org.apache.kafka` package.

To produce records by using console producer use:
```
$ /bin/kafka-console-producer --broker-list kafka:29092 --topic ${topicName}
```

# ZOOKEEPER 
For configuration <a href='https://hub.docker.com/r/confluentinc/cp-zookeeper'>confluentinc/cp-zookeeper</a> image only. Alternatively, one can use <a href="https://hub.docker.com/r/bitnami/zookeeper/">bitnami/zookeeper</a> image instead. However, be careful with variable names.
See <a href='https://zookeeper.apache.org/doc/r3.3.2/zookeeperAdmin.html#sc_maintenance'>ZooKeeper Administrator's Guide.</a> 
See <a href='https://zookeeper.apache.org/doc/r3.4.0/zookeeperProgrammers.html'>ZooKeeper Programmer's Guide.</a> <br>

### Minimum Configuration
- Instructs ZooKeeper where to listen for connections by clients such as Kafka.<br>
    `**ZOOKEEPER_CLIENT_PORT**`<br>
- the basic time unit in milliseconds used by ZooKeeper. It is used to do heartbeats and the minimum session timeout will be twice the tickTime.<br>
    `**ZOOKEEPER_TICK_TIME**`<br>
- the location to store the in-memory database snapshots and, unless specified otherwise, the transaction log of updates to the database.<br>
    `**ZOOKEEPER_DATA_DIR**`<br>

# KAFKA
For configuration <a href='https://hub.docker.com/r/confluentinc/cp-kafka/'>confluentinc/cp-kafka</a> image only.
Alternatively, one can use <a href="https://hub.docker.com/r/bitnami/kafka/"></a> bitnami/kafka image instead.<br>
See <a href='https://kafka.apache.org/documentation/'>Official's Kafka Documentation.</a>
See <a href='https://www.confluent.io/blog/kafka-listeners-explained/'>Listener.</a>


# SCHEMA REGISTRY
For configuration <a href='https://hub.docker.com/r/confluentinc/cp-schema-registry'>confluentinc/cp-schema-registry
</a> image only.<br>
See <a href='https://docs.confluent.io/platform/current/schema-registry/index.html'>Schema Management Overview.</a>
See <a href='https://docs.confluent.io/platform/current/schema-registry/installation/config.html#schemaregistry-config'>Configuration Details.</a><br>

# Reliability Guarantees
By designing a certain level of reliable system, there are trade-offs to be considered.

- ### Broker Configuration
    - Replication Factor: higher replication factor leads to higher availability, higher reliability, and fewer disasters at the cost of hardware. Recommend for general use case: 3.
    - Unclean Leader Election: if we allow out-of-sync replicas to become leaders(`unclean.leader.election.enable=true`), we risk data loss and data inconsistencies. If we don’t allow them to become leaders, we face lower availability. As we must wait for the original leader to become available before the partition is back online.
    - Minimum In-Sync Replicas: A trade-off between availability and consistency.
- ### Producers Configuration
    - Send Acknowledgments : `acks=0` means that a message is considered to be written successfully to Kafka if
    the producer managed to send it over the network. `acks=1` means that the leader will send either an acknowledgment or an error the moment it got the message but sometime it was not replicated to the followers - before the crash. `acks=all` means that the leader will wait until all in-sync replicas got the message
    before sending back an acknowledgment or an error.
    - Configuring Producer Retries.
- ### Consumers Configuration
    - `group.id`
    - `auto.offset.reset`
    - `enable.auto.commit`. One should not set `enable.auto.commit=true`, instead, one should commit massages manually according to these scenarios.
        * Always commit offsets after events were processed.
        * Commit frequency is a trade-off between performance and number of duplicates in the event of a crash.
        * Make sure you know exactly what offsets you are committing.
        * Rebalances. See Manually commit offset below.
        * Consumers may need to retry.
        * Consumers may need to maintain state.
        * Handling long processing times.
        * Exactly-once delivery.

- ### Methods of sending messages

    - **Fire-and-forget**:
    We send a message to the server and don’t really care if it arrives succesfully or
    not. Most of the time, it will arrive successfully, since Kafka is highly available
    and the producer will retry sending messages automatically. However, some messages
    will get lost using this method.

    - **Synchronous send**: 
    We send a message, the `send()` method returns a Future object, and we use `get()`
    to wait on the future and see if the `send()` was successful or not.

    - **Asynchronous send**: 
    We call the `send()` method with a callback function, which gets triggered when it
    receives a response from the Kafka broker.


- ### Commit Current Offset

    - **Default setting--** `enable.auto.commit=true`: 
    With autocommit enabled, a call to poll will always commit the last offset returned by the previous poll every `auto.commit.interval.ms`. This rule of commit do not 
    guarantee if the massages are actully be processed or not.
    By setting `enable.auto.commit=false`, the appication has to commit the consumed massages by itself. There
    are several alternatives to do so which has its own use cases.
    asynchronous commits only make sense for “at least once” message delivery. To get “at most once,” you need to know if the commit succeeded before consuming the message. This implies a synchronous commit 

    - **Sync Commit**: 
    `commitSync()` will commit the latest offset returned
    by `poll()`, so make sure you call `commitSync()` after you are done processing all the
    records in the collection, or you risk missing messages. `commitSync()` will retry the commit until it either succeeds or encounters a nonretriable failure. However, the application is blocked until the broker
    responds to the commit request, limitting the throughput of the application.
    you can still scale up by increasing the number of topic partitions and the number of consumers in the group

    - **Asynchronous Commit**: 
    In contrast of `commitSync()`, `commitAsync()` will not retry because by the time `commitAsync()` receives a response from the server, there may have been a later commit that was already successful. To address that problem, a callback function is provided if necessary. The callback is a implemented class of `OffsetCommitCallback` interface.



- ### Exactly-Once Semantics (in 0.11 release)

    - **Idempotent Producer Guarantees**:
    PID, and sequence numbers.
    This ensures that, even though a producer must retry requests upon failures, every message will be persisted in the log exactly once. Further, since each new instance of a producer is assigned a new, unique, PID, we can only guarantee idempotent production within a single producer session.

    - **Transactional Guarantees**:
    At the core, transactional guarantees enable applications to produce to multiple TopicPartitions atomically, ie. all writes to these TopicPartitions will succeed or fail as a unit. 

    Further, since consumer progress is recorded as a write to the offsets topic, the above capability is leveraged to enable applications to batch consumed and produced messages into a single atomic unit, ie. a set of messages may be considered consumed only if the entire ‘consume-transform-produce’ executed in its entirety. Stream processing applications written in the Kafka Streams library can turn on exactly-once semantics by simply making a single config change, to set the config named `processing.guarantee` to `exactly_once `(default value is `at_least_once`), with no code change required.


# AVRO Serializing and deserializing 

There are 2 options to use AVRO Ser/De(require `avro-maven-plugin`):

- Use Serializing and deserializing with code generation: This enable client to create a Java class
that holds intented data, and provides more simple code. To do so, first, you have to define the schema
in the configured DIR in a .avsc file than the plugin automatically performs code generation on any .avsc files present in the configured source directory.

- Use Serializing and deserializing without code generation: The key is the producer appication itself holds
the schema within its source code.

However, both methods should use schema registry to store generated schemas, which is competible(and required)
with `io.confluent.kafka.serializers.KafkaAvroDeserializer`/`io.confluent.kafka.serializers.KafkaAvroSerializer`.
For more details see <a href='https://avro.apache.org/docs/current/gettingstartedjava.html'>avro.apache.org</a>.


For more detailed examples see <a href='https://github.com/confluentinc'>Confluentinc</a> repo.
More on custom Serde see <a href='https://docs.confluent.io/platform/current/streams/developer-guide/datatypes.html'>Confluentinc</a> doc.

Keep learning <a href='https://docs.confluent.io/platform/current/kafka-rest/index.html'>Confluent REST APIs<a>, 
<a href='https://docs.confluent.io/platform/current/schema-registry/index.html'>Schema Management Overview</a> ,
<a href='https://docs.ksqldb.io/en/latest/'>ksqlDB</a>, <a href='https://kafka.apache.org/documentation.html#connect'>Kafka connect</a>.

