package com.gridpadel.application.service;

import com.gridpadel.domain.model.Match;
import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.*;
import com.gridpadel.domain.repository.TournamentRepository;
import com.gridpadel.domain.service.BracketGenerationService;
import com.gridpadel.domain.service.MatchAdvancementService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TournamentService implements ApplicationService {

    private final TournamentRepository repository;
    private final BracketGenerationService bracketGenerationService;
    private final MatchAdvancementService matchAdvancementService;

    public TournamentService(TournamentRepository repository,
                             BracketGenerationService bracketGenerationService,
                             MatchAdvancementService matchAdvancementService) {
        this.repository = repository;
        this.bracketGenerationService = bracketGenerationService;
        this.matchAdvancementService = matchAdvancementService;
    }

    public Tournament createTournament(String name) {
        Tournament tournament = Tournament.create(name);
        repository.save(tournament);
        return tournament;
    }

    public Tournament getTournament(TournamentId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + id.value()));
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

    private Match findMatch(Tournament tournament, MatchId matchId) {
        return tournament.allMatches().stream()
                .filter(m -> m.id().equals(matchId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId.value()));
    }
}
