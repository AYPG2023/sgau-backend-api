package com.umg.sgau.estadogeneral.mapper;

import com.umg.sgau.colegiatura.mapper.ColegiaturaMapper;
import com.umg.sgau.estadogeneral.dto.EstadoGeneralEstudianteResponseDTO;
import com.umg.sgau.estadogeneral.dto.ResumenAcademicoResponseDTO;
import com.umg.sgau.estadogeneral.dto.ResumenFinancieroResponseDTO;
import com.umg.sgau.estadogeneral.model.EstadoGeneralEstudiante;

public final class EstadoGeneralEstudianteMapper {

    private EstadoGeneralEstudianteMapper() {
    }

    public static EstadoGeneralEstudianteResponseDTO toResponseDTO(EstadoGeneralEstudiante estadoGeneral) {
        return EstadoGeneralEstudianteResponseDTO.builder()
                .estudianteId(estadoGeneral.estudiante().getId())
                .nombreCompleto(estadoGeneral.estudiante().getNombres() + " " + estadoGeneral.estudiante().getApellidos())
                .correo(estadoGeneral.estudiante().getCorreo())
                .codigoEstudiantil(estadoGeneral.estudiante().getCodigoEstudiantil())
                .activo(estadoGeneral.estudiante().getActivo())
                .fechaRegistro(estadoGeneral.estudiante().getFechaCreacion())
                .estadoGeneral(estadoGeneral.estadoGeneral())
                .resumenAcademico(toResumenAcademicoDTO(estadoGeneral))
                .resumenFinanciero(toResumenFinancieroDTO(estadoGeneral))
                .detalleAcademico(estadoGeneral.historialAcademico().getDetalleCursos())
                .detalleFinanciero(ColegiaturaMapper.toResponseDTOList(estadoGeneral.estadoCuenta().detalle()))
                .build();
    }

    private static ResumenAcademicoResponseDTO toResumenAcademicoDTO(EstadoGeneralEstudiante estadoGeneral) {
        return ResumenAcademicoResponseDTO.builder()
                .promedioGeneral(estadoGeneral.historialAcademico().getPromedioGeneral())
                .totalInscripciones(estadoGeneral.totalInscripciones())
                .inscripcionesActivas(estadoGeneral.inscripcionesActivas())
                .cicloMasReciente(estadoGeneral.cicloMasReciente())
                .totalCursos(estadoGeneral.historialAcademico().getTotalCursos())
                .cursosAprobados(estadoGeneral.historialAcademico().getCursosAprobados())
                .cursosReprobados(estadoGeneral.historialAcademico().getCursosReprobados())
                .cursosEnCurso(estadoGeneral.cursosEnCurso())
                .cursosSinCalificacion(estadoGeneral.historialAcademico().getCursosSinCalificacion())
                .estadoAcademico(estadoGeneral.estadoAcademico())
                .build();
    }

    private static ResumenFinancieroResponseDTO toResumenFinancieroDTO(EstadoGeneralEstudiante estadoGeneral) {
        return ResumenFinancieroResponseDTO.builder()
                .totalCargos(estadoGeneral.estadoCuenta().totalCargos())
                .totalPagado(estadoGeneral.estadoCuenta().totalPagado())
                .saldoPendiente(estadoGeneral.estadoCuenta().saldoPendiente())
                .cantidadCargos(estadoGeneral.estadoCuenta().cantidadCargos())
                .cantidadPendientes(estadoGeneral.estadoCuenta().cantidadPendientes())
                .cantidadPagadas(estadoGeneral.cantidadPagadas())
                .estadoFinanciero(estadoGeneral.estadoFinanciero())
                .build();
    }
}
