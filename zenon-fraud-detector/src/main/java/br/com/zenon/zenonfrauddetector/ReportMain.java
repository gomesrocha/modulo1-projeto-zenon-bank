package br.com.zenon.zenonfrauddetector;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {

    public static void main(String[] args) {
        if (args.length < 2) {
            Locale defaultLocale = new Locale("pt", "BR");
            ResourceBundle bundle = ResourceBundle.getBundle("report", defaultLocale);

            System.out.println(bundle.getString("report.missingFile"));
            System.out.println(bundle.getString("report.example") + ":");
            System.out.println("java -Xmx128m -cp target/classes br.com.zenon.zenonfrauddetector.ReportMain pt ../data/PS_20174392719_1491204439457_log.csv");
            System.out.println("java -Xmx128m -cp target/classes br.com.zenon.zenonfrauddetector.ReportMain en ../data/PS_20174392719_1491204439457_log.csv");
            return;
        }

        String language = args[0];
        String fileName = args[1];

        Locale locale;
        if ("pt".equalsIgnoreCase(language)) {
            locale = new Locale("pt", "BR");
        } else if ("en".equalsIgnoreCase(language)) {
            locale = Locale.US;
        } else {
            Locale defaultLocale = new Locale("pt", "BR");
            ResourceBundle bundle = ResourceBundle.getBundle("report", defaultLocale);
            System.out.println(bundle.getString("report.invalidLanguage"));
            return;
        }

        ResourceBundle bundle = ResourceBundle.getBundle("report", locale);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);

        TransactionReport report = new TransactionReport();

        try {
            TransactionReportResult result = report.generate(fileName);

            System.out.println(bundle.getString("report.totalLines") + ": " + result.totalLines());
            System.out.println(bundle.getString("report.totalFrauds") + ": " + result.totalFrauds());
            System.out.println(bundle.getString("report.totalAmount") + ": " + currencyFormat.format(result.totalAmount()));
        } catch (IOException exception) {
            System.err.println("Erro ao ler o arquivo: " + exception.getMessage());
        }
    }
}