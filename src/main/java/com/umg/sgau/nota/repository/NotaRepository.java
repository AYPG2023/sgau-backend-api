package com.umg.sgau.nota.repository;

import com.umg.sgau.nota.entity.Nota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {

    boolean existsByIdAndEstudiante_Id(Long id, Long estudianteId);
    boolean existsByIdAndCurso_Docente_Id(Long id, Long docenteId);

    @Query("""
            SELECT CASE WHEN COUNT(n)>0 THEN true ELSE false END FROM Nota n
            WHERE n.id=:notaId AND n.curso.docente.id=:docenteId AND EXISTS (
              SELECT i.id FROM Inscripcion i WHERE i.estudiante.id=n.estudiante.id
                AND i.curso.id=n.curso.id AND i.cicloAnio=n.cicloAnio AND i.activo=true
                AND UPPER(i.estado)<>'ANULADA')
            """)
    boolean existsGestionablePorDocente(@Param("notaId") Long notaId, @Param("docenteId") Long docenteId);

    Page<Nota> findByEstudiante_Id(Long estudianteId, Pageable pageable);

    default Page<Nota> findByEstudianteId(Long estudianteId, Pageable pageable) {
        return findByEstudiante_Id(estudianteId, pageable);
    }

    Page<Nota> findByEstudiante_IdAndActivoTrue(Long estudianteId, Pageable pageable);

    default Page<Nota> findByEstudianteIdAndActivoTrue(Long estudianteId, Pageable pageable) {
        return findByEstudiante_IdAndActivoTrue(estudianteId, pageable);
    }

    Page<Nota> findByCurso_Id(Long cursoId, Pageable pageable);
    Page<Nota> findByCurso_IdAndCicloAnio(Long cursoId, Integer cicloAnio, Pageable pageable);

    @Query("""
            SELECT n FROM Nota n WHERE n.curso.id=:cursoId
              AND (:cicloAnio IS NULL OR n.cicloAnio=:cicloAnio) AND EXISTS (
                SELECT i.id FROM Inscripcion i WHERE i.estudiante.id=n.estudiante.id
                  AND i.curso.id=n.curso.id AND i.cicloAnio=n.cicloAnio AND i.activo=true
                  AND UPPER(i.estado)<>'ANULADA')
            """)
    Page<Nota> findValidasByCurso(@Param("cursoId") Long cursoId,
            @Param("cicloAnio") Integer cicloAnio, Pageable pageable);

    default Page<Nota> findByCursoId(Long cursoId, Pageable pageable) {
        return findByCurso_Id(cursoId, pageable);
    }

    Page<Nota> findByCurso_IdAndActivoTrue(Long cursoId, Pageable pageable);

    default Page<Nota> findByCursoIdAndActivoTrue(Long cursoId, Pageable pageable) {
        return findByCurso_IdAndActivoTrue(cursoId, pageable);
    }

    Page<Nota> findByEstudiante_IdAndCurso_Id(Long estudianteId, Long cursoId, Pageable pageable);

    default Page<Nota> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId, Pageable pageable) {
        return findByEstudiante_IdAndCurso_Id(estudianteId, cursoId, pageable);
    }

    Page<Nota> findByEstudiante_IdAndCurso_IdAndActivoTrue(Long estudianteId, Long cursoId, Pageable pageable);

    default Page<Nota> findByEstudianteIdAndCursoIdAndActivoTrue(
            Long estudianteId,
            Long cursoId,
            Pageable pageable) {
        return findByEstudiante_IdAndCurso_IdAndActivoTrue(estudianteId, cursoId, pageable);
    }

    Page<Nota> findByCicloAnio(Integer cicloAnio, Pageable pageable);

    Page<Nota> findByActivo(Boolean activo, Pageable pageable);

    List<Nota> findByEstudiante_IdAndCicloAnioAndActivoTrue(
            Long estudianteId, Integer cicloAnio);
    Page<Nota> findByEstudiante_IdAndCicloAnioAndActivoTrue(Long estudianteId, Integer cicloAnio, Pageable pageable);

    default List<Nota> findByEstudianteIdAndCicloAnioAndActivoTrue(Long estudianteId, Integer cicloAnio) {
        return findByEstudiante_IdAndCicloAnioAndActivoTrue(estudianteId, cicloAnio);
    }

    List<Nota> findByEstudiante_IdAndActivoTrue(Long estudianteId);

    default List<Nota> findByEstudianteIdAndActivoTrue(Long estudianteId) {
        return findByEstudiante_IdAndActivoTrue(estudianteId);
    }

    List<Nota> findByEstudiante_IdAndCurso_IdAndActivoTrue(Long estudianteId, Long cursoId);

    default List<Nota> findByEstudianteIdAndCursoIdAndActivoTrue(Long estudianteId, Long cursoId) {
        return findByEstudiante_IdAndCurso_IdAndActivoTrue(estudianteId, cursoId);
    }

    boolean existsByEstudiante_IdAndCurso_IdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
            Long estudianteId, Long cursoId, Integer cicloAnio, String tipoEvaluacion);

    default boolean existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
            Long estudianteId,
            Long cursoId,
            Integer cicloAnio,
            String tipoEvaluacion) {
        return existsByEstudiante_IdAndCurso_IdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
                estudianteId, cursoId, cicloAnio, tipoEvaluacion);
    }

    boolean existsByEstudiante_IdAndCurso_IdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
            Long estudianteId, Long cursoId, Integer cicloAnio,
            String tipoEvaluacion, Long id);

    default boolean existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
            Long estudianteId,
            Long cursoId,
            Integer cicloAnio,
            String tipoEvaluacion,
            Long id) {
        return existsByEstudiante_IdAndCurso_IdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
                estudianteId, cursoId, cicloAnio, tipoEvaluacion, id);
    }

    @Query("""
            SELECT n FROM Nota n
            WHERE (:estudianteId IS NULL OR n.estudiante.id = :estudianteId)
              AND (:cursoId IS NULL OR n.curso.id = :cursoId)
              AND (:cicloAnio IS NULL OR n.cicloAnio = :cicloAnio)
              AND (:tipoEvaluacion IS NULL OR n.tipoEvaluacion = :tipoEvaluacion)
              AND (:activo IS NULL OR n.activo = :activo)
            """)
    Page<Nota> buscarConFiltros(
            @Param("estudianteId") Long estudianteId,
            @Param("cursoId") Long cursoId,
            @Param("cicloAnio") Integer cicloAnio,
            @Param("tipoEvaluacion") String tipoEvaluacion,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
