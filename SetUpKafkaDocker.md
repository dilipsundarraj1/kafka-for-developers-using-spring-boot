# Set Up Kafka in Local using Docker

## Set up broker and zookeeper

- Navigate to the path where the **docker-compose.yml** is located and then run the below command.

```
docker-compose up
```

## Producer and Consume the Messages

- Let's going to the container by running the below command.

```
docker exec -it kafka1 bash
```

- Create a Kafka topic using the **kafka-topics** command.
  - **kafka1:19092** refers to the **KAFKA_ADVERTISED_LISTENERS** in the docker-compose.yml file.

```
kafka-topics --bootstrap-server kafka1:19092 \
             --create \
             --topic test-topic \
             --replication-factor 1 --partitions 1
```

- Produce Messages to the topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server kafka1:19092 \
                       --topic test-topic
```

- Consume Messages from the topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --from-beginning
```

## Producer and Consume the Messages With Key and Value

- Produce Messages with Key and Value to the topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --property "key.separator=-" --property "parse.key=true"
```

- Consuming messages with Key and Value from a topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --from-beginning \
                       --property "key.separator= - " --property "print.key=true"
```

### Consume Messages using Consumer Groups


```
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic --group console-consumer-41911\
                       --property "key.separator= - " --property "print.key=true"
```

- Example Messages:

```
a-abc
b-bus
```

## Advanced Kafka Commands

### List the topics in a cluster

```
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --list

```

### Describe topic

- Command to describe all the Kafka topics.

```
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe
```

- Command to describe all the Kafka topics.

```
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe \
--topic test-topic

```

### Alter topic Partitions

```
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 \
--alter --topic test-topic --partitions 40
```

### How to view consumer groups

```
docker exec --interactive --tty kafka1  \
kafka-consumer-groups --bootstrap-server kafka1:19092 --list
```

#### Consumer Groups and their Offset

```
docker exec --interactive --tty kafka1  \
kafka-consumer-groups --bootstrap-server kafka1:19092 \
--describe --group console-consumer-41911
```

## Log file and related config

- Log into the container.

```
docker exec --interactive --tty kafka1
```

- The config file is present in the below path.

```
/etc/kafka/server.properties
```

- The log file is present in the below path.

```
/var/lib/kafka/data/
```

### How to view the commit log?

```
docker exec --interactive --tty kafka1  \
kafka-run-class kafka.tools.DumpLogSegments \
--deep-iteration \
--files /var/lib/kafka/data/test-topic-0/00000000000000000000.log

```
