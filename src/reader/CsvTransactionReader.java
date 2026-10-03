package reader;
import model.Transaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;

public class CsvTransactionReader {

    public ArrayList<Transaction> read(String path) throws IOException {
        ArrayList<Transaction> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                try {
                    result.add(parseLine(line));
                } catch (RuntimeException e) {
                    System.err.println("Skipping line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        result.sort(Comparator.comparing(Transaction::getTimestamp));
        return result;
    }

    private Transaction parseLine(String line) {
        String[] p = line.split(",");

        // Convert ISO 8601 with Z offset (UTC) to ISO 8601
        Instant instant = Instant.parse(p[5].trim());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

        return new Transaction(
                Integer.parseInt(p[0].trim()),
                p[1].trim(),
                Double.parseDouble(p[2].trim()),
                p[3].trim(),
                p[4].trim(),
                localDateTime
        );
    }
}