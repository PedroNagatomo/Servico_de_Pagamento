package com.pagamentos.service;

import com.pagamentos.dto.PaymentRequestDTO;
import com.pagamentos.dto.RefundRequestDTO;
import com.pagamentos.model.Payment;
import com.pagamentos.model.PaymentStatus;
import com.pagamentos.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final Map<String, PaymentGatewayService> gatewayServices;

    @Transactional
    public Payment processPayment(PaymentRequestDTO request) {
        log.info("Processando pagamento para cliente: {}", request.getCustomerEmail());

        // Selecionar o gateway de pagamento
        PaymentGatewayService gatewayService = getGatewayService(request.getGateway());

        // Processar pagamento no gateway
        Payment payment = gatewayService.processPayment(request);

        // Salvar no banco de dados
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Pagamento salvo com ID: {}", savedPayment.getId());
        return savedPayment;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByCustomer(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(PaymentStatus.valueOf(status.toUpperCase()));
    }

    @Transactional
    public Payment refundPayment(RefundRequestDTO request) {
        log.info("Processando reembolso para pagamento: {}", request.getPaymentId());

        // Buscar pagamento original
        Payment originalPayment = getPaymentByPaymentId(request.getPaymentId());

        // Verificar se o pagamento pode ser reembolsado
        if (originalPayment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Somente pagamentos com status SUCCESS podem ser reembolsados");
        }

        // Selecionar o gateway apropriado
        PaymentGatewayService gatewayService = getGatewayService(originalPayment.getGateway());

        // Processar reembolso no gateway
        Payment refund = gatewayService.refundPayment(
                request.getPaymentId(),
                request.getAmount().doubleValue(),
                request.getReason()
        );

        if (refund == null) {
            throw new RuntimeException("Falha ao processar reembolso no gateway");
        }

        // Atualizar status do pagamento original
        originalPayment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(originalPayment);

        // Salvar registro do reembolso
        Payment refundPayment = new Payment();
        refundPayment.setPaymentId(refund.getPaymentId() + "_refund");
        refundPayment.setCustomerId(originalPayment.getCustomerId());
        refundPayment.setCustomerEmail(originalPayment.getCustomerEmail());
        refundPayment.setAmount(request.getAmount().negate()); // Valor negativo para reembolso
        refundPayment.setCurrency(originalPayment.getCurrency());
        refundPayment.setPaymentMethod(originalPayment.getPaymentMethod());
        refundPayment.setGateway(originalPayment.getGateway());
        refundPayment.setDescription("Reembolso: " + (request.getReason() != null ? request.getReason() : ""));
        refundPayment.setStatus(PaymentStatus.REFUNDED);
        refundPayment.setGatewayResponse(refund.getGatewayResponse());

        return paymentRepository.save(refundPayment);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatistics() {
        List<Payment> allPayments = paymentRepository.findAll();

        BigDecimal totalAmount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long successfulPayments = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .count();

        long failedPayments = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAmount", totalAmount);
        stats.put("successfulPayments", successfulPayments);
        stats.put("failedPayments", failedPayments);
        stats.put("totalTransactions", allPayments.size());

        return stats;
    }

    private PaymentGatewayService getGatewayService(String gateway) {
        if ("STRIPE".equalsIgnoreCase(gateway)) {
            return stripeService;
        }
        // Adicionar outros gateways aqui (ex: PagSeguroService)
        throw new RuntimeException("Gateway não suportado: " + gateway);
    }
}