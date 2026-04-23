package br.com.zenon.zenonfrauddetector;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
                    System.out.println("Informe o caminho do arquivo CSV com erros.");
                    return;
                }
                testarIngestaoCsvComErros(args[1]);
            }
            case "streams" -> {
                if (args.length < 2) {
                    System.out.println("Informe o caminho do arquivo CSV.");
                    return;
                }
                testarStreams(args[1]);
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
        System.out.println("  streams <arquivo_csv>");
        System.out.println("  todos <arquivo_csv_normal> <arquivo_csv_com_erros>");
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

    private static void testarStreams(String fileName) {
        TransactionIngestor ingestor = new TransactionIngestor();

        try {
            List<Transaction> transactions = ingestor.ingest(fileName, 50_000);
            FraudAnalyzer analyzer = new FraudAnalyzer(transactions);

            System.out.println("1. Total de Fraudes: " + analyzer.countFrauds());

            System.out.println("2. Top 3 Fraudes de Maior Valor:");
            analyzer.top3FraudsByAmount()
                    .stream()
                    .map(Transaction::amount)
                    .forEach(System.out::println);

            System.out.println("3. Clientes Suspeitos:");
            analyzer.top5SuspiciousCustomers()
                    .forEach(System.out::println);

            System.out.println("4. Prejuízo Total: " + analyzer.totalFraudLoss());

            System.out.println("5. Fraudes por Tipo:");
            analyzer.countFraudsByType()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::name)))
                    .forEach(entry ->
                            System.out.println(" - " + entry.getKey() + ": " + entry.getValue())
                    );

        } catch (IOException exception) {
            System.out.println("Erro ao ler o arquivo CSV: " + exception.getMessage());
        }
    }
}