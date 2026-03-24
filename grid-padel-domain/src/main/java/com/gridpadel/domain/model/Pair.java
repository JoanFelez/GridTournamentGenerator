package com.gridpadel.domain.model;

import com.gridpadel.domain.exception.InvalidOperationException;
import com.gridpadel.domain.exception.ValidationException;
import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.PlayerName;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pair implements DomainEntity {

    @Getter @EqualsAndHashCode.Include
    private final PairId id;
    private PlayerName player1Name;
    private PlayerName player2Name;
    private final boolean bye;
    private Integer seed;

    private Pair(PairId id, PlayerName player1Name, PlayerName player2Name, boolean bye, Integer seed) {
        this.id = Objects.requireNonNull(id);
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.bye = bye;
        this.seed = seed;
    }

    public static Pair create(PlayerName player1Name, PlayerName player2Name) {
        if (player1Name == null) {
            throw new ValidationException("Player 1 name is required", "player1Name");
        }
        if (player2Name == null) {
            throw new ValidationException("Player 2 name is required", "player2Name");
        }
        return new Pair(PairId.generate(), player1Name, player2Name, false, null);
    }

    public static Pair bye() {
        return new Pair(PairId.generate(), null, null, true, null);
    }

    public static Pair restore(PairId id, PlayerName player1Name, PlayerName player2Name, boolean bye, Integer seed) {
        return new Pair(id, player1Name, player2Name, bye, seed);
    }

    public boolean isBye() {
        return bye;
    }

    public PlayerName player1Name() {
        if (bye) {
            throw new InvalidOperationException("BYE pair has no player data");
        }
        return player1Name;
    }

    public PlayerName player2Name() {
        if (bye) {
            throw new InvalidOperationException("BYE pair has no player data");
        }
        return player2Name;
    }

    public Option<Integer> seed() {
        return Option.of(seed);
    }

    public boolean isSeeded() {
        return seed != null;
    }

    public String displayName() {
        if (bye) {
            return "BYE";
        }
        String base = player1Name.value() + " / " + player2Name.value();
        return seed != null ? "[" + seed + "] " + base : base;
    }

    public void assignSeed(int seedNumber) {
        if (bye) {
            throw new InvalidOperationException("Cannot assign seed to a BYE pair");
        }
        if (seedNumber <= 0) {
            throw new ValidationException("Seed must be a positive number", "seed");
        }
        this.seed = seedNumber;
    }

    public void removeSeed() {
        this.seed = null;
    }

    public void updatePlayer1Name(PlayerName newName) {
        if (bye) {
            throw new InvalidOperationException("Cannot update player names on a BYE pair");
        }
        this.player1Name = Objects.requireNonNull(newName);
    }

    public void updatePlayer2Name(PlayerName newName) {
        if (bye) {
            throw new InvalidOperationException("Cannot update player names on a BYE pair");
        }
        this.player2Name = Objects.requireNonNull(newName);
    }
}
