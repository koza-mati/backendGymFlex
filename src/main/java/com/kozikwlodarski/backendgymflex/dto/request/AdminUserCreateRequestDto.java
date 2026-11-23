package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserCreateRequestDto {
    @NotBlank
    @Size(max = 50)
    private String firstName;
    @NotBlank
    @Size(max = 50)
    private String lastName;
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    @NotBlank
    private String password;
    @Pattern(regexp = "TRAINER|ADMIN", message = "Rola musi być TRAINER albo ADMIN")
    @NotBlank
    private String role;
}
