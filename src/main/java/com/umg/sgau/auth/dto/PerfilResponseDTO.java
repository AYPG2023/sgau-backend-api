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
public class PerfilResponseDTO {

    private Long id;
    private Long usuarioId;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private Set<String> roles;
    private Set<String> permisos;
    private Boolean activo;
    private Boolean requiereNuevoLogin;
}
