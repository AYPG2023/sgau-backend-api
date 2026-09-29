package com.umg.sgau.estudiante.repository;

import com.umg.sgau.estudiante.entity.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByCodigoEstudiantil(String codigoEstudiantil);

    Optional<Estudiante> findByNumeroIdentificacion(String numeroIdentificacion);

    Optional<Estudiante> findByCorreo(String correo);
    Optional<Estudiante> findByCorreoIgnoreCase(String correo);
    Optional<Estudiante> findByUsuarioId(Long usuarioId);
    Optional<Estudiante> findByUsuario_EmailIgnoreCase(String email);
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    boolean existsByCodigoEstudiantil(String codigoEstudiantil);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByCorreo(String correo);

    boolean existsByCodigoEstudiantilAndIdNot(
            String codigoEstudiantil,
            Long id
    );

    boolean existsByNumeroIdentificacionAndIdNot(
            String numeroIdentificacion,
            Long id
    );

    boolean existsByCorreoAndIdNot(
            String correo,
            Long id
    );

    @Query("""
    SELECT e
    FROM Estudiante e LEFT JOIN e.usuario u
    WHERE
        (
            :texto IS NULL
            OR :texto = ''
            OR LOWER(e.codigoEstudiantil) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(e.numeroIdentificacion) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(u.nombre, e.nombres)) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(u.apellido, e.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(u.email, e.correo)) LIKE LOWER(CONCAT('%', :texto, '%'))
        )
        AND
        (
            :activo IS NULL
            OR e.activo = :activo
        )
    """)
    Page<Estudiante> buscar(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Estudiante e JOIN e.usuario u JOIN u.roles r
            WHERE e.activo = true AND u.activo = true AND r.activo = true
              AND UPPER(r.codigo) = 'ESTUDIANTE'
            """)
    java.util.List<Estudiante> findSeleccionables();
}
