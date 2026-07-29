package com.umg.sgau.estudiante.repository;

import com.umg.sgau.estudiante.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByCodigoEstudiantil(String codigoEstudiantil);

    Optional<Estudiante> findByNumeroIdentificacion(String numeroIdentificacion);

    Optional<Estudiante> findByCorreo(String correo);

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
}