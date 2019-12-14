package com.learnkafka.exception;

public class RecoverableException extends RuntimeException {

    public RecoverableException(String message) {
        super(message);
    }
}
