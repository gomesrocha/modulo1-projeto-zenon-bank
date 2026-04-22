package br.com.zenon.zenonfrauddetector;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionIngestor {

    private static final int MAX_TRANSACTIONS = 1000;
    private static final int EXPECTED_COLUMNS = 11;

    public List<Transaction> ingest(String fileName) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            reader.readLine(); // ignora o cabeçalho do CSV

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null && count < MAX_TRANSACTIONS) {
                Transaction transaction = parseLine(line);
                transactions.add(transaction);
                count++;
            }
        }

        return transactions;
    }

    private Transaction parseLine(String line) {
        String[] columns = line.split(",", -1);

        if (columns.length != EXPECTED_COLUMNS) {
            throw new IllegalArgumentException("Linha inválida no CSV: " + line);
        }

        int step = Integer.parseInt(columns[0].trim());

        TransactionType type = TransactionType.valueOf(columns[1].trim());

        BigDecimal amount = new BigDecimal(columns[2].trim());

        CustomerBalance originCustomer = new CustomerBalance(
                columns[3].trim(),
                new BigDecimal(columns[4].trim()),
                new BigDecimal(columns[5].trim())
        );

        CustomerBalance destinationCustomer = new CustomerBalance(
                columns[6].trim(),
                new BigDecimal(columns[7].trim()),
                new BigDecimal(columns[8].trim())
        );

        boolean fraud = parseBoolean(columns[9]);
        boolean flaggedFraud = parseBoolean(columns[10]);

        return new Transaction(
                step,
                type,
                amount,
                originCustomer,
                destinationCustomer,
                fraud,
                flaggedFraud
        );
    }

    private boolean parseBoolean(String value) {
        return "1".equals(value.trim());
    }
}