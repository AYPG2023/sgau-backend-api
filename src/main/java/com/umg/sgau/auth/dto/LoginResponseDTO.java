package com.umg.sgau.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Long usuarioId;
    private String username;
    private String nombre;
    private String apellido;
    private String rol;
}
