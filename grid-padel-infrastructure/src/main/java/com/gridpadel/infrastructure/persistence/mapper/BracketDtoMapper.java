package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.Bracket;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.vo.BracketType;
import com.gridpadel.infrastructure.persistence.dto.BracketDto;
import com.gridpadel.infrastructure.persistence.dto.RoundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BracketDtoMapper {

    private final RoundDtoMapper roundDtoMapper;

    public BracketDto toDto(Bracket bracket) {
        return new BracketDto(
                bracket.type().name(),
                bracket.rounds().stream().map(roundDtoMapper::toDto).toList()
        );
    }

    public Bracket fromDto(BracketDto dto, Map<String, Pair> pairLookup) {
        BracketType type = BracketType.valueOf(dto.type());
        Bracket bracket = Bracket.create(type);
        for (RoundDto roundDto : dto.rounds()) {
            bracket.addRound(roundDtoMapper.fromDto(roundDto, pairLookup));
        }
        return bracket;
    }
}
