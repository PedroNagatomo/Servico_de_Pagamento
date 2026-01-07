package com.pagamentos.controller;

import com.pagamentos.dto.PaymentRequestDTO;
import com.pagamentos.dto.PaymentResponseDTO;
import com.pagamentos.dto.RefundRequestDTO;
import com.pagamentos.model.Payment;
import com.pagamentos.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO request) {
        Payment payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(convertToDTO(payment));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByPaymentId(@PathVariable String paymentId) {
        Payment payment = paymentService.getPaymentByPaymentId(paymentId);
        return ResponseEntity.ok(convertToDTO(payment));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponseDTO>> getCustomerPayments(@PathVariable String customerId) {
        List<Payment> payments = paymentService.getPaymentsByCustomer(customerId);
        List<PaymentResponseDTO> dtos = payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(@PathVariable String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        List<PaymentResponseDTO> dtos = payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(@Valid @RequestBody RefundRequestDTO request) {
        Payment refund = paymentService.refundPayment(request);
        return ResponseEntity.ok(convertToDTO(refund));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = paymentService.getPaymentStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running!");
    }

    private PaymentResponseDTO convertToDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setPaymentId(payment.getPaymentId());
        dto.setCustomerId(payment.getCustomerId());
        dto.setCustomerEmail(payment.getCustomerEmail());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setGateway(payment.getGateway());
        dto.setDescription(payment.getDescription());
        dto.setCreatedAt(payment.getCreatedAt());
        // Adicionar URL para PIX/Boleto se necessário
        return dto;
    }
}