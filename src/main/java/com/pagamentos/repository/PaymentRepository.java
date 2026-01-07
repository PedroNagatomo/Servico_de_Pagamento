package com.pagamentos.repository;

import com.pagamentos.model.Payment;
import com.pagamentos.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByCustomerId(String customerId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByGatewayAndStatus(String gateway, PaymentStatus status);

    boolean existsByPaymentId(String paymentId);
}