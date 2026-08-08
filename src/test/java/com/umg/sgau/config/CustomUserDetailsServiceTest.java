package com.umg.sgau.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(usuarioRepository);
    }

    @Test
    void loadUserByUsernameCargaRolesYPermisosActivos() {
        Usuario usuario = usuario(true);
        when(usuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hash");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "USUARIOS_CREAR");
    }

    @Test
    void loadUserByUsernameConUsuarioInactivoLanzaDisabledException() {
        when(usuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("admin"))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void loadUserByUsernameConUsuarioInexistenteLanzaUsernameNotFoundException() {
        when(usuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("admin"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Credenciales invalidas");
    }

    private Usuario usuario(boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPassword("$2a$10$hash");
        usuario.setEmail("admin@sgau.test");
        usuario.setNombre("Admin");
        usuario.setApellido("SGAU");
        usuario.setActivo(activo);
        usuario.setRoles(new HashSet<>(List.of(rolAdmin())));
        return usuario;
    }

    private Rol rolAdmin() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setCodigo("ADMIN");
        rol.setNombre("Administrador");
        rol.setActivo(true);
        rol.setPermisos(new HashSet<>(List.of(permiso("USUARIOS_CREAR", true), permiso("INACTIVO", false))));
        return rol;
    }

    private Permiso permiso(String codigo, boolean activo) {
        Permiso permiso = new Permiso();
        permiso.setId(1L);
        permiso.setCodigo(codigo);
        permiso.setNombre(codigo);
        permiso.setActivo(activo);
        return permiso;
    }
}
