package com.gridpadel.domain.service;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.*;

import java.util.ArrayList;
import java.util.List;

public class MatchAdvancementService {

    public void processMatchResult(Tournament tournament, MatchId matchId, MatchResult result) {
        Match match = findMatch(tournament, matchId);

        if (!match.isComplete()) {
            throw new IllegalStateException("Cannot record result for incomplete match — both pairs must be set");
        }

        match.recordResult(result);

        Pair winner = match.winner().getOrElseThrow(() ->
                new IllegalStateException("Match has result but no winner"));
        Pair loser = match.loser().getOrElseThrow(() ->
                new IllegalStateException("Match has result but no loser"));

        advanceWinner(tournament, match, winner);

        if (match.bracketType() == BracketType.MAIN && match.roundNumber() == 1) {
            routeLoserToConsolation(tournament, match, loser);
        }
    }

    public void clearMatchResult(Tournament tournament, MatchId matchId) {
        Match match = findMatch(tournament, matchId);

        if (!match.isPlayed()) return;

        Pair previousWinner = match.winner().getOrNull();

        match.clearResult();

        if (previousWinner != null) {
            removeFromNextRound(tournament, match, previousWinner);
        }

        if (match.bracketType() == BracketType.MAIN && match.roundNumber() == 1) {
            removeFromConsolation(tournament, match);
        }
    }

    private void advanceWinner(Tournament tournament, Match match, Pair winner) {
        Bracket bracket = match.bracketType() == BracketType.MAIN
                ? tournament.mainBracket()
                : tournament.consolationBracket();

        int nextRoundNumber = match.roundNumber() + 1;
        bracket.round(nextRoundNumber).forEach(nextRound -> {
            int nextPosition = match.position() / 2;
            Match nextMatch = nextRound.matchAt(nextPosition);

            if (match.position() % 2 == 0) {
                nextMatch.setPair1(winner);
            } else {
                nextMatch.setPair2(winner);
            }
        });
    }

    private void routeLoserToConsolation(Tournament tournament, Match match, Pair loser) {
        Bracket consolation = tournament.consolationBracket();

        int consolationPosition = match.position() / 2;
        int consolationRound = 1;

        if (consolation.round(consolationRound).isEmpty()) {
            int numConsolationMatches = tournament.mainBracket().rounds().get(0).matchCount() / 2;
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < numConsolationMatches; i++) {
                matches.add(Match.createEmpty(consolationRound, i, BracketType.CONSOLATION));
            }
            consolation.addRound(Round.of(consolationRound, matches, BracketType.CONSOLATION));
        }

        Round consolationR1 = consolation.round(consolationRound).getOrElseThrow(() ->
                new IllegalStateException("Consolation round 1 should exist"));
        Match consolationMatch = consolationR1.matchAt(consolationPosition);

        if (match.position() % 2 == 0) {
            consolationMatch.setPair1(loser);
        } else {
            consolationMatch.setPair2(loser);
        }
    }

    private void removeFromNextRound(Tournament tournament, Match match, Pair pair) {
        Bracket bracket = match.bracketType() == BracketType.MAIN
                ? tournament.mainBracket()
                : tournament.consolationBracket();

        int nextRoundNumber = match.roundNumber() + 1;
        bracket.round(nextRoundNumber).forEach(nextRound -> {
            int nextPosition = match.position() / 2;
            Match nextMatch = nextRound.matchAt(nextPosition);

            if (nextMatch.pair1() != null && nextMatch.pair1().equals(pair)) {
                nextMatch.setPair1(null);
            }
            if (nextMatch.pair2() != null && nextMatch.pair2().equals(pair)) {
                nextMatch.setPair2(null);
            }
        });
    }

    private void removeFromConsolation(Tournament tournament, Match match) {
        Bracket consolation = tournament.consolationBracket();
        int consolationPosition = match.position() / 2;

        consolation.round(1).forEach(cr1 -> {
            Match consolationMatch = cr1.matchAt(consolationPosition);
            if (match.position() % 2 == 0) {
                consolationMatch.setPair1(null);
            } else {
                consolationMatch.setPair2(null);
            }
        });
    }

    private Match findMatch(Tournament tournament, MatchId matchId) {
        return io.vavr.collection.List.ofAll(tournament.allMatches())
                .find(m -> m.id().equals(matchId))
                .getOrElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
    }
}
