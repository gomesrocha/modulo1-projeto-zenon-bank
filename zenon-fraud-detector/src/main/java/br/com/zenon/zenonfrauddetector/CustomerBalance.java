package br.com.zenon.zenonfrauddetector;

import java.math.BigDecimal;
import java.util.Objects;

public record CustomerBalance(
        String name,
        BigDecimal oldBalance,
        BigDecimal newBalance
) {
    public CustomerBalance {
        Objects.requireNonNull(name, "O nome do cliente não pode ser nulo");
        Objects.requireNonNull(oldBalance, "O saldo anterior não pode ser nulo");
        Objects.requireNonNull(newBalance, "O novo saldo não pode ser nulo");

        if (name.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente não pode ser vazio");
        }

        if (oldBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O saldo anterior não pode ser negativo");
        }

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O novo saldo não pode ser negativo");
        }
    }
}