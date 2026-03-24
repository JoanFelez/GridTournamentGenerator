package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.Bracket;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.vo.BracketType;
import com.gridpadel.infrastructure.persistence.dto.BracketDto;
import io.vavr.collection.List;
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
                bracket.rounds().map(roundDtoMapper::toDto).toJavaList()
        );
    }

    public Bracket fromDto(BracketDto dto, Map<String, Pair> pairLookup) {
        BracketType type = BracketType.valueOf(dto.type());
        Bracket bracket = Bracket.create(type);
        List.ofAll(dto.rounds()).forEach(roundDto ->
                bracket.addRound(roundDtoMapper.fromDto(roundDto, pairLookup)));
        return bracket;
    }
}
