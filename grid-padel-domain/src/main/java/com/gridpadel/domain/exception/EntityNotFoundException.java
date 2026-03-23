package com.gridpadel.domain.exception;

/**
 * Thrown when a requested entity (tournament, match, pair) is not found.
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
