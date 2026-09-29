package com.umg.sgau.auth.dto;

import java.util.Set;
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
    private Long docenteId;
    private Long estudianteId;
    private String username;
    private String nombre;
    private String apellido;
    private Set<String> roles;
    private Set<String> permisos;
}
