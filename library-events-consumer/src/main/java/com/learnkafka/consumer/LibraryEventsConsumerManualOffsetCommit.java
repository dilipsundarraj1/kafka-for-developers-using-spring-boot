package com.learnkafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class LibraryEventsConsumerManualOffsetCommit implements AcknowledgingMessageListener<Integer, String> {

    @Override
    @KafkaListener(topics = {"${spring.kafka.topic}"}
            //,groupId = "abc"
    )
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("Consumer Record is : {} " , consumerRecord.toString());
        acknowledgment.acknowledge(); // committing the offset manually
    }

}