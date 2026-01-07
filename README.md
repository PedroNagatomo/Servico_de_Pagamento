# Payment Service - Microsserviço de Pagamentos

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-green)

Um microsserviço de pagamentos completo com suporte a múltiplos gateways (Stripe, PagSeguro) e métodos de pagamento (Cartão, PIX, Boleto).

## Funcionalidades

- ✅ **Processamento de pagamentos** com Stripe
- ✅ **Múltiplos métodos**: Cartão, PIX, Boleto
- ✅ **Reembolsos** completos
- ✅ **Estatísticas** em tempo real
- ✅ **Webhooks** para notificações
- ✅ **Banco de dados** MySQL/H2
- ✅ **API REST** documentada
- ✅ **Docker** pronto para uso

## Começando Rápido

### Pré-requisitos
- Java 17+
- Maven 3.8+
- MySQL 8.0+ (opcional)
- Docker (opcional)
git p
### Opção 1: Executar com Docker (Recomendado)
```bash
# Clone o repositório
git clone https://github.com/seu-usuario/payment-service.git
cd payment-service

# Inicie com Docker Compose
docker-compose up -d

# Acesse: http://localhost:8080
