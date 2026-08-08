package com.umg.sgau.rol.dto;

import com.umg.sgau.permiso.dto.PermisoSummaryDTO;
import java.time.LocalDateTime;
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
public class RolResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private Set<PermisoSummaryDTO> permisos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
