package com.kozikwlodarski.backendgymflex.dto.request;

import lombok.Data;

@Data
public class PurchaseMembershipRequestDto {
    private Long userId;
    private Long membershipTypeId;
    private String paymentMethod;
}
