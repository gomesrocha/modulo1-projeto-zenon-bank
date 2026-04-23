package br.com.zenon.zenonfrauddetector;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class TransactionReport {

    public TransactionReportResult generate(String fileName) throws IOException {
        Path path = Path.of(fileName);

        AtomicLong totalLines = new AtomicLong(0);
        AtomicLong totalFrauds = new AtomicLong(0);
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);

        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1).forEach(line -> {
                String[] columns = line.split(",", -1);

                if (columns.length != 11) {
                    System.err.println("Erro: " + line + " | linha inválida");
                    return;
                }

                try {
                    BigDecimal amount = new BigDecimal(columns[2].trim());
                    boolean isFraud = "1".equals(columns[9].trim());

                    totalLines.incrementAndGet();

                    if (isFraud) {
                        totalFrauds.incrementAndGet();
                    }

                    totalAmount.updateAndGet(current -> current.add(amount));
                } catch (Exception exception) {
                    System.err.println("Erro: " + line + " | " + exception);
                }
            });
        }

        return new TransactionReportResult(
                totalLines.get(),
                totalFrauds.get(),
                totalAmount.get()
        );
    }
}