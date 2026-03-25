package com.gridpadel.domain.model;

import com.gridpadel.domain.exception.DomainException;

import com.gridpadel.domain.model.vo.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TournamentTest {

    private Pair pair(String p1, String p2) {
        return Pair.create(PlayerName.of(p1), PlayerName.of(p2));
    }

    @Test
    void shouldCreateTournamentWithName() {
        Tournament tournament = Tournament.create("Torneo Primavera 2026");
        assertThat(tournament.id()).isNotNull();
        assertThat(tournament.name()).isEqualTo("Torneo Primavera 2026");
        assertThat(tournament.pairs()).isEmpty();
        assertThat(tournament.createdAt()).isNotNull();
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Tournament.create("  "))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldAddPairs() {
        Tournament tournament = Tournament.create("Test");
        Pair p = pair("Carlos", "María");
        tournament.addPair(p);
        assertThat(tournament.pairs()).hasSize(1);
    }

    @Test
    void shouldEnforceMaximum64Pairs() {
        Tournament tournament = Tournament.create("Test");
        for (int i = 0; i < 64; i++) {
            tournament.addPair(pair("P" + i + "a", "P" + i + "b"));
        }
        assertThatThrownBy(() -> tournament.addPair(pair("Extra1", "Extra2")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("64");
    }

    @Test
    void shouldRemovePair() {
        Tournament tournament = Tournament.create("Test");
        Pair p = pair("Carlos", "María");
        tournament.addPair(p);
        tournament.removePair(p.id());
        assertThat(tournament.pairs()).isEmpty();
    }

    @Test
    void shouldUpdateTournamentName() {
        Tournament tournament = Tournament.create("Old Name");
        tournament.updateName("New Name");
        assertThat(tournament.name()).isEqualTo("New Name");
    }

    @Test
    void shouldTrackUpdatedAt() {
        Tournament tournament = Tournament.create("Test");
        LocalDateTime before = tournament.updatedAt();
        tournament.updateName("Updated");
        assertThat(tournament.updatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void shouldHaveMainAndConsolationBrackets() {
        Tournament tournament = Tournament.create("Test");
        assertThat(tournament.mainBracket()).isNotNull();
        assertThat(tournament.mainBracket().type()).isEqualTo(BracketType.MAIN);
        assertThat(tournament.consolationBracket()).isNotNull();
        assertThat(tournament.consolationBracket().type()).isEqualTo(BracketType.CONSOLATION);
    }

    @Test
    void shouldReturnPairCount() {
        Tournament tournament = Tournament.create("Test");
        tournament.addPair(pair("A", "B"));
        tournament.addPair(pair("C", "D"));
        assertThat(tournament.pairCount()).isEqualTo(2);
    }

    @Test
    void shouldReturnAllMatchesAcrossBothBrackets() {
        Tournament tournament = Tournament.create("Test");
        assertThat(tournament.allMatches()).isEmpty();
    }

    @Test
    void shouldSupportRestoreFromPersistedState() {
        TournamentId id = TournamentId.generate();
        List<Pair> pairs = List.of(pair("A", "B"));
        Bracket main = Bracket.create(BracketType.MAIN);
        Bracket consolation = Bracket.create(BracketType.CONSOLATION);
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2026, 3, 1, 0, 0);

        Tournament tournament = Tournament.restore(id, "Restored", "Test Category", pairs, main, consolation, created, updated);
        assertThat(tournament.id()).isEqualTo(id);
        assertThat(tournament.name()).isEqualTo("Restored");
        assertThat(tournament.category()).isEqualTo("Test Category");
        assertThat(tournament.pairs()).hasSize(1);
        assertThat(tournament.createdAt()).isEqualTo(created);
        assertThat(tournament.updatedAt()).isEqualTo(updated);
    }
}
