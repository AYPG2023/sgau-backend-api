package com.umg.sgau.inscripcion.service;

import com.umg.sgau.inscripcion.entity.Inscripcion;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contrato de operaciones del dominio Inscripcion.
 * 
 * Cada metodo trabaja con entidades. Los DTO pertenecen a la capa HTTP.
 */
public interface InscripcionService {

    /**
     * Registra una nueva inscripcion.
     * Valida que no exista una inscripcion activa duplicada
     * (mismo estudiante, carrera, grado, seccion, ciclo).
     *
     * @param inscripcion datos de la inscripcion a crear
     * @return inscripcion creada con id, estado=ACTIVA, activo=true
     */
    Inscripcion registrar(Inscripcion inscripcion, Long estudianteId, Long carreraId, Long cursoId);

    default Inscripcion registrar(Inscripcion inscripcion) {
        return registrar(
                inscripcion,
                inscripcion.getEstudianteId(),
                inscripcion.getCarreraId(),
                inscripcion.getCursoId());
    }

    /**
     * Busca una inscripcion por su ID.
     *
     * @param id identificador de la inscripcion
     * @return inscripcion encontrada
     */
    Inscripcion obtenerPorId(Long id);

    /**
     * Lista inscripciones con filtros opcionales y paginacion.
     * Cada parametro en NULL significa "no filtrar por este campo".
     */
    Page<Inscripcion> listarConFiltros(
            Long estudianteId,
            Long carreraId,
            Long cursoId,
            Integer cicloAnio,
            String grado,
            String seccion,
            String estado,
            Boolean activo,
            Pageable pageable
    );

    /**
     * Actualiza los campos editables de una inscripcion existente.
     * NO toca: id, estudianteId, estado, activo, fechaInscripcion, auditoria.
     *
     * @param id  identificador de la inscripcion
     * @param inscripcion nuevos valores para los campos editables
     * @return inscripcion actualizada
     */
    Inscripcion actualizar(Long id, Inscripcion inscripcion, Long carreraId, Long cursoId);

    default Inscripcion actualizar(Long id, Inscripcion inscripcion) {
        return actualizar(id, inscripcion, inscripcion.getCarreraId(), inscripcion.getCursoId());
    }

    /**
     * Anula (soft-delete) una inscripcion: estado=ANULADA, activo=false.
     * La inscripcion sigue existiendo en base de datos pero no cuenta como activa.
     *
     * @param id  identificador de la inscripcion
     * @param motivo observacion del motivo de anulacion
     * @return inscripcion anulada
     * @throws IllegalStateException si la inscripcion ya esta anulada (409)
     */
    Inscripcion anular(Long id, String motivo);

    Inscripcion reactivar(Long id);

    /**
     * Obtiene el historial completo de inscripciones de un estudiante,
     * ordenado por fecha de inscripcion descendente.
     *
     * @param estudianteId identificador del estudiante
     * @return lista de inscripciones del estudiante
     */
    Page<Inscripcion> historialPorEstudiante(Long estudianteId, Pageable pageable);

    Page<Inscripcion> inscripcionesPorCurso(Long cursoId, Pageable pageable);

    Page<Inscripcion> inscripcionesActivasPorEstudiante(Long estudianteId, Pageable pageable);

    Page<Inscripcion> inscripcionesActivasPorCurso(Long cursoId, Pageable pageable);

    List<Inscripcion> obtenerActivas();

    List<Long> obtenerEstudiantesConInscripcionActiva();

    boolean existeInscripcionActiva(Long estudianteId, Long cursoId, Integer cicloAnio);
}
