package com.learnkafka.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventStatusEnum;
import com.learnkafka.producer.LibraryEventsProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
public class LibraryEventController {

    @Autowired
    LibraryEventsProducer libraryEventsProducer;

    @Value("${spring.kafka.topic}")
    private String topic;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        libraryEvent.setEventStatus(LibraryEventStatusEnum.ADD);
        libraryEventsProducer.sendMessage(libraryEvent,topic);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> putLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {
        libraryEvent.setEventStatus(LibraryEventStatusEnum.MODIFY);
        libraryEventsProducer.sendMessage(libraryEvent,topic);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

}
