package com.umg.sgau.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EncryptedLoginRequestDTO(
        @NotBlank @Size(max = 16384) String encryptedCredentials) {
}
