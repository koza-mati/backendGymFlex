package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MembershipTypeRequestDto {
    @NotBlank(message = "Nazwa karnetu nie może być pusta")
    private String name;

    @Min(value = 0, message = "Cena nie może być ujemna")
    private double price;

    @Min(value = 1, message = "Karnet musi trwać co najmniej 1 dzień")
    private int durationDays;
}