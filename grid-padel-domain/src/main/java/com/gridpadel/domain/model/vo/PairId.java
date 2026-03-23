package com.gridpadel.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class PairId {

    private final String value;

    private PairId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PairId cannot be null or blank");
        }
        this.value = value;
    }

    public static PairId generate() {
        return new PairId(UUID.randomUUID().toString());
    }

    public static PairId of(String value) {
        return new PairId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PairId other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "PairId{" + value + "}";
    }
}
