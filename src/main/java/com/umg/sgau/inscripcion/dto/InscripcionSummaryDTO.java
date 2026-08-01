package com.umg.sgau.inscripcion.dto;


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
public class InscripcionSummaryDTO {

    private Long id;
    private Long estudianteId;
    private Long carreraId;
    private String grado;
    private String seccion;
    private Integer cicloAnio;
    private String estado;
    private Boolean activo;
}