package com.gridpadel.domain.model.vo;

import java.util.Objects;

public final class Location {

    private final String value;

    private Location(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Location cannot be null or blank");
        }
        this.value = value.trim();
    }

    public static Location of(String value) {
        return new Location(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Location{" + value + "}";
    }
}
