package com.gridpadel.domain.model;

import com.gridpadel.domain.exception.EntityNotFoundException;
import com.gridpadel.domain.exception.ValidationException;
import com.gridpadel.domain.model.vo.BracketType;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;

@Getter
public class Round {

    private final int roundNumber;
    @Getter(AccessLevel.NONE) private final List<Match> matches;
    private final BracketType bracketType;

    private Round(int roundNumber, List<Match> matches, BracketType bracketType) {
        if (matches == null || matches.isEmpty()) {
            throw new ValidationException("Round must have at least one match", "matches");
        }
        this.roundNumber = roundNumber;
        this.matches = new ArrayList<>(matches);
        this.bracketType = Objects.requireNonNull(bracketType);
    }

    public static Round of(int roundNumber, List<Match> matches, BracketType bracketType) {
        return new Round(roundNumber, matches, bracketType);
    }

    public List<Match> matches() {
        return Collections.unmodifiableList(matches);
    }

    public Match matchAt(int position) {
        return matches.stream()
                .filter(m -> m.position() == position)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No match at position " + position));
    }

    public boolean isComplete() {
        return matches.stream().allMatch(Match::isPlayed);
    }

    public int matchCount() {
        return matches.size();
    }
}
