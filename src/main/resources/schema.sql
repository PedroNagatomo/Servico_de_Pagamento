-- Criação das tabelas
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    gateway_response TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    processed_at TIMESTAMP
);

-- Índices para melhor performance
CREATE INDEX idx_payment_id ON payments(payment_id);
CREATE INDEX idx_customer_id ON payments(customer_id);
CREATE INDEX idx_status ON payments(status);