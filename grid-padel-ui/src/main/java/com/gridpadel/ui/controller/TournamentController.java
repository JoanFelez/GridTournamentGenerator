package com.gridpadel.ui.controller;

import com.gridpadel.application.service.TournamentService;
import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.ui.dialog.*;
import com.gridpadel.ui.view.MainView;
import com.gridpadel.domain.model.vo.BracketType;
import io.vavr.collection.List;
import io.vavr.control.Try;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private MainView mainView;
    private Tournament currentTournament;

    public void setMainView(MainView mainView) {
        this.mainView = mainView;
    }

    public Tournament currentTournament() {
        return currentTournament;
    }

    public void createNewTournament() {
        TournamentDialog.showCreate().ifPresent(data ->
                Try.run(() -> {
                    currentTournament = tournamentService.createTournament(data.name());
                    mainView.updateTitle(currentTournament.name());
                    refreshTournamentList();
                    managePairs();
                }).onFailure(e -> showError("Error creating tournament", e.getMessage()))
        );
    }

    public void editTournamentName() {
        if (currentTournament == null) return;
        TournamentDialog.showEdit(currentTournament.name()).ifPresent(data ->
                Try.run(() -> {
                    tournamentService.updateTournamentName(currentTournament.id(), data.name());
                    refreshTournament();
                    mainView.updateTitle(currentTournament.name());
                    refreshTournamentList();
                }).onFailure(e -> showError("Error updating tournament", e.getMessage()))
        );
    }

    public void managePairs() {
        if (currentTournament == null) return;
        PairManagementDialog.show(currentTournament.pairs().toJavaList(), this::parseImportFile).ifPresent(entries ->
                Try.run(() -> {
                    syncPairs(entries);
                    refreshTournament();
                    refreshDisplay();
                    refreshTournamentList();
                }).onFailure(e -> showError("Error managing pairs", e.getMessage()))
        );
    }

    private java.util.List<PairManagementDialog.PairEntry> parseImportFile(java.io.File file) {
        String name = file.getName();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";

        return Try.withResources(() -> new java.io.FileInputStream(file))
                .of(is -> tournamentService.parsePairsFromFile(is, ext))
                .flatMap(result -> result)
                .map(importedPairs -> importedPairs.map(ip ->
                        new PairManagementDialog.PairEntry(ip.player1(), ip.player2(), ip.seed(), true))
                        .toJavaList())
                .getOrElseThrow(e -> new RuntimeException(e.getMessage()));
    }

    public void generateBracket() {
        if (currentTournament == null) return;
        if (currentTournament.pairCount() < 2) {
            showError("Cannot generate bracket", "At least 2 pairs are required.");
            return;
        }
        Try.run(() -> {
            tournamentService.generateBracket(currentTournament.id());
            refreshTournament();
            refreshDisplay();
        }).onFailure(e -> showError("Error generating bracket", e.getMessage()));
    }

    public void onMatchClicked(Match match) {
        if (currentTournament == null) return;

        if (!match.isComplete()) {
            showMatchDetails(match);
            return;
        }

        boolean isR1Main = match.bracketType() == BracketType.MAIN
                && match.roundNumber() == 1
                && !match.isPlayed()
                && !match.isByeMatch();

        Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        choiceAlert.setTitle("Match Action");
        choiceAlert.setHeaderText(buildMatchHeader(match));

        ButtonType resultBtn = new ButtonType("Enter Result");
        ButtonType detailsBtn = new ButtonType("Edit Details");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());

        if (isR1Main) {
            ButtonType swapBtn = new ButtonType("Swap Pair");
            choiceAlert.getButtonTypes().setAll(resultBtn, detailsBtn, swapBtn, cancelBtn);
            choiceAlert.showAndWait().ifPresent(chosen -> {
                if (chosen == resultBtn) {
                    showResultDialog(match);
                } else if (chosen == detailsBtn) {
                    showMatchDetails(match);
                } else if (chosen == swapBtn) {
                    showSwapPairDialog(match);
                }
            });
        } else {
            choiceAlert.getButtonTypes().setAll(resultBtn, detailsBtn, cancelBtn);
            choiceAlert.showAndWait().ifPresent(chosen -> {
                if (chosen == resultBtn) {
                    showResultDialog(match);
                } else if (chosen == detailsBtn) {
                    showMatchDetails(match);
                }
            });
        }
    }

    public void showResultDialog(Match match) {
        if (currentTournament == null || !match.isComplete()) return;

        MatchResultDialog.show(match).ifPresent(action ->
                Try.run(() -> {
                    switch (action) {
                        case MatchResultDialog.SaveResult save ->
                                tournamentService.recordMatchResult(currentTournament.id(), match.id(), save.result());
                        case MatchResultDialog.ClearResult ignored ->
                                tournamentService.clearMatchResult(currentTournament.id(), match.id());
                    }
                    refreshTournament();
                    refreshDisplay();
                }).onFailure(e -> showError("Invalid result", e.getMessage()))
        );
    }

    public void showMatchDetails(Match match) {
        if (currentTournament == null) return;

        MatchDetailsDialog.show(match).ifPresent(details ->
                Try.run(() -> {
                    if (details.location() != null) {
                        tournamentService.setMatchLocation(currentTournament.id(), match.id(), details.location());
                    }
                    if (details.dateTime() != null) {
                        tournamentService.setMatchDateTime(currentTournament.id(), match.id(), details.dateTime());
                    }
                    refreshTournament();
                    refreshDisplay();
                }).onFailure(e -> showError("Error updating match details", e.getMessage()))
        );
    }

    public void showSwapPairDialog(Match match) {
        if (currentTournament == null) return;

        currentTournament.mainBracket().round(1).forEach(r1 ->
                SwapPairDialog.show(match, r1).ifPresent(swap ->
                        Try.run(() -> {
                            tournamentService.swapDrawPairs(
                                    currentTournament.id(),
                                    swap.selectedPair().id(),
                                    swap.targetPair().id());
                            refreshTournament();
                            refreshDisplay();
                            showInfo("Swap Complete", "Pairs swapped successfully.");
                        }).onFailure(e -> showError("Swap failed", e.getMessage()))
                )
        );
    }

    public void openTournament(Tournament tournament) {
        currentTournament = tournamentService.getTournament(tournament.id());
        mainView.updateTitle(currentTournament.name());
        refreshDisplay();
        refreshTournamentList();
    }

    public void refreshTournamentList() {
        Try.of(() -> tournamentService.listTournaments())
                .onSuccess(tournaments -> mainView.updateTournamentList(tournaments.toJavaList()))
                .onFailure(e -> showError("Error loading tournaments", e.getMessage()));
    }

    public void openTournamentHistory() {
        Try.of(() -> tournamentService.listTournaments())
                .onSuccess(tournaments -> {
                    if (tournaments.isEmpty()) {
                        showInfo("No Tournaments", "No saved tournaments found. Create a new one.");
                        return;
                    }
                    TournamentHistoryDialog.show(tournaments.toJavaList()).ifPresent(result -> {
                        switch (result.action()) {
                            case OPEN -> openTournament(result.tournament());
                            case DELETE -> {
                                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                        "Delete tournament '" + result.tournament().name() + "'?");
                                confirm.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
                                    tournamentService.deleteTournament(result.tournament().id());
                                    if (currentTournament != null &&
                                            currentTournament.id().equals(result.tournament().id())) {
                                        currentTournament = null;
                                        mainView.clearDisplay();
                                    }
                                    refreshTournamentList();
                                });
                            }
                        }
                    });
                })
                .onFailure(e -> showError("Error loading tournaments", e.getMessage()));
    }

    public void saveTournament() {
        if (currentTournament == null) {
            showInfo("Nothing to save", "No tournament is currently open.");
            return;
        }
        showInfo("Saved", "Tournament '" + currentTournament.name() + "' saved successfully.");
    }

    private void syncPairs(java.util.List<PairManagementDialog.PairEntry> entries) {
        List.ofAll(currentTournament.pairs())
                .forEach(p -> tournamentService.removePair(currentTournament.id(), p.id()));
        refreshTournament();

        List.ofAll(entries).forEach(entry -> {
            if (entry.seed() != null) {
                tournamentService.addSeededPair(currentTournament.id(),
                        entry.player1(), entry.player2(), entry.seed());
            } else {
                tournamentService.addPair(currentTournament.id(),
                        entry.player1(), entry.player2());
            }
        });
    }

    private void refreshTournament() {
        if (currentTournament != null) {
            currentTournament = tournamentService.getTournament(currentTournament.id());
        }
    }

    private void refreshDisplay() {
        if (mainView != null && currentTournament != null) {
            mainView.displayTournament(currentTournament);
        }
    }

    private String buildMatchHeader(Match match) {
        String p1 = match.pair1() != null ? match.pair1().displayName() : "TBD";
        String p2 = match.pair2() != null ? match.pair2().displayName() : "TBD";
        return p1 + "  vs  " + p2;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
