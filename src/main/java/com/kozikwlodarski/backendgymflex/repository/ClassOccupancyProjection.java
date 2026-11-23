package com.kozikwlodarski.backendgymflex.repository;

import java.time.LocalDateTime;

public interface ClassOccupancyProjection {
    Long getClassId();
    String getTitle();
    LocalDateTime getStartTime();
    String getTrainer();
    Integer getMaxCapacity();
    Long getBooked();
    Double getOccupancyPct();
}
