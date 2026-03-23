package com.gridpadel.ui.layout;

import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.PlayerName;
import com.gridpadel.domain.service.BracketGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BracketLayoutCalculatorTest {

    private BracketLayoutCalculator calculator;
    private BracketGenerationService bracketService;

    @BeforeEach
    void setUp() {
        calculator = new BracketLayoutCalculator();
        bracketService = new BracketGenerationService();
    }

    @Test
    void shouldCalculateLayoutFor4Pairs() {
        Tournament t = createTournament(4);
        BracketLayout layout = calculator.calculate(t);

        long mainMatches = layout.matchPositions().stream()
                .filter(p -> !p.isConsolation())
                .count();
        assertThat(mainMatches).isEqualTo(3); // R1=2 + R2=1
    }

    @Test
    void shouldCalculateLayoutFor8Pairs() {
        Tournament t = createTournament(8);
        BracketLayout layout = calculator.calculate(t);

        assertThat(layout.matchPositions()).isNotEmpty();
        assertThat(layout.connectors()).isNotEmpty();
    }

    @Test
    void shouldPlaceR1InCenterColumn() {
        Tournament t = createTournament(4);
        BracketLayout layout = calculator.calculate(t);

        List<MatchPosition> r1Main = layout.matchPositions().stream()
                .filter(p -> !p.isConsolation() && p.roundNumber() == 1)
                .toList();

        assertThat(r1Main).isNotEmpty();
        double r1X = r1Main.get(0).x();
        assertThat(r1Main).allMatch(p -> p.x() == r1X);
    }

    @Test
    void shouldFlowMainBracketRight() {
        Tournament t = createTournament(8);
        BracketLayout layout = calculator.calculate(t);

        List<MatchPosition> mainPositions = layout.matchPositions().stream()
                .filter(p -> !p.isConsolation())
                .toList();

        if (mainPositions.size() > 1) {
            double r1X = mainPositions.stream()
                    .filter(p -> p.roundNumber() == 1)
                    .mapToDouble(MatchPosition::x)
                    .findFirst().orElse(0);

            mainPositions.stream()
                    .filter(p -> p.roundNumber() > 1)
                    .forEach(p -> assertThat(p.x())
                            .as("Main R%d should be right of R1", p.roundNumber())
                            .isGreaterThan(r1X));
        }
    }

    @Test
    void shouldCalculateLayoutForOddPairCount() {
        Tournament t = createTournament(5);
        BracketLayout layout = calculator.calculate(t);

        assertThat(layout.matchPositions()).isNotEmpty();
    }

    @Test
    void shouldCalculateLayoutFor16Pairs() {
        Tournament t = createTournament(16);
        BracketLayout layout = calculator.calculate(t);

        long r1Matches = layout.matchPositions().stream()
                .filter(p -> !p.isConsolation() && p.roundNumber() == 1)
                .count();
        assertThat(r1Matches).isEqualTo(8);
    }

    @Test
    void shouldHaveConnectorsBetweenRounds() {
        Tournament t = createTournament(8);
        BracketLayout layout = calculator.calculate(t);

        assertThat(layout.connectors()).isNotEmpty();
        layout.connectors().forEach(c -> {
            boolean isHorizontal = c.startY() == c.endY();
            boolean isVertical = c.startX() == c.endX();
            assertThat(isHorizontal || isVertical)
                    .as("Connector should be horizontal or vertical")
                    .isTrue();
        });
    }

    @Test
    void shouldAlignLaterRoundsVerticallyBetweenFeeders() {
        Tournament t = createTournament(4);
        BracketLayout layout = calculator.calculate(t);

        List<MatchPosition> r1 = layout.matchPositions().stream()
                .filter(p -> !p.isConsolation() && p.roundNumber() == 1)
                .sorted((a, b) -> Double.compare(a.y(), b.y()))
                .toList();

        List<MatchPosition> r2 = layout.matchPositions().stream()
                .filter(p -> !p.isConsolation() && p.roundNumber() == 2)
                .toList();

        if (r1.size() >= 2 && !r2.isEmpty()) {
            double expectedY = (r1.get(0).y() + r1.get(1).y()) / 2;
            assertThat(r2.get(0).y()).isCloseTo(expectedY, within(1.0));
        }
    }

    @Test
    void shouldHandleEmptyBracket() {
        Tournament t = Tournament.create("Empty");
        BracketLayout layout = calculator.calculate(t);

        assertThat(layout.matchPositions()).isEmpty();
        assertThat(layout.connectors()).isEmpty();
    }

    @Test
    void shouldCalculateLayoutFor32Pairs() {
        Tournament t = createTournament(32);
        BracketLayout layout = calculator.calculate(t);

        long r1Matches = layout.matchPositions().stream()
                .filter(p -> !p.isConsolation() && p.roundNumber() == 1)
                .count();
        assertThat(r1Matches).isEqualTo(16);
        assertThat(layout.matchPositions().size()).isGreaterThan(16);
    }

    private Tournament createTournament(int pairCount) {
        Tournament t = Tournament.create("Test " + pairCount);
        for (int i = 0; i < pairCount; i++) {
            t.addPair(Pair.create(
                    PlayerName.of("P" + (i * 2 + 1)),
                    PlayerName.of("P" + (i * 2 + 2))
            ));
        }
        bracketService.generateMainBracket(t);
        return t;
    }
}
