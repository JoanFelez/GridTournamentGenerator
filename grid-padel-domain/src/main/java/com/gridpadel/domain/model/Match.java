package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.*;

import java.util.Objects;
import java.util.Optional;

public class Match implements DomainEntity {

    private final MatchId id;
    private Pair pair1;
    private Pair pair2;
    private Location location;
    private MatchDateTime dateTime;
    private MatchResult result;
    private final int roundNumber;
    private final int position;
    private final BracketType bracketType;

    private Match(MatchId id, Pair pair1, Pair pair2, int roundNumber, int position, BracketType bracketType) {
        this.id = Objects.requireNonNull(id);
        this.pair1 = pair1;
        this.pair2 = pair2;
        this.roundNumber = roundNumber;
        this.position = position;
        this.bracketType = Objects.requireNonNull(bracketType);
    }

    public static Match create(Pair pair1, Pair pair2, int roundNumber, int position, BracketType bracketType) {
        return new Match(MatchId.generate(), pair1, pair2, roundNumber, position, bracketType);
    }

    public static Match createWithPair1Only(Pair pair1, int roundNumber, int position, BracketType bracketType) {
        return new Match(MatchId.generate(), pair1, null, roundNumber, position, bracketType);
    }

    public static Match createEmpty(int roundNumber, int position, BracketType bracketType) {
        return new Match(MatchId.generate(), null, null, roundNumber, position, bracketType);
    }

    public static Match restore(MatchId id, Pair pair1, Pair pair2, Location location,
                                 MatchDateTime dateTime, MatchResult result,
                                 int roundNumber, int position, BracketType bracketType) {
        Match match = new Match(id, pair1, pair2, roundNumber, position, bracketType);
        match.location = location;
        match.dateTime = dateTime;
        match.result = result;
        return match;
    }

    public MatchId id() {
        return id;
    }

    public Pair pair1() {
        return pair1;
    }

    public Pair pair2() {
        return pair2;
    }

    public Optional<Location> location() {
        return Optional.ofNullable(location);
    }

    public Optional<MatchDateTime> dateTime() {
        return Optional.ofNullable(dateTime);
    }

    public Optional<MatchResult> result() {
        return Optional.ofNullable(result);
    }

    public int roundNumber() {
        return roundNumber;
    }

    public int position() {
        return position;
    }

    public BracketType bracketType() {
        return bracketType;
    }

    public boolean isPlayed() {
        return result != null;
    }

    public boolean isComplete() {
        return pair1 != null && pair2 != null;
    }

    public boolean isByeMatch() {
        return (pair1 != null && pair1.isBye()) || (pair2 != null && pair2.isBye());
    }

    public Optional<Pair> winner() {
        if (result == null) return Optional.empty();
        return Optional.of(result.winnerPosition() == 1 ? pair1 : pair2);
    }

    public Optional<Pair> loser() {
        if (result == null) return Optional.empty();
        return Optional.of(result.loserPosition() == 1 ? pair1 : pair2);
    }

    public void recordResult(MatchResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public void clearResult() {
        this.result = null;
    }

    public void setLocation(Location location) {
        this.location = Objects.requireNonNull(location);
    }

    public void clearLocation() {
        this.location = null;
    }

    public void setDateTime(MatchDateTime dateTime) {
        this.dateTime = Objects.requireNonNull(dateTime);
    }

    public void clearDateTime() {
        this.dateTime = null;
    }

    public void setPair1(Pair pair) {
        this.pair1 = pair;
    }

    public void setPair2(Pair pair) {
        this.pair2 = pair;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String p1 = pair1 != null ? pair1.displayName() : "TBD";
        String p2 = pair2 != null ? pair2.displayName() : "TBD";
        return "Match{R" + roundNumber + "P" + position + " " + p1 + " vs " + p2 + "}";
    }
}
