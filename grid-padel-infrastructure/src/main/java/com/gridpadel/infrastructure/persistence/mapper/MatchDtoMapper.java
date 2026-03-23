package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.vo.*;
import com.gridpadel.infrastructure.persistence.dto.MatchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MatchDtoMapper {

    static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final MatchResultDtoMapper matchResultDtoMapper;

    public MatchDto toDto(Match match) {
        return new MatchDto(
                match.id().value(),
                match.pair1() != null ? match.pair1().id().value() : null,
                match.pair2() != null ? match.pair2().id().value() : null,
                match.location().map(Location::value).orElse(null),
                match.dateTime().map(dt -> dt.value().format(DT_FORMAT)).orElse(null),
                match.result().map(matchResultDtoMapper::toDto).orElse(null),
                match.roundNumber(),
                match.position(),
                match.bracketType().name()
        );
    }

    public Match fromDto(MatchDto dto, Map<String, Pair> pairLookup) {
        Pair pair1 = dto.pair1Id() != null ? pairLookup.get(dto.pair1Id()) : null;
        Pair pair2 = dto.pair2Id() != null ? pairLookup.get(dto.pair2Id()) : null;

        Location location = dto.location() != null ? Location.of(dto.location()) : null;
        MatchDateTime dateTime = dto.dateTime() != null
                ? MatchDateTime.of(LocalDateTime.parse(dto.dateTime(), DT_FORMAT))
                : null;
        MatchResult result = dto.result() != null ? matchResultDtoMapper.fromDto(dto.result()) : null;

        return Match.restore(
                MatchId.of(dto.id()),
                pair1, pair2,
                location, dateTime, result,
                dto.roundNumber(), dto.position(),
                BracketType.valueOf(dto.bracketType())
        );
    }
}
