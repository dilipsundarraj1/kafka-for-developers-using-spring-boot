package com.learnkafka.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.config.LibraryEventsConsumerConfig;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.jpa.FailureRecordRepository;
import com.learnkafka.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryScheduler {

    @Autowired
    LibraryEventsService libraryEventsService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    FailureRecordRepository failureRecordRepository;

    @Scheduled(fixedRate = 5000 )
    public void retryFailedRecords(){

        var status = LibraryEventsConsumerConfig.RETRY;
        failureRecordRepository.findAllByStatus(status)
                .forEach(failureRecord -> {
                    try {
                        var libraryEvent = objectMapper.readValue(failureRecord.getErrorRecord(), LibraryEvent.class);
                        //libraryEventsService.processLibraryEvent();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                });

    }
}
