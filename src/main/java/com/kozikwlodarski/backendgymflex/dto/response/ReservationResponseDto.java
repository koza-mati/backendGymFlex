package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationResponseDto {
    private Long reservationId;
    private Long userId;
    private String userFullName;
    private String trainerFullName;
    private String className;
    private LocalDateTime startTime;
    private String status;
}