package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Round;
import com.gridpadel.domain.model.vo.BracketType;
import com.gridpadel.infrastructure.persistence.dto.RoundDto;
import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoundDtoMapper {

    private final MatchDtoMapper matchDtoMapper;

    public RoundDto toDto(Round round) {
        return new RoundDto(
                round.roundNumber(),
                round.bracketType().name(),
                round.matches().map(matchDtoMapper::toDto).toJavaList()
        );
    }

    public Round fromDto(RoundDto dto, Map<String, Pair> pairLookup) {
        BracketType type = BracketType.valueOf(dto.bracketType());
        List<Match> matches = List.ofAll(dto.matches())
                .map(m -> matchDtoMapper.fromDto(m, pairLookup));
        return Round.of(dto.roundNumber(), matches, type);
    }
}
