package com.umg.sgau.docente.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.umg.sgau.docente.dto.DocenteCreateRequestDTO;
import com.umg.sgau.docente.dto.DocenteResponseDTO;
import com.umg.sgau.docente.dto.DocenteUpdateRequestDTO;
import com.umg.sgau.docente.entity.Docente;

public final class DocenteMapper {

    private DocenteMapper() {
    }

    public static Docente aEntidad(DocenteCreateRequestDTO dto) {

        Docente docente = new Docente();

        docente.setCodigoDocente(dto.getCodigoDocente());
        docente.setNombre(dto.getNombre());
        docente.setApellido(dto.getApellido());
        docente.setEmail(dto.getEmail());
        docente.setTelefono(dto.getTelefono());
        docente.setEspecialidad(dto.getEspecialidad());

        return docente;
    }

    public static void actualizarEntidad(DocenteUpdateRequestDTO dto, Docente docente) {
        docente.setCodigoDocente(dto.getCodigoDocente());
        docente.setTelefono(dto.getTelefono());
        docente.setEspecialidad(dto.getEspecialidad());
    }

    public static DocenteResponseDTO aResponseDTO(Docente docente) {
        return DocenteResponseDTO.builder()
                .id(docente.getId())
                .codigoDocente(docente.getCodigoDocente())
                .nombre(docente.getNombre())
                .apellido(docente.getApellido())
                .email(docente.getEmail())
                .telefono(docente.getTelefono())
                .especialidad(docente.getEspecialidad())
                .activo(docente.getActivo())
                .usuarioId(docente.getUsuario() == null ? null : docente.getUsuario().getId())
                .accesoApp(docente.getUsuario() != null && Boolean.TRUE.equals(docente.getUsuario().getActivo()))
                .identidadFuente(docente.getUsuario() == null ? "PERFIL_HISTORICO" : "USUARIO")
                .fechaCreacion(docente.getFechaCreacion())
                .fechaActualizacion(docente.getFechaActualizacion())
                .build();
    }

    public static List<DocenteResponseDTO> aResponseDTOList(List<Docente> docentes) {
        return docentes.stream()
                .map(DocenteMapper::aResponseDTO)
                .collect(Collectors.toList());
    }
}
