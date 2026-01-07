package com.pagamentos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefundRequestDTO {

    @NotBlank(message = "Payment ID é obrigatório")
    private String paymentId;

    @NotNull(message = "Valor é obrigatório")
    private BigDecimal amount;

    private String reason;
}