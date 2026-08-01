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

    Page<Nota> findByEstudianteId(Long estudianteId, Pageable pageable);

    Page<Nota> findByEstudianteIdAndActivoTrue(Long estudianteId, Pageable pageable);

    Page<Nota> findByCursoId(Long cursoId, Pageable pageable);

    Page<Nota> findByCursoIdAndActivoTrue(Long cursoId, Pageable pageable);

    Page<Nota> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId, Pageable pageable);

    Page<Nota> findByEstudianteIdAndCursoIdAndActivoTrue(Long estudianteId, Long cursoId, Pageable pageable);

    Page<Nota> findByCicloAnio(Integer cicloAnio, Pageable pageable);

    Page<Nota> findByActivo(Boolean activo, Pageable pageable);

    List<Nota> findByEstudianteIdAndCicloAnioAndActivoTrue(
            Long estudianteId, Integer cicloAnio);

    List<Nota> findByEstudianteIdAndActivoTrue(Long estudianteId);

    List<Nota> findByEstudianteIdAndCursoIdAndActivoTrue(Long estudianteId, Long cursoId);

    boolean existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
            Long estudianteId, Long cursoId, Integer cicloAnio, String tipoEvaluacion);

    boolean existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
            Long estudianteId, Long cursoId, Integer cicloAnio,
            String tipoEvaluacion, Long id);

    @Query("""
            SELECT n FROM Nota n
            WHERE (:estudianteId IS NULL OR n.estudianteId = :estudianteId)
              AND (:cursoId IS NULL OR n.cursoId = :cursoId)
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
