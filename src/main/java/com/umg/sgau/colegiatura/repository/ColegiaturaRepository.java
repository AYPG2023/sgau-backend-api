package com.umg.sgau.colegiatura.repository;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ColegiaturaRepository
        extends JpaRepository<Colegiatura, Long> {

    Page<Colegiatura> findByEstudiante_Id(
            Long estudianteId,
            Pageable pageable
    );

    default Page<Colegiatura> findByEstudianteId(Long estudianteId, Pageable pageable) {
        return findByEstudiante_Id(estudianteId, pageable);
    }

    List<Colegiatura> findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(
            Long estudianteId
    );

    default List<Colegiatura> findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(Long estudianteId) {
        return findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(estudianteId);
    }

    Page<Colegiatura> findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(
            Long estudianteId,
            Pageable pageable
    );

    default Page<Colegiatura> findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(
            Long estudianteId,
            Pageable pageable) {
        return findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(estudianteId, pageable);
    }

    List<Colegiatura> findByEstudiante_IdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
            Long estudianteId,
            BigDecimal saldoPendiente
    );

    default List<Colegiatura> findByEstudianteIdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
            Long estudianteId,
            BigDecimal saldoPendiente) {
        return findByEstudiante_IdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
                estudianteId, saldoPendiente);
    }

    Page<Colegiatura> findByEstudiante_IdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
            Long estudianteId,
            BigDecimal saldoPendiente,
            Pageable pageable
    );

    default Page<Colegiatura> findByEstudianteIdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
            Long estudianteId,
            BigDecimal saldoPendiente,
            Pageable pageable) {
        return findByEstudiante_IdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
                estudianteId, saldoPendiente, pageable);
    }

    Page<Colegiatura> findByCicloAnio(
            Integer cicloAnio,
            Pageable pageable
    );

    Page<Colegiatura> findByEstado(
            String estado,
            Pageable pageable
    );

    Page<Colegiatura> findByActivo(
            Boolean activo,
            Pageable pageable
    );

    List<Colegiatura> findByActivoTrue();

    boolean existsByEstudiante_IdAndCicloAnioAndConceptoAndActivoTrue(
            Long estudianteId,
            Integer cicloAnio,
            String concepto
    );

    default boolean existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrue(
            Long estudianteId,
            Integer cicloAnio,
            String concepto) {
        return existsByEstudiante_IdAndCicloAnioAndConceptoAndActivoTrue(estudianteId, cicloAnio, concepto);
    }

    boolean existsByEstudiante_IdAndCicloAnioAndConceptoAndActivoTrueAndIdNot(
            Long estudianteId,
            Integer cicloAnio,
            String concepto,
            Long id
    );

    default boolean existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrueAndIdNot(
            Long estudianteId,
            Integer cicloAnio,
            String concepto,
            Long id) {
        return existsByEstudiante_IdAndCicloAnioAndConceptoAndActivoTrueAndIdNot(
                estudianteId, cicloAnio, concepto, id);
    }

    @Query("""
        SELECT c
        FROM Colegiatura c
        WHERE
            (
                :estudianteId IS NULL
                OR c.estudiante.id = :estudianteId
            )
        AND
            (
                :cicloAnio IS NULL
                OR c.cicloAnio = :cicloAnio
            )
        AND
            (
                :estado IS NULL
                OR c.estado = :estado
            )
        AND
            (
                :activo IS NULL
                OR c.activo = :activo
            )
        AND
            (
                :concepto IS NULL
                OR :concepto = ''
                OR LOWER(c.concepto)
                    LIKE LOWER(CONCAT('%', :concepto, '%'))
            )
        """)
    Page<Colegiatura> buscar(
            @Param("estudianteId") Long estudianteId,
            @Param("cicloAnio") Integer cicloAnio,
            @Param("estado") String estado,
            @Param("activo") Boolean activo,
            @Param("concepto") String concepto,
            Pageable pageable
    );

}
