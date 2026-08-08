package com.umg.sgau.permiso.repository;

import com.umg.sgau.permiso.entity.Permiso;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByCodigoIgnoreCase(String codigo);

    Optional<Permiso> findByNombreIgnoreCase(String nombre);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Page<Permiso> findByActivo(Boolean activo, Pageable pageable);

    @Query("""
            SELECT p
            FROM Permiso p
            WHERE (
                :texto IS NULL
                OR :texto = ''
                OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(p.descripcion, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
            )
            AND (
                :activo IS NULL
                OR p.activo = :activo
            )
            """)
    Page<Permiso> buscarConFiltros(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
