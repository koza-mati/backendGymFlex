package com.kozikwlodarski.backendgymflex.dto.response;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipTypeResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private int durationDays;
    private boolean active;
}
