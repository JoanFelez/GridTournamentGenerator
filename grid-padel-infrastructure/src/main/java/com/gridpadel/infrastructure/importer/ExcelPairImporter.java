package com.gridpadel.infrastructure.importer;

import com.gridpadel.domain.port.PairImportPort;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;

@Component
public class ExcelPairImporter implements PairImportPort {

    @Override
    public Try<List<ImportedPair>> importPairs(InputStream inputStream) {
        return Try.of(() -> {
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bis.mark(8);
            byte[] header = new byte[4];
            int read = bis.read(header);
            bis.reset();

            Workbook workbook;
            if (read >= 4 && header[0] == (byte) 0x50 && header[1] == (byte) 0x4B) {
                workbook = new XSSFWorkbook(bis);
            } else {
                workbook = new HSSFWorkbook(bis);
            }

            try {
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                    throw new IllegalArgumentException("Excel file has no data");
                }

                int startRow = isHeaderRow(sheet.getRow(0)) ? 1 : 0;
                int lastRow = sheet.getLastRowNum();

                List<ImportedPair> pairs = List.empty();
                for (int i = startRow; i <= lastRow; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || isEmptyRow(row)) continue;
                    pairs = pairs.append(parseRow(row, i + 1));
                }

                if (pairs.isEmpty()) {
                    throw new IllegalArgumentException("No pair data found in the Excel file");
                }

                return pairs;
            } finally {
                workbook.close();
            }
        });
    }

    @Override
    public boolean supports(String fileExtension) {
        return "xls".equalsIgnoreCase(fileExtension)
                || "xlsx".equalsIgnoreCase(fileExtension);
    }

    private boolean isHeaderRow(Row row) {
        if (row == null) return false;
        Cell cell = row.getCell(0);
        if (cell == null || cell.getCellType() != CellType.STRING) return false;
        String val = cell.getStringCellValue().trim().toLowerCase();
        return val.equals("player1") || val.equals("player 1")
                || val.equals("jugador1") || val.equals("jugador 1")
                || val.equals("nombre1") || val.equals("name1");
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellStringValue(cell);
                if (!val.isEmpty()) return false;
            }
        }
        return true;
    }

    private ImportedPair parseRow(Row row, int rowNumber) {
        String player1 = getCellStringValue(row.getCell(0)).trim();
        String player2 = getCellStringValue(row.getCell(1)).trim();

        if (player1.isEmpty()) {
            throw new IllegalArgumentException("Row " + rowNumber + ": Player 1 name is empty");
        }
        if (player2.isEmpty()) {
            throw new IllegalArgumentException("Row " + rowNumber + ": Player 2 name is empty");
        }

        Integer seed = null;
        Cell seedCell = row.getCell(2);
        if (seedCell != null && seedCell.getCellType() != CellType.BLANK) {
            String seedStr = getCellStringValue(seedCell).trim();
            if (!seedStr.isEmpty()) {
                try {
                    seed = (int) Double.parseDouble(seedStr);
                    if (seed <= 0) {
                        throw new IllegalArgumentException(
                                "Row " + rowNumber + ": Seed must be a positive number, got " + seed);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Row " + rowNumber + ": Invalid seed value '" + seedStr + "'");
                }
            }
        }

        return new ImportedPair(player1, player2, seed);
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    yield String.valueOf((int) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
