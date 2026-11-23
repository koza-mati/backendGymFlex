package com.kozikwlodarski.backendgymflex.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttendanceRequestDto {
    @Pattern(regexp = "ATTENDED|NO_SHOW", message = "Status musi być ATTENDED albo NO_SHOW")
    @NotBlank
    private String status;
}
