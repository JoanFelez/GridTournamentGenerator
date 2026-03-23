package com.gridpadel.domain.model;

import com.gridpadel.domain.model.vo.BracketType;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Bracket {

    private final BracketType type;
    @Getter(AccessLevel.NONE) private final List<Round> rounds;

    private Bracket(BracketType type) {
        this.type = Objects.requireNonNull(type);
        this.rounds = new ArrayList<>();
    }

    public static Bracket create(BracketType type) {
        return new Bracket(type);
    }

    public List<Round> rounds() {
        return Collections.unmodifiableList(rounds);
    }

    public void addRound(Round round) {
        rounds.add(Objects.requireNonNull(round));
    }

    public Optional<Round> round(int roundNumber) {
        return rounds.stream()
                .filter(r -> r.roundNumber() == roundNumber)
                .findFirst();
    }

    public int totalMatches() {
        return rounds.stream().mapToInt(Round::matchCount).sum();
    }

    public List<Match> allMatches() {
        return rounds.stream()
                .flatMap(r -> r.matches().stream())
                .collect(Collectors.toUnmodifiableList());
    }
}
