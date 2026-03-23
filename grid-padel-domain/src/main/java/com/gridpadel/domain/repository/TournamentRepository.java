package com.gridpadel.domain.repository;

import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.TournamentId;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository {

    void save(Tournament tournament);

    Optional<Tournament> findById(TournamentId id);

    List<Tournament> findAll();

    void delete(TournamentId id);
}
