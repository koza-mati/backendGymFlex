package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseMembershipRequestDto {
    @NotNull(message = "ID użytkownika jest wymagane")
    private Long userId;

    @NotNull(message = "ID typu karnetu jest wymagane")
    private Long membershipTypeId;

    @NotBlank(message = "Metoda płatności jest wymagana")
    private String paymentMethod;
}