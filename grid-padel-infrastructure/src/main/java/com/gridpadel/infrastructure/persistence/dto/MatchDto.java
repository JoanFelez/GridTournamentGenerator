package com.gridpadel.infrastructure.persistence.dto;


public record MatchDto(
        String id,
        String pair1Id,
        String pair2Id,
        String location,
        String dateTime,
        MatchResultDto result,
        int roundNumber,
        int position,
        String bracketType
) {}
