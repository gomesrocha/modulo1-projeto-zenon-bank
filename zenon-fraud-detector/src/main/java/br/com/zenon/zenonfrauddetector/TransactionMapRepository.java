package br.com.zenon.zenonfrauddetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TransactionMapRepository implements TransactionRepository {

    private final Map<String, Transaction> transactionsByOriginName;

    public TransactionMapRepository(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);

        this.transactionsByOriginName = new HashMap<>();
        for (Transaction transaction : transactions) {
            this.transactionsByOriginName.put(transaction.origin().name(), transaction);
        }
    }

    @Override
    public Optional<Transaction> findByOriginCustomerName(String customerName) {
        return Optional.ofNullable(transactionsByOriginName.get(customerName));
    }

    @Override
    public void save(Transaction transaction) {
        transactionsByOriginName.put(
                Objects.requireNonNull(transaction).origin().name(),
                transaction
        );
    }
}