package com.umg.sgau.estudiante.service;

import com.umg.sgau.estudiante.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EstudianteService {

    EstudianteResponseDTO crear(
            EstudianteCreateRequestDTO request
    );

    EstudianteResponseDTO obtenerPorId(Long id);

    Page<EstudianteResponseDTO> listar(
            String texto,
            Boolean activo,
            Pageable pageable
    );

    EstudianteResponseDTO actualizar(
            Long id,
            EstudianteUpdateRequestDTO request
    );

    EstudianteResponseDTO cambiarEstado(
            Long id,
            EstudianteStatusRequestDTO request
    );

    EstudianteSummaryDTO obtenerResumenPorId(Long id);

    Object obtenerHistorialAcademico(Long id);

}