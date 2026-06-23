package com.codibly.energymix.service;

/**
 * Thrown when the upstream API does not return enough contiguous forecast periods
 * to cover the requested charging window.
 */
public class InsufficientForecastDataException extends RuntimeException {

    public InsufficientForecastDataException(String message) {
        super(message);
    }
}
