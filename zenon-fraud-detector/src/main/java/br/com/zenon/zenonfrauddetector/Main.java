package br.com.zenon.zenonfrauddetector;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        testarTransacoesManuais();

        System.out.println();
        System.out.println("==== Transações importadas do CSV ====");

        if (args.length == 0) {
            System.out.println("Informe o caminho do arquivo CSV do PaySim.");
            System.out.println("Exemplo:");
            System.out.println("./gradlew run --args=\"data/PS_20174392719_1491204439457_log.csv\"");
            return;
        }

        testarIngestaoCsv(args[0]);
    }

    private static void testarTransacoesManuais() {
        System.out.println("==== Transações criadas manualmente ====");

        Transaction transaction1 = new Transaction(
                1,
                TransactionType.PAYMENT,
                new BigDecimal("9839.64"),
                new CustomerBalance(
                        "C1231006815",
                        new BigDecimal("170136.0"),
                        new BigDecimal("160296.36")
                ),
                new CustomerBalance(
                        "M1979787155",
                        new BigDecimal("0.0"),
                        new BigDecimal("0.0")
                ),
                false,
                false
        );

        Transaction transaction2 = new Transaction(
                743,
                TransactionType.CASH_OUT,
                new BigDecimal("850002.52"),
                new CustomerBalance(
                        "C1280323807",
                        new BigDecimal("850002.52"),
                        new BigDecimal("0.0")
                ),
                new CustomerBalance(
                        "C873221189",
                        new BigDecimal("6510099.11"),
                        new BigDecimal("7360101.63")
                ),
                true,
                false
        );

        System.out.println(transaction1);
        System.out.println(transaction2);
    }

    private static void testarIngestaoCsv(String fileName) {
        TransactionIngestor ingestor = new TransactionIngestor();

        try {
            List<Transaction> transactions = ingestor.ingest(fileName);

            transactions.stream()
                    .limit(10)
                    .forEach(System.out::println);

        } catch (IOException exception) {
            System.out.println("Erro ao ler o arquivo CSV: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.out.println("Erro ao processar uma linha do CSV: " + exception.getMessage());
        }
    }
}