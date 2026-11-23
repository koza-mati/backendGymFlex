package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequestDto {
    @NotBlank(message = "Imię jest wymagane")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(max = 50)
    private String lastName;

    @Email(message = "Nieprawidłowy format adresu e-mail")
    @NotBlank(message = "E-mail jest wymagany")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}
