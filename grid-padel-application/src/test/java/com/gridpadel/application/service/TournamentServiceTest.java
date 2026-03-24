package com.gridpadel.application.service;

import com.gridpadel.domain.exception.ValidationException;
import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.*;
import com.gridpadel.domain.port.PairImportPort;
import com.gridpadel.domain.repository.TournamentRepository;
import com.gridpadel.domain.service.BracketGenerationService;
import com.gridpadel.domain.service.MatchAdvancementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository repository;

    @Mock
    private PairImportPort csvImporter;

    private TournamentService service;

    @BeforeEach
    void setUp() {
        service = new TournamentService(
                repository,
                new BracketGenerationService(),
                new MatchAdvancementService(),
                java.util.List.of(csvImporter)
        );
    }

    // --- Create Tournament ---

    @Test
    void shouldCreateTournament() {
        Tournament result = service.createTournament("Torneo Primavera");

        assertThat(result.name()).isEqualTo("Torneo Primavera");
        assertThat(result.id()).isNotNull();
        verify(repository).save(result);
    }

    @Test
    void shouldRejectBlankTournamentName() {
        assertThatThrownBy(() -> service.createTournament("  "))
                .isInstanceOf(ValidationException.class);
    }

    // --- Get / List ---

    @Test
    void shouldGetTournamentById() {
        Tournament t = Tournament.create("Test");
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        Tournament found = service.getTournament(t.id());
        assertThat(found).isEqualTo(t);
    }

    @Test
    void shouldThrowWhenTournamentNotFound() {
        TournamentId id = TournamentId.generate();
        when(repository.findById(id)).thenReturn(Option.none());

        assertThatThrownBy(() -> service.getTournament(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldListAllTournaments() {
        List<Tournament> tournaments = List.of(
                Tournament.create("T1"),
                Tournament.create("T2")
        );
        when(repository.findAll()).thenReturn(tournaments);

        List<Tournament> result = service.listTournaments();
        assertThat(result).hasSize(2);
    }

    // --- Pair management ---

    @Test
    void shouldAddPairToTournament() {
        Tournament t = Tournament.create("Test");
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        Pair pair = service.addPair(t.id(), "Carlos", "María");

        assertThat(pair.player1Name().value()).isEqualTo("Carlos");
        assertThat(pair.player2Name().value()).isEqualTo("María");
        assertThat(t.pairs()).contains(pair);
        verify(repository).save(t);
    }

    @Test
    void shouldAddSeededPair() {
        Tournament t = Tournament.create("Test");
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        Pair pair = service.addSeededPair(t.id(), "Carlos", "María", 1);

        assertThat(pair.isSeeded()).isTrue();
        assertThat(pair.seed()).contains(1);
        verify(repository).save(t);
    }

    @Test
    void shouldRemovePair() {
        Tournament t = Tournament.create("Test");
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        t.addPair(pair);
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        service.removePair(t.id(), pair.id());

        assertThat(t.pairs()).isEmpty();
        verify(repository).save(t);
    }

    // --- Bracket generation ---

    @Test
    void shouldGenerateBracket() {
        Tournament t = Tournament.create("Test");
        t.addPair(Pair.create(PlayerName.of("A"), PlayerName.of("B")));
        t.addPair(Pair.create(PlayerName.of("C"), PlayerName.of("D")));
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        service.generateBracket(t.id());

        assertThat(t.mainBracket().rounds()).isNotEmpty();
        verify(repository).save(t);
    }

    // --- Match result ---

    @Test
    void shouldRecordMatchResult() {
        Tournament t = createTournamentWithBracket();
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        MatchResult result = MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4));

        service.recordMatchResult(t.id(), r1m0.id(), result);

        assertThat(r1m0.isPlayed()).isTrue();
        verify(repository).save(t);
    }

    @Test
    void shouldClearMatchResult() {
        Tournament t = createTournamentWithBracket();
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        MatchResult result = MatchResult.of(SetResult.of(6, 3), SetResult.of(6, 4));
        service.recordMatchResult(t.id(), r1m0.id(), result);

        service.clearMatchResult(t.id(), r1m0.id());

        assertThat(r1m0.isPlayed()).isFalse();
        verify(repository, times(2)).save(t); // record + clear
    }

    // --- Update / Delete ---

    @Test
    void shouldUpdateTournamentName() {
        Tournament t = Tournament.create("Old");
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        service.updateTournamentName(t.id(), "New Name");

        assertThat(t.name()).isEqualTo("New Name");
        verify(repository).save(t);
    }

    @Test
    void shouldDeleteTournament() {
        TournamentId id = TournamentId.generate();

        service.deleteTournament(id);

        verify(repository).delete(id);
    }

    // --- Match details ---

    @Test
    void shouldSetMatchLocation() {
        Tournament t = createTournamentWithBracket();
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        service.setMatchLocation(t.id(), r1m0.id(), "Pista Central");

        assertThat(r1m0.location().isDefined()).isTrue();
        assertThat(r1m0.location().get().value()).isEqualTo("Pista Central");
        verify(repository).save(t);
    }

    @Test
    void shouldSetMatchDateTime() {
        Tournament t = createTournamentWithBracket();
        when(repository.findById(t.id())).thenReturn(Option.of(t));

        Match r1m0 = t.mainBracket().rounds().get(0).matchAt(0);
        java.time.LocalDateTime dt = java.time.LocalDateTime.of(2026, 5, 1, 10, 0);
        service.setMatchDateTime(t.id(), r1m0.id(), dt);

        assertThat(r1m0.dateTime().isDefined()).isTrue();
        verify(repository).save(t);
    }

    // --- Import pairs ---

    @Test
    void shouldParsePairsFromFile() {
        when(csvImporter.supports("csv")).thenReturn(true);

        List<PairImportPort.ImportedPair> imported = List.of(
                new PairImportPort.ImportedPair("Alice", "Bob", null),
                new PairImportPort.ImportedPair("Charlie", "Diana", 1)
        );
        when(csvImporter.importPairs(any())).thenReturn(Try.success(imported));

        Try<List<PairImportPort.ImportedPair>> result = service.parsePairsFromFile(
                java.io.InputStream.nullInputStream(), "csv");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get().get(0).player1()).isEqualTo("Alice");
    }

    @Test
    void shouldRejectUnsupportedFileFormat() {
        when(csvImporter.supports("pdf")).thenReturn(false);

        assertThatThrownBy(() -> service.parsePairsFromFile(java.io.InputStream.nullInputStream(), "pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file format");
    }

    // --- Helpers ---

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
