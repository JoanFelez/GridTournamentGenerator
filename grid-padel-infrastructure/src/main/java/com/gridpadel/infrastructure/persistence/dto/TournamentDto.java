package com.gridpadel.infrastructure.persistence.dto;

import java.util.List;

public record TournamentDto(
        String id,
        String name,
        List<PairDto> pairs,
        BracketDto mainBracket,
        BracketDto consolationBracket,
        String createdAt,
        String updatedAt
) {}
