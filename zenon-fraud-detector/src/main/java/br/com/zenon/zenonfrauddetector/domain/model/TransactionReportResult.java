package br.com.zenon.zenonfrauddetector.domain.model;

import java.math.BigDecimal;

public record TransactionReportResult(
        long totalLines,
        long totalFrauds,
        BigDecimal totalAmount
) {
}