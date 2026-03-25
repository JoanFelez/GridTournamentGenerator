package com.gridpadel.domain.service;

import com.gridpadel.domain.exception.InvalidOperationException;
import com.gridpadel.domain.exception.ValidationException;
import com.gridpadel.domain.model.*;
import com.gridpadel.domain.model.vo.PairId;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class BracketEditService {

    public void swapPairsInDraw(Tournament tournament, PairId pairId1, PairId pairId2) {
        if (pairId1.equals(pairId2)) {
            throw new InvalidOperationException("Cannot swap a pair with itself");
        }

        Round r1 = tournament.mainBracket().round(1)
                .getOrElseThrow(() -> new InvalidOperationException("Bracket not generated yet"));

        if (r1.matches().exists(Match::isPlayed)) {
            throw new InvalidOperationException("Cannot swap pairs after results have been entered in round 1");
        }

        MatchPairSlot slot1 = findPairSlot(r1, pairId1);
        MatchPairSlot slot2 = findPairSlot(r1, pairId2);

        Pair pair1 = slot1.pair();
        Pair pair2 = slot2.pair();

        setSlotPair(slot1, pair2);
        setSlotPair(slot2, pair1);

        clearByeAdvancementsFromR2(tournament);
        autoResolveByes(tournament.mainBracket());

        validateDrawIntegrity(tournament);
    }

    public void validateDrawIntegrity(Tournament tournament) {
        Round r1 = tournament.mainBracket().round(1)
                .getOrElseThrow(() -> new InvalidOperationException("Bracket not generated yet"));

        List<Pair> tournamentPairs = tournament.pairs();
        Set<PairId> expectedIds = HashSet.ofAll(tournamentPairs.map(Pair::id));

        List<Pair> drawPairs = r1.matches()
                .flatMap(m -> {
                    List<Pair> pairs = List.empty();
                    if (m.pair1() != null && !m.pair1().isBye()) pairs = pairs.append(m.pair1());
                    if (m.pair2() != null && !m.pair2().isBye()) pairs = pairs.append(m.pair2());
                    return pairs;
                });

        Set<PairId> drawIds = HashSet.ofAll(drawPairs.map(Pair::id));

        Set<PairId> missing = expectedIds.diff(drawIds);
        if (missing.nonEmpty()) {
            throw new ValidationException(
                    "Pairs missing from the draw: " + missing.size() + " pair(s)", "draw");
        }

        Set<PairId> extra = drawIds.diff(expectedIds);
        if (extra.nonEmpty()) {
            throw new ValidationException(
                    "Unknown pairs found in the draw: " + extra.size() + " pair(s)", "draw");
        }

        if (drawPairs.size() != drawIds.size()) {
            throw new ValidationException(
                    "Duplicate pairs found in the draw", "draw");
        }
    }

    private MatchPairSlot findPairSlot(Round r1, PairId pairId) {
        for (Match match : r1.matches()) {
            if (match.pair1() != null && match.pair1().id().equals(pairId)) {
                return new MatchPairSlot(match, true, match.pair1());
            }
            if (match.pair2() != null && match.pair2().id().equals(pairId)) {
                return new MatchPairSlot(match, false, match.pair2());
            }
        }
        throw new InvalidOperationException("Pair not found in round 1 draw");
    }

    private void setSlotPair(MatchPairSlot slot, Pair newPair) {
        if (slot.isPair1()) {
            slot.match().setPair1(newPair);
        } else {
            slot.match().setPair2(newPair);
        }
    }

    private void clearByeAdvancementsFromR2(Tournament tournament) {
        Bracket main = tournament.mainBracket();
        if (main.rounds().size() < 2) return;

        Round r1 = main.rounds().get(0);
        Round r2 = main.rounds().get(1);

        r1.matches()
                .filter(Match::isByeMatch)
                .forEach(match -> {
                    int nextPosition = match.position() / 2;
                    Match nextMatch = r2.matchAt(nextPosition);

                    if (match.position() % 2 == 0) {
                        nextMatch.setPair1(null);
                    } else {
                        nextMatch.setPair2(null);
                    }
                });
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

    private record MatchPairSlot(Match match, boolean isPair1, Pair pair) {}
}
