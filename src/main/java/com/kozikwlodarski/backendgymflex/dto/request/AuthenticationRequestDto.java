package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthenticationRequestDto {
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny format email")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    private String password;

    // Dodaj gettery i settery
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
