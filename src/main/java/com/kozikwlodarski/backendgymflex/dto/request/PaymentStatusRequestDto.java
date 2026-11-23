package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentStatusRequestDto {
    @NotBlank
    @Pattern(regexp = "PENDING|PAID|FAILED|REFUNDED")
    private String status;
}
