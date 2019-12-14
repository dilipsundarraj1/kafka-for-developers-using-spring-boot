package com.learnkafka.config;

import com.learnkafka.exception.RecoverableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);

        //factory.setConcurrency(4);
        //factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setErrorHandler((thrownException, data) -> {
            log.info("Exception  is {} and the record is \n {} ", thrownException.getMessage(), data);
        });
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryCallback((context) -> {
            Arrays.asList(context.attributeNames())
                    .forEach(attributeName -> {
                        log.info("Attribute  is : {}", attributeName);
                        log.info("Attribute  Value  is : {}", context.getAttribute(attributeName));
                    });

            /*log.info("Record inside recovery : {} ", record.value());
            log.info("Throwable is {} ",  context.getLastThrowable().toString());
            log.info("Throwable cause is {} ",  context.getLastThrowable().getCause().toString());*/
            if (context.getLastThrowable().getCause() instanceof IllegalStateException) {
                log.info("No Need to Recover as this is an irrecoverable exception");
            } else {
                ConsumerRecord<Integer, String> record = (ConsumerRecord<Integer, String>) context.getAttribute("record");
                log.info("Recover as this is a recoverrable exception");

            }

            return null;
        });
        log.info("container properties " + factory.getContainerProperties());
        return factory;
    }

    public RetryTemplate retryTemplate() {

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);
        RetryTemplate retryTemplate = new RetryTemplate();
        //retryTemplate.setRetryPolicy(simpleRetryPolicy());
        retryTemplate.setRetryPolicy(retrySpecificExpections());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    public SimpleRetryPolicy simpleRetryPolicy() {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(3);
        return simpleRetryPolicy;
    }

    public SimpleRetryPolicy retrySpecificExpections() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(IllegalStateException.class, false);
        exceptionMap.put(RecoverableException.class, true);
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(5, exceptionMap, true);
        return simpleRetryPolicy;
    }
}
