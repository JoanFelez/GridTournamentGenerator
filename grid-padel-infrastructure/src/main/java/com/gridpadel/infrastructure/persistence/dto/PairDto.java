package com.gridpadel.infrastructure.persistence.dto;

public record PairDto(String id, String player1Name, String player2Name, boolean bye, Integer seed) {}
