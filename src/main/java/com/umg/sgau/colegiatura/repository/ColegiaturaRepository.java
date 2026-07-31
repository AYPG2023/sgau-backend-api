package com.umg.sgau.colegiatura.repository;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColegiaturaRepository
        extends JpaRepository<Colegiatura, Long> {

    Page<Colegiatura> findByEstudianteId(
            Long estudianteId,
            Pageable pageable
    );

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

    boolean existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrue(
            Long estudianteId,
            Integer cicloAnio,
            String concepto
    );

    boolean existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrueAndIdNot(
            Long estudianteId,
            Integer cicloAnio,
            String concepto,
            Long id
    );

    @Query("""
        SELECT c
        FROM Colegiatura c
        WHERE
            (
                :estudianteId IS NULL
                OR c.estudianteId = :estudianteId
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