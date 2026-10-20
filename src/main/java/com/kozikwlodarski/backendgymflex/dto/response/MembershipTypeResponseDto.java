package com.kozikwlodarski.backendgymflex.dto.response;


import lombok.Data;

@Data
public class MembershipTypeResponseDto {
    private Long id;
    private String name;
    private double price;
    private int durationDays;
    private boolean active;
}
