package br.com.zenon.zenonfrauddetector;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
            case "benchmark" -> {
                if (args.length < 2) {
                    System.out.println("Informe o caminho do arquivo CSV.");
                    return;
                }
                testarBenchmark(args[1]);
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
        System.out.println("  benchmark <arquivo_csv>");
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
                    .map(value -> value.setScale(2).toPlainString())
                    .forEach(System.out::println);

            System.out.println("3. Clientes Suspeitos:");
            analyzer.top5SuspiciousCustomers().forEach(System.out::println);

            System.out.println("4. Prejuízo Total: " +
                    analyzer.totalFraudLoss().setScale(2).toPlainString());

            System.out.println("5. Fraudes por Tipo:");
            analyzer.countFraudsByType()
                    .forEach((type, total) ->
                            System.out.println(" - " + type + ": " + total));
        } catch (IOException exception) {
            System.out.println("Erro ao ler o arquivo CSV: " + exception.getMessage());
        }
    }

    private static void testarBenchmark(String fileName) {
        TransactionIngestor ingestor = new TransactionIngestor();

        try {
            List<Transaction> transactions = ingestor.ingest(fileName, 100_000);

            System.out.println("Total de transações carregadas: " + transactions.size());

            TransactionRepository listRepository = new TransactionListRepository(transactions);
            TransactionRepository mapRepository = new TransactionMapRepository(transactions);

            String existingCustomer = "C1231006815";
            String nonExistingCustomer = "C12345";
            String worstCaseCustomer = "C1868032458";

            System.out.println();
            System.out.println("Busca com List - cliente existente:");
            imprimirResultado(listRepository.findByOriginCustomerName(existingCustomer), existingCustomer);

            System.out.println();
            System.out.println("Busca com List - cliente inexistente:");
            imprimirResultado(listRepository.findByOriginCustomerName(nonExistingCustomer), nonExistingCustomer);

            System.out.println();
            System.out.println("Benchmark pior caso com List:");
            long startList = System.nanoTime();
            Optional<Transaction> listResult = listRepository.findByOriginCustomerName(worstCaseCustomer);
            long endList = System.nanoTime();
            imprimirResultado(listResult, worstCaseCustomer);
            System.out.println("Tempo List (ns): " + (endList - startList));

            System.out.println();
            System.out.println("Benchmark com Map:");
            long startMap = System.nanoTime();
            Optional<Transaction> mapResult = mapRepository.findByOriginCustomerName(worstCaseCustomer);
            long endMap = System.nanoTime();
            imprimirResultado(mapResult, worstCaseCustomer);
            System.out.println("Tempo Map (ns): " + (endMap - startMap));

        } catch (IOException exception) {
            System.out.println("Erro ao ler o arquivo CSV: " + exception.getMessage());
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