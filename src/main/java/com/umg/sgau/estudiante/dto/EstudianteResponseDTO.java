package com.umg.sgau.estudiante.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteResponseDTO {

    private Long id;

    private String codigoEstudiantil;

    private String numeroIdentificacion;

    @Schema(description = "Desde Usuario si usuarioId existe; en caso contrario, dato histórico del perfil.")
    private String nombres;

    @Schema(description = "Desde Usuario si usuarioId existe; en caso contrario, dato histórico del perfil.")
    private String apellidos;

    private LocalDate fechaNacimiento;

    @Schema(description = "Desde Usuario si usuarioId existe; en caso contrario, dato histórico del perfil.")
    private String correo;

    private String telefono;

    private String direccion;

    private Boolean activo;
    private Long usuarioId;
    private Boolean accesoApp;
    @Schema(description = "USUARIO cuando está vinculado; PERFIL_HISTORICO cuando usuarioId es null.")
    private String identidadFuente;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;
}
