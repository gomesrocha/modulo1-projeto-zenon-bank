package br.com.zenon.zenonfrauddetector.infrastructure.db;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/zenon_fraud" +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC" +
                    "&rewriteBatchedStatements=true" +
                    "&cachePrepStmts=true" +
                    "&useServerPrepStmts=true";

    public static final String JDBC_USER = "zenon";
    public static final String JDBC_PASSWORD = "zenon";
}