package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEventRecord;
import com.learnkafka.util.TestUtil;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventProducer eventProducer;

    @Test
    void sendLibraryEvent_Approach2_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(CompletableFuture.supplyAsync(()-> new RuntimeException("Exception Calling Kafka")));
        //when

        assertThrows(Exception.class, ()->eventProducer.sendLibraryEvent_Approach2(TestUtil.libraryEventRecord()).get());

    }

    @Test
    void sendLibraryEvent_Approach2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        LibraryEventRecord libraryEventRecord = TestUtil.libraryEventRecord();
        String record = objectMapper.writeValueAsString(libraryEventRecord);


        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("library-events", libraryEventRecord.libraryEventId(),record );
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1),
                1,1,System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<Integer, String>(producerRecord,recordMetadata);

        var future = CompletableFuture.supplyAsync(()-> sendResult);
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        //when

        var completableFuture = eventProducer.sendLibraryEvent_Approach2(libraryEventRecord);

        //then
        SendResult<Integer,String> sendResult1 = completableFuture.get();
        assert sendResult1.getRecordMetadata().partition()==1;

    }
}
