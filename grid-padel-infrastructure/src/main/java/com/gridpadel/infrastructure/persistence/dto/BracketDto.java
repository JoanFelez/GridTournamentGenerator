package com.gridpadel.infrastructure.persistence.dto;

import java.util.List;

public record BracketDto(String type, List<RoundDto> rounds) {}
