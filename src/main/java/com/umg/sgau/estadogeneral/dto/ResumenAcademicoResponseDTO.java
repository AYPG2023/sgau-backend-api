package com.umg.sgau.estadogeneral.dto;

import java.math.BigDecimal;
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
public class ResumenAcademicoResponseDTO {

    private BigDecimal promedioGeneral;
    private Integer totalInscripciones;
    private Integer inscripcionesActivas;
    private Integer cicloMasReciente;
    private Integer totalCursos;
    private Integer cursosAprobados;
    private Integer cursosReprobados;
    private Integer cursosEnCurso;
    private Integer cursosSinCalificacion;
    private String estadoAcademico;
}
