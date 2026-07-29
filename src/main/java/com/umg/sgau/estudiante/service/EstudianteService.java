package com.umg.sgau.estudiante.service;

import com.umg.sgau.estudiante.entity.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EstudianteService {

    Estudiante crear(Estudiante estudiante);

    Estudiante obtenerPorId(Long id);

    Page<Estudiante> listar(
            String texto,
            Boolean activo,
            Pageable pageable
    );

    Estudiante actualizar(
            Long id,
            Estudiante estudiante
    );

    Estudiante cambiarEstado(
            Long id,
            Boolean activo
    );

    Estudiante obtenerResumenPorId(Long id);

    Object obtenerHistorialAcademico(Long id);

}