package com.learnkafka.jpa;

import com.learnkafka.config.LibraryEventsConsumerConfig;
import com.learnkafka.entity.FailureRecord;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import scala.util.Failure;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class FailureRecordRepositoryTest {

    @Autowired
    FailureRecordRepository failureRecordRepository;

    @BeforeEach
    public void setUp(){
        var record = "{\"libraryEventId\":1,\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot 2.X\",\"bookAuthor\":\"Dilip\"}}";

        var failureRecord = new FailureRecord(null,"library-events", 123, record,1,0L, "exception occurred", LibraryEventsConsumerConfig.RETRY);
        var failureRecord1= new FailureRecord(null,"library-events", 123, record,1,1L, "exception occurred",LibraryEventsConsumerConfig.DEAD);

        failureRecordRepository.saveAll(List.of(failureRecord, failureRecord1));
    }

    @Test
    void findAllByStatus() {

        //when
        var failRecordList = failureRecordRepository.findAllByStatus(LibraryEventsConsumerConfig.RETRY);

        //then
        assertEquals(1, failRecordList.size());
    }
}