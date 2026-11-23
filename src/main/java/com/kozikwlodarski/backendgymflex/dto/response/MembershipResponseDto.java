package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MembershipResponseDto {
    private Long membershipId;
    private String membershipTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal pricePaid;
    private String paymentStatus;
    private boolean active;
}
