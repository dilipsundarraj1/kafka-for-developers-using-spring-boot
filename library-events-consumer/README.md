# Spring Boot Consumer

## Mandatory Inputs

```
spring:
  profiles: local
  kafka:
    topic: library-events
    consumer:
      bootstrap-servers:
        - localhost:9092,localhost:9093,localhost:9094
      #enable-auto-commit: true
      key-deserializer: org.apache.kafka.common.serialization.IntegerDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      group-id: library-events-listener-group
```

## HeartBeats

-   Set the logging level to **debug**

```aidl
#logging:
#  level:
#    root: debug
```

- Check for this message **Heartbeat request to coordinator** in the logs which will show you the heartbeat information.

## How Polling Works in Spring Kafka?

### KafkaMessageListenerContainer
-   **KafkaMessageListenerContainer** is one of the important class in Spring Kafka.
    -   This is the class that takes care of polling the Kafka to retrieve the records.
-   This class has a **run()** method that takes care of starting the consumer which in turn invokes the **pollAndInvoke**.

```
run()
| 
pollAndInvoke()
|
processCommmits() (Default Commit is BATCH, meaning once the records are successfully processed then it will take care of commitinng the offset)
|
doPoll() (polliTimeOut is 5000 seconds)
```    

-   The **ContainerProperties** class has all the information about ackMode, pollTimeOut
```aidl
  @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        //factory.setConcurrency(4);

        log.info("container properties " + factory.getContainerProperties());
        return factory;
    }
```
    
## @KafkaListener LifeCycle

- The KafkaListener's lifecycle is managed by **KafkaListenerEndpointRegistry** which is a infrastructe bean.

```aidl
@Autowired
KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

//Below code will list the containers it manage.
kafkaListenerEndpointRegistry.getAllListenerContainers().forEach(messageListenerContainer -> {
            log.info(messageListenerContainer.toString());
            log.info("Groups id is {} ",  messageListenerContainer.getGroupId());
});
```
 
- Check this link - https://docs.spring.io/spring-kafka/reference/html/#kafkalistener-lifecycle


## Error Handling

There are two different approaches available to handle this.
-   Using the Bean approach 
-   Using the factory approach.
 
### Using the Bean approach

- Create a bean that extends  **KafkaListenerErrorHandler**. With this approach it takes care of committing the offset too.

```aidl
@Component
@Slf4j
public class LibraryEventErrorHandler implements KafkaListenerErrorHandler {
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.info("Exception is {} and message is {} ", exception.getMessage(), message.getPayload());
        return null;
    }
}
```

### Using the factory approach

-   This does not commit the offset, meaning if you restart the app then it will still process the message.

```
factory.setErrorHandler((thrownException, data) -> {
            log.info("Exception  is {} and the record is \n {} ", thrownException.getMessage(), data);
        });
```

## Retry Kafka Consumer Records

### Simple Retry with back Off

-   The below retryTemplate takes care of retrying any error with
    -   Fixed number of retries
    -   BackOff for every retry

```aidl
 public RetryTemplate retryTemplate() {

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);
        RetryTemplate retryTemplate = new RetryTemplate();
        //retryTemplate.setRetryPolicy(simpleRetryPolicy());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    public SimpleRetryPolicy simpleRetryPolicy() {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(3);
        return simpleRetryPolicy;
    }

```

### Retrying Specific Exceptions with a back off

```aidl
 public RetryTemplate retryTemplate() {

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retrySpecificExpections());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    public SimpleRetryPolicy retrySpecificExpections() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(IllegalStateException.class, false);
        exceptionMap.put(ApplicationException.class, true);
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(5, exceptionMap, true);
        return simpleRetryPolicy;
    }
```
