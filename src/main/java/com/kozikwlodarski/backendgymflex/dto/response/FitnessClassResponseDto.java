package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FitnessClassResponseDto {
    private Long id;
    private String title;
    private String trainer;
    private Long trainerId;
    private Integer maxCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
}
