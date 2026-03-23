package com.gridpadel.domain.model;

import com.gridpadel.domain.exception.InvalidOperationException;
import com.gridpadel.domain.exception.ValidationException;
import com.gridpadel.domain.model.vo.BracketType;
import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.TournamentId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tournament implements DomainEntity {

    public static final int MAX_PAIRS = 32;

    @EqualsAndHashCode.Include
    private final TournamentId id;
    private String name;
    @Getter(AccessLevel.NONE) private final List<Pair> pairs;
    private final Bracket mainBracket;
    private final Bracket consolationBracket;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Tournament(TournamentId id, String name, List<Pair> pairs,
                       Bracket mainBracket, Bracket consolationBracket,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.pairs = new ArrayList<>(pairs);
        this.mainBracket = Objects.requireNonNull(mainBracket);
        this.consolationBracket = Objects.requireNonNull(consolationBracket);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Tournament create(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Tournament name cannot be blank", "name");
        }
        LocalDateTime now = LocalDateTime.now();
        return new Tournament(
                TournamentId.generate(),
                name.trim(),
                new ArrayList<>(),
                Bracket.create(BracketType.MAIN),
                Bracket.create(BracketType.CONSOLATION),
                now,
                now
        );
    }

    public static Tournament restore(TournamentId id, String name, List<Pair> pairs,
                                      Bracket mainBracket, Bracket consolationBracket,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Tournament(id, name, pairs, mainBracket, consolationBracket, createdAt, updatedAt);
    }

    public List<Pair> pairs() {
        return Collections.unmodifiableList(pairs);
    }

    public int pairCount() {
        return pairs.size();
    }

    public void updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new ValidationException("Tournament name cannot be blank", "name");
        }
        this.name = newName.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void addPair(Pair pair) {
        if (pairs.size() >= MAX_PAIRS) {
            throw new InvalidOperationException("Tournament cannot have more than " + MAX_PAIRS + " pairs");
        }
        pairs.add(Objects.requireNonNull(pair));
        this.updatedAt = LocalDateTime.now();
    }

    public void removePair(PairId pairId) {
        pairs.removeIf(p -> p.id().equals(pairId));
        this.updatedAt = LocalDateTime.now();
    }

    public List<Match> allMatches() {
        return Stream.concat(
                mainBracket.allMatches().stream(),
                consolationBracket.allMatches().stream()
        ).toList();
    }
}
