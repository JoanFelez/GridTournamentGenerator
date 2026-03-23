package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.PlayerName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class PairTest {

    @Test
    void shouldCreatePairWithTwoPlayers() {
        Pair pair = Pair.create(
                PlayerName.of("Carlos"),
                PlayerName.of("María")
        );
        assertThat(pair.id()).isNotNull();
        assertThat(pair.player1Name().value()).isEqualTo("Carlos");
        assertThat(pair.player2Name().value()).isEqualTo("María");
        assertThat(pair.isBye()).isFalse();
        assertThat(pair.seed()).isEmpty();
        assertThat(pair.isSeeded()).isFalse();
    }

    @Test
    void shouldCreateByePair() {
        Pair bye = Pair.bye();
        assertThat(bye.isBye()).isTrue();
        assertThat(bye.displayName()).isEqualTo("BYE");
        assertThat(bye.seed()).isEmpty();
        assertThat(bye.isSeeded()).isFalse();
    }

    @Test
    void shouldNotAllowAccessingPlayerNamesOnByePair() {
        Pair bye = Pair.bye();
        assertThatThrownBy(() -> bye.player1Name())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> bye.player2Name())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRestorePairFromExistingData() {
        PairId id = PairId.generate();
        Pair pair = Pair.restore(id, PlayerName.of("A"), PlayerName.of("B"), false, null);
        assertThat(pair.id()).isEqualTo(id);
    }

    @Test
    void shouldRestorePairWithSeed() {
        PairId id = PairId.generate();
        Pair pair = Pair.restore(id, PlayerName.of("A"), PlayerName.of("B"), false, 1);
        assertThat(pair.seed()).isPresent().contains(1);
    }

    @Test
    void shouldUpdatePlayer1Name() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        pair.updatePlayer1Name(PlayerName.of("Juan"));
        assertThat(pair.player1Name().value()).isEqualTo("Juan");
    }

    @Test
    void shouldUpdatePlayer2Name() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        pair.updatePlayer2Name(PlayerName.of("Ana"));
        assertThat(pair.player2Name().value()).isEqualTo("Ana");
    }

    @Test
    void shouldNotAllowUpdatingByePairNames() {
        Pair bye = Pair.bye();
        assertThatThrownBy(() -> bye.updatePlayer1Name(PlayerName.of("Carlos")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReturnDisplayName() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        assertThat(pair.displayName()).isEqualTo("Carlos / María");
    }

    @Test
    void shouldBeEqualByIdOnly() {
        PairId id = PairId.generate();
        Pair pair1 = Pair.restore(id, PlayerName.of("A"), PlayerName.of("B"), false, null);
        Pair pair2 = Pair.restore(id, PlayerName.of("X"), PlayerName.of("Y"), false, null);
        assertThat(pair1).isEqualTo(pair2);
    }

    // --- Seed tests ---

    @Test
    void shouldAssignSeed() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        pair.assignSeed(1);
        assertThat(pair.seed()).isPresent().contains(1);
        assertThat(pair.isSeeded()).isTrue();
    }

    @Test
    void shouldUpdateSeed() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        pair.assignSeed(1);
        pair.assignSeed(3);
        assertThat(pair.seed()).isPresent().contains(3);
    }

    @Test
    void shouldRemoveSeed() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        pair.assignSeed(1);
        pair.removeSeed();
        assertThat(pair.seed()).isEmpty();
        assertThat(pair.isSeeded()).isFalse();
    }

    @Test
    void shouldRejectNonPositiveSeed() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        assertThatThrownBy(() -> pair.assignSeed(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pair.assignSeed(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotAllowSeedOnByePair() {
        Pair bye = Pair.bye();
        assertThatThrownBy(() -> bye.assignSeed(1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldIncludeSeedInDisplayName() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        pair.assignSeed(1);
        assertThat(pair.displayName()).isEqualTo("[1] Carlos / María");
    }

    @Test
    void shouldNotIncludeSeedInDisplayNameWhenUnseeded() {
        Pair pair = Pair.create(PlayerName.of("Carlos"), PlayerName.of("María"));
        assertThat(pair.displayName()).isEqualTo("Carlos / María");
    }
}
