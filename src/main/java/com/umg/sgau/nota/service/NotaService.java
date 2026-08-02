package com.umg.sgau.nota.service;

import com.umg.sgau.nota.entity.Nota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato de operaciones del dominio Nota usando exclusivamente la entidad Nota.
 * No recibe ni devuelve DTOs. La conversion DTO <-> Entity ocurre solo en el Controller.
 */
public interface NotaService {

    /**
     * Registra una nueva calificacion.
     * Valida que no exista una nota activa duplicada para la misma combinacion.
     */
    Nota crear(Nota nota);

    /**
     * Obtiene una nota por su ID.
     */
    Nota obtenerPorId(Long id);

    /**
     * Lista notas con filtros opcionales y paginacion.
     */
    Page<Nota> listar(
            Long estudianteId,
            Long cursoId,
            Integer cicloAnio,
            String tipoEvaluacion,
            Boolean activo,
            Pageable pageable
    );

    /**
     * Actualiza tipoEvaluacion, calificacion y observaciones de una nota existente.
     * No modifica estudianteId, cursoId, cicloAnio ni activo.
     */
    Nota actualizar(Long id, Nota nota);

    /**
     * Habilita o inhabilita (soft-delete) una nota.
     */
    Nota cambiarEstado(Long id, Boolean activo);

    /**
     * Obtiene las notas activas de un estudiante.
     */
    List<Nota> obtenerNotasActivasPorEstudiante(Long estudianteId);

    List<Nota> obtenerNotasActivasPorEstudianteYCurso(Long estudianteId, Long cursoId);

    Page<Nota> obtenerNotasPorEstudiante(Long estudianteId, Pageable pageable);

    Page<Nota> obtenerNotasActivasPorEstudiante(Long estudianteId, Pageable pageable);

    Page<Nota> obtenerNotasPorCurso(Long cursoId, Pageable pageable);

    Page<Nota> obtenerNotasPorEstudianteYCurso(Long estudianteId, Long cursoId, Pageable pageable);

    /**
     * Obtiene la lista de calificaciones activas de un estudiante.
     * Demuestra map() en Streams.
     */
    List<BigDecimal> obtenerCalificacionesActivas(Long estudianteId);

    /**
     * Calcula el promedio general de un estudiante.
     * Usa mapToDouble() y average(). Redondea a 2 decimales con HALF_UP.
     */
    BigDecimal calcularPromedioGeneral(Long estudianteId);
}
