package com.learnkafka.consumer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events", "library-events.RETRY","library-events.DLT" }, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
        , "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
, "bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class EmbeddedKafkaAppTests {

    @Test
    void testPartitions(@Autowired KafkaAdmin admin) throws InterruptedException, ExecutionException {
        AdminClient client = AdminClient.create(admin.getConfigurationProperties());
        Map<String, TopicDescription> map = client.describeTopics(Collections.singletonList("library-events")).all().get();
        System.out.println(map.values().iterator().next().partitions().size());
    }

}
