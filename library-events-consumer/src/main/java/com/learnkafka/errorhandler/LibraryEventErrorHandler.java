package com.learnkafka.errorhandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventErrorHandler implements KafkaListenerErrorHandler {
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.info("Exception is {} and message is {} ", exception.getMessage(), message.getPayload());
        return null;
    }

    /*@Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        log.info("Exception is {} and message is {}  and the consumer is {} ", exception.getMessage(), message.getPayload(), consumer);
        return null;
    }*/
}
