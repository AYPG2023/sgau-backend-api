package com.umg.sgau.usuario.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.exception.RolInactivoException;
import com.umg.sgau.rol.exception.RolNoEncontradoException;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplRolesTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, rolRepository, passwordEncoder);
    }

    @Test
    void asignarRolesRechazaRolInexistente() {
        when(usuarioRepository.findWithRolesById(1L)).thenReturn(Optional.of(usuario(true)));
        when(rolRepository.findAllById(Set.of(1L, 99L))).thenReturn(List.of(rol("ADMIN", true)));

        assertThatThrownBy(() -> usuarioService.asignarRoles(1L, Set.of(1L, 99L)))
                .isInstanceOf(RolNoEncontradoException.class);
    }

    @Test
    void asignarRolesRechazaRolInactivo() {
        when(usuarioRepository.findWithRolesById(1L)).thenReturn(Optional.of(usuario(true)));
        when(rolRepository.findAllById(Set.of(1L))).thenReturn(List.of(rol("ADMIN", false)));

        assertThatThrownBy(() -> usuarioService.asignarRoles(1L, Set.of(1L)))
                .isInstanceOf(RolInactivoException.class);
    }

    @Test
    void obtenerAutoridadesIncluyeSoloRolesYPermisosActivos() {
        Rol admin = rol("ADMIN", true);
        admin.setPermisos(new HashSet<>(Set.of(
                permiso("USUARIO_CREAR", true),
                permiso("USUARIO_ELIMINAR", false))));
        Rol inactivo = rol("DOCENTE", false);
        inactivo.setPermisos(new HashSet<>(Set.of(permiso("NOTA_EDITAR", true))));
        Usuario usuario = usuario(true);
        usuario.setRoles(new HashSet<>(Set.of(admin, inactivo)));
        when(usuarioRepository.findWithRolesAndPermisosByUsername("admin")).thenReturn(Optional.of(usuario));

        Set<String> autoridades = usuarioService.obtenerAutoridades("admin");

        assertThat(autoridades).containsExactlyInAnyOrder("ROLE_ADMIN", "USUARIO_CREAR");
    }

    private Usuario usuario(Boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setActivo(activo);
        return usuario;
    }

    private Rol rol(String codigo, Boolean activo) {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setCodigo(codigo);
        rol.setNombre(codigo);
        rol.setActivo(activo);
        return rol;
    }

    private Permiso permiso(String codigo, Boolean activo) {
        Permiso permiso = new Permiso();
        permiso.setCodigo(codigo);
        permiso.setNombre(codigo);
        permiso.setActivo(activo);
        return permiso;
    }
}
