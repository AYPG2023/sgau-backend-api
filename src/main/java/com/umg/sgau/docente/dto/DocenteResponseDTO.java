package com.umg.sgau.docente.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteResponseDTO {

    private Long id;
    private String codigoDocente;
    @Schema(description = "Desde Usuario si usuarioId existe; en caso contrario, dato histórico del perfil.")
    private String nombre;
    @Schema(description = "Desde Usuario si usuarioId existe; en caso contrario, dato histórico del perfil.")
    private String apellido;
    @Schema(description = "Desde Usuario si usuarioId existe; en caso contrario, dato histórico del perfil.")
    private String email;
    private String telefono;
    private String especialidad;
    private Boolean activo;
    private Long usuarioId;
    private Boolean accesoApp;
    @Schema(description = "USUARIO cuando está vinculado; PERFIL_HISTORICO cuando usuarioId es null.")
    private String identidadFuente;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
