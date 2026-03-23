package com.gridpadel.domain.repository;

import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.TournamentId;
import io.vavr.control.Option;

import java.util.List;

public interface TournamentRepository {

    void save(Tournament tournament);

    Option<Tournament> findById(TournamentId id);

    List<Tournament> findAll();

    void delete(TournamentId id);
}
