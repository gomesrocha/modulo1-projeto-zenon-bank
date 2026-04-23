package br.com.zenon.zenonfrauddetector;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            exibirAjuda();
            return;
        }

        String modo = args[0];

        switch (modo) {
            case "manual" -> testarTransacoesManuais();
            case "ingestao" -> {
                if (args.length < 2) {
                    System.out.println("Informe o caminho do arquivo CSV.");
                    return;
                }
                testarIngestaoCsv(args[1]);
            }
            case "erros" -> {
                if (args.length < 2) {
                    System.out.println("Informe o caminho do arquivo CSV com dados ruins.");
                    return;
                }
                testarIngestaoCsvComErros(args[1]);
            }
            case "todos" -> {
                testarTransacoesManuais();

                if (args.length >= 2) {
                    System.out.println();
                    System.out.println("==== Transações importadas do CSV ====");
                    testarIngestaoCsv(args[1]);
                }

                if (args.length >= 3) {
                    System.out.println();
                    System.out.println("==== Transações válidas do CSV com erros ====");
                    testarIngestaoCsvComErros(args[2]);
                }
            }
            default -> exibirAjuda();
        }
    }

    private static void exibirAjuda() {
        System.out.println("Uso:");
        System.out.println("  manual");
        System.out.println("  ingestao <arquivo_csv>");
        System.out.println("  erros <arquivo_csv_com_erros>");
        System.out.println("  todos <arquivo_csv_normal> <arquivo_csv_com_erros>");
        System.out.println();
        System.out.println("Exemplos com Maven:");
        System.out.println("  mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.Main\" -Dexec.args=\"manual\"");
        System.out.println("  mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.Main\" -Dexec.args=\"ingestao ../data/PS_20174392719_1491204439457_log.csv\"");
        System.out.println("  mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.Main\" -Dexec.args=\"erros data/paysim_with_bad_data.csv\"");
        System.out.println("  mvn exec:java -Dexec.mainClass=\"br.com.zenon.zenonfrauddetector.Main\" -Dexec.args=\"todos ../data/PS_20174392719_1491204439457_log.csv data/paysim_with_bad_data.csv\"");
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
        }
    }

    private static void testarIngestaoCsvComErros(String fileName) {
        TransactionIngestor ingestor = new TransactionIngestor();

        try {
            List<Transaction> transactions = ingestor.ingest(fileName);

            System.out.println(transactions.size());
            transactions.forEach(System.out::println);

        } catch (IOException exception) {
            System.out.println("Erro ao ler o arquivo CSV: " + exception.getMessage());
        }
    }
}