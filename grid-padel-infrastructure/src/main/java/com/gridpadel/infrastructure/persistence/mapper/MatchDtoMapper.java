package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.vo.*;
import com.gridpadel.infrastructure.persistence.dto.MatchDto;
import io.vavr.control.Option;
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
                match.location().map(Location::value).getOrNull(),
                match.dateTime().map(dt -> dt.value().format(DT_FORMAT)).getOrNull(),
                match.result().map(matchResultDtoMapper::toDto).getOrNull(),
                match.roundNumber(),
                match.position(),
                match.bracketType().name()
        );
    }

    public Match fromDto(MatchDto dto, Map<String, Pair> pairLookup) {
        Pair pair1 = Option.of(dto.pair1Id()).map(pairLookup::get).getOrNull();
        Pair pair2 = Option.of(dto.pair2Id()).map(pairLookup::get).getOrNull();

        Location location = Option.of(dto.location()).map(Location::of).getOrNull();
        MatchDateTime dateTime = Option.of(dto.dateTime())
                .map(dt -> MatchDateTime.of(LocalDateTime.parse(dt, DT_FORMAT)))
                .getOrNull();
        MatchResult result = Option.of(dto.result())
                .map(matchResultDtoMapper::fromDto)
                .getOrNull();

        return Match.restore(
                MatchId.of(dto.id()),
                pair1, pair2,
                location, dateTime, result,
                dto.roundNumber(), dto.position(),
                BracketType.valueOf(dto.bracketType())
        );
    }
}
