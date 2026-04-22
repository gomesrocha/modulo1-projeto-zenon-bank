package br.com.zenon.zenonfrauddetector;

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
        Objects.requireNonNull(type, "O tipo da transação não pode ser nulo");
        Objects.requireNonNull(amount, "O valor da transação não pode ser nulo");
        Objects.requireNonNull(origin, "O cliente de origem não pode ser nulo");
        Objects.requireNonNull(recipient, "O cliente de destino não pode ser nulo");

        if (step <= 0) {
            throw new IllegalArgumentException("O step deve ser maior que zero");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor da transação não pode ser negativo");
        }
    }
}