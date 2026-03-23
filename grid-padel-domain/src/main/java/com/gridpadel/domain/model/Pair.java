package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.PlayerName;

import java.util.Objects;
import java.util.Optional;

public class Pair implements DomainEntity {

    private final PairId id;
    private PlayerName player1Name;
    private PlayerName player2Name;
    private final boolean bye;
    private Integer seed;

    private Pair(PairId id, PlayerName player1Name, PlayerName player2Name, boolean bye, Integer seed) {
        this.id = Objects.requireNonNull(id);
        this.player1Name = Objects.requireNonNull(player1Name);
        this.player2Name = Objects.requireNonNull(player2Name);
        this.bye = bye;
        this.seed = seed;
    }

    public static Pair create(PlayerName player1Name, PlayerName player2Name) {
        return new Pair(PairId.generate(), player1Name, player2Name, false, null);
    }

    public static Pair bye() {
        return new Pair(PairId.generate(), PlayerName.of("BYE"), PlayerName.of("BYE"), true, null);
    }

    public static Pair restore(PairId id, PlayerName player1Name, PlayerName player2Name, boolean bye, Integer seed) {
        return new Pair(id, player1Name, player2Name, bye, seed);
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

    public Optional<Integer> seed() {
        return Optional.ofNullable(seed);
    }

    public boolean isSeeded() {
        return seed != null;
    }

    public String displayName() {
        String base = player1Name.value() + " / " + player2Name.value();
        return seed != null ? "[" + seed + "] " + base : base;
    }

    public void assignSeed(int seedNumber) {
        if (bye) {
            throw new IllegalStateException("Cannot assign seed to a BYE pair");
        }
        if (seedNumber <= 0) {
            throw new IllegalArgumentException("Seed must be a positive number");
        }
        this.seed = seedNumber;
    }

    public void removeSeed() {
        this.seed = null;
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
