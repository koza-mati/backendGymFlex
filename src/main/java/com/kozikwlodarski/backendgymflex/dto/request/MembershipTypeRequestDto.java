package com.kozikwlodarski.backendgymflex.dto.request;

import lombok.Data;

@Data
public class MembershipTypeRequestDto {
    private String name;
    private double price;
    private int durationDays;
}
