package br.com.zenon.zenonfrauddetector;

import java.math.BigDecimal;
import java.util.Objects;

public record Transaction(
        int step,
        TransactionType type,
        BigDecimal amount,
        CustomerBalance originCustomer,
        CustomerBalance destinationCustomer,
        boolean fraud,
        boolean flaggedFraud
) {
    public Transaction {
        Objects.requireNonNull(type, "O tipo da transação não pode ser nulo");
        Objects.requireNonNull(amount, "O valor da transação não pode ser nulo");
        Objects.requireNonNull(originCustomer, "O cliente de origem não pode ser nulo");
        Objects.requireNonNull(destinationCustomer, "O cliente de destino não pode ser nulo");

        if (step < 0) {
            throw new IllegalArgumentException("O step não pode ser negativo");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor da transação não pode ser negativo");
        }
    }
}