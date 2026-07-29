package com.umg.sgau.estudiante.mapper;

import com.umg.sgau.estudiante.dto.*;
import com.umg.sgau.estudiante.entity.Estudiante;

import java.util.List;
import java.util.stream.Collectors;

public class EstudianteMapper {

    private EstudianteMapper() {
    }

    public static Estudiante toEntity(EstudianteCreateRequestDTO dto) {

        Estudiante estudiante = new Estudiante();

        estudiante.setCodigoEstudiantil(dto.getCodigoEstudiantil());
        estudiante.setNumeroIdentificacion(dto.getNumeroIdentificacion());
        estudiante.setNombres(dto.getNombres());
        estudiante.setApellidos(dto.getApellidos());
        estudiante.setFechaNacimiento(dto.getFechaNacimiento());
        estudiante.setCorreo(dto.getCorreo());
        estudiante.setTelefono(dto.getTelefono());
        estudiante.setDireccion(dto.getDireccion());

        return estudiante;
    }

    public static EstudianteResponseDTO toResponseDTO(Estudiante estudiante) {

        return EstudianteResponseDTO.builder()
                .id(estudiante.getId())
                .codigoEstudiantil(estudiante.getCodigoEstudiantil())
                .numeroIdentificacion(estudiante.getNumeroIdentificacion())
                .nombres(estudiante.getNombres())
                .apellidos(estudiante.getApellidos())
                .fechaNacimiento(estudiante.getFechaNacimiento())
                .correo(estudiante.getCorreo())
                .telefono(estudiante.getTelefono())
                .direccion(estudiante.getDireccion())
                .activo(estudiante.getActivo())
                .fechaCreacion(estudiante.getFechaCreacion())
                .fechaActualizacion(estudiante.getFechaActualizacion())
                .build();
    }

    public static EstudianteSummaryDTO toSummaryDTO(Estudiante estudiante) {

        return EstudianteSummaryDTO.builder()
                .id(estudiante.getId())
                .codigoEstudiantil(estudiante.getCodigoEstudiantil())
                .nombres(estudiante.getNombres())
                .apellidos(estudiante.getApellidos())
                .activo(estudiante.getActivo())
                .build();
    }

    public static List<EstudianteResponseDTO> toResponseDTOList(
            List<Estudiante> estudiantes) {

        return estudiantes.stream()
                .map(EstudianteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntity(
            EstudianteUpdateRequestDTO dto,
            Estudiante estudiante) {

        estudiante.setCodigoEstudiantil(dto.getCodigoEstudiantil());
        estudiante.setNumeroIdentificacion(dto.getNumeroIdentificacion());
        estudiante.setNombres(dto.getNombres());
        estudiante.setApellidos(dto.getApellidos());
        estudiante.setFechaNacimiento(dto.getFechaNacimiento());
        estudiante.setCorreo(dto.getCorreo());
        estudiante.setTelefono(dto.getTelefono());
        estudiante.setDireccion(dto.getDireccion());

        // No se modifican:
        // id
        // activo
        // fechaCreacion
        // fechaActualizacion
    }

}