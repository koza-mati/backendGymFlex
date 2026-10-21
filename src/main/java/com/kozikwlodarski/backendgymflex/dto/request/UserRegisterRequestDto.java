package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterRequestDto {
    @NotBlank(message = "Imię jest wymagane")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    private String lastName;

    @Email(message = "Nieprawidłowy format adresu e-mail")
    @NotBlank(message = "E-mail jest wymagany")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}