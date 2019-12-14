package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventStatusEnum;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.concurrent.ListenableFuture;

import javax.validation.valueextraction.UnwrapByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.learnkafka.producer.LibraryEventsProducer.EVENT_SOURCE;
import static com.learnkafka.producer.LibraryEventsProducer.SCANNER;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(ports = 9099, topics = {"library-events"}, brokerProperties = {"auto.create.topics.enable=false", "log.dir=out/embedded-kafka"} )
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsProducerEmbeddedKafkaTest {

    @Autowired
    LibraryEventsProducer libraryEventsProducer;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${spring.kafka.topic}")
    private String topic;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp(){
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "false", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    public void tearDown(){
        consumer.close();
    }

    @Test
    void sendMessageWithKey() throws JsonProcessingException, InterruptedException, ExecutionException {

        //given
        Book book = new Book().builder()
                .bookId(456)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        //when
        ListenableFuture<SendResult<Integer, String>> listenableFuture =libraryEventsProducer.sendMessage(libraryEvent, topic);
        SendResult<Integer, String> sendResult =  listenableFuture.get();

        //then
        assertNotNull(sendResult.getRecordMetadata().offset());
    }

    @Test
    void sendMessageWithNullKey() throws JsonProcessingException, InterruptedException, ExecutionException {

        //given
        Book book = new Book().builder()
                .bookId(null)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        //when
        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventsProducer.sendMessage(libraryEvent, topic);
        SendResult<Integer, String> sendResult =  listenableFuture.get();

        //then
        assertNotNull(sendResult.getRecordMetadata().offset());

    }


    @Test
    void sendMessageWithKeySynchronous() throws JsonProcessingException, InterruptedException, ExecutionException {

        //given
        Book book = new Book().builder()
                .bookId(456)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        //when
        SendResult<Integer, String> sendResult = libraryEventsProducer.sendMessageSynchronous(libraryEvent, topic);

        //then
        assertNotNull(sendResult.getRecordMetadata().offset());
    }

    @Test
    void sendMessageWithNullKeySynchronous() throws JsonProcessingException, InterruptedException, ExecutionException {

        //given
        Book book = new Book().builder()
                .bookId(null)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        //when
        SendResult<Integer, String> sendResult = libraryEventsProducer.sendMessageSynchronous(libraryEvent, topic);

        //then
        assertNotNull(sendResult.getRecordMetadata().offset());
    }



    @Test
    void sendMessageWithKafkaHeaders() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        Book book = new Book().builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        //when
        SendResult<Integer, String> sendResult = libraryEventsProducer.sendMessageWithHeaders(libraryEvent, topic).get();

        //then
        assertNotNull(sendResult.getRecordMetadata().offset());
        ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        assertEquals(123, record.key());
        record.headers().forEach((recordHeader)->{
            System.out.println("recordHeader key : "+recordHeader.key() + " , value : "+ new String(recordHeader.value()));
            assertEquals(recordHeader.key(), EVENT_SOURCE);
            assertEquals(SCANNER, new String(recordHeader.value()));
        });

    }

    @Test
    void sendMessageWithErrorTopic() {

        //given

        Book book = new Book().builder()
                .bookId(null)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        //then
        assertThrows(ExecutionException.class,()-> libraryEventsProducer.sendMessage(libraryEvent, "sample").get());
    }

    @Test
    void sendMessageWithErrorTopicSynchronous() {
        //given

        Book book = new Book().builder()
                .bookId(null)
                .bookAuthor("Dilip")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .eventStatus(LibraryEventStatusEnum.ADD)
                .book(book)
                .build();

        //then
        assertThrows(ExecutionException.class,()-> libraryEventsProducer.sendMessageSynchronous(libraryEvent, "sample"));
    }

}
