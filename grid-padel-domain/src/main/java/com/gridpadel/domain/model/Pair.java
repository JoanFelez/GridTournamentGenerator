package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.PlayerName;

import java.util.Objects;

public class Pair implements DomainEntity {

    private final PairId id;
    private PlayerName player1Name;
    private PlayerName player2Name;
    private final boolean bye;

    private Pair(PairId id, PlayerName player1Name, PlayerName player2Name, boolean bye) {
        this.id = Objects.requireNonNull(id);
        this.player1Name = Objects.requireNonNull(player1Name);
        this.player2Name = Objects.requireNonNull(player2Name);
        this.bye = bye;
    }

    public static Pair create(PlayerName player1Name, PlayerName player2Name) {
        return new Pair(PairId.generate(), player1Name, player2Name, false);
    }

    public static Pair bye() {
        return new Pair(PairId.generate(), PlayerName.of("BYE"), PlayerName.of("BYE"), true);
    }

    public static Pair restore(PairId id, PlayerName player1Name, PlayerName player2Name, boolean bye) {
        return new Pair(id, player1Name, player2Name, bye);
    }

    public PairId id() {
        return id;
    }

    public PlayerName player1Name() {
        return player1Name;
    }

    public PlayerName player2Name() {
        return player2Name;
    }

    public boolean isBye() {
        return bye;
    }

    public String displayName() {
        return player1Name.value() + " / " + player2Name.value();
    }

    public void updatePlayer1Name(PlayerName newName) {
        if (bye) {
            throw new IllegalStateException("Cannot update player names on a BYE pair");
        }
        this.player1Name = Objects.requireNonNull(newName);
    }

    public void updatePlayer2Name(PlayerName newName) {
        if (bye) {
            throw new IllegalStateException("Cannot update player names on a BYE pair");
        }
        this.player2Name = Objects.requireNonNull(newName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pair{" + displayName() + (bye ? " [BYE]" : "") + "}";
    }
}
