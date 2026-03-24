package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.*;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.AccessLevel;

import java.util.Objects;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Match implements DomainEntity {

    @EqualsAndHashCode.Include
    private final MatchId id;
    private Pair pair1;
    private Pair pair2;
    @Getter(AccessLevel.NONE) private Location location;
    @Getter(AccessLevel.NONE) private MatchDateTime dateTime;
    @Getter(AccessLevel.NONE) private MatchResult result;
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

    public Option<Location> location() {
        return Option.of(location);
    }

    public Option<MatchDateTime> dateTime() {
        return Option.of(dateTime);
    }

    public Option<MatchResult> result() {
        return Option.of(result);
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

    public boolean isWalkover() {
        return result != null && result.isWalkover();
    }

    public Option<Pair> winner() {
        return Option.of(result).map(r -> r.winnerPosition() == 1 ? pair1 : pair2);
    }

    public Option<Pair> loser() {
        return Option.of(result).map(r -> r.loserPosition() == 1 ? pair1 : pair2);
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
}
