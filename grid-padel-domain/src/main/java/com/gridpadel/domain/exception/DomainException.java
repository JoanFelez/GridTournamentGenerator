package com.gridpadel.domain.exception;

/**
 * Base exception for all domain-level errors.
 * UI layer should catch this to display informative messages without crashing.
 */
public abstract class DomainException extends RuntimeException {

    private final String field;

    protected DomainException(String message, String field) {
        super(message);
        this.field = field;
    }

    protected DomainException(String message) {
        super(message);
        this.field = null;
    }

    public String field() {
        return field;
    }
}
