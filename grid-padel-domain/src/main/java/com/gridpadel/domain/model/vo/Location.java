package com.gridpadel.domain.model.vo;

public record Location(String value) {

    public Location {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Location cannot be null or blank");
        }
        value = value.trim();
    }

    public static Location of(String value) {
        return new Location(value);
    }

    @Override
    public String toString() {
        return "Location{" + value + "}";
    }
}
