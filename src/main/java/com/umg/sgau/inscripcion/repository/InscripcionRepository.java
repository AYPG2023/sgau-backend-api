package com.umg.sgau.inscripcion.repository;

import com.umg.sgau.inscripcion.entity.Inscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Page<Inscripcion> findByEstudianteId(Long estudianteId, Pageable pageable);

    Page<Inscripcion> findByCarreraId(Long carreraId, Pageable pageable);

    Page<Inscripcion> findByCicloAnio(Integer cicloAnio, Pageable pageable);

    Page<Inscripcion> findByEstado(String estado, Pageable pageable);

    Page<Inscripcion> findByActivo(Boolean activo, Pageable pageable);

    // Regla de negocio: no permitir inscripcion activa duplicada
    boolean existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
            Long estudianteId,
            Long carreraId,
            String grado,
            String seccion,
            Integer cicloAnio
    );

    // Igual que el anterior pero excluyendo un id: se usa al ACTUALIZAR,
    // para que el propio registro no aparezca como "duplicado" de si mismo
    boolean existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
            Long estudianteId,
            Long carreraId,
            String grado,
            String seccion,
            Integer cicloAnio,
            Long id
    );

    // Filtros combinados: cada parametro en NULL significa "no filtrar por este campo"
    @Query("""
            SELECT i FROM Inscripcion i
            WHERE (:estudianteId IS NULL OR i.estudianteId = :estudianteId)
              AND (:carreraId IS NULL OR i.carreraId = :carreraId)
              AND (:cursoId IS NULL OR i.cursoId = :cursoId)
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