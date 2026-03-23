package com.gridpadel.domain.port;

import io.vavr.collection.List;
import io.vavr.control.Try;

import java.io.InputStream;

public interface PairImportPort {

    record ImportedPair(String player1, String player2, Integer seed) {}

    Try<List<ImportedPair>> importPairs(InputStream inputStream);

    boolean supports(String fileExtension);
}
