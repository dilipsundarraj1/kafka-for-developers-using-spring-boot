package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventStatusEnum;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventsProducer libraryEventsProducer;

    String topic="library-events";

    @Test
    void sendMessage() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        Book book = new Book().builder()
                .bookId(456)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        SettableListenableFuture future = new SettableListenableFuture<>();
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 1),1,
                1, 342, System.currentTimeMillis(),123,123);
        future.set(new SendResult<Integer, String>(new ProducerRecord<Integer, String>(topic, libraryEvent.getLibraryEventId(), libraryEvent.toString()),
                recordMetadata));

        when(objectMapper.writeValueAsString(libraryEvent)).thenCallRealMethod();
        when(kafkaTemplate.send(anyString(),anyInt(),anyString())).thenReturn(future);

        //when
        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventsProducer.sendMessage(libraryEvent,topic);
        SendResult<Integer, String> sendResult = listenableFuture.get();
        //then
        //verify(libraryEventsProducer, times(0)).handleFailure(anyInt(), anyString(), isA(Throwable.class));
        assertEquals(sendResult.getRecordMetadata().toString(), recordMetadata.toString());

    }

    @Test
    void sendMessage_exception() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        Book book = new Book().builder()
                .bookId(456)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        SettableListenableFuture future = new SettableListenableFuture<>();
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 1),1,
                1, 342, System.currentTimeMillis(),123,123);
        future.setException(new RuntimeException("Exception producing to Kafka"));

        when(objectMapper.writeValueAsString(libraryEvent)).thenCallRealMethod();
        when(kafkaTemplate.send(anyString(),anyInt(),anyString())).thenReturn(future);

        //expect
        assertThrows(ExecutionException.class,()->libraryEventsProducer.sendMessage(libraryEvent,topic).get() );

    }
}
