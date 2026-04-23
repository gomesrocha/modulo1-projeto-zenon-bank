package br.com.zenon.zenonfrauddetector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionIngestor {

    public List<Transaction> ingest(String fileName) throws IOException {
        return ingest(fileName, Integer.MAX_VALUE);
    }

    public List<Transaction> ingest(String fileName, int maxLines) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            reader.readLine(); // cabeçalho

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null && count < maxLines) {
                try {
                    transactions.add(parseLine(line));
                    count++;
                } catch (Exception exception) {
                    System.err.println("Erro: " + line + " | " + exception);
                }
            }
        }

        return transactions;
    }

    private Transaction parseLine(String line) {
        String[] columns = line.split(",", -1);

        if (columns.length != 11) {
            throw new IllegalArgumentException("invalid CSV line");
        }

        int step = Integer.parseInt(required(columns[0]));
        TransactionType type = TransactionType.valueOf(required(columns[1]));
        BigDecimal amount = parseBigDecimal(columns[2]);

        CustomerBalance origin = new CustomerBalance(
                parseName(columns[3]),
                parseBigDecimal(columns[4]),
                parseBigDecimal(columns[5])
        );

        CustomerBalance recipient = new CustomerBalance(
                parseName(columns[6]),
                parseBigDecimal(columns[7]),
                parseBigDecimal(columns[8])
        );

        boolean isFraud = parseBoolean(columns[9]);
        boolean isFlaggedFraud = parseBoolean(columns[10]);

        return new Transaction(
                step,
                type,
                amount,
                origin,
                recipient,
                isFraud,
                isFlaggedFraud
        );
    }

    private String required(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("value should not be null"));
    }

    private String parseName(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .orElse("");
    }

    private BigDecimal parseBigDecimal(String value) {
        String cleaned = Optional.ofNullable(value)
                .map(String::trim)
                .orElse("");
        return new BigDecimal(cleaned);
    }

    private boolean parseBoolean(String value) {
        String cleaned = required(value);
        return switch (cleaned) {
            case "0" -> false;
            case "1" -> true;
            default -> throw new IllegalArgumentException("boolean value should be 0 or 1: " + cleaned);
        };
    }
}