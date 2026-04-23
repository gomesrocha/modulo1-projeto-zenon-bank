package br.com.zenon.zenonfrauddetector.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record CustomerBalance(
        String name,
        BigDecimal oldBalance,
        BigDecimal newBalance
) {
    public CustomerBalance {
        Objects.requireNonNull(name, "name should not be null");
        Objects.requireNonNull(oldBalance, "oldBalance should not be null");
        Objects.requireNonNull(newBalance, "newBalance should not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name should not be empty");
        }

        if (oldBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("oldBalance should be positive: " + oldBalance);
        }

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("newBalance should be positive: " + newBalance);
        }
    }
}