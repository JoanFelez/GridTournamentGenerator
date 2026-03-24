package com.gridpadel.infrastructure.persistence.dto;

import java.util.List;

public record MatchResultDto(List<SetResultDto> sets, boolean walkover, int walkoverPosition) {}
