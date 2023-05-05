package com.learnkafka.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.BookRecord;
import com.learnkafka.domain.LibraryEventRecord;
import com.learnkafka.domain.LibraryEventType;

public class TestUtil {

    public static BookRecord bookRecord(){

        return new BookRecord(123, "Dilip","Kafka Using Spring Boot" );
    }

    public static BookRecord bookRecordWithInvalidValues(){

        return new BookRecord(null, "","Kafka Using Spring Boot" );
    }

    public static LibraryEventRecord libraryEventRecord(){

        return
                new LibraryEventRecord(null,
                        LibraryEventType.NEW,
                        bookRecord());
    }

    public static LibraryEventRecord libraryEventRecordUpdate(){

        return
                new LibraryEventRecord(123,
                        LibraryEventType.UPDATE,
                        bookRecord());
    }

    public static LibraryEventRecord libraryEventRecordUpdateWithNullLibraryEventId(){

        return
                new LibraryEventRecord(null,
                        LibraryEventType.UPDATE,
                        bookRecord());
    }

    public static LibraryEventRecord libraryEventRecordWithInvalidBook(){

        return
                new LibraryEventRecord(null,
                        LibraryEventType.NEW,
                        bookRecordWithInvalidValues());
    }

    public static LibraryEventRecord parseLibraryEventRecord(ObjectMapper objectMapper , String json){

        try {
            return  objectMapper.readValue(json, LibraryEventRecord.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
