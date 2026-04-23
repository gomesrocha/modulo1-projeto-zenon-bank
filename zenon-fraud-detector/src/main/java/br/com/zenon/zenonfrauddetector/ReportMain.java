package br.com.zenon.zenonfrauddetector;

import java.io.IOException;

public class ReportMain {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Informe o caminho do arquivo CSV.");
            System.out.println("Exemplo:");
            System.out.println("java -Xmx128m -cp target/classes br.com.zenon.zenonfrauddetector.ReportMain ../data/PS_20174392719_1491204439457_log.csv");
            return;
        }

        String fileName = args[0];
        TransactionReport report = new TransactionReport();

        try {
            TransactionReportResult result = report.generate(fileName);

            System.out.println("Total de linhas: " + result.totalLines());
            System.out.println("Total de fraudes: " + result.totalFrauds());
            System.out.println("Valor total transacionado: " + result.totalAmount().setScale(2).toPlainString());
        } catch (IOException exception) {
            System.err.println("Erro ao ler o arquivo: " + exception.getMessage());
        }
    }
}