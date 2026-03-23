package com.gridpadel.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TournamentIdTest {

    @Test
    void shouldCreateTournamentIdWithGeneratedUUID() {
        TournamentId id = TournamentId.generate();
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldCreateTournamentIdFromExistingValue() {
        String uuid = "770e8400-e29b-41d4-a716-446655440000";
        TournamentId id = TournamentId.of(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> TournamentId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        TournamentId id1 = TournamentId.of("770e8400-e29b-41d4-a716-446655440000");
        TournamentId id2 = TournamentId.of("770e8400-e29b-41d4-a716-446655440000");
        assertThat(id1).isEqualTo(id2);
    }
}
