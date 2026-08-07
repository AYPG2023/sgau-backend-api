package com.umg.sgau.nota.mapper;

import com.umg.sgau.nota.dto.NotaCreateRequestDTO;
import com.umg.sgau.nota.dto.NotaResponseDTO;
import com.umg.sgau.nota.dto.NotaUpdateRequestDTO;
import com.umg.sgau.nota.entity.Nota;

import java.util.List;
import java.util.stream.Collectors;

public final class NotaMapper {

    private NotaMapper() {
    }

    public static Nota aEntidad(NotaCreateRequestDTO dto) {
        Nota nota = new Nota();
        nota.setCicloAnio(dto.getCicloAnio());
        nota.setTipoEvaluacion(dto.getTipoEvaluacion());
        nota.setCalificacion(dto.getCalificacion());
        nota.setObservaciones(dto.getObservaciones());
        return nota;
    }

    public static NotaResponseDTO aResponseDTO(Nota nota) {
        return NotaResponseDTO.builder()
                .id(nota.getId())
                .estudianteId(nota.getEstudiante() != null ? nota.getEstudiante().getId() : null)
                .cursoId(nota.getCurso() != null ? nota.getCurso().getId() : null)
                .cicloAnio(nota.getCicloAnio())
                .tipoEvaluacion(nota.getTipoEvaluacion())
                .calificacion(nota.getCalificacion())
                .observaciones(nota.getObservaciones())
                .activo(nota.getActivo())
                .fechaCreacion(nota.getFechaCreacion())
                .fechaActualizacion(nota.getFechaActualizacion())
                .build();
    }

    public static List<NotaResponseDTO> aResponseDTOList(List<Nota> notas) {
        return notas.stream()
                .map(NotaMapper::aResponseDTO)
                .collect(Collectors.toList());
    }

    public static void actualizarEntidad(NotaUpdateRequestDTO dto, Nota nota) {
        nota.setTipoEvaluacion(dto.getTipoEvaluacion());
        nota.setCalificacion(dto.getCalificacion());
        nota.setObservaciones(dto.getObservaciones());
    }
}
