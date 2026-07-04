package at.papo.partnerportal.importer;

import at.papo.partnerportal.common.ApiException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Parses an uploaded partner/article file into headers + row maps (data-import
// spec). Exact client column layout isn't known yet (Specification/
// CONTINUATION_PROMPT.md "still open"); column mapping in ImportService is
// what makes that not matter - any header layout can be mapped by the user.
public final class ImportedFileParser {

    private ImportedFileParser() {
    }

    public static ParsedFile parse(byte[] content, String filename) {
        String lower = filename == null ? "" : filename.toLowerCase();
        if (lower.endsWith(".csv")) {
            return parseCsv(content);
        }
        if (lower.endsWith(".xlsx")) {
            return parseXlsx(content);
        }
        throw new ApiException(400, "Nicht unterstütztes Dateiformat. Bitte .csv oder .xlsx hochladen.");
    }

    private static ParsedFile parseCsv(byte[] content) {
        String text = decodeCsvText(content);
        String firstLine = text.lines().findFirst().orElse("");
        // German-locale spreadsheet exports commonly use ';' - sniff by frequency.
        char separator = countChar(firstLine, ';') > countChar(firstLine, ',') ? ';' : ',';
        CSVParser parser = new CSVParserBuilder().withSeparator(separator).build();

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(text)).withCSVParser(parser).build()) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) {
                return new ParsedFile(List.of(), List.of());
            }
            List<String> headers = List.of(allRows.get(0));
            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i < allRows.size(); i++) {
                Map<String, String> row = toRowMap(headers, allRows.get(i));
                if (!isEmptyRow(row)) {
                    rows.add(row);
                }
            }
            return new ParsedFile(headers, rows);
        } catch (Exception e) {
            throw new ApiException(400, "CSV-Datei konnte nicht gelesen werden: " + e.getMessage());
        }
    }

    private static ParsedFile parseXlsx(byte[] content) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return new ParsedFile(List.of(), List.of());
            }

            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(formatter.formatCellValue(cell));
            }

            List<Map<String, String>> rows = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row sheetRow = rowIterator.next();
                String[] values = new String[headers.size()];
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = sheetRow.getCell(i);
                    values[i] = cell != null ? formatter.formatCellValue(cell) : "";
                }
                Map<String, String> row = toRowMap(headers, values);
                if (!isEmptyRow(row)) {
                    rows.add(row);
                }
            }
            return new ParsedFile(headers, rows);
        } catch (Exception e) {
            throw new ApiException(400, "Excel-Datei konnte nicht gelesen werden: " + e.getMessage());
        }
    }

    // German-locale Excel exports are often ISO-8859-1/windows-1252, not UTF-8.
    // Strict UTF-8 decode first; on malformed input (typical for umlaut bytes in
    // a non-UTF-8 file) fall back to ISO-8859-1 rather than silently importing
    // replacement characters (data-import spec: umlaut/ß correctness).
    private static String decodeCsvText(byte[] content) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(content, StandardCharsets.ISO_8859_1);
        }
    }

    private static Map<String, String> toRowMap(List<String> headers, String[] values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(headers.get(i), i < values.length && values[i] != null ? values[i] : "");
        }
        return map;
    }

    private static boolean isEmptyRow(Map<String, String> row) {
        return row.values().stream().allMatch(String::isBlank);
    }

    private static int countChar(String text, char c) {
        return (int) text.chars().filter(ch -> ch == c).count();
    }
}
