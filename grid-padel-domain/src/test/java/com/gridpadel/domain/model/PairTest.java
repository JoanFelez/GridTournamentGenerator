package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.PlayerName;
import org.junit.jupiter.api.Test;

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
    }

    @Test
    void shouldCreateByePair() {
        Pair bye = Pair.bye();
        assertThat(bye.isBye()).isTrue();
        assertThat(bye.player1Name().value()).isEqualTo("BYE");
        assertThat(bye.player2Name().value()).isEqualTo("BYE");
    }

    @Test
    void shouldRestorePairFromExistingData() {
        PairId id = PairId.generate();
        Pair pair = Pair.restore(id, PlayerName.of("A"), PlayerName.of("B"), false);
        assertThat(pair.id()).isEqualTo(id);
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
        Pair pair1 = Pair.restore(id, PlayerName.of("A"), PlayerName.of("B"), false);
        Pair pair2 = Pair.restore(id, PlayerName.of("X"), PlayerName.of("Y"), false);
        assertThat(pair1).isEqualTo(pair2);
    }
}
