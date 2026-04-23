package br.com.zenon.zenonfrauddetector;

import java.math.BigDecimal;

public record TransactionReportResult(
        long totalLines,
        long totalFrauds,
        BigDecimal totalAmount
) {
}