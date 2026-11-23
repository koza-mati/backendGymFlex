package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PurchaseMembershipRequestDto {
    @NotNull(message = "ID typu karnetu jest wymagane")
    private Long membershipTypeId;

    @NotBlank(message = "Metoda płatności jest wymagana")
    @Pattern(regexp = "CARD|BLIK|CASH|TRANSFER", message = "Nieobsługiwana metoda płatności")
    private String paymentMethod;
}
