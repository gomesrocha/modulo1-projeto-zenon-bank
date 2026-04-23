package br.com.zenon.zenonfrauddetector.infrastructure.persistence;

import br.com.zenon.zenonfrauddetector.domain.model.Transaction;
import br.com.zenon.zenonfrauddetector.domain.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionListRepository implements TransactionRepository {

    private final List<Transaction> transactions;

    public TransactionListRepository(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(Objects.requireNonNull(transactions));
    }

    @Override
    public Optional<Transaction> findByOriginCustomerName(String customerName) {
        return transactions.stream()
                .filter(transaction -> transaction.origin().name().equals(customerName))
                .findFirst();
    }

    @Override
    public void save(Transaction transaction) {
        transactions.add(Objects.requireNonNull(transaction));
    }
}