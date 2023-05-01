# Understanding Kafka Docker Compose works

- More info is available in this link - https://rmoff.net/2018/08/02/kafka-listeners-explained/

## Kafka broker docker-compose config:

```
kafka1:
  image: confluentinc/cp-kafka:7.3.2
  hostname: kafka1
  container_name: kafka1
  ports:
    - "9092:9092"
    - "29092:29092"
  environment:
    KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka1:19092,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092,DOCKER://host.docker.internal:29092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,DOCKER:PLAINTEXT
    KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
    KAFKA_ZOOKEEPER_CONNECT: "zoo1:2181"
    KAFKA_BROKER_ID: 1
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
  depends_on:
    - zoo1
```
- KAFKA_INTER_BROKER_LISTENER_NAME
  - Kafka brokers communicate between themselves, usually on the internal network. This is where the **KAFKA_INTER_BROKER_LISTENER_NAME** property comes in handy.
- KAFKA_ADVERTISED_LISTENERS
  - The config that's present in this property is the data that's shared to the clients when they are connected.
  - Kafka clients may not be in the network where the kafka broker is running.
    - For a broker that's running in the docker network, the client is very much likely possible outside the docker network.
