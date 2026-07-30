package com.umg.sgau.carrera.repository;

import com.umg.sgau.carrera.entity.Carrera;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Long> {

    Optional<Carrera> findByCodigo(String codigo);

    Optional<Carrera> findByNombreIgnoreCase(String nombre);

    boolean existsByCodigo(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Page<Carrera> findByActivo(Boolean activo, Pageable pageable);

    /*
    * Realiza una busqueda de pagina de todas las carreras que cumplas con los filtros opciones si viene el texto null
    * no se aplican los filtros
     */
    @Query("""
            SELECT c
            FROM Carrera c
            WHERE (
                :texto IS NULL
                OR :texto = ''
                OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(c.descripcion, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
            )
            AND (
                :activo IS NULL
                OR c.activo = :activo
            )
            """)
    Page<Carrera> buscarConFiltros(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
