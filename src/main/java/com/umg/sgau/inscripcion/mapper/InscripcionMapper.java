package com.umg.sgau.inscripcion.mapper;

import com.umg.sgau.inscripcion.dto.InscripcionCreateRequestDTO;
import com.umg.sgau.inscripcion.dto.InscripcionResponseDTO;
import com.umg.sgau.inscripcion.dto.InscripcionUpdateRequestDTO;
import com.umg.sgau.inscripcion.entity.Inscripcion;

import java.util.List;
import java.util.stream.Collectors;

public final class InscripcionMapper {

    private InscripcionMapper() {
    }

    public static Inscripcion aEntidad(InscripcionCreateRequestDTO dto) {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setGrado(dto.getGrado());
        inscripcion.setSeccion(dto.getSeccion());
        inscripcion.setCicloAnio(dto.getCicloAnio());
        inscripcion.setFechaInscripcion(dto.getFechaInscripcion());
        inscripcion.setObservaciones(dto.getObservaciones());
        return inscripcion;
    }

    public static InscripcionResponseDTO aResponseDTO(Inscripcion inscripcion) {
        return InscripcionResponseDTO.builder()
                .id(inscripcion.getId())
                .estudianteId(inscripcion.getEstudiante() != null ? inscripcion.getEstudiante().getId() : null)
                .carreraId(inscripcion.getCarrera() != null ? inscripcion.getCarrera().getId() : null)
                .cursoId(inscripcion.getCurso() != null ? inscripcion.getCurso().getId() : null)
                .grado(inscripcion.getGrado())
                .seccion(inscripcion.getSeccion())
                .cicloAnio(inscripcion.getCicloAnio())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .estado(inscripcion.getEstado())
                .observaciones(inscripcion.getObservaciones())
                .activo(inscripcion.getActivo())
                .fechaCreacion(inscripcion.getFechaCreacion())
                .fechaActualizacion(inscripcion.getFechaActualizacion())
                .build();
    }

    public static List<InscripcionResponseDTO> aResponseDTOList(List<Inscripcion> inscripciones) {
        return inscripciones.stream()
                .map(InscripcionMapper::aResponseDTO)
                .collect(Collectors.toList());
    }

    // Modifica la entity EXISTENTE solo con campos permitidos.
    // Nunca toca: id, estudiante, estado, activo, fechaInscripcion ni auditoria.
    public static void actualizarEntidad(InscripcionUpdateRequestDTO dto, Inscripcion inscripcion) {
        inscripcion.setGrado(dto.getGrado());
        inscripcion.setSeccion(dto.getSeccion());
        inscripcion.setCicloAnio(dto.getCicloAnio());
        inscripcion.setObservaciones(dto.getObservaciones());
    }
}
