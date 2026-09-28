package com.umg.sgau.inscripcion.repository;

import com.umg.sgau.inscripcion.entity.Inscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    boolean existsByEstudiante_IdAndCurso_IdAndActivoTrue(Long estudianteId, Long cursoId);
    boolean existsByIdAndEstudiante_Id(Long id, Long estudianteId);
    boolean existsByIdAndCurso_Docente_Id(Long id, Long docenteId);

    Page<Inscripcion> findByEstudiante_Id(Long estudianteId, Pageable pageable);

    default Page<Inscripcion> findByEstudianteId(Long estudianteId, Pageable pageable) {
        return findByEstudiante_Id(estudianteId, pageable);
    }

    Page<Inscripcion> findByEstudiante_IdAndActivoTrue(Long estudianteId, Pageable pageable);

    default Page<Inscripcion> findByEstudianteIdAndActivoTrue(Long estudianteId, Pageable pageable) {
        return findByEstudiante_IdAndActivoTrue(estudianteId, pageable);
    }

    Page<Inscripcion> findByCarrera_Id(Long carreraId, Pageable pageable);

    default Page<Inscripcion> findByCarreraId(Long carreraId, Pageable pageable) {
        return findByCarrera_Id(carreraId, pageable);
    }

    Page<Inscripcion> findByCurso_Id(Long cursoId, Pageable pageable);

    default Page<Inscripcion> findByCursoId(Long cursoId, Pageable pageable) {
        return findByCurso_Id(cursoId, pageable);
    }

    Page<Inscripcion> findByCurso_IdAndActivoTrue(Long cursoId, Pageable pageable);

    default Page<Inscripcion> findByCursoIdAndActivoTrue(Long cursoId, Pageable pageable) {
        return findByCurso_IdAndActivoTrue(cursoId, pageable);
    }

    Page<Inscripcion> findByCicloAnio(Integer cicloAnio, Pageable pageable);

    Page<Inscripcion> findByEstado(String estado, Pageable pageable);

    Page<Inscripcion> findByActivo(Boolean activo, Pageable pageable);

    List<Inscripcion> findByActivoTrue();

    boolean existsByEstudiante_IdAndCurso_IdAndCicloAnioAndActivoTrue(
            Long estudianteId,
            Long cursoId,
            Integer cicloAnio
    );

    default boolean existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
            Long estudianteId,
            Long cursoId,
            Integer cicloAnio) {
        return existsByEstudiante_IdAndCurso_IdAndCicloAnioAndActivoTrue(estudianteId, cursoId, cicloAnio);
    }

    boolean existsByEstudiante_IdAndCurso_IdAndCicloAnioAndActivoTrueAndIdNot(
            Long estudianteId,
            Long cursoId,
            Integer cicloAnio,
            Long id
    );

    default boolean existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrueAndIdNot(
            Long estudianteId,
            Long cursoId,
            Integer cicloAnio,
            Long id) {
        return existsByEstudiante_IdAndCurso_IdAndCicloAnioAndActivoTrueAndIdNot(estudianteId, cursoId, cicloAnio, id);
    }

    // Regla de negocio: no permitir inscripcion activa duplicada
    boolean existsByEstudiante_IdAndCarrera_IdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
            Long estudianteId,
            Long carreraId,
            String grado,
            String seccion,
            Integer cicloAnio
    );

    default boolean existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
            Long estudianteId,
            Long carreraId,
            String grado,
            String seccion,
            Integer cicloAnio) {
        return existsByEstudiante_IdAndCarrera_IdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
                estudianteId, carreraId, grado, seccion, cicloAnio);
    }

    // Igual que el anterior pero excluyendo un id: se usa al ACTUALIZAR,
    // para que el propio registro no aparezca como "duplicado" de si mismo
    boolean existsByEstudiante_IdAndCarrera_IdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
            Long estudianteId,
            Long carreraId,
            String grado,
            String seccion,
            Integer cicloAnio,
            Long id
    );

    default boolean existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
            Long estudianteId,
            Long carreraId,
            String grado,
            String seccion,
            Integer cicloAnio,
            Long id) {
        return existsByEstudiante_IdAndCarrera_IdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
                estudianteId, carreraId, grado, seccion, cicloAnio, id);
    }

    // Filtros combinados: cada parametro en NULL significa "no filtrar por este campo"
    @Query("""
            SELECT i FROM Inscripcion i
            WHERE (:estudianteId IS NULL OR i.estudiante.id = :estudianteId)
              AND (:carreraId IS NULL OR i.carrera.id = :carreraId)
              AND (:cursoId IS NULL OR i.curso.id = :cursoId)
              AND (:cicloAnio IS NULL OR i.cicloAnio = :cicloAnio)
              AND (:grado IS NULL OR i.grado = :grado)
              AND (:seccion IS NULL OR i.seccion = :seccion)
              AND (:estado IS NULL OR i.estado = :estado)
              AND (:activo IS NULL OR i.activo = :activo)
            """)
    Page<Inscripcion> buscarConFiltros(
            @Param("estudianteId") Long estudianteId,
            @Param("carreraId") Long carreraId,
            @Param("cursoId") Long cursoId,
            @Param("cicloAnio") Integer cicloAnio,
            @Param("grado") String grado,
            @Param("seccion") String seccion,
            @Param("estado") String estado,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
}
