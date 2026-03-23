package com.gridpadel.domain.service;

import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.BracketType;

import java.util.*;
import java.util.stream.Collectors;

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

        List<Pair> seeded = pairs.stream()
                .filter(Pair::isSeeded)
                .sorted(Comparator.comparingInt(p -> p.seed().getOrElse(Integer.MAX_VALUE)))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Pair> unseeded = pairs.stream()
                .filter(p -> !p.isSeeded())
                .collect(Collectors.toCollection(ArrayList::new));

        int[] seedMatchPositions = standardSeedMatchPositions(numMatches);

        // Determine which match positions get BYEs (top seed positions first)
        Set<Integer> byePositions = new LinkedHashSet<>();
        for (int i = 0; i < byeCount && i < seedMatchPositions.length; i++) {
            byePositions.add(seedMatchPositions[i]);
        }

        // Build R1 matches
        Match[] r1 = new Match[numMatches];
        int seedIdx = 0;

        // Place seeded pairs at their standard positions
        for (int i = 0; i < seedMatchPositions.length && seedIdx < seeded.size(); i++) {
            int pos = seedMatchPositions[i];
            Pair seed = seeded.get(seedIdx++);
            if (byePositions.contains(pos)) {
                r1[pos] = Match.create(seed, Pair.bye(), 1, pos, BracketType.MAIN);
            } else {
                Pair opponent = unseeded.remove(unseeded.size() - 1);
                r1[pos] = Match.create(seed, opponent, 1, pos, BracketType.MAIN);
            }
        }

        // BYE matches without seeds: pair unseeded pair with BYE
        for (int pos : byePositions) {
            if (r1[pos] == null) {
                Pair pair = unseeded.remove(0);
                r1[pos] = Match.create(pair, Pair.bye(), 1, pos, BracketType.MAIN);
            }
        }

        // Fill remaining matches with unseeded pairs
        for (int i = 0; i < numMatches; i++) {
            if (r1[i] == null) {
                Pair p1 = unseeded.remove(0);
                Pair p2 = unseeded.remove(0);
                r1[i] = Match.create(p1, p2, 1, i, BracketType.MAIN);
            }
        }

        tournament.mainBracket().addRound(
                Round.of(1, Arrays.asList(r1), BracketType.MAIN));

        // Create subsequent rounds with empty matches
        int matchesInRound = numMatches / 2;
        for (int round = 2; round <= totalRounds; round++) {
            List<Match> matches = new ArrayList<>();
            for (int pos = 0; pos < matchesInRound; pos++) {
                matches.add(Match.createEmpty(round, pos, BracketType.MAIN));
            }
            tournament.mainBracket().addRound(
                    Round.of(round, matches, BracketType.MAIN));
            matchesInRound /= 2;
        }

        autoResolveByes(tournament.mainBracket());
    }

    private void autoResolveByes(Bracket bracket) {
        if (bracket.rounds().size() < 2) return;

        Round r1 = bracket.rounds().get(0);
        Round r2 = bracket.rounds().get(1);

        for (Match match : r1.matches()) {
            if (match.isByeMatch()) {
                Pair advancing = match.pair1().isBye() ? match.pair2() : match.pair1();
                int nextPosition = match.position() / 2;
                Match nextMatch = r2.matchAt(nextPosition);

                if (match.position() % 2 == 0) {
                    nextMatch.setPair1(advancing);
                } else {
                    nextMatch.setPair2(advancing);
                }
            }
        }
    }

    /**
     * Standard seed match positions using BPP algorithm.
     * Returns match positions in seed order (index 0 = seed 1's match, etc.)
     */
    int[] standardSeedMatchPositions(int numMatches) {
        List<Integer> positions = new ArrayList<>();
        positions.add(0);

        if (numMatches == 1) {
            return new int[]{0};
        }

        positions.add(1);

        int currentSize = 2;
        while (currentSize < numMatches) {
            List<Integer> expanded = new ArrayList<>();
            int nextSize = currentSize * 2;
            for (int p : positions) {
                expanded.add(p);
                expanded.add(nextSize - 1 - p);
            }
            positions = expanded;
            currentSize = nextSize;
        }

        return positions.stream().mapToInt(Integer::intValue).toArray();
    }

    private int nextPowerOf2(int n) {
        if (n <= 2) return 2;
        return Integer.highestOneBit(n - 1) << 1;
    }

    private int log2(int n) {
        return (int) (Math.log(n) / Math.log(2));
    }
}
