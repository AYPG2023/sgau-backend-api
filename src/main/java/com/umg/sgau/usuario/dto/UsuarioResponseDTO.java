package com.umg.sgau.usuario.dto;

import com.umg.sgau.rol.dto.RolSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private Set<RolSummaryDTO> roles;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
