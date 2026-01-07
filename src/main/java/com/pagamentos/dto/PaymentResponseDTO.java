package com.pagamentos.dto;

import com.pagamentos.model.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {
    private Long id;
    private String paymentId;
    private String customerId;
    private String customerEmail;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus status;
    private String gateway;
    private String description;
    private LocalDateTime createdAt;
    private String paymentUrl; // URL para PIX ou Boleto
    private String qrCode; // QR Code para PIX
}