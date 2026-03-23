package com.gridpadel.infrastructure.persistence;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.*;
import com.gridpadel.domain.service.BracketGenerationService;
import com.gridpadel.domain.service.MatchAdvancementService;
import com.gridpadel.infrastructure.persistence.dto.TournamentDto;
import com.gridpadel.infrastructure.persistence.mapper.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class TournamentDtoMapperTest {

    private final TournamentDtoMapper mapper = createMapper();

    private static TournamentDtoMapper createMapper() {
        SetResultDtoMapper setResultMapper = new SetResultDtoMapperImpl();
        MatchResultDtoMapper matchResultMapper = new MatchResultDtoMapperImpl(setResultMapper);
        MatchDtoMapper matchMapper = new MatchDtoMapper(matchResultMapper);
        RoundDtoMapper roundMapper = new RoundDtoMapper(matchMapper);
        BracketDtoMapper bracketMapper = new BracketDtoMapper(roundMapper);
        PairDtoMapper pairMapper = new PairDtoMapper();
        return new TournamentDtoMapper(pairMapper, bracketMapper);
    }

    @Test
    void shouldRoundTripEmptyTournament() {
        Tournament original = Tournament.create("Test Tournament");

        TournamentDto dto = mapper.toDto(original);
        Tournament restored = mapper.fromDto(dto);

        assertThat(restored.id()).isEqualTo(original.id());
        assertThat(restored.name()).isEqualTo("Test Tournament");
        assertThat(restored.pairs()).isEmpty();
    }

    @Test
    void shouldRoundTripTournamentWithPairs() {
        Tournament original = Tournament.create("With Pairs");
        Pair p1 = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        Pair p2 = Pair.create(PlayerName.of("Juan"), PlayerName.of("Ana"));
        p1.assignSeed(1);
        original.addPair(p1);
        original.addPair(p2);

        TournamentDto dto = mapper.toDto(original);
        Tournament restored = mapper.fromDto(dto);

        assertThat(restored.pairs()).hasSize(2);
        Pair restoredP1 = restored.pairs().stream()
                .filter(p -> p.id().equals(p1.id())).findFirst().orElseThrow();
        assertThat(restoredP1.player1Name().value()).isEqualTo("Carlos");
        assertThat(restoredP1.player2Name().value()).isEqualTo("María");
        assertThat(restoredP1.seed()).contains(1);

        Pair restoredP2 = restored.pairs().stream()
                .filter(p -> p.id().equals(p2.id())).findFirst().orElseThrow();
        assertThat(restoredP2.isSeeded()).isFalse();
    }

    @Test
    void shouldRoundTripTournamentWithBracket() {
        Tournament original = createTournamentWithBracket();

        TournamentDto dto = mapper.toDto(original);
        Tournament restored = mapper.fromDto(dto);

        assertThat(restored.mainBracket().rounds()).hasSameSizeAs(original.mainBracket().rounds());
        assertThat(restored.allMatches()).hasSameSizeAs(original.allMatches());
    }

    @Test
    void shouldRoundTripMatchWithLocationAndDateTime() {
        Tournament original = createTournamentWithBracket();
        Match match = original.mainBracket().rounds().get(0).matchAt(0);
        match.setLocation(Location.of("Pista Central"));
        match.setDateTime(MatchDateTime.of(LocalDateTime.of(2026, 5, 1, 10, 30)));

        TournamentDto dto = mapper.toDto(original);
        Tournament restored = mapper.fromDto(dto);

        Match restoredMatch = restored.mainBracket().rounds().get(0).matchAt(0);
        assertThat(restoredMatch.location().get().value()).isEqualTo("Pista Central");
        assertThat(restoredMatch.dateTime().get().value()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 30));
    }

    @Test
    void shouldRoundTripMatchWithResult() {
        Tournament original = createTournamentWithBracket();
        Match targetMatch = original.mainBracket().rounds().get(0).matches().stream()
                .filter(m -> !m.isByeMatch() && m.isComplete())
                .findFirst().orElseThrow();

        MatchResult result = MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4));
        targetMatch.recordResult(result);

        TournamentDto dto = mapper.toDto(original);
        Tournament restored = mapper.fromDto(dto);

        Match restoredMatch = restored.allMatches().stream()
                .filter(m -> m.id().equals(targetMatch.id()))
                .findFirst().orElseThrow();
        assertThat(restoredMatch.isPlayed()).isTrue();
        assertThat(restoredMatch.result().get().sets()).hasSize(2);
        assertThat(restoredMatch.result().get().sets().get(0).pair1Games()).isEqualTo(6);
        assertThat(restoredMatch.result().get().sets().get(0).pair2Games()).isEqualTo(3);
    }

    @Test
    void shouldRoundTripWithAdvancement() {
        Tournament original = createTournamentWithBracket();
        MatchAdvancementService advService = new MatchAdvancementService();

        Match r1match = original.mainBracket().rounds().get(0).matches().stream()
                .filter(m -> !m.isByeMatch() && m.isComplete())
                .findFirst().orElseThrow();

        MatchResult result = MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4));
        advService.processMatchResult(original, r1match.id(), result);

        TournamentDto dto = mapper.toDto(original);
        Tournament restored = mapper.fromDto(dto);

        assertThat(restored.allMatches()).hasSameSizeAs(original.allMatches());
        Match restoredR1 = restored.allMatches().stream()
                .filter(m -> m.id().equals(r1match.id()))
                .findFirst().orElseThrow();
        assertThat(restoredR1.isPlayed()).isTrue();
    }

    @Test
    void shouldPreserveTimestamps() {
        Tournament original = Tournament.create("Test");

        TournamentDto dto = mapper.toDto(original);
        Tournament restored = mapper.fromDto(dto);

        assertThat(restored.createdAt()).isEqualToIgnoringNanos(original.createdAt());
        assertThat(restored.updatedAt()).isEqualToIgnoringNanos(original.updatedAt());
    }

    private Tournament createTournamentWithBracket() {
        Tournament t = Tournament.create("Test");
        t.addPair(Pair.create(PlayerName.of("A"), PlayerName.of("B")));
        t.addPair(Pair.create(PlayerName.of("C"), PlayerName.of("D")));
        t.addPair(Pair.create(PlayerName.of("E"), PlayerName.of("F")));
        t.addPair(Pair.create(PlayerName.of("G"), PlayerName.of("H")));
        new BracketGenerationService().generateMainBracket(t);
        return t;
    }
}
