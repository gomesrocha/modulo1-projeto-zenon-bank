CREATE TABLE IF NOT EXISTS TRANSACTIONS (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            step INT NOT NULL,
                                            type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,

    origin_name VARCHAR(50) NOT NULL,
    origin_old_balance DECIMAL(19,2) NOT NULL,
    origin_new_balance DECIMAL(19,2) NOT NULL,

    recipient_name VARCHAR(50) NOT NULL,
    recipient_old_balance DECIMAL(19,2) NOT NULL,
    recipient_new_balance DECIMAL(19,2) NOT NULL,

    is_fraud BOOLEAN NOT NULL,
    is_flagged_fraud BOOLEAN NOT NULL
    );