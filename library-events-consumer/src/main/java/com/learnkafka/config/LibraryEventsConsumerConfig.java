package com.learnkafka.config;

import com.learnkafka.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.*;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    @Autowired
    LibraryEventsService libraryEventsService;

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    KafkaTemplate kafkaTemplate;


    public DeadLetterPublishingRecoverer publishingRecoverer(){

                DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate
//                        ,(r, e) -> {
//                    if (e instanceof IllegalStateException) {
//                        return new TopicPartition(r.topic() + ".Foo.failures", r.partition());
//                    }
//                    else {
//                        return new TopicPartition(r.topic() + ".other.failures", r.partition());
//                    }
//                }
                );

        return recoverer;

    }

    ConsumerRecordRecoverer consumerRecordRecoverer = (record, exception) -> {
        log.error("Exception is : {} Failed Record : {} ", exception, record);
        if (exception.getCause() instanceof RecoverableDataAccessException) {
            log.info("Inside the recoverable logic");
            //Add any Recovery Code here.
        } else {
            log.info("Inside the non recoverable logic and skipping the record : {}", record);

        }
    };

    public DefaultErrorHandler errorHandler() {

        var exceptiopnToIgnorelist = List.of(
                IllegalArgumentException.class
        );

        /**
         * Just the Custom Error Handler
         */
//        var defaultErrorHandler =  new DefaultErrorHandler((record, exception) -> {
//            log.error("Exception is : {} Failed Record : {} ", exception, record);
//            if (exception.getCause() instanceof RecoverableDataAccessException) {
//                log.info("Inside the recoverable logic");
//                // libraryEventsService.handleRecovery((ConsumerRecord<Integer, String>) record);
//
//            } else {
//                log.info("Inside the non recoverable logic and skipping the record : {}", record);
//
//            }
//        });

        /**
         * Error Handler with the BackOff, Exceptions to Ignore, RetryListener
         */
        var defaultErrorHandler = new DefaultErrorHandler(
                //consumerRecordRecoverer
                publishingRecoverer()
                ,new FixedBackOff(1000L, 2L));

           // exceptiopnToIgnorelist.forEach(defaultErrorHandler::addNotRetryableExceptions);

            defaultErrorHandler.setRetryListeners(
                    (record, ex, deliveryAttempt) ->
                            log.info("Failed Record in Retry Listener  exception : {} , deliveryAttempt : {} ", ex.getMessage(), deliveryAttempt)
            );

            return defaultErrorHandler;
        }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory
                .getIfAvailable(() -> new DefaultKafkaConsumerFactory<>(this.kafkaProperties.buildConsumerProperties())));
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        //factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//        factory.setErrorHandler(((thrownException, data) -> {
//            log.info("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), data);
//            //persist
//        }));
//        factory.setRetryTemplate(retryTemplate());
//        factory.setRecoveryCallback((context -> {
//            if(context.getLastThrowable().getCause() instanceof RecoverableDataAccessException){
//                //invoke recovery logic
//                log.info("Inside the recoverable logic");
//                Arrays.asList(context.attributeNames())
//                        .forEach(attributeName -> {
//                    log.info("Attribute name is : {} ", attributeName);
//                    log.info("Attribute Value is : {} ", context.getAttribute(attributeName));
//                });
//
//                ConsumerRecord<Integer, String> consumerRecord = (ConsumerRecord<Integer, String>) context.getAttribute("record");
//                libraryEventsService.handleRecovery(consumerRecord);
//            }else{
//                log.info("Inside the non recoverable logic");
//                throw new RuntimeException(context.getLastThrowable().getMessage());
//            }
//
//
//            return null;
//        }));
        return factory;
    }

   /* @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
       // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setErrorHandler(((thrownException, data) -> {
            log.info("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), data);
            //persist
        }));
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryCallback((context -> {
            if(context.getLastThrowable().getCause() instanceof RecoverableDataAccessException){
                //invoke recovery logic
                log.info("Inside the recoverable logic");
               Arrays.asList(context.attributeNames())
                        .forEach(attributeName -> {
                            log.info("Attribute name is : {} ", attributeName);
                            log.info("Attribute Value is : {} ", context.getAttribute(attributeName));
                        });

               ConsumerRecord<Integer, String> consumerRecord = (ConsumerRecord<Integer, String>) context.getAttribute("record");
                libraryEventsService.handleRecovery(consumerRecord);
            }else{
                log.info("Inside the non recoverable logic");
                throw new RuntimeException(context.getLastThrowable().getMessage());
            }


            return null;
        }));
        return factory;
    }*/

//    private RetryTemplate retryTemplate() {
//
//        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
//        fixedBackOffPolicy.setBackOffPeriod(1000);
//        RetryTemplate retryTemplate = new RetryTemplate();
//        retryTemplate.setRetryPolicy(simpleRetryPolicy());
//        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
//        return retryTemplate;
//    }
//
//    private RetryPolicy simpleRetryPolicy() {
//
//        /*SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
//        simpleRetryPolicy.setMaxAttempts(3);*/
//        Map<Class<? extends Throwable>, Boolean> exceptionsMap = new HashMap<>();
//        exceptionsMap.put(IllegalArgumentException.class, false);
//        exceptionsMap.put(RecoverableDataAccessException.class, true);
//        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3, exceptionsMap, true);
//        return simpleRetryPolicy;
//    }
}