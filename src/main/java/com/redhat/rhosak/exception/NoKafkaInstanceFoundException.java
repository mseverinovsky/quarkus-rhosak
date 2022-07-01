package com.redhat.rhosak.exception;

public class NoKafkaInstanceFoundException extends Exception {

    public NoKafkaInstanceFoundException(String message) {
        super(message);
    }
}
