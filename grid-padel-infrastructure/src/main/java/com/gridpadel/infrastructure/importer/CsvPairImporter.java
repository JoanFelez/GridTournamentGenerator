package com.gridpadel.infrastructure.importer;

import com.gridpadel.domain.port.PairImportPort;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class CsvPairImporter implements PairImportPort {

    @Override
    public Try<List<ImportedPair>> importPairs(InputStream inputStream) {
        return Try.of(() -> {
            try (CSVReader reader = new CSVReaderBuilder(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)).build()) {

                java.util.List<String[]> rows = reader.readAll();
                if (rows.isEmpty()) {
                    throw new IllegalArgumentException("CSV file is empty");
                }

                int startRow = isHeaderRow(rows.get(0)) ? 1 : 0;

                return List.ofAll(rows.subList(startRow, rows.size()))
                        .filter(row -> !isEmptyRow(row))
                        .zipWithIndex()
                        .map(tuple -> parseRow(tuple._1, tuple._2.intValue() + startRow + 1));
            }
        });
    }

    @Override
    public boolean supports(String fileExtension) {
        return "csv".equalsIgnoreCase(fileExtension);
    }

    private boolean isHeaderRow(String[] row) {
        if (row.length == 0) return false;
        String first = row[0].trim().toLowerCase();
        return first.equals("player1") || first.equals("player 1")
                || first.equals("jugador1") || first.equals("jugador 1")
                || first.equals("nombre1") || first.equals("name1");
    }

    private boolean isEmptyRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) return false;
        }
        return true;
    }

    private ImportedPair parseRow(String[] row, int rowNumber) {
        if (row.length < 2) {
            throw new IllegalArgumentException(
                    "Row " + rowNumber + ": expected at least 2 columns (Player1, Player2), got " + row.length);
        }

        String player1 = row[0].trim();
        String player2 = row[1].trim();

        if (player1.isEmpty()) {
            throw new IllegalArgumentException("Row " + rowNumber + ": Player 1 name is empty");
        }
        if (player2.isEmpty()) {
            throw new IllegalArgumentException("Row " + rowNumber + ": Player 2 name is empty");
        }

        Integer seed = null;
        if (row.length >= 3 && !row[2].trim().isEmpty()) {
            try {
                seed = Integer.parseInt(row[2].trim());
                if (seed <= 0) {
                    throw new IllegalArgumentException("Row " + rowNumber + ": Seed must be a positive number, got " + seed);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Row " + rowNumber + ": Invalid seed value '" + row[2].trim() + "'");
            }
        }

        return new ImportedPair(player1, player2, seed);
    }
}
