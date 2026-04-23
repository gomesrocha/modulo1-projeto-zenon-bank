package br.com.zenon.zenonfrauddetector.domain.model;

import java.math.BigDecimal;

public record TransactionDBStats(
        long totalTransactions,
        long totalFrauds,
        BigDecimal totalAmount
) {
}