# Phase 4 Plan: UI Grid Rendering

## Prerequisites
- Branch from `develop` as `feature/ui-grid-rendering`
- All Phase 3 code merged (PRs #7, #8 ✅)

## Task 1: MatchBoxView Component
**Files:** `grid-padel-ui/src/main/java/com/gridpadel/ui/component/MatchBoxView.java`

Create a custom JavaFX VBox component that renders a single match:
- Pair 1 name and pair 2 name (or "BYE", "TBD" for empty slots)
- Score display (set results when played)
- Location and date-time (when set)
- Visual distinction for: BYE matches, played matches, pending matches
- Winner highlighting
- Fixed dimensions for consistent layout

## Task 2: BracketLayoutCalculator
**Files:** `grid-padel-ui/src/main/java/com/gridpadel/ui/layout/BracketLayoutCalculator.java`

Pure layout logic (no JavaFX dependency, testable):
- Calculate X,Y position for each match box given bracket structure
- Round 1 in center column, main bracket rounds flow right, consolation rounds flow left
- Vertical spacing doubles per round (matches align with their feeders)
- Returns a list of positioned match descriptors (match + x,y coordinates)
- Calculate connector line endpoints between rounds

## Task 3: BracketPane (Rendering)
**Files:** `grid-padel-ui/src/main/java/com/gridpadel/ui/component/BracketPane.java`

JavaFX Pane that renders the full bracket:
- Takes Tournament and uses BracketLayoutCalculator for positions
- Places MatchBoxView instances at calculated positions
- Draws connector lines between rounds
- Supports re-rendering when tournament state changes

## Task 4: MainView Integration
**Files:** `grid-padel-ui/src/main/java/com/gridpadel/ui/view/MainView.java`, modify `GridPadelApplication.java`

Replace the placeholder label with the bracket view:
- BorderPane layout with ScrollPane center containing BracketPane
- Zoom controls (Ctrl+scroll wheel)
- Wire Spring context to load/display a tournament
- Toolbar placeholder (for Phase 5 actions)

## Task 5: CSS Styling
**Files:** `grid-padel-ui/src/main/resources/css/bracket.css`

Style the bracket components:
- Match box styling (borders, backgrounds, fonts)
- BYE match styling (greyed out)
- Played match styling (winner highlighted)
- Connector line styling

## Task 6: Tests
**Files:** `grid-padel-ui/src/test/java/com/gridpadel/ui/layout/BracketLayoutCalculatorTest.java`

Unit tests for BracketLayoutCalculator:
- Position calculations for various bracket sizes (4, 8, 16, 32 pairs)
- Center column placement verification
- Main bracket flows right, consolation flows left
- Connector line endpoint calculations
- Edge cases: 1 pair, odd numbers, full 32

## Task 7: Commit, Push, PR
- Conventional commit messages
- Create PR to develop

## Success Criteria (from ROADMAP.md)
- [ ] First-round matches in center column
- [ ] Main bracket flows right with connecting lines
- [ ] Consolation bracket flows left with connecting lines
- [ ] Scrollable/zoomable canvas for large brackets
- [ ] Match boxes show pair names, location, date-time, result
