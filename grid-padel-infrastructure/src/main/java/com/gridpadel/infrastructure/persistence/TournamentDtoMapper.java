package com.gridpadel.infrastructure.persistence;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.*;
import com.gridpadel.infrastructure.persistence.dto.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TournamentDtoMapper {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TournamentDto toDto(Tournament tournament) {
        Map<PairId, String> pairIdMap = buildPairIdMap(tournament);

        return new TournamentDto(
                tournament.id().value(),
                tournament.name(),
                tournament.pairs().stream().map(this::toPairDto).toList(),
                toBracketDto(tournament.mainBracket(), pairIdMap),
                toBracketDto(tournament.consolationBracket(), pairIdMap),
                tournament.createdAt().format(DT_FORMAT),
                tournament.updatedAt().format(DT_FORMAT)
        );
    }

    public Tournament fromDto(TournamentDto dto) {
        List<Pair> pairs = dto.pairs().stream().map(this::fromPairDto).toList();
        Map<String, Pair> pairLookup = buildPairLookup(pairs);

        collectBracketPairs(dto.mainBracket(), pairLookup);
        collectBracketPairs(dto.consolationBracket(), pairLookup);

        Bracket mainBracket = fromBracketDto(dto.mainBracket(), pairLookup);
        Bracket consolationBracket = fromBracketDto(dto.consolationBracket(), pairLookup);

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

    private PairDto toPairDto(Pair pair) {
        if (pair.isBye()) {
            return new PairDto(pair.id().value(), null, null, true, null);
        }
        return new PairDto(
                pair.id().value(),
                pair.player1Name().value(),
                pair.player2Name().value(),
                false,
                pair.seed().orElse(null)
        );
    }

    private Pair fromPairDto(PairDto dto) {
        if (dto.bye()) {
            return Pair.restore(PairId.of(dto.id()), null, null, true, null);
        }
        return Pair.restore(
                PairId.of(dto.id()),
                PlayerName.of(dto.player1Name()),
                PlayerName.of(dto.player2Name()),
                false,
                dto.seed()
        );
    }

    private BracketDto toBracketDto(Bracket bracket, Map<PairId, String> pairIdMap) {
        return new BracketDto(
                bracket.type().name(),
                bracket.rounds().stream()
                        .map(r -> toRoundDto(r, pairIdMap))
                        .toList()
        );
    }

    private RoundDto toRoundDto(Round round, Map<PairId, String> pairIdMap) {
        return new RoundDto(
                round.roundNumber(),
                round.bracketType().name(),
                round.matches().stream()
                        .map(m -> toMatchDto(m, pairIdMap))
                        .toList()
        );
    }

    private MatchDto toMatchDto(Match match, Map<PairId, String> pairIdMap) {
        return new MatchDto(
                match.id().value(),
                match.pair1() != null ? match.pair1().id().value() : null,
                match.pair2() != null ? match.pair2().id().value() : null,
                match.location().map(Location::value).orElse(null),
                match.dateTime().map(dt -> dt.value().format(DT_FORMAT)).orElse(null),
                match.result().map(this::toMatchResultDto).orElse(null),
                match.roundNumber(),
                match.position(),
                match.bracketType().name()
        );
    }

    private MatchResultDto toMatchResultDto(MatchResult result) {
        return new MatchResultDto(
                result.sets().stream()
                        .map(s -> new SetResultDto(s.pair1Games(), s.pair2Games()))
                        .toList()
        );
    }

    private Bracket fromBracketDto(BracketDto dto, Map<String, Pair> pairLookup) {
        BracketType type = BracketType.valueOf(dto.type());
        Bracket bracket = Bracket.create(type);
        for (RoundDto roundDto : dto.rounds()) {
            bracket.addRound(fromRoundDto(roundDto, pairLookup));
        }
        return bracket;
    }

    private Round fromRoundDto(RoundDto dto, Map<String, Pair> pairLookup) {
        BracketType type = BracketType.valueOf(dto.bracketType());
        List<Match> matches = dto.matches().stream()
                .map(m -> fromMatchDto(m, pairLookup))
                .toList();
        return Round.of(dto.roundNumber(), matches, type);
    }

    private Match fromMatchDto(MatchDto dto, Map<String, Pair> pairLookup) {
        Pair pair1 = dto.pair1Id() != null ? pairLookup.get(dto.pair1Id()) : null;
        Pair pair2 = dto.pair2Id() != null ? pairLookup.get(dto.pair2Id()) : null;

        Location location = dto.location() != null ? Location.of(dto.location()) : null;
        MatchDateTime dateTime = dto.dateTime() != null
                ? MatchDateTime.of(LocalDateTime.parse(dto.dateTime(), DT_FORMAT))
                : null;
        MatchResult result = dto.result() != null ? fromMatchResultDto(dto.result()) : null;

        return Match.restore(
                MatchId.of(dto.id()),
                pair1, pair2,
                location, dateTime, result,
                dto.roundNumber(), dto.position(),
                BracketType.valueOf(dto.bracketType())
        );
    }

    private MatchResult fromMatchResultDto(MatchResultDto dto) {
        List<SetResult> sets = dto.sets().stream()
                .map(s -> SetResult.of(s.pair1Games(), s.pair2Games()))
                .toList();
        return MatchResult.of(sets);
    }

    private Map<PairId, String> buildPairIdMap(Tournament tournament) {
        Map<PairId, String> map = new HashMap<>();
        tournament.pairs().forEach(p -> map.put(p.id(), p.id().value()));
        // Also collect pairs from bracket matches (BYE pairs, advanced pairs)
        tournament.allMatches().forEach(m -> {
            if (m.pair1() != null) map.put(m.pair1().id(), m.pair1().id().value());
            if (m.pair2() != null) map.put(m.pair2().id(), m.pair2().id().value());
        });
        return map;
    }

    private Map<String, Pair> buildPairLookup(List<Pair> pairs) {
        Map<String, Pair> lookup = new HashMap<>();
        pairs.forEach(p -> lookup.put(p.id().value(), p));
        return lookup;
    }

    private void collectBracketPairs(BracketDto bracketDto, Map<String, Pair> pairLookup) {
        // Pairs in bracket matches that aren't in the tournament pairs list (BYE pairs from generation)
        for (RoundDto round : bracketDto.rounds()) {
            for (MatchDto match : round.matches()) {
                if (match.pair1Id() != null && !pairLookup.containsKey(match.pair1Id())) {
                    // Must be a BYE pair — recreate it
                    Pair bye = Pair.restore(PairId.of(match.pair1Id()), null, null, true, null);
                    pairLookup.put(match.pair1Id(), bye);
                }
                if (match.pair2Id() != null && !pairLookup.containsKey(match.pair2Id())) {
                    Pair bye = Pair.restore(PairId.of(match.pair2Id()), null, null, true, null);
                    pairLookup.put(match.pair2Id(), bye);
                }
            }
        }
    }
}
