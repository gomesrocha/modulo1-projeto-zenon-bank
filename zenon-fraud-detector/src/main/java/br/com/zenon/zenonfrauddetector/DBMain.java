package br.com.zenon.zenonfrauddetector;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DBMain {

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

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Informe o caminho do arquivo CSV.");
            System.out.println("Exemplo:");
            System.out.println("mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.DBMain\" -Dexec.args=\"../data/PS_20174392719_1491204439457_log.csv\"");
            return;
        }

        String fileName = args[0];
        TransactionIngestor ingestor = new TransactionIngestor();

        try {
            List<Transaction> transactions = ingestor.ingest(fileName, 10_000);
            System.out.println("Transações carregadas do CSV: " + transactions.size());

            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                TransactionSQLRepository repository = new TransactionSQLRepository(connection);

                // limpa a tabela antes do teste
                repository.deleteAll();

                long start = System.nanoTime();
                repository.saveBatch(transactions);
                long end = System.nanoTime();

                System.out.println("Tempo de inserção (ns): " + (end - start));
                System.out.println("Tempo de inserção (ms): " + ((end - start) / 1_000_000.0));

                Optional<Transaction> existing = repository.findByOriginCustomerName("C1231006815");
                Optional<Transaction> missing = repository.findByOriginCustomerName("C12345");

                imprimirResultado(existing, "C1231006815");
                imprimirResultado(missing, "C12345");

                // opcional: conferir os números no banco
                TransactionDBStats stats = repository.fetchStats();
                System.out.println("Total de transações no banco: " + stats.totalTransactions());
                System.out.println("Total de fraudes no banco: " + stats.totalFrauds());
                System.out.println("Valor total no banco: " + stats.totalAmount().setScale(2).toPlainString());
            }
        } catch (IOException exception) {
            System.err.println("Erro ao ler o arquivo CSV: " + exception.getMessage());
        } catch (SQLException exception) {
            System.err.println("Erro ao conectar no banco: " + exception.getMessage());
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }

    private static void imprimirResultado(Optional<Transaction> transaction, String customerName) {
        if (transaction.isPresent()) {
            System.out.println(transaction.get());
        } else {
            System.out.println("Transação não encontrada para o cliente " + customerName);
        }
    }
}