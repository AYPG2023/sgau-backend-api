package com.umg.sgau.curso.repository;

import com.umg.sgau.curso.entity.Curso;
import java.util.List;
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
    boolean existsByIdAndDocente_Id(Long id, Long docenteId);

    Page<Curso> findByCarrera_Id(Long carreraId, Pageable pageable);

    default Page<Curso> findByCarreraId(Long carreraId, Pageable pageable) {
        return findByCarrera_Id(carreraId, pageable);
    }

    Page<Curso> findByDocente_Id(Long docenteId, Pageable pageable);

    default Page<Curso> findByDocenteId(Long docenteId, Pageable pageable) {
        return findByDocente_Id(docenteId, pageable);
    }

    List<Curso> findByDocente_Id(Long docenteId);

    default List<Curso> findByDocenteId(Long docenteId) {
        return findByDocente_Id(docenteId);
    }

    Page<Curso> findByCicloAnio(Integer cicloAnio, Pageable pageable);

    Page<Curso> findByActivo(Boolean activo, Pageable pageable);

    boolean existsByNombreIgnoreCaseAndCarrera_IdAndCicloAnioAndActivoTrue(
            String nombre,
            Long carreraId,
            Integer cicloAnio);

    default boolean existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrue(
            String nombre,
            Long carreraId,
            Integer cicloAnio) {
        return existsByNombreIgnoreCaseAndCarrera_IdAndCicloAnioAndActivoTrue(nombre, carreraId, cicloAnio);
    }

    boolean existsByNombreIgnoreCaseAndCarrera_IdAndCicloAnioAndActivoTrueAndIdNot(
            String nombre,
            Long carreraId,
            Integer cicloAnio,
            Long id);

    default boolean existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrueAndIdNot(
            String nombre,
            Long carreraId,
            Integer cicloAnio,
            Long id) {
        return existsByNombreIgnoreCaseAndCarrera_IdAndCicloAnioAndActivoTrueAndIdNot(nombre, carreraId, cicloAnio, id);
    }

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
                OR c.carrera.id = :carreraId
            )
            AND (
                :docenteId IS NULL
                OR c.docente.id = :docenteId
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
