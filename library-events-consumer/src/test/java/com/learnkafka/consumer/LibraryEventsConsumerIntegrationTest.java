package com.learnkafka.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.Book;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.entity.LibraryEventStatusEnum;
import com.learnkafka.jpa.LibraryEventsRepository;
import com.learnkafka.service.LibraryEventsConsumerService;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import javax.validation.Valid;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(ports = 9097, topics = {"library-events"}, brokerProperties = {"auto.create.topics.enable=false"})
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=localhost:9097",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @SpyBean
    LibraryEventsConsumerService libraryEventsConsumerService;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumer;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @Value("${spring.kafka.topic}")
    String topic;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer container : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void publishNewLibraryEvent() throws JsonProcessingException, InterruptedException, ExecutionException {
        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, libraryEvent.getLibraryEventId(), json).get();
        System.out.println("sendResult : " + sendResult);


        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        LibraryEvent peristedLibraryEvent = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();
        assertNotNull(peristedLibraryEvent.getBook());
        assertEquals("Dilip" , peristedLibraryEvent.getBook().getBookAuthor());
    }

    @Test
    void publishModifyLibraryEvent() throws JsonProcessingException, InterruptedException, ExecutionException {
        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.MODIFY)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, libraryEvent.getLibraryEventId(), json).get();
        System.out.println("sendResult : " + sendResult);


        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        LibraryEvent peristedLibraryEvent = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();
        assertNotNull(peristedLibraryEvent.getBook());
        assertEquals("Dilip" , peristedLibraryEvent.getBook().getBookAuthor());
    }

    @Test
    void publishModifyLibraryEvent_IrrecoverableError() throws JsonProcessingException, InterruptedException, ExecutionException {
        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(0)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, libraryEvent.getLibraryEventId(), json).get();
        System.out.println("sendResult : " + sendResult);


        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);


        verify(libraryEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));

        //LibraryEvent peristedLibraryEvent = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();

    }

    @Test
    void publishModifyLibraryEvent_RecoverableError() throws JsonProcessingException, InterruptedException, ExecutionException {
        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(100)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, libraryEvent.getLibraryEventId(), json).get();
        System.out.println("sendResult : " + sendResult);


        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(6, TimeUnit.SECONDS);


        verify(libraryEventsConsumer, times(5)).onMessage(isA(ConsumerRecord.class));


    }

}
