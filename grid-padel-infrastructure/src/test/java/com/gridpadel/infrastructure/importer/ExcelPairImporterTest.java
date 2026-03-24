package com.gridpadel.infrastructure.importer;

import com.gridpadel.domain.port.PairImportPort.ImportedPair;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelPairImporterTest {

    private ExcelPairImporter importer;

    @BeforeEach
    void setUp() {
        importer = new ExcelPairImporter();
    }

    @Test
    void supports_excel_extensions() {
        assertThat(importer.supports("xls")).isTrue();
        assertThat(importer.supports("xlsx")).isTrue();
        assertThat(importer.supports("XLS")).isTrue();
        assertThat(importer.supports("csv")).isFalse();
    }

    @Test
    void imports_pairs_from_xlsx_without_header() throws IOException {
        byte[] data = createXlsx(false, new String[][]{
                {"Alice", "Bob"},
                {"Charlie", "Diana"}
        });

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(data));

        assertThat(result.isSuccess()).isTrue();
        List<ImportedPair> pairs = result.get();
        assertThat(pairs).hasSize(2);
        assertThat(pairs.get(0).player1()).isEqualTo("Alice");
        assertThat(pairs.get(0).player2()).isEqualTo("Bob");
    }

    @Test
    void imports_pairs_from_xlsx_with_header_and_seed() throws IOException {
        byte[] data = createXlsx(true, new String[][]{
                {"Alice", "Bob", "1"},
                {"Charlie", "Diana", "2"}
        });

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(data));

        assertThat(result.isSuccess()).isTrue();
        List<ImportedPair> pairs = result.get();
        assertThat(pairs).hasSize(2);
        assertThat(pairs.get(0).seed()).isEqualTo(1);
        assertThat(pairs.get(1).seed()).isEqualTo(2);
    }

    @Test
    void imports_pairs_from_xls() throws IOException {
        byte[] data = createXls(false, new String[][]{
                {"Alice", "Bob"}
        });

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(data));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).hasSize(1);
    }

    @Test
    void imports_pairs_with_numeric_seed_cell() throws IOException {
        byte[] data = createXlsxWithNumericSeed();

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(data));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get().get(0).seed()).isEqualTo(5);
    }

    @Test
    void fails_on_empty_workbook() throws IOException {
        Workbook wb = new XSSFWorkbook();
        wb.createSheet("Sheet1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(out.toByteArray()));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("no data");
    }

    @Test
    void fails_on_empty_player_name() throws IOException {
        byte[] data = createXlsx(false, new String[][]{
                {"", "Bob"}
        });

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(data));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("Player 1 name is empty");
    }

    @Test
    void fails_on_invalid_seed_value() throws IOException {
        byte[] data = createXlsx(false, new String[][]{
                {"Alice", "Bob", "abc"}
        });

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(data));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause()).hasMessageContaining("Invalid seed");
    }

    @Test
    void skips_empty_rows() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Alice");
        row0.createCell(1).setCellValue("Bob");
        sheet.createRow(1); // empty row
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Charlie");
        row2.createCell(1).setCellValue("Diana");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        Try<List<ImportedPair>> result = importer.importPairs(new ByteArrayInputStream(out.toByteArray()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).hasSize(2);
    }

    private byte[] createXlsx(boolean withHeader, String[][] data) throws IOException {
        Workbook wb = new XSSFWorkbook();
        populateWorkbook(wb, withHeader, data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    private byte[] createXls(boolean withHeader, String[][] data) throws IOException {
        Workbook wb = new HSSFWorkbook();
        populateWorkbook(wb, withHeader, data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    private void populateWorkbook(Workbook wb, boolean withHeader, String[][] data) {
        Sheet sheet = wb.createSheet("Sheet1");
        int rowIdx = 0;
        if (withHeader) {
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Player1");
            header.createCell(1).setCellValue("Player2");
            header.createCell(2).setCellValue("Seed");
        }
        for (String[] rowData : data) {
            Row row = sheet.createRow(rowIdx++);
            for (int i = 0; i < rowData.length; i++) {
                row.createCell(i).setCellValue(rowData[i]);
            }
        }
    }

    private byte[] createXlsxWithNumericSeed() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Alice");
        row.createCell(1).setCellValue("Bob");
        row.createCell(2).setCellValue(5.0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }
}
