package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.vo.MatchResult;
import com.gridpadel.infrastructure.persistence.dto.MatchResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchResultDtoMapper {

    private final SetResultDtoMapper setResultDtoMapper;

    public MatchResultDto toDto(MatchResult result) {
        return new MatchResultDto(
                result.sets().stream().map(setResultDtoMapper::toDto).toList()
        );
    }

    public MatchResult fromDto(MatchResultDto dto) {
        return MatchResult.of(
                dto.sets().stream().map(setResultDtoMapper::fromDto).toList()
        );
    }
}
