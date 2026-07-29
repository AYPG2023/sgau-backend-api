package com.umg.sgau.estudiante.mapper;

import com.umg.sgau.estudiante.dto.EstudianteCreateRequestDTO;
import com.umg.sgau.estudiante.dto.EstudianteResponseDTO;
import com.umg.sgau.estudiante.dto.EstudianteSummaryDTO;
import com.umg.sgau.estudiante.dto.EstudianteUpdateRequestDTO;
import com.umg.sgau.estudiante.entity.Estudiante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstudianteMapper {

    Estudiante toEntity(
            EstudianteCreateRequestDTO request
    );

    EstudianteResponseDTO toResponseDTO(
            Estudiante estudiante
    );

    EstudianteSummaryDTO toSummaryDTO(
            Estudiante estudiante
    );

    List<EstudianteResponseDTO> toResponseDTOList(
            List<Estudiante> estudiantes
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void updateEntity(
            EstudianteUpdateRequestDTO request,
            @MappingTarget Estudiante estudiante
    );
}