package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReservationRequestDto {
    @NotNull(message = "ID użytkownika jest wymagane")
    private Long userId;

    @NotNull(message = "ID zajęć jest wymagane")
    private Long fitnessClassId;
}