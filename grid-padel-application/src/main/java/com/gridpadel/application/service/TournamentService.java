package com.gridpadel.application.service;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.*;
import com.gridpadel.domain.port.BracketExportPort;
import com.gridpadel.domain.port.PairImportPort;
import com.gridpadel.domain.port.PairImportPort.ImportedPair;
import com.gridpadel.domain.repository.TournamentRepository;
import com.gridpadel.domain.service.BracketEditService;
import com.gridpadel.domain.service.BracketGenerationService;
import com.gridpadel.domain.service.MatchAdvancementService;
import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TournamentService implements ApplicationService {

    private final TournamentRepository repository;
    private final BracketGenerationService bracketGenerationService;
    private final MatchAdvancementService matchAdvancementService;
    private final BracketEditService bracketEditService;
    private final java.util.List<PairImportPort> importers;
    private final BracketExportPort bracketExportPort;

    public Tournament createTournament(String name) {
        Tournament tournament = Tournament.create(name);
        repository.save(tournament);
        return tournament;
    }

    public Tournament getTournament(TournamentId id) {
        return repository.findById(id)
                .getOrElseThrow(() -> new IllegalArgumentException("Tournament not found: " + id.value()));
    }

    public List<Tournament> listTournaments() {
        return repository.findAll();
    }

    public Pair addPair(TournamentId tournamentId, String player1, String player2) {
        Tournament tournament = getTournament(tournamentId);
        Pair pair = Pair.create(PlayerName.of(player1), PlayerName.of(player2));
        tournament.addPair(pair);
        repository.save(tournament);
        return pair;
    }

    public Pair addSeededPair(TournamentId tournamentId, String player1, String player2, int seed) {
        Tournament tournament = getTournament(tournamentId);
        Pair pair = Pair.create(PlayerName.of(player1), PlayerName.of(player2));
        pair.assignSeed(seed);
        tournament.addPair(pair);
        repository.save(tournament);
        return pair;
    }

    public void removePair(TournamentId tournamentId, PairId pairId) {
        Tournament tournament = getTournament(tournamentId);
        tournament.removePair(pairId);
        repository.save(tournament);
    }

    public void clearBrackets(TournamentId tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        tournament.clearBrackets();
        repository.save(tournament);
    }

    public void generateBracket(TournamentId tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        bracketGenerationService.generateMainBracket(tournament);
        repository.save(tournament);
    }

    public void recordMatchResult(TournamentId tournamentId, MatchId matchId, MatchResult result) {
        Tournament tournament = getTournament(tournamentId);
        matchAdvancementService.processMatchResult(tournament, matchId, result);
        repository.save(tournament);
    }

    public void clearMatchResult(TournamentId tournamentId, MatchId matchId) {
        Tournament tournament = getTournament(tournamentId);
        matchAdvancementService.clearMatchResult(tournament, matchId);
        repository.save(tournament);
    }

    public void swapDrawPairs(TournamentId tournamentId, PairId pairId1, PairId pairId2) {
        Tournament tournament = getTournament(tournamentId);
        bracketEditService.swapPairsInDraw(tournament, pairId1, pairId2);
        repository.save(tournament);
    }

    public void updateTournamentName(TournamentId tournamentId, String newName) {
        Tournament tournament = getTournament(tournamentId);
        tournament.updateName(newName);
        repository.save(tournament);
    }

    public void deleteTournament(TournamentId id) {
        repository.delete(id);
    }

    public void setMatchLocation(TournamentId tournamentId, MatchId matchId, String location) {
        Tournament tournament = getTournament(tournamentId);
        Match match = findMatch(tournament, matchId);
        match.setLocation(Location.of(location));
        repository.save(tournament);
    }

    public void setMatchDateTime(TournamentId tournamentId, MatchId matchId, LocalDateTime dateTime) {
        Tournament tournament = getTournament(tournamentId);
        Match match = findMatch(tournament, matchId);
        match.setDateTime(MatchDateTime.of(dateTime));
        repository.save(tournament);
    }

    public Try<List<ImportedPair>> parsePairsFromFile(InputStream inputStream, String fileExtension) {
        PairImportPort importer = List.ofAll(importers)
                .find(i -> i.supports(fileExtension))
                .getOrElseThrow(() -> new IllegalArgumentException(
                        "Unsupported file format: " + fileExtension + ". Supported formats: CSV, XLS, XLSX"));

        return importer.importPairs(inputStream);
    }

    public void exportToPdf(TournamentId tournamentId, OutputStream outputStream) {
        Tournament tournament = getTournament(tournamentId);
        bracketExportPort.exportToPdf(tournament, outputStream);
    }

    private Match findMatch(Tournament tournament, MatchId matchId) {
        return tournament.allMatches()
                .find(m -> m.id().equals(matchId))
                .getOrElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId.value()));
    }
}
