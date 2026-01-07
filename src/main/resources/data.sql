-- Inserir dados de teste APÓS a criação das tabelas
INSERT INTO payments (payment_id, customer_id, customer_email, amount, currency,
                      payment_method, status, gateway, description, created_at)
VALUES
('ch_1JABCD123456', 'cust_123', 'cliente1@email.com', 100.50, 'BRL',
 'CREDIT_CARD', 'SUCCESS', 'STRIPE', 'Compra de livro', CURRENT_TIMESTAMP),

('ch_1JDEFG789012', 'cust_456', 'cliente2@email.com', 75.00, 'BRL',
 'PIX', 'SUCCESS', 'STRIPE', 'Assinatura mensal', CURRENT_TIMESTAMP),

('ch_1JHIJK345678', 'cust_123', 'cliente1@email.com', 200.00, 'BRL',
 'CREDIT_CARD', 'FAILED', 'STRIPE', 'Compra de curso', CURRENT_TIMESTAMP);