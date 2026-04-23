package br.com.zenon.zenonfrauddetector;

import java.math.BigDecimal;

public record TransactionDBStats(
        long totalTransactions,
        long totalFrauds,
        BigDecimal totalAmount
) {
}