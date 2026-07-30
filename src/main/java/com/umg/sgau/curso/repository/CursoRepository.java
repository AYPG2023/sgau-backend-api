package com.umg.sgau.curso.repository;

import com.umg.sgau.curso.entity.Curso;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    Optional<Curso> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    Page<Curso> findByCarreraId(Long carreraId, Pageable pageable);

    Page<Curso> findByDocenteId(Long docenteId, Pageable pageable);

    Page<Curso> findByCicloAnio(Integer cicloAnio, Pageable pageable);

    Page<Curso> findByActivo(Boolean activo, Pageable pageable);

    boolean existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrue(
            String nombre,
            Long carreraId,
            Integer cicloAnio);

    boolean existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrueAndIdNot(
            String nombre,
            Long carreraId,
            Integer cicloAnio,
            Long id);

    /*
    * Busca cursos aplicando filtros opcionales por texto, carrera,
    * docente, ciclo academico y estado. La paginacion y el
    * ordenamiento se reciben mediante Pageable.
     */
    @Query("""
            SELECT c
            FROM Curso c
            WHERE (
                :texto IS NULL
                OR :texto = ''
                OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(c.descripcion, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
            )
            AND (
                :carreraId IS NULL
                OR c.carreraId = :carreraId
            )
            AND (
                :docenteId IS NULL
                OR c.docenteId = :docenteId
            )
            AND (
                :cicloAnio IS NULL
                OR c.cicloAnio = :cicloAnio
            )
            AND (
                :activo IS NULL
                OR c.activo = :activo
            )
            """)
    Page<Curso> buscarConFiltros(
            @Param("texto") String texto,
            @Param("carreraId") Long carreraId,
            @Param("docenteId") Long docenteId,
            @Param("cicloAnio") Integer cicloAnio,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
