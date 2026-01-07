package com.pagamentos.dto;

import com.pagamentos.model.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {

    @NotBlank(message = "Customer ID é obrigatório")
    private String customerId;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String customerEmail;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que 0")
    private BigDecimal amount;

    @Pattern(regexp = "BRL|USD|EUR", message = "Moeda inválida")
    private String currency = "BRL";

    @NotNull(message = "Método de pagamento é obrigatório")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Token do cartão é obrigatório")
    private String paymentToken; // Token do Stripe ou similar

    private String description;

    @Pattern(regexp = "STRIPE|PAGSEGURO", message = "Gateway inválido")
    private String gateway = "STRIPE";
}