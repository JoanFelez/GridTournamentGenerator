package com.gridpadel.domain.exception;

/**
 * Thrown when user-provided input fails domain validation.
 * The message is safe to display directly to the user.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message, String field) {
        super(message, field);
    }

    public ValidationException(String message) {
        super(message);
    }
}
