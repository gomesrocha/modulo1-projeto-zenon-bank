package br.com.zenon.zenonfrauddetector.domain.repository;

import br.com.zenon.zenonfrauddetector.domain.model.Transaction;

import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findByOriginCustomerName(String customerName);
    void save(Transaction transaction);
}