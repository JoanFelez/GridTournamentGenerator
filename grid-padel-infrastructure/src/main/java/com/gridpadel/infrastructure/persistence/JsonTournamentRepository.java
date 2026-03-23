package com.gridpadel.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gridpadel.domain.model.Tournament;
import com.gridpadel.domain.model.vo.TournamentId;
import com.gridpadel.domain.repository.TournamentRepository;
import com.gridpadel.infrastructure.persistence.dto.TournamentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class JsonTournamentRepository implements TournamentRepository {

    private final Path storageDir;
    private final ObjectMapper objectMapper;
    private final TournamentDtoMapper mapper;

    public JsonTournamentRepository(
            @Value("${gridpadel.storage.dir:#{systemProperties['user.home'] + '/.gridpadel/tournaments'}}") String storageDir,
            ObjectMapper objectMapper) {
        this.storageDir = Path.of(storageDir);
        this.objectMapper = objectMapper;
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper = new TournamentDtoMapper();
        ensureStorageDir();
    }

    // Package-private constructor for testing
    JsonTournamentRepository(Path storageDir, ObjectMapper objectMapper, TournamentDtoMapper mapper) {
        this.storageDir = storageDir;
        this.objectMapper = objectMapper;
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper = mapper;
        ensureStorageDir();
    }

    @Override
    public void save(Tournament tournament) {
        TournamentDto dto = mapper.toDto(tournament);
        Path file = tournamentFile(tournament.id());
        try {
            objectMapper.writeValue(file.toFile(), dto);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save tournament: " + tournament.id().value(), e);
        }
    }

    @Override
    public Optional<Tournament> findById(TournamentId id) {
        Path file = tournamentFile(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            TournamentDto dto = objectMapper.readValue(file.toFile(), TournamentDto.class);
            return Optional.of(mapper.fromDto(dto));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load tournament: " + id.value(), e);
        }
    }

    @Override
    public List<Tournament> findAll() {
        if (!Files.exists(storageDir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(storageDir)) {
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

    private Path tournamentFile(TournamentId id) {
        return storageDir.resolve(id.value() + ".json");
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
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + storageDir, e);
        }
    }
}
