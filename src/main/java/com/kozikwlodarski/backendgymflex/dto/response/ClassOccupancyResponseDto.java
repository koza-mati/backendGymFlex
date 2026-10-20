package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassOccupancyResponseDto {
    private Long classId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String trainer;
    private int maxCapacity;
    private int booked;
    private double occupancyPct;
}
