package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.vo.MatchResult;
import com.gridpadel.domain.model.vo.SetResult;
import com.gridpadel.infrastructure.persistence.dto.MatchResultDto;
import com.gridpadel.infrastructure.persistence.dto.SetResultDto;
import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchResultDtoMapper {

    private final SetResultDtoMapper setResultDtoMapper;

    public MatchResultDto toDto(MatchResult result) {
        java.util.List<SetResultDto> sets = result.sets()
                .map(setResultDtoMapper::toDto)
                .toJavaList();
        return new MatchResultDto(sets);
    }

    public MatchResult fromDto(MatchResultDto dto) {
        List<SetResult> sets = List.ofAll(dto.sets())
                .map(setResultDtoMapper::fromDto);
        return new MatchResult(sets);
    }
}
