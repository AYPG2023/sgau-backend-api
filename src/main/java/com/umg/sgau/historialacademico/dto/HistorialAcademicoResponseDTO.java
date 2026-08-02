package com.umg.sgau.historialacademico.dto;

import java.math.BigDecimal;
import java.util.List;
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
public class HistorialAcademicoResponseDTO {

    private Long estudianteId;
    private String nombreCompleto;
    private Boolean estudianteActivo;
    private BigDecimal promedioGeneral;
    private Integer totalCursos;
    private Integer cursosAprobados;
    private Integer cursosReprobados;
    private Integer cursosSinCalificacion;
    private List<HistorialCursoResponseDTO> detalleCursos;
}
