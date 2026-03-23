package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.ValidationException;

import java.util.UUID;

public record PairId(String value) {

    public PairId {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Pair ID cannot be blank", "pairId");
        }
    }

    public static PairId generate() {
        return new PairId(UUID.randomUUID().toString());
    }

    public static PairId of(String value) {
        return new PairId(value);
    }

    @Override
    public String toString() {
        return "PairId{" + value + "}";
    }
}
