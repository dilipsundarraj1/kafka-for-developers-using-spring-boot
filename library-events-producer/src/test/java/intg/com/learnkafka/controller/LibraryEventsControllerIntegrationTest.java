package com.learnkafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEventRecord;
import com.learnkafka.util.TestUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(5)
    void postLibraryEvent() throws InterruptedException {
        //given
        LibraryEventRecord libraryEventRecord = TestUtil.libraryEventRecord();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEventRecord> request = new HttpEntity<>(libraryEventRecord, headers);

        //when
        ResponseEntity<LibraryEventRecord> responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, request, LibraryEventRecord.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());


        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
        //Thread.sleep(3000);
        assert consumerRecords.count() == 1;
        consumerRecords.forEach(record -> {
            var libraryEventActual = TestUtil.parseLibraryEventRecord(objectMapper, record.value());
            assertEquals(libraryEventRecord, libraryEventActual);

        });


    }

    @Test
    @Timeout(5)
    void putLibraryEvent() throws InterruptedException {
        //given
        var libraryEventUpdate = TestUtil.libraryEventRecordUpdate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEventRecord> request = new HttpEntity<>(libraryEventUpdate, headers);


        //when
        ResponseEntity<LibraryEventRecord> responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.PUT, request, LibraryEventRecord.class);

        //then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());


        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
        //Thread.sleep(3000);
        assert consumerRecords.count() == 2;
        consumerRecords.forEach(record -> {
            if (record.key() != null) {
                var libraryEventActual = TestUtil.parseLibraryEventRecord(objectMapper, record.value());
                assertEquals(libraryEventUpdate, libraryEventActual);
            }
        });


    }
}