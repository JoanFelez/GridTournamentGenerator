package com.gridpadel.infrastructure.persistence.dto;

import java.util.List;

public record RoundDto(int roundNumber, String bracketType, List<MatchDto> matches) {}
