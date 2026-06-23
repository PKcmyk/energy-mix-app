package com.codibly.energymix.service;

public class InsufficientForecastDataException extends RuntimeException {

    public InsufficientForecastDataException(String message) {
        super(message);
    }
}
