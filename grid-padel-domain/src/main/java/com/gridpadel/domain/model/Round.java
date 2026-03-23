package com.gridpadel.domain.model;

import com.gridpadel.domain.exception.EntityNotFoundException;
import com.gridpadel.domain.exception.ValidationException;
import com.gridpadel.domain.model.vo.BracketType;
import io.vavr.collection.List;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Round {

    private final int roundNumber;
    private final List<Match> matches;
    private final BracketType bracketType;

    private Round(int roundNumber, List<Match> matches, BracketType bracketType) {
        if (matches == null || matches.isEmpty()) {
            throw new ValidationException("Round must have at least one match", "matches");
        }
        this.roundNumber = roundNumber;
        this.matches = matches;
        this.bracketType = Objects.requireNonNull(bracketType);
    }

    public static Round of(int roundNumber, java.util.List<Match> matches, BracketType bracketType) {
        return new Round(roundNumber, List.ofAll(matches), bracketType);
    }

    public static Round of(int roundNumber, List<Match> matches, BracketType bracketType) {
        return new Round(roundNumber, matches, bracketType);
    }

    public static Round of(int roundNumber, BracketType bracketType, Match... matches) {
        return new Round(roundNumber, List.of(matches), bracketType);
    }

    public Match matchAt(int position) {
        return matches.find(m -> m.position() == position)
                .getOrElseThrow(() -> new EntityNotFoundException("No match at position " + position));
    }

    public boolean isComplete() {
        return matches.forAll(Match::isPlayed);
    }

    public int matchCount() {
        return matches.size();
    }
}
