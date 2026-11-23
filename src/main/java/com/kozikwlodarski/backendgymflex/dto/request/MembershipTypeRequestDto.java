package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipTypeRequestDto {
    @NotBlank(message = "Nazwa karnetu nie może być pusta")
    @Size(max = 50)
    private String name;

    @Min(value = 0, message = "Cena nie może być ujemna")
    @NotNull
    @Digits(integer = 6, fraction = 2)
    @DecimalMax(value = "999999.99")
    private BigDecimal price;

    @Min(value = 1, message = "Karnet musi trwać co najmniej 1 dzień")
    private int durationDays;
}
