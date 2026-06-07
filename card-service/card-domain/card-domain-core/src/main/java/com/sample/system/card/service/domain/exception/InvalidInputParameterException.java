package com.sample.system.card.service.domain.exception;

/**
 * Exception thrown when input parameters are invalid
 * Following DDD patterns for domain exceptions
 * This is a RuntimeException (unchecked exception)
 */
public class InvalidInputParameterException extends RuntimeException {

    public InvalidInputParameterException(String message) {
        super(message);
    }

    public InvalidInputParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputParameterException(String parameterName, String parameterValue, String reason) {
        super(String.format("Invalid input parameter '%s' with value '%s': %s", parameterName, parameterValue, reason));
    }
}
