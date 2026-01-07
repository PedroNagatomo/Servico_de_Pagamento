package com.pagamentos.service;

import com.pagamentos.dto.PaymentRequestDTO;
import com.pagamentos.model.Payment;

public interface PaymentGatewayService {

    Payment processPayment(PaymentRequestDTO request);

    Payment getPaymentStatus(String paymentId);

    Payment refundPayment(String paymentId, Double amount, String reason);

    String generatePaymentMethodToken(PaymentRequestDTO request);
}