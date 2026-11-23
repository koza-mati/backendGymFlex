package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long membershipId;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime paidAt;
}
