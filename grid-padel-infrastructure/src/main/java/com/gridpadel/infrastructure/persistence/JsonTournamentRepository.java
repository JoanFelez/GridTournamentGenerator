package com.gridpadel.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.TournamentId;
import com.gridpadel.domain.repository.TournamentRepository;
import com.gridpadel.infrastructure.persistence.dto.TournamentDto;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class JsonTournamentRepository implements TournamentRepository {

    @Value("${gridpadel.storage.dir:#{systemProperties['user.home'] + '/.gridpadel/tournaments'}}")
    private final String storageDir;
    private final ObjectMapper objectMapper;
    private final TournamentDtoMapper mapper;

    @PostConstruct
    void init() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        ensureStorageDir();
    }

    @Override
    public void save(Tournament tournament) {
        TournamentDto dto = mapper.toDto(tournament);
        Path file = tournamentFile(tournament.id());
        Try.run(() -> objectMapper.writeValue(file.toFile(), dto))
                .getOrElseThrow(e -> new RuntimeException("Failed to save tournament: " + tournament.id().value(), e));
    }

    @Override
    public Option<Tournament> findById(TournamentId id) {
        Path file = tournamentFile(id);
        if (!Files.exists(file)) {
            return Option.none();
        }
        return Option.of(
                Try.of(() -> objectMapper.readValue(file.toFile(), TournamentDto.class))
                        .map(mapper::fromDto)
                        .getOrElseThrow(e -> new RuntimeException("Failed to load tournament: " + id.value(), e))
        );
    }

    @Override
    public List<Tournament> findAll() {
        Path dir = storagePath();
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(f -> f.toString().endsWith(".json"))
                    .map(this::loadTournament)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list tournaments", e);
        }
    }

    @Override
    public void delete(TournamentId id) {
        Path file = tournamentFile(id);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete tournament: " + id.value(), e);
        }
    }

    private Path storagePath() {
        return Path.of(storageDir);
    }

    private Path tournamentFile(TournamentId id) {
        return storagePath().resolve(id.value() + ".json");
    }

    private Tournament loadTournament(Path file) {
        try {
            TournamentDto dto = objectMapper.readValue(file.toFile(), TournamentDto.class);
            return mapper.fromDto(dto);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load tournament from: " + file, e);
        }
    }

    private void ensureStorageDir() {
        try {
            Files.createDirectories(storagePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + storageDir, e);
        }
    }
}
