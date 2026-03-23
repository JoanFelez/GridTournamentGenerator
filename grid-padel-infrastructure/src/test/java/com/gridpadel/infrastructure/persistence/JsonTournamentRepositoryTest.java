package com.gridpadel.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.PlayerName;
import com.gridpadel.domain.model.vo.TournamentId;
import com.gridpadel.domain.service.BracketGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.gridpadel.infrastructure.persistence.mapper.*;

import java.nio.file.Path;
import java.util.List;
import io.vavr.control.Option;

import static org.assertj.core.api.Assertions.*;

class JsonTournamentRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonTournamentRepository repository;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        TournamentDtoMapper mapper = createMapper();
        repository = new JsonTournamentRepository(tempDir.toString(), objectMapper, mapper);
        repository.init();
    }

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
    void shouldSaveAndLoadTournament() {
        Tournament tournament = Tournament.create("Test");
        tournament.addPair(Pair.create(PlayerName.of("A"), PlayerName.of("B")));

        repository.save(tournament);

        Option<Tournament> loaded = repository.findById(tournament.id());
        assertThat(loaded.isDefined()).isTrue();
        assertThat(loaded.get().name()).isEqualTo("Test");
        assertThat(loaded.get().pairs()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Option<Tournament> result = repository.findById(TournamentId.generate());
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldListAllTournaments() {
        repository.save(Tournament.create("T1"));
        repository.save(Tournament.create("T2"));
        repository.save(Tournament.create("T3"));

        List<Tournament> all = repository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    void shouldDeleteTournament() {
        Tournament tournament = Tournament.create("ToDelete");
        repository.save(tournament);
        assertThat(repository.findById(tournament.id()).isDefined()).isTrue();

        repository.delete(tournament.id());
        assertThat(repository.findById(tournament.id()).isEmpty()).isTrue();
    }

    @Test
    void shouldOverwriteOnSave() {
        Tournament tournament = Tournament.create("Original");
        repository.save(tournament);

        tournament.updateName("Updated");
        repository.save(tournament);

        Tournament loaded = repository.findById(tournament.id()).getOrElseThrow(() -> new AssertionError("Expected tournament"));
        assertThat(loaded.name()).isEqualTo("Updated");
    }

    @Test
    void shouldSaveAndLoadWithBracket() {
        Tournament tournament = Tournament.create("Bracket Test");
        tournament.addPair(Pair.create(PlayerName.of("A"), PlayerName.of("B")));
        tournament.addPair(Pair.create(PlayerName.of("C"), PlayerName.of("D")));
        tournament.addPair(Pair.create(PlayerName.of("E"), PlayerName.of("F")));
        tournament.addPair(Pair.create(PlayerName.of("G"), PlayerName.of("H")));
        new BracketGenerationService().generateMainBracket(tournament);

        repository.save(tournament);

        Tournament loaded = repository.findById(tournament.id()).getOrElseThrow(() -> new AssertionError("Expected tournament"));
        assertThat(loaded.mainBracket().rounds()).isNotEmpty();
        assertThat(loaded.allMatches()).hasSameSizeAs(tournament.allMatches());
    }

    @Test
    void shouldReturnEmptyListWhenNoFiles() {
        List<Tournament> result = repository.findAll();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldHandleDeleteNonExistent() {
        assertThatCode(() -> repository.delete(TournamentId.generate()))
                .doesNotThrowAnyException();
    }
}
