package br.com.zenon.zenonfrauddetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TransactionMapRepository implements TransactionRepository {

    private final Map<String, Transaction> transactionsByOriginCustomer;

    public TransactionMapRepository(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);

        this.transactionsByOriginCustomer = new HashMap<>();

        for (Transaction transaction : transactions) {
            this.transactionsByOriginCustomer.put(
                    transaction.origin().name(),
                    transaction
            );
        }
    }

    @Override
    public Optional<Transaction> findByOriginCustomerName(String customerName) {
        return Optional.ofNullable(transactionsByOriginCustomer.get(customerName));
    }
}