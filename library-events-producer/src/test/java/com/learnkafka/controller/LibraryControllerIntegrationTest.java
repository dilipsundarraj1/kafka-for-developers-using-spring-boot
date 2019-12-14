package com.learnkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

import static com.learnkafka.producer.LibraryEventsProducer.EVENT_SOURCE;
import static com.learnkafka.producer.LibraryEventsProducer.SCANNER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(ports = 9098, topics = {"library-events"}, brokerProperties = {"auto.create.topics.enable=false"})
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=localhost:9098"})
@AutoConfigureWebClient
public class LibraryControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;


    private Consumer<Integer, String> consumer;

    @BeforeEach
    public void setUp(){
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("controller-group1", "false", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    public void tearDown(){
        consumer.close();
    }

    @Test
    @Timeout(5)
    void postLibraryEvent() throws JsonProcessingException, InterruptedException {

        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);


        //when
        webTestClient
                .post().uri("/v1/libraryevent")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isCreated();

        //Thread.sleep(3000);
        String expectedJson = "{\"libraryEventId\":123,\"eventStatus\":\"ADD\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        System.out.println("record : " + record);
        assertEquals(123, record.key());
        assertEquals(expectedJson, record.value());

    }

    @Test
    @Timeout(5)
    void putLibraryEvent() throws JsonProcessingException, InterruptedException {

        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);

        //when
        webTestClient
                .put().uri("/v1/libraryevent")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isOk();

        //Thread.sleep(3000);
        String expectedJson = "{\"libraryEventId\":123,\"eventStatus\":\"MODIFY\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        System.out.println("record : " + record);
        assertEquals(123, record.key());
        assertEquals(expectedJson, record.value());

    }
}
