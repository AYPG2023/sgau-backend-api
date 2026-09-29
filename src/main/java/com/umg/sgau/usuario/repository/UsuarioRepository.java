package com.umg.sgau.usuario.repository;

import com.umg.sgau.usuario.entity.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // Interfaz como repositorio
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Override
    @EntityGraph(attributePaths = "roles")
    List<Usuario> findAll();

    // Buscar por email
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Buscar por username
    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    @EntityGraph(attributePaths = {"roles", "roles.permisos"})
    Optional<Usuario> findWithRolesAndPermisosByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    @EntityGraph(attributePaths = {"roles", "roles.permisos"})
    Optional<Usuario> findWithRolesAndPermisosByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permisos"})
    Optional<Usuario> findWithRolesAndPermisosByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findWithRolesById(Long id);

    // Validar si existe email
    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // Validar si existe username
    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
}
