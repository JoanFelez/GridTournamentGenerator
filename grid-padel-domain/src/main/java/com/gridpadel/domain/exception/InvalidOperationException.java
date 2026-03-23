package com.gridpadel.domain.exception;

/**
 * Thrown when a domain operation is invalid given the current state.
 * E.g., generating a bracket when one already exists, recording result on incomplete match.
 */
public class InvalidOperationException extends DomainException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, String field) {
        super(message, field);
    }
}
