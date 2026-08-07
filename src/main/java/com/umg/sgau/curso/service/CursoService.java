package com.umg.sgau.curso.service;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.docente.entity.Docente;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Define las operaciones de negocio del dominio Curso usando exclusivamente la entidad Curso.
 */
public interface CursoService {

    /**
     * Registra un nuevo curso.
     *
     * @param curso entidad con los datos del curso a crear
     * @return curso guardado
     */
    Curso crear(Curso curso, Long carreraId);

    default Curso crear(Curso curso) {
        return crear(curso, curso.getCarreraId());
    }

    /**
     * Obtiene un curso por su identificador.
     *
     * @param id identificador del curso
     * @return curso encontrado
     */
    Curso obtenerPorId(Long id);

    /**
     * Lista cursos aplicando filtros opcionales y paginacion.
     *
     * @param texto texto opcional para busqueda
     * @param carreraId identificador opcional de carrera
     * @param docenteId identificador opcional de docente
     * @param cicloAnio anio opcional del ciclo academico
     * @param activo estado opcional para filtrar cursos
     * @param pageable configuracion de paginacion y ordenamiento
     * @return pagina de cursos que cumplen los filtros
     */
    Page<Curso> listar(
            String texto,
            Long carreraId,
            Long docenteId,
            Integer cicloAnio,
            Boolean activo,
            Pageable pageable);

    /**
     * Actualiza los campos editables de un curso existente.
     *
     * @param id identificador del curso a actualizar
     * @param curso entidad con los nuevos datos editables
     * @return curso actualizado
     */
    Curso actualizar(Long id, Curso curso, Long carreraId);

    default Curso actualizar(Long id, Curso curso) {
        return actualizar(id, curso, curso.getCarreraId());
    }

    /**
     * Cambia el estado activo o inactivo de un curso.
     *
     * @param id identificador del curso
     * @param activo nuevo estado del curso
     * @return curso con el estado actualizado
     */
    Curso cambiarEstado(Long id, Boolean activo);

    /**
     * Asigna un docente a un curso.
     *
     * @param id identificador del curso
     * @param docenteId identificador del docente
     * @return curso con el docente asignado
     */
    Curso asignarDocente(Long id, Long docenteId);

    Curso retirarDocente(Long id);

    Docente obtenerDocenteAsignado(Long id);

    Page<Curso> obtenerCursosActivosPorCarrera(Long carreraId, Pageable pageable);

    /**
     * Obtiene los cursos activos.
     *
     * @return lista de cursos activos
     */
    List<Curso> obtenerCursosActivos();

    /**
     * Obtiene los nombres de los cursos activos.
     *
     * @return lista de nombres de cursos activos
     */
    List<String> obtenerNombresDeCursosActivos();

    /**
     * Obtiene los cursos activos asignados a un docente.
     *
     * @param docenteId identificador del docente
     * @return lista de cursos activos del docente
     */
    List<Curso> obtenerCursosPorDocente(Long docenteId);
}
