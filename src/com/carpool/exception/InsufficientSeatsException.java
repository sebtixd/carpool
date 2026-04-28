package com.carpool.exception;

public class InsufficientSeatsException extends CarpoolException {
    public InsufficientSeatsException(String message) {
        super(message);
    }
}
