package com.learnkafka.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.config.LibraryEventsConsumerConfig;
import com.learnkafka.consumer.LibraryEventsConsumer;
import com.learnkafka.entity.FailureRecord;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.jpa.FailureRecordRepository;
import com.learnkafka.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
public class RetryScheduler {

    @Autowired
    LibraryEventsService libraryEventsService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    FailureRecordRepository failureRecordRepository;

    @Autowired
    LibraryEventsConsumer libraryEventsConsumer;

    @Scheduled(fixedRate = 60000 )
    public void retryFailedRecords(){

        log.info("Retrying Failed Records Started!");
        var status = LibraryEventsConsumerConfig.RETRY;
        failureRecordRepository.findAllByStatus(status)
                .forEach(failureRecord -> {
                    try {
                        var libraryEvent = objectMapper.readValue(failureRecord.getErrorRecord(), LibraryEvent.class);
                        //libraryEventsService.processLibraryEvent();
                        var consumerRecord = buildConsumerRecord(failureRecord);
                        libraryEventsService.processLibraryEvent(consumerRecord);
                       // libraryEventsConsumer.onMessage(consumerRecord); // This does not involve the recovery code for in the consumerConfig
                        failureRecord.setStatus(LibraryEventsConsumerConfig.SUCCESS);
                    } catch (JsonProcessingException e) {
                        log.error("JsonProcessingException in retryFailedRecords : ", e);
                        failureRecord.setStatus(LibraryEventsConsumerConfig.DEAD);
                    }catch (Exception e){
                        log.error("Exception in retryFailedRecords : ", e);
                    }

                });

    }

    private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {

        return new ConsumerRecord<>(failureRecord.getTopic(),
                failureRecord.getPartition(), failureRecord.getOffset_value(), failureRecord.getKey(),
                failureRecord.getErrorRecord());

    }
}
