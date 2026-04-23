package br.com.zenon.zenonfrauddetector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {

    private final Connection connection;

    public TransactionSQLRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public Optional<Transaction> findByOriginCustomerName(String customerName) {
        String sql = """
                SELECT step, type, amount,
                       origin_name, origin_old_balance, origin_new_balance,
                       recipient_name, recipient_old_balance, recipient_new_balance,
                       is_fraud, is_flagged_fraud
                FROM TRANSACTIONS
                WHERE origin_name = ?
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Transaction transaction = new Transaction(
                            resultSet.getInt("step"),
                            TransactionType.valueOf(resultSet.getString("type")),
                            resultSet.getBigDecimal("amount"),
                            new CustomerBalance(
                                    resultSet.getString("origin_name"),
                                    resultSet.getBigDecimal("origin_old_balance"),
                                    resultSet.getBigDecimal("origin_new_balance")
                            ),
                            new CustomerBalance(
                                    resultSet.getString("recipient_name"),
                                    resultSet.getBigDecimal("recipient_old_balance"),
                                    resultSet.getBigDecimal("recipient_new_balance")
                            ),
                            resultSet.getBoolean("is_fraud"),
                            resultSet.getBoolean("is_flagged_fraud")
                    );

                    return Optional.of(transaction);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Erro ao buscar transação no MySQL", exception);
        }

        return Optional.empty();
    }

    @Override
    public void save(Transaction transaction) {
        String sql = """
                INSERT INTO TRANSACTIONS (
                    step, type, amount,
                    origin_name, origin_old_balance, origin_new_balance,
                    recipient_name, recipient_old_balance, recipient_new_balance,
                    is_fraud, is_flagged_fraud
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            preencherStatement(statement, transaction);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Erro ao salvar transação no MySQL", exception);
        }
    }

    public void saveBatch(List<Transaction> transactions) {
        String sql = """
                INSERT INTO TRANSACTIONS (
                    step, type, amount,
                    origin_name, origin_old_balance, origin_new_balance,
                    recipient_name, recipient_old_balance, recipient_new_balance,
                    is_fraud, is_flagged_fraud
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Transaction transaction : transactions) {
                    preencherStatement(statement, transaction);
                    statement.addBatch();
                }

                statement.executeBatch();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Erro ao salvar lote de transações no MySQL", exception);
        }
    }

    public void deleteAll() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE TRANSACTIONS");
        } catch (SQLException exception) {
            throw new RuntimeException("Erro ao limpar a tabela TRANSACTIONS", exception);
        }
    }

    public TransactionDBStats fetchStats() {
        String sql = """
                SELECT COUNT(*) AS total_transactions,
                       COALESCE(SUM(CASE WHEN is_fraud THEN 1 ELSE 0 END), 0) AS total_frauds,
                       COALESCE(SUM(amount), 0) AS total_amount
                FROM TRANSACTIONS
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return new TransactionDBStats(
                        resultSet.getLong("total_transactions"),
                        resultSet.getLong("total_frauds"),
                        resultSet.getBigDecimal("total_amount")
                );
            }

            return new TransactionDBStats(0, 0, java.math.BigDecimal.ZERO);
        } catch (SQLException exception) {
            throw new RuntimeException("Erro ao obter estatísticas do banco", exception);
        }
    }

    private void preencherStatement(PreparedStatement statement, Transaction transaction) throws SQLException {
        statement.setInt(1, transaction.step());
        statement.setString(2, transaction.type().name());
        statement.setBigDecimal(3, transaction.amount());

        statement.setString(4, transaction.origin().name());
        statement.setBigDecimal(5, transaction.origin().oldBalance());
        statement.setBigDecimal(6, transaction.origin().newBalance());

        statement.setString(7, transaction.recipient().name());
        statement.setBigDecimal(8, transaction.recipient().oldBalance());
        statement.setBigDecimal(9, transaction.recipient().newBalance());

        statement.setBoolean(10, transaction.isFraud());
        statement.setBoolean(11, transaction.isFlaggedFraud());
    }
}