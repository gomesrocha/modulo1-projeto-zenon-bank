package br.com.zenon.zenonfrauddetector;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FraudAnalyzer {

    private final List<Transaction> transactions;

    public FraudAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long countFrauds() {
        return transactions.stream()
                .filter(Transaction::isFraud)
                .count();
    }

    public List<Transaction> top3FraudsByAmount() {
        return transactions.stream()
                .filter(Transaction::isFraud)
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .limit(3)
                .toList();
    }

    public List<String> top5SuspiciousCustomers() {
        return transactions.stream()
                .filter(Transaction::isFraud)
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .map(transaction -> transaction.origin().name())
                .distinct()
                .limit(5)
                .toList();
    }

    public BigDecimal totalFraudLoss() {
        return transactions.stream()
                .filter(Transaction::isFraud)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<TransactionType, Long> countFraudsByType() {
        return transactions.stream()
                .filter(Transaction::isFraud)
                .collect(java.util.stream.Collectors.groupingBy(
                        Transaction::type,
                        java.util.stream.Collectors.counting()
                ));
    }
}