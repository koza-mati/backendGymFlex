package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FitnessClassRequestDto {
    @NotBlank
    @Size(max = 100)
    private String title;
    @NotNull
    @Positive
    private Integer maxCapacity;
    @NotNull
    @Future
    private LocalDateTime startTime;
    @NotNull
    @Future
    private LocalDateTime endTime;
    private Long trainerId;
}
