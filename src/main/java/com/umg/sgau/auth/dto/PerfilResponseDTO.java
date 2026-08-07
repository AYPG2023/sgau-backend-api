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
public class PerfilResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;
    private Boolean activo;
}
