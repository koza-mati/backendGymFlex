package com.kozikwlodarski.backendgymflex.dto.request;


import lombok.Data;

@Data
public class CreateReservationRequestDto {
    private Long userId;
    private Long fitnessClassId;
}
