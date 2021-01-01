docker-compose -f docker-compose-kafka.yml up 

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

EXPLAIN:
- connection:
    - we use port 29098 to communicate inside docker and port 9098 to communicate outside docker.
- producer/consumer
    - pro/con are on localhost(docker host) -- outside docker.


start ZooKeeper: bin/zkServer.sh start

/usr/bin ->  ./bin/kafka-topics --create --topic CountryTopic --boots
trap-server kafka:29092

mvn clean compile
mvn exec:java '-Dexec.mainClass=my.own.packages.MyProducer'