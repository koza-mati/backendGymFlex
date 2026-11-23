package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;

@Data
public class UserProfileResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean active;
}
