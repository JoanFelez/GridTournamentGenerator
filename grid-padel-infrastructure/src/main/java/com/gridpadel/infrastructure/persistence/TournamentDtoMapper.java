package com.gridpadel.infrastructure.persistence;

import com.gridpadel.domain.model.Bracket;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.TournamentId;
import com.gridpadel.infrastructure.persistence.dto.*;
import com.gridpadel.infrastructure.persistence.mapper.BracketDtoMapper;
import com.gridpadel.infrastructure.persistence.mapper.PairDtoMapper;
import io.vavr.collection.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TournamentDtoMapper {

    static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final PairDtoMapper pairDtoMapper;
    private final BracketDtoMapper bracketDtoMapper;

    public TournamentDto toDto(Tournament tournament) {
        return new TournamentDto(
                tournament.id().value(),
                tournament.name(),
                tournament.pairs().map(pairDtoMapper::toDto).toJavaList(),
                bracketDtoMapper.toDto(tournament.mainBracket()),
                bracketDtoMapper.toDto(tournament.consolationBracket()),
                tournament.createdAt().format(DT_FORMAT),
                tournament.updatedAt().format(DT_FORMAT)
        );
    }

    public Tournament fromDto(TournamentDto dto) {
        java.util.List<Pair> pairs = List.ofAll(dto.pairs()).map(pairDtoMapper::fromDto).toJavaList();
        Map<String, Pair> pairLookup = buildPairLookup(pairs);

        collectBracketPairs(dto.mainBracket(), pairLookup);
        collectBracketPairs(dto.consolationBracket(), pairLookup);

        Bracket mainBracket = bracketDtoMapper.fromDto(dto.mainBracket(), pairLookup);
        Bracket consolationBracket = bracketDtoMapper.fromDto(dto.consolationBracket(), pairLookup);

        return Tournament.restore(
                TournamentId.of(dto.id()),
                dto.name(),
                pairs,
                mainBracket,
                consolationBracket,
                LocalDateTime.parse(dto.createdAt(), DT_FORMAT),
                LocalDateTime.parse(dto.updatedAt(), DT_FORMAT)
        );
    }

    Map<String, Pair> buildPairLookup(java.util.List<Pair> pairs) {
        java.util.Map<String, Pair> lookup = new java.util.HashMap<>();
        List.ofAll(pairs).forEach(p -> lookup.put(p.id().value(), p));
        return lookup;
    }

    void collectBracketPairs(BracketDto bracketDto, Map<String, Pair> pairLookup) {
        List.ofAll(bracketDto.rounds())
                .flatMap(round -> List.ofAll(round.matches()))
                .forEach(match -> {
                    if (match.pair1Id() != null && !pairLookup.containsKey(match.pair1Id())) {
                        Pair bye = Pair.restore(PairId.of(match.pair1Id()), null, null, true, null);
                        pairLookup.put(match.pair1Id(), bye);
                    }
                    if (match.pair2Id() != null && !pairLookup.containsKey(match.pair2Id())) {
                        Pair bye = Pair.restore(PairId.of(match.pair2Id()), null, null, true, null);
                        pairLookup.put(match.pair2Id(), bye);
                    }
                });
    }
}
