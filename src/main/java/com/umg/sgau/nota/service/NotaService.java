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
     * Obtiene las notas activas de un estudiante en un ciclo academico.
     * Demuestra filter() en Streams.
     */
    List<Nota> obtenerNotasActivasPorEstudiante(Long estudianteId, Integer cicloAnio);

    /**
     * Obtiene la lista de calificaciones activas de un estudiante.
     * Demuestra map() en Streams.
     */
    List<BigDecimal> obtenerCalificacionesActivas(Long estudianteId, Integer cicloAnio);

    /**
     * Calcula el promedio general de un estudiante en un ciclo academico.
     * Demuestra reduce() en Streams. Redondea a 2 decimales con HALF_UP.
     */
    BigDecimal calcularPromedioGeneral(Long estudianteId, Integer cicloAnio);
}
