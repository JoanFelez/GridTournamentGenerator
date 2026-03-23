package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.ValidationException;

public record Location(String value) {

    public Location {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Location cannot be blank", "location");
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
