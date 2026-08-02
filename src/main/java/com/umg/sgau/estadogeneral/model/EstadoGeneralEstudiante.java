package com.umg.sgau.estadogeneral.model;

import com.umg.sgau.colegiatura.model.EstadoCuentaEstudiante;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.historialacademico.dto.HistorialAcademicoResponseDTO;

public record EstadoGeneralEstudiante(
        Estudiante estudiante,
        HistorialAcademicoResponseDTO historialAcademico,
        EstadoCuentaEstudiante estadoCuenta,
        int totalInscripciones,
        int inscripcionesActivas,
        Integer cicloMasReciente,
        int cursosEnCurso,
        int cantidadPagadas,
        String estadoAcademico,
        String estadoFinanciero,
        String estadoGeneral
) {
}
