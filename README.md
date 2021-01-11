# ZOOKEEPER 
For configuration <a href='https://hub.docker.com/r/confluentinc/cp-zookeeper'>confluentinc/cp-zookeeper</a> image only. Alternatively, one can use <a href="https://hub.docker.com/r/bitnami/zookeeper/">bitnami/zookeeper</a> image instead. However, be careful with variable names.<br>
See <a href='https://zookeeper.apache.org/doc/r3.3.2/zookeeperAdmin.html#sc_maintenance'>ZooKeeper Administrator's Guide</a> <br>
See <a href='https://zookeeper.apache.org/doc/r3.4.0/zookeeperProgrammers.html'>ZooKeeper Programmer's Guide</a> <br>

## Standalone Operation
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
See <a href='https://kafka.apache.org/documentation/'>Official's Kafka Documentation.</a><br>
See <a href='https://www.confluent.io/blog/kafka-listeners-explained/'>Listener</a><br>


# SCHEMA REGISTRY
For configuration <a href='https://hub.docker.com/r/confluentinc/cp-schema-registry'>confluentinc/cp-schema-registry
</a> image only.<br>
See <a href='https://docs.confluent.io/platform/current/schema-registry/index.html'>Schema Management Overview</a><br>
See <a href='https://docs.confluent.io/platform/current/schema-registry/installation/config.html#schemaregistry-config'>Configuration Details.</a><br>

## Reliability Guarantees
By designing a certain level of reliable system, there are trade-offs to be considered.

- ### Broker Configuration
    - Replication Factor : higher replication factor leads to higher availability, higher reliability, and fewer disasters at the cost of hardware. Recommend for general use case : 3.
    - Unclean Leader Election : if we allow out-of-sync replicas to become leaders(`unclean.leader.election.enable=true`), we risk data loss and data inconsistencies. If we don’t allow them to become leaders, we face lower availability. As we must wait for the original leader to become available before the partition is back online.
    - Minimum In-Sync Replicas : A trade-off between availability and consistency.
- ### Producers Configuration
    - Send Acknowledgments : `acks=0` means that a message is considered to be written successfully to Kafka if
    the producer managed to send it over the network. `acks=1` means that the leader will send either an acknowledgment or an error the moment it got the message but sometime it was not replicated to the followers before the crash. `acks=all` means that the leader will wait until all in-sync replicas got the message
    before sending back an acknowledgment or an error.
    - Configuring Producer Retries
- ### Consumers Configuration
    - `group.id`
    - `auto.offset.reset`
    - `enable.auto.commit`. One should not set `enable.auto.commit=true`, instead, one should commit massages manually according to these scenarios.
        * Always commit offsets after events were processed.
        * Commit frequency is a trade-off between performance and number of duplicates in the event of a crash.
        * Make sure you know exactly what offsets you are committing.
        * Rebalances. See <a id="commit">Manually commit offset below.</a>
        * Consumers may need to retry.
        * Consumers may need to maintain state.
        * Handling long processing times.
        * Exactly-once delivery.

[methods of sending messages](#send)
Fire-and-forget
We send a message to the server and don’t really care if it arrives succesfully or
not. Most of the time, it will arrive successfully, since Kafka is highly available
and the producer will retry sending messages automatically. However, some messages
will get lost using this method.

Synchronous send
We send a message, the send() method returns a Future object, and we use get()
to wait on the future and see if the send() was successful or not.

Asynchronous send
We call the send() method with a callback function, which gets triggered when it
receives a response from the Kafka broker









[Commit Current Offset](#commit)

Defult setting `enable.auto.commit=true`. With autocommit enabled, a call to poll will always commit the last offset returned by the previous poll every `auto.commit.interval.ms`. This rule of commit do not 
guarantee if the massages are actully be processed or not.
By setting `enable.auto.commit=false`, the appication has to commit the consumed massages by itself. There
are several alternatives to do so which has its own use cases.
asynchronous commits only make sense for “at least once” message delivery. To get “at most once,” you need to know if the commit succeeded before consuming the message. This implies a synchronous commit 

Sync Commit
`commitSync()` will commit the latest offset returned
by `poll()`, so make sure you call `commitSync()` after you are done processing all the
records in the collection, or you risk missing messages. `commitSync()` will retry the commit until it either succeeds or encounters a nonretriable failure. However, the application is blocked until the broker
responds to the commit request, limitting the throughput of the application.
you can still scale up by increasing the number of topic partitions and the number of consumers in the group

Asynchronous Commit
In contrast of `commitSync()`, `commitAsync()` will not retry because by the time `commitAsync()` receives a response from the server, there may have been a later commit that was already successful. To address that problem,
a callback function is provided if necessary. The callback is a implemented class of OffsetCommitCallback interface.



Exactly-Once Semantics in 0.11 release
- Idempotent Producer Guarantees : PID, and sequence numbers
This ensures that, even though a producer must retry requests upon failures, every message will be persisted in the log exactly once. Further, since each new instance of a producer is assigned a new, unique, PID, we can only guarantee idempotent production within a single producer session.
- Transactional Guarantees :
At the core, transactional guarantees enable applications to produce to multiple TopicPartitions atomically, ie. all writes to these TopicPartitions will succeed or fail as a unit. 

Further, since consumer progress is recorded as a write to the offsets topic, the above capability is leveraged to enable applications to batch consumed and produced messages into a single atomic unit, ie. a set of messages may be considered consumed only if the entire ‘consume-transform-produce’ executed in its entirety.

V1.0

Simple Producer/Consumer 

V1.1

Simple Producer/Consumer with manually commit offsets

V1.2

Producer/Consumer with Arvo/JSON Serde and Schema registry

V1.3

Producer/Consumer with Exactly-Once Guarantee.

V2.0

Simple Stream application, simple agg, stateless.

V2.1

Simple Stream application, simple agg, stateful.

V2.2

Simple Stream application, windowed operation.

V3.0

Stream application with DB(data enrichment)


processing.guarantee=exactly_once

/usr/bin ->  ./bin/kafka-topics --create --topic CountryTopic --boots
trap-server kafka:29092

mvn clean compile
mvn exec:java '-Dexec.mainClass=my.own.packages.MyProducer'
kafka-console-consumer --topic GoTCharacter --from-beginning --bootstrap-server kafka:29092
kafka-topics --create --topic GoTCharacter --bootstrap-server kafka:29092

docker-compose -f docker-compose-kafka.yml up -d