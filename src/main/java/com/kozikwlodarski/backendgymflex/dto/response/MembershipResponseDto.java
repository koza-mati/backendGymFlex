package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MembershipResponseDto {
    private Long membershipId;
    private String membershipTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double pricePaid;
    private String paymentStatus;
    private boolean active;
}
