package com.gridpadel.infrastructure.importer;

import com.gridpadel.domain.port.PairImportPort.ImportedPair;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CsvPairImporterTest {

    private CsvPairImporter importer;

    @BeforeEach
    void setUp() {
        importer = new CsvPairImporter();
    }

    @Test
    void supports_csv_extension() {
        assertThat(importer.supports("csv")).isTrue();
        assertThat(importer.supports("CSV")).isTrue();
        assertThat(importer.supports("xls")).isFalse();
    }

    @Test
    void imports_pairs_without_header() {
        String csv = "Alice,Bob\nCharlie,Diana\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isSuccess()).isTrue();
        List<ImportedPair> pairs = result.get();
        assertThat(pairs).hasSize(2);
        assertThat(pairs.get(0).player1()).isEqualTo("Alice");
        assertThat(pairs.get(0).player2()).isEqualTo("Bob");
        assertThat(pairs.get(0).seed()).isNull();
    }

    @Test
    void imports_pairs_with_header() {
        String csv = "Player1,Player2,Seed\nAlice,Bob,1\nCharlie,Diana,2\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isSuccess()).isTrue();
        List<ImportedPair> pairs = result.get();
        assertThat(pairs).hasSize(2);
        assertThat(pairs.get(0).seed()).isEqualTo(1);
        assertThat(pairs.get(1).seed()).isEqualTo(2);
    }

    @Test
    void imports_pairs_with_spanish_header() {
        String csv = "Jugador1,Jugador2,Seed\nAlice,Bob\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).hasSize(1);
    }

    @Test
    void imports_pairs_with_optional_seed() {
        String csv = "Alice,Bob,3\nCharlie,Diana\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isSuccess()).isTrue();
        List<ImportedPair> pairs = result.get();
        assertThat(pairs.get(0).seed()).isEqualTo(3);
        assertThat(pairs.get(1).seed()).isNull();
    }

    @Test
    void skips_empty_rows() {
        String csv = "Alice,Bob\n\n,,\nCharlie,Diana\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).hasSize(2);
    }

    @Test
    void fails_on_empty_file() {
        Try<List<ImportedPair>> result = importer.importPairs(toStream(""));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("empty");
    }

    @Test
    void fails_on_missing_player2() {
        String csv = "Alice\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("Row 1");
    }

    @Test
    void fails_on_empty_player1_name() {
        String csv = ",Bob\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("Player 1 name is empty");
    }

    @Test
    void fails_on_invalid_seed() {
        String csv = "Alice,Bob,abc\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("Invalid seed");
    }

    @Test
    void fails_on_negative_seed() {
        String csv = "Alice,Bob,-1\n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("positive number");
    }

    @Test
    void trims_whitespace_from_names() {
        String csv = "  Alice  ,  Bob  \n";
        Try<List<ImportedPair>> result = importer.importPairs(toStream(csv));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get().get(0).player1()).isEqualTo("Alice");
        assertThat(result.get().get(0).player2()).isEqualTo("Bob");
    }

    private InputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
