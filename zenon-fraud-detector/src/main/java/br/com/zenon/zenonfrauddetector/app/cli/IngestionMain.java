package br.com.zenon.zenonfrauddetector.app.cli;

import br.com.zenon.zenonfrauddetector.domain.model.Transaction;
import br.com.zenon.zenonfrauddetector.domain.model.TransactionDBStats;
import br.com.zenon.zenonfrauddetector.infrastructure.ingestion.EfficientTransactionIngestor;
import br.com.zenon.zenonfrauddetector.infrastructure.persistence.TransactionSQLRepository;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class IngestionMain {

    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/zenon_fraud" +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC" +
                    "&rewriteBatchedStatements=true" +
                    "&cachePrepStmts=true" +
                    "&useServerPrepStmts=true";

    private static final String JDBC_USER = "zenon";
    private static final String JDBC_PASSWORD = "zenon";

    private static final int BATCH_SIZE = 10_000;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso:");
            System.out.println("mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.app.cli.IngestionMain\" -Dexec.args=\"../data/PS_20174392719_1491204439457_log.csv 4 false\"");
            return;
        }

        String fileName = args[0];
        int threadCount = args.length >= 2 ? Integer.parseInt(args[1]) : 4;
        boolean useVirtualThreads = args.length >= 3 && Boolean.parseBoolean(args[2]);

        EfficientTransactionIngestor ingestor = new EfficientTransactionIngestor();

        try {
            System.out.println("=== Teste 1: inserção individual com stream (10 mil) ===");
            runSingleInsertTest(ingestor, fileName);

            System.out.println();
            System.out.println("=== Teste 2: inserção em lote sequencial (10 mil) ===");
            runSequentialBatchTest(ingestor, fileName);

            System.out.println();
            System.out.println("=== Teste 3: inserção em lote concorrente (arquivo completo) ===");
            runConcurrentBatchFullFile(ingestor, fileName, threadCount, useVirtualThreads);

        } catch (IOException exception) {
            System.err.println("Erro ao ler arquivo: " + exception.getMessage());
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }

    private static void runSingleInsertTest(EfficientTransactionIngestor ingestor, String fileName) throws IOException {
        try (Connection connection = openConnection()) {
            TransactionSQLRepository repository = new TransactionSQLRepository(connection);
            repository.deleteAll();

            long start = System.nanoTime();

            ingestor.readAsStream(fileName, 10_000, repository::save);

            long end = System.nanoTime();

            TransactionDBStats stats = repository.fetchStats();
            imprimirTempo(start, end);
            imprimirStats(stats);
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void runSequentialBatchTest(EfficientTransactionIngestor ingestor, String fileName) throws IOException {
        try (Connection connection = openConnection()) {
            TransactionSQLRepository repository = new TransactionSQLRepository(connection);
            repository.deleteAll();

            long start = System.nanoTime();

            ingestor.readBatch(
                    fileName,
                    BATCH_SIZE,
                    10_000,
                    repository::saveBatch,
                    1,
                    false
            );

            long end = System.nanoTime();

            TransactionDBStats stats = repository.fetchStats();
            imprimirTempo(start, end);
            imprimirStats(stats);
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void runConcurrentBatchFullFile(
            EfficientTransactionIngestor ingestor,
            String fileName,
            int threadCount,
            boolean useVirtualThreads
    ) throws IOException {

        try (Connection connection = openConnection()) {
            TransactionSQLRepository repository = new TransactionSQLRepository(connection);
            repository.deleteAll();
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        long start = System.nanoTime();

        ingestor.readBatch(
                fileName,
                BATCH_SIZE,
                Integer.MAX_VALUE,
                IngestionMain::saveBatchInNewConnection,
                threadCount,
                useVirtualThreads
        );

        long end = System.nanoTime();

        try (Connection connection = openConnection()) {
            TransactionSQLRepository repository = new TransactionSQLRepository(connection);
            TransactionDBStats stats = repository.fetchStats();

            System.out.println("Threads: " + threadCount);
            System.out.println("Virtual threads: " + useVirtualThreads);
            imprimirTempo(start, end);
            imprimirStats(stats);
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void saveBatchInNewConnection(List<Transaction> batch) {
        try (Connection connection = openConnection()) {
            TransactionSQLRepository repository = new TransactionSQLRepository(connection);
            repository.saveBatch(batch);
        } catch (SQLException exception) {
            throw new RuntimeException("Erro ao salvar lote no banco", exception);
        }
    }

    private static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    private static void imprimirTempo(long start, long end) {
        System.out.println("Tempo total (ns): " + (end - start));
        System.out.println("Tempo total (ms): " + ((end - start) / 1_000_000.0));
        System.out.println("Tempo total (s): " + ((end - start) / 1_000_000_000.0));
    }

    private static void imprimirStats(TransactionDBStats stats) {
        System.out.println("Total de transações: " + stats.totalTransactions());
        System.out.println("Total de fraudes: " + stats.totalFrauds());
        System.out.println("Valor total transacionado: " + stats.totalAmount().setScale(2).toPlainString());
    }
}