package com.gridpadel.domain.service;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.BracketType;
import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.LinkedHashSet;

public class BracketGenerationService {

    public void generateMainBracket(Tournament tournament) {
        List<Pair> pairs = tournament.pairs();

        if (pairs.size() < 2) {
            throw new IllegalStateException("Tournament must have at least 2 pairs to generate a bracket");
        }
        if (!tournament.mainBracket().rounds().isEmpty()) {
            throw new IllegalStateException("Main bracket has already been generated");
        }

        int drawSize = nextPowerOf2(pairs.size());
        int totalRounds = log2(drawSize);
        int numMatches = drawSize / 2;
        int byeCount = drawSize - pairs.size();

        List<Pair> seeded = pairs
                .filter(Pair::isSeeded)
                .sortBy(p -> p.seed().getOrElse(Integer.MAX_VALUE));

        List<Pair> unseeded = pairs.filter(p -> !p.isSeeded());

        Array<Integer> seedPositions = Array.ofAll(standardSeedPositions(numMatches));

        Set<Integer> byePositions = LinkedHashSet.ofAll(seedPositions.take(byeCount));

        // Build R1 matches using mutable array (positional placement)
        Match[] r1 = new Match[numMatches];
        int seedIdx = 0;
        List<Pair> remainingUnseeded = unseeded;

        // Place seeded pairs at their standard positions
        for (int i = 0; i < seedPositions.size() && seedIdx < seeded.size(); i++) {
            int pos = seedPositions.get(i);
            Pair seed = seeded.get(seedIdx++);
            if (byePositions.contains(pos)) {
                r1[pos] = Match.create(seed, Pair.bye(), 1, pos, BracketType.MAIN);
            } else {
                Pair opponent = remainingUnseeded.last();
                remainingUnseeded = remainingUnseeded.dropRight(1);
                r1[pos] = Match.create(seed, opponent, 1, pos, BracketType.MAIN);
            }
        }

        // BYE matches without seeds
        for (Integer pos : byePositions) {
            if (r1[pos] == null) {
                Pair pair = remainingUnseeded.head();
                remainingUnseeded = remainingUnseeded.tail();
                r1[pos] = Match.create(pair, Pair.bye(), 1, pos, BracketType.MAIN);
            }
        }

        // Fill remaining with unseeded pairs
        for (int i = 0; i < numMatches; i++) {
            if (r1[i] == null) {
                Pair p1 = remainingUnseeded.head();
                remainingUnseeded = remainingUnseeded.tail();
                Pair p2 = remainingUnseeded.head();
                remainingUnseeded = remainingUnseeded.tail();
                r1[i] = Match.create(p1, p2, 1, i, BracketType.MAIN);
            }
        }

        tournament.mainBracket().addRound(
                Round.of(1, java.util.Arrays.asList(r1), BracketType.MAIN));

        // Create subsequent rounds with empty matches
        int matchesInRound = numMatches / 2;
        for (int roundNum = 2; roundNum <= totalRounds; roundNum++) {
            final int rn = roundNum;
            List<Match> matches = List.range(0, matchesInRound)
                    .map(pos -> Match.createEmpty(rn, pos, BracketType.MAIN));
            tournament.mainBracket().addRound(Round.of(roundNum, matches, BracketType.MAIN));
            matchesInRound /= 2;
        }

        autoResolveByes(tournament.mainBracket());

        generateConsolationBracket(tournament, numMatches);
    }

    private void generateConsolationBracket(Tournament tournament, int mainR1MatchCount) {
        int numConsolationMatches = mainR1MatchCount / 2;
        if (numConsolationMatches < 1) return;

        int matchesInRound = numConsolationMatches;
        int roundNum = 1;
        while (matchesInRound >= 1) {
            final int rn = roundNum;
            List<Match> matches = List.range(0, matchesInRound)
                    .map(pos -> Match.createEmpty(rn, pos, BracketType.CONSOLATION));
            tournament.consolationBracket().addRound(Round.of(roundNum, matches, BracketType.CONSOLATION));
            matchesInRound /= 2;
            roundNum++;
        }
    }

    private void autoResolveByes(Bracket bracket) {
        if (bracket.rounds().size() < 2) return;

        Round r1 = bracket.rounds().get(0);
        Round r2 = bracket.rounds().get(1);

        r1.matches()
                .filter(Match::isByeMatch)
                .forEach(match -> {
                    Pair advancing = match.pair1().isBye() ? match.pair2() : match.pair1();
                    int nextPosition = match.position() / 2;
                    Match nextMatch = r2.matchAt(nextPosition);

                    if (match.position() % 2 == 0) {
                        nextMatch.setPair1(advancing);
                    } else {
                        nextMatch.setPair2(advancing);
                    }
                });
    }

    Array<Integer> standardSeedPositions(int numMatches) {
        if (numMatches == 1) {
            return Array.of(0);
        }

        // Generate draw order: which seed number occupies each match position
        // Uses the standard tournament seeding algorithm where the sum of
        // paired seeds equals (round size + 1), ensuring top seeds meet last
        java.util.List<Integer> draw = new java.util.ArrayList<>(java.util.List.of(1, 2));

        while (draw.size() < numMatches) {
            int sum = draw.size() * 2 + 1;
            java.util.List<Integer> newDraw = new java.util.ArrayList<>();
            for (int i = 0; i < draw.size(); i++) {
                if (i % 2 == 0) {
                    newDraw.add(draw.get(i));
                    newDraw.add(sum - draw.get(i));
                } else {
                    newDraw.add(sum - draw.get(i));
                    newDraw.add(draw.get(i));
                }
            }
            draw = newDraw;
        }

        // Convert draw order to seed→position mapping
        Integer[] positions = new Integer[numMatches];
        for (int matchPos = 0; matchPos < draw.size(); matchPos++) {
            positions[draw.get(matchPos) - 1] = matchPos;
        }

        return Array.of(positions);
    }

    private int nextPowerOf2(int n) {
        if (n <= 2) return 2;
        return Integer.highestOneBit(n - 1) << 1;
    }

    private int log2(int n) {
        return (int) (Math.log(n) / Math.log(2));
    }
}
