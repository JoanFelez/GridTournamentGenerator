package com.gridpadel.domain.model;

import com.gridpadel.domain.exception.InvalidOperationException;
import com.gridpadel.domain.exception.ValidationException;
import com.gridpadel.domain.model.vo.BracketType;
import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.TournamentId;
import io.vavr.collection.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tournament implements DomainEntity {

    public static final int MAX_PAIRS = 64;

    @EqualsAndHashCode.Include
    private final TournamentId id;
    private String name;
    private List<Pair> pairs;
    private final Bracket mainBracket;
    private final Bracket consolationBracket;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Tournament(TournamentId id, String name, List<Pair> pairs,
                       Bracket mainBracket, Bracket consolationBracket,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.pairs = pairs;
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
                List.empty(),
                Bracket.create(BracketType.MAIN),
                Bracket.create(BracketType.CONSOLATION),
                now,
                now
        );
    }

    public static Tournament restore(TournamentId id, String name, java.util.List<Pair> pairs,
                                      Bracket mainBracket, Bracket consolationBracket,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Tournament(id, name, List.ofAll(pairs), mainBracket, consolationBracket, createdAt, updatedAt);
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
        pairs = pairs.append(Objects.requireNonNull(pair));
        this.updatedAt = LocalDateTime.now();
    }

    public void removePair(PairId pairId) {
        pairs = pairs.removeFirst(p -> p.id().equals(pairId));
        this.updatedAt = LocalDateTime.now();
    }

    public void clearBrackets() {
        mainBracket.clearRounds();
        consolationBracket.clearRounds();
        this.updatedAt = LocalDateTime.now();
    }

    public List<Match> allMatches() {
        return mainBracket.allMatches().appendAll(consolationBracket.allMatches());
    }
}
