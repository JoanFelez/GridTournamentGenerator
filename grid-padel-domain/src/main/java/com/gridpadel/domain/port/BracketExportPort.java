package com.gridpadel.domain.port;

import com.gridpadel.domain.model.Tournament;

import java.io.OutputStream;

public interface BracketExportPort {
    void exportToPdf(Tournament tournament, OutputStream outputStream);
}
