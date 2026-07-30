package com.umg.sgau.inscripcion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionResponseDTO {

    private Long id;
    private Long estudianteId;
    private Long carreraId;
    private Long cursoId;
    private String grado;
    private String seccion;
    private Integer cicloAnio;
    private LocalDate fechaInscripcion;
    private String estado;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
