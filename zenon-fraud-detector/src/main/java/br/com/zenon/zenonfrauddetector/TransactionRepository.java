package br.com.zenon.zenonfrauddetector;


import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findByOriginCustomerName(String customerName);
}