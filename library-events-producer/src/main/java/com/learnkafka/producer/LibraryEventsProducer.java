package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventsProducer {

    @Autowired
    private ObjectMapper objectMapper;

    public static String EVENT_SOURCE ="SOURCE";
    public static String SCANNER ="SCANNER";

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    public ListenableFuture<SendResult<Integer, String>> sendMessage(LibraryEvent libraryEvent, String topic) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(libraryEvent);
        Integer key = libraryEvent.getLibraryEventId();
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(topic, key, message);
            listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
                @Override
                public void onFailure(Throwable ex) {
                    handleFailure(key, message, ex);
                }

                @Override
                public void onSuccess(SendResult<Integer, String> result) {
                    log.info("Message Sent SuccessFully with Key : {} and the message is {}", key, message);
                }
            });
        return listenableFuture;
    }

    public ListenableFuture<SendResult<Integer, String>> sendMessageWithHeaders(LibraryEvent libraryEvent, String topic) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(libraryEvent);
        Integer key = libraryEvent.getLibraryEventId();
        ProducerRecord<Integer,String> producerRecord = buildProducerRecord(key, message,topic);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, message, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("Message Sent SuccessFully with Key : {} and the message is {}", key, message);
            }
        });
        return listenableFuture;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String message, String topic) {

        List<Header> recordHeaders = List.of(new RecordHeader(EVENT_SOURCE, SCANNER.getBytes()));

        return new ProducerRecord<Integer, String>(topic, null, key, message, recordHeaders);
        //return new ProducerRecord<Integer, String>(topic, null, key, message);

    }

    public SendResult<Integer, String> sendMessageSynchronous(LibraryEvent libraryEvent, String topic) throws JsonProcessingException, ExecutionException, InterruptedException {
        String message = objectMapper.writeValueAsString(libraryEvent);
        Integer key = libraryEvent.getLibraryEventId();
        SendResult<Integer, String> sendResult = null;
        try {
            sendResult = kafkaTemplate.send(topic, key, message).get();
        } catch (ExecutionException | InterruptedException ex) {
            log.error("ExecutionException/InterruptedException Sending the Message {} and the exception is : {}", message , ex.getMessage());
            throw ex;
        }catch (Exception ex) {
            log.error("Exception/InterruptedException Sending the Message {} and the exception is : {}", message , ex.getMessage());
            throw ex;

        }
        return sendResult;
    }

    public void handleFailure(Integer key, String message, Throwable ex){
        log.error("Error Sending the Message : {} and the exception is : {} ", message , ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure : {}", throwable.getMessage());
        }
    }
}
