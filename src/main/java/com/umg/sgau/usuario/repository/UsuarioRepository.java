package com.umg.sgau.usuario.repository;

import com.umg.sgau.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // Interfaz como repositorio
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por email
    Optional<Usuario> findByEmail(String email);

    // Buscar por username
    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    // Validar si existe email
    boolean existsByEmail(String email);

    // Validar si existe username
    boolean existsByUsername(String username);
}
