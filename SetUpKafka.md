# Setting Up Kafka

<details><summary>Mac</summary>
<p>

-   Start up the Zookeeper.

```youtrack
./zookeeper-server-start.sh ../config/zookeeper.properties
```

-   Start up the Kafka Broker.

```youtrack
./kafka-server-start.sh ../config/server.properties
```

</p>

</details>

## Setting Up Multiple Kafka Brokers

- The first step is to add a new **server.properties**.

- We need to modify three properties to start up a multi broker set up.

```
broker.id=<unique-broker-d>
listeners=PLAINTEXT://localhost:<unique-port>
log.dirs=/tmp/<unique-kafka-folder>
auto.create.topics.enable=false
```

- Example config will be like below.

```
broker.id=1
listeners=PLAINTEXT://localhost:9093
log.dirs=/tmp/kafka-logs-1
auto.create.topics.enable=false
```

### Starting up the new Broker

- Provide the new **server.properties** thats added.

```
./kafka-server-start.sh ../config/server-1.properties
```

```
./kafka-server-start.sh ../config/server-2.properties
```


## How to create a topic ?

**my-first-topic:**
```youtrack
./kafka-topics.sh --create --topic test-topic -zookeeper localhost:2181 --replication-factor 1 --partitions 4
```

## How to instantiate a Console Producer?

### Without Key

```
./kafka-console-producer.sh --broker-list localhost:9092 --topic test-topic
```

### With Key

```
./kafka-console-producer.sh --broker-list localhost:9092 --topic test-topic --property "key.separator=-" --property "parse.key=true"
```

## How to instantiate a Console Consumer?

### Without Key

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test-topic --from-beginning
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic library-events --from-beginning
```

### With Key

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test-topic --from-beginning -property "key.separator= | " --property "print.key=true"
```

### With Key and Values

- From Beginnning

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic library-events --from-begining \
--property key.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer \
--property value.deserialzer=org.apache.kafka.common.serialization.StringDeserializer \
--property print.key=true \
```


- Latest

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic library-events \
--property key.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer \
--property value.deserialzer=org.apache.kafka.common.serialization.StringDeserializer \
--property print.key=true \
```

### With ConsumerGroup

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic library-events --group console-consumer-73147
```

# Advanced Kafka CLI operations:

## List the topics in a cluster

```
./kafka-topics.sh --zookeeper localhost:2181 --list
```

## Describe topic

- The below command can be used to describe all the topics.

```
./kafka-topics.sh --zookeeper localhost:2181 --describe
```

- The below command can be used to describe a specific topic.

```
./kafka-topics.sh --zookeeper localhost:2181 --topic library-events
```
## How to view consumer groups

```
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

### Consumer Groups and their Offset

```
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group console-consumer-27773
```

## Viewing the Commit Log

```
./kafka-run-class.sh kafka.tools.DumpLogSegments --deep-iteration --files /tmp/kafka-logs/test-topic-0/00000000000000000000.log
```
