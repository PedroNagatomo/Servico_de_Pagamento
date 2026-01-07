package com.pagamentos.service;

import com.pagamentos.dto.PaymentRequestDTO;
import com.pagamentos.model.Payment;
import com.pagamentos.model.PaymentStatus;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
public class StripeService implements PaymentGatewayService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe API Key configurada: {}",
                stripeSecretKey != null && !stripeSecretKey.isEmpty() ? "SIM" : "NÃO");
        log.info("Stripe API Key: {}",
                stripeSecretKey != null ? maskKey(stripeSecretKey) : "NÃO CONFIGURADA");
    }

    @Override
    public Payment processPayment(PaymentRequestDTO request) {
        try {
            log.info("Processando pagamento via Stripe para: {}", request.getCustomerEmail());
            log.info("Valor: {} {}", request.getAmount(), request.getCurrency());
            log.info("Token: {}", request.getPaymentToken());

            if (stripeSecretKey == null || stripeSecretKey.isEmpty() ||
                    stripeSecretKey.equals(stripeSecretKey)) {
                log.warn("Usando chave de teste padrão do Stripe");
            }

            Long amountInCents = convertToCents(request.getAmount());

            log.info("Criando charge no Stripe...");

            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setSource(request.getPaymentToken())
                    .setDescription(request.getDescription())
                    .putMetadata("customer_id", request.getCustomerId())
                    .putMetadata("customer_email", request.getCustomerEmail())
                    .build();

            Charge charge = Charge.create(params);
            log.info("Charge criada com sucesso: {}", charge.getId());
            log.info("Status da charge: {}", charge.getStatus());
            log.info("Charge paga: {}", charge.getPaid());

            Payment payment = new Payment();
            payment.setPaymentId(charge.getId());
            payment.setCustomerId(request.getCustomerId());
            payment.setCustomerEmail(request.getCustomerEmail());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setPaymentMethod(request.getPaymentMethod().toString());
            payment.setGateway("STRIPE");
            payment.setDescription(request.getDescription());
            payment.setGatewayResponse(charge.toJson());

            if (charge.getPaid()) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setProcessedAt(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(charge.getCreated()),
                        ZoneId.systemDefault()
                ));
                log.info("Pagamento marcado como SUCCESS");
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Pagamento marcado como FAILED");
            }

            return payment;

        } catch (StripeException e) {
            log.error("Erro ao processar pagamento no Stripe: {}", e.getMessage(), e);

            Payment failedPayment = new Payment();
            failedPayment.setPaymentId("error_" + System.currentTimeMillis());
            failedPayment.setCustomerId(request.getCustomerId());
            failedPayment.setCustomerEmail(request.getCustomerEmail());
            failedPayment.setAmount(request.getAmount());
            failedPayment.setCurrency(request.getCurrency());
            failedPayment.setPaymentMethod(request.getPaymentMethod().toString());
            failedPayment.setGateway("STRIPE");
            failedPayment.setDescription(request.getDescription());
            failedPayment.setStatus(PaymentStatus.FAILED);
            failedPayment.setGatewayResponse("Stripe Error: " + e.getMessage());

            log.info("Pagamento de falha criado com ID: {}", failedPayment.getPaymentId());
            return failedPayment;
        }
    }

    @Override
    public Payment getPaymentStatus(String paymentId) {
        try {
            log.info("Buscando status do pagamento: {}", paymentId);
            Charge charge = Charge.retrieve(paymentId);

            Payment payment = new Payment();
            payment.setPaymentId(charge.getId());
            payment.setStatus(charge.getPaid() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
            payment.setGatewayResponse(charge.toJson());

            return payment;

        } catch (StripeException e) {
            log.error("Erro ao buscar status do pagamento: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Payment refundPayment(String paymentId, Double amount, String reason) {
        try {
            log.info("Processando reembolso para: {}", paymentId);

            RefundCreateParams params = RefundCreateParams.builder()
                    .setCharge(paymentId)
                    .setAmount(convertToCents(BigDecimal.valueOf(amount)))
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .putMetadata("reason", reason != null ? reason : "Solicitado pelo cliente")
                    .build();

            Refund refund = Refund.create(params);

            Payment payment = new Payment();
            payment.setPaymentId(refund.getCharge());
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setGatewayResponse(refund.toJson());

            log.info("Reembolso processado com sucesso: {}", refund.getId());
            return payment;

        } catch (StripeException e) {
            log.error("Erro ao processar reembolso: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String generatePaymentMethodToken(PaymentRequestDTO request) {

        String token = "tok_" + System.currentTimeMillis();
        log.info("Token gerado para teste: {}", token);
        return token;
    }

    private Long convertToCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}