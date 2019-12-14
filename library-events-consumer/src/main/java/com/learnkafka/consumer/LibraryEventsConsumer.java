package com.learnkafka.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.service.LibraryEventsConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LibraryEventsConsumer {

    @Autowired
    LibraryEventsConsumerService libraryEventsConsumerService;

    @KafkaListener(topics = {"${spring.kafka.topic}"}
   // , errorHandler = "libraryEventErrorHandler"
    )
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("OnMessage Record : {} ", consumerRecord);
        libraryEventsConsumerService.processLibraryEvent(consumerRecord);
    }
}
