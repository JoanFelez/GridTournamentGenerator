package com.gridpadel.domain.model.vo;

import java.util.UUID;

public record PairId(String value) {

    public PairId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PairId cannot be null or blank");
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
