package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.BracketType;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Bracket {

    private final BracketType type;
    private List<Round> rounds;

    private Bracket(BracketType type) {
        this.type = Objects.requireNonNull(type);
        this.rounds = List.empty();
    }

    public static Bracket create(BracketType type) {
        return new Bracket(type);
    }

    public List<Round> rounds() {
        return rounds;
    }

    public void addRound(Round round) {
        rounds = rounds.append(Objects.requireNonNull(round));
    }

    public void clearRounds() {
        rounds = List.empty();
    }

    public Option<Round> round(int roundNumber) {
        return rounds.find(r -> r.roundNumber() == roundNumber);
    }

    public int totalMatches() {
        return rounds.map(Round::matchCount).sum().intValue();
    }

    public List<Match> allMatches() {
        return rounds.flatMap(Round::matches);
    }
}
