package com.umg.sgau.rol.repository;

import com.umg.sgau.rol.entity.Rol;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByCodigoIgnoreCase(String codigo);

    Optional<Rol> findByNombreIgnoreCase(String nombre);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Page<Rol> findByActivo(Boolean activo, Pageable pageable);

    @EntityGraph(attributePaths = "permisos")
    Optional<Rol> findWithPermisosById(Long id);

    @EntityGraph(attributePaths = "permisos")
    @Query("""
            SELECT r
            FROM Rol r
            WHERE (
                :texto IS NULL
                OR :texto = ''
                OR LOWER(r.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(r.descripcion, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
            )
            AND (
                :activo IS NULL
                OR r.activo = :activo
            )
            """)
    Page<Rol> buscarConFiltros(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
