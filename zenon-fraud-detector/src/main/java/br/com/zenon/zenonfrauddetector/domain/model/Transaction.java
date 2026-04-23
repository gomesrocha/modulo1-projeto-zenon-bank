package br.com.zenon.zenonfrauddetector.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Transaction(
        int step,
        TransactionType type,
        BigDecimal amount,
        CustomerBalance origin,
        CustomerBalance recipient,
        boolean isFraud,
        boolean isFlaggedFraud
) {
    public Transaction {
        Objects.requireNonNull(type, "type should not be null");
        Objects.requireNonNull(amount, "amount should not be null");
        Objects.requireNonNull(origin, "origin should not be null");
        Objects.requireNonNull(recipient, "recipient should not be null");

        if (step < 1) {
            throw new IllegalArgumentException("step should be positive: " + step);
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount should be positive: " + amount);
        }
    }
}