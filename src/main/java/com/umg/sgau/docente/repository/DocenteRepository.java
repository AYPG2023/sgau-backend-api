package com.umg.sgau.docente.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.umg.sgau.docente.entity.Docente;

@Repository
public interface DocenteRepository  extends JpaRepository <Docente, Long> {
	
	Optional<Docente> findByEmail(String email);
    Optional<Docente> findByEmailIgnoreCase(String email);
    Optional<Docente> findByUsuarioId(Long usuarioId);
    Optional<Docente> findByUsuario_EmailIgnoreCase(String email);
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Docente> findByCodigoDocente(String codigoDocente);

    boolean existsByEmail(String email);

    boolean existsByCodigoDocente(String codigoDocente);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCodigoDocenteAndIdNot(String codigoDocente, Long id);

    Page<Docente> findByActivo(Boolean activo, Pageable pageable);

    @Query("""
            SELECT d FROM Docente d LEFT JOIN d.usuario u
            WHERE (:activo IS NULL OR d.activo = :activo)
              AND (
                   LOWER(COALESCE(u.nombre, d.nombre)) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(u.apellido, d.apellido)) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(COALESCE(u.email, d.email)) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                   LOWER(d.codigoDocente) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<Docente> buscarConFiltros(
            @Param("busqueda") String busqueda,
            @Param("activo") Boolean activo,
            Pageable pageable);

    @Query("""
            SELECT d FROM Docente d JOIN d.usuario u JOIN u.roles r
            WHERE d.activo = true AND u.activo = true AND r.activo = true
              AND UPPER(r.codigo) = 'DOCENTE'
            """)
    java.util.List<Docente> findSeleccionables();
	
}
