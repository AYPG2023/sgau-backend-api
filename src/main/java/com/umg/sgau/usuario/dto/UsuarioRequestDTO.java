package com.umg.sgau.usuario.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioRequestDTO {

    private String username;
    private String password;
    private String email;
    private String nombre;
    private String apellido;
}
