package com.umg.sgau.curso.mapper;

import com.umg.sgau.curso.dto.CursoCreateRequestDTO;
import com.umg.sgau.curso.dto.CursoResponseDTO;
import com.umg.sgau.curso.dto.CursoSummaryDTO;
import com.umg.sgau.curso.dto.CursoUpdateRequestDTO;
import com.umg.sgau.curso.entity.Curso;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilidad de mapeo entre la entidad Curso y sus DTO.
 */
public final class CursoMapper {

    private CursoMapper() {
    }

    public static Curso aEntidad(CursoCreateRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El DTO de creacion de curso es obligatorio");
        }

        return Curso.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .creditos(dto.getCreditos())
                .horasSemanales(dto.getHorasSemanales())
                .cicloAnio(dto.getCicloAnio())
                .build();
    }

    public static CursoResponseDTO aResponseDTO(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("La entidad curso es obligatoria");
        }

        return CursoResponseDTO.builder()
                .id(curso.getId())
                .codigo(curso.getCodigo())
                .nombre(curso.getNombre())
                .descripcion(curso.getDescripcion())
                .creditos(curso.getCreditos())
                .horasSemanales(curso.getHorasSemanales())
                .carreraId(curso.getCarrera() != null ? curso.getCarrera().getId() : null)
                .docenteId(curso.getDocente() != null ? curso.getDocente().getId() : null)
                .cicloAnio(curso.getCicloAnio())
                .cicloId(curso.getCiclo() == null ? null : curso.getCiclo().getId())
                .activo(curso.getActivo())
                .fechaCreacion(curso.getFechaCreacion())
                .fechaActualizacion(curso.getFechaActualizacion())
                .build();
    }

    public static CursoSummaryDTO aSummaryDTO(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("La entidad curso es obligatoria");
        }

        return CursoSummaryDTO.builder()
                .id(curso.getId())
                .codigo(curso.getCodigo())
                .nombre(curso.getNombre())
                .carreraId(curso.getCarrera() != null ? curso.getCarrera().getId() : null)
                .activo(curso.getActivo())
                .build();
    }

    public static List<CursoResponseDTO> aResponseDTOList(List<Curso> cursos) {
        if (cursos == null) {
            return Collections.emptyList();
        }

        return cursos.stream()
                .map(CursoMapper::aResponseDTO)
                .collect(Collectors.toList());
    }

    public static void actualizarEntidad(CursoUpdateRequestDTO dto, Curso curso) {
        if (dto == null || curso == null) {
            return;
        }

        curso.setCodigo(dto.getCodigo());
        curso.setNombre(dto.getNombre());
        curso.setDescripcion(dto.getDescripcion());
        curso.setCreditos(dto.getCreditos());
        curso.setHorasSemanales(dto.getHorasSemanales());
        curso.setCicloAnio(dto.getCicloAnio());
    }
}
