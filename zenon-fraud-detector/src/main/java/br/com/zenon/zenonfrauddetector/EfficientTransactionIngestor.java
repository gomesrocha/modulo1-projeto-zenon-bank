package br.com.zenon.zenonfrauddetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class EfficientTransactionIngestor {

    public void readAsStream(String fileName, int maxTransactions, Consumer<Transaction> consumer) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {
            reader.readLine(); // cabeçalho

            String line;
            int processed = 0;

            while ((line = reader.readLine()) != null && processed < maxTransactions) {
                Optional<Transaction> parsed = parseLineSafely(line);

                if (parsed.isPresent()) {
                    consumer.accept(parsed.get());
                    processed++;
                }
            }
        }
    }

    public void readBatch(
            String fileName,
            int batchSize,
            int maxTransactions,
            Consumer<List<Transaction>> consumer,
            int threadCount,
            boolean useVirtualThreads
    ) throws IOException {

        ExecutorService executor = useVirtualThreads
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threadCount);

        Semaphore inFlightBatches = new Semaphore(useVirtualThreads ? 32 : Math.max(1, threadCount * 2));
        List<Future<?>> futures = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {
            reader.readLine(); // cabeçalho

            String line;
            int processed = 0;
            List<Transaction> batch = new ArrayList<>(batchSize);

            while ((line = reader.readLine()) != null && processed < maxTransactions) {
                Optional<Transaction> parsed = parseLineSafely(line);

                if (parsed.isPresent()) {
                    batch.add(parsed.get());
                    processed++;

                    if (batch.size() == batchSize) {
                        submitBatch(new ArrayList<>(batch), consumer, executor, inFlightBatches, futures);
                        batch.clear();
                    }
                }
            }

            if (!batch.isEmpty()) {
                submitBatch(new ArrayList<>(batch), consumer, executor, inFlightBatches, futures);
            }

            for (Future<?> future : futures) {
                future.get();
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processamento interrompido", exception);
        } catch (ExecutionException exception) {
            throw new RuntimeException("Erro ao processar lote", exception.getCause());
        } finally {
            executor.shutdown();
        }
    }

    private void submitBatch(
            List<Transaction> batch,
            Consumer<List<Transaction>> consumer,
            ExecutorService executor,
            Semaphore inFlightBatches,
            List<Future<?>> futures
    ) {
        inFlightBatches.acquireUninterruptibly();

        futures.add(executor.submit(() -> {
            try {
                consumer.accept(batch);
            } finally {
                inFlightBatches.release();
            }
        }));
    }

    private Optional<Transaction> parseLineSafely(String line) {
        try {
            return Optional.of(parseLine(line));
        } catch (Exception exception) {
            System.err.println("Erro: " + line + " | " + exception);
            return Optional.empty();
        }
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