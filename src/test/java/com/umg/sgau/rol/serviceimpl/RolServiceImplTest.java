package com.umg.sgau.rol.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.permiso.exception.PermisoInactivoException;
import com.umg.sgau.permiso.exception.PermisoNoEncontradoException;
import com.umg.sgau.permiso.repository.PermisoRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.exception.CodigoRolDuplicadoException;
import com.umg.sgau.rol.repository.RolRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    private RolServiceImpl rolService;

    @BeforeEach
    void setUp() {
        rolService = new RolServiceImpl(rolRepository, permisoRepository);
    }

    @Test
    void crearNormalizaCodigoYRechazaDuplicados() {
        when(rolRepository.existsByCodigoIgnoreCase("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> rolService.crear(rol(" admin ", true)))
                .isInstanceOf(CodigoRolDuplicadoException.class);

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void asignarPermisosReemplazaColeccionConPermisosActivos() {
        Rol rol = rol("ADMIN", true);
        Permiso crear = permiso(1L, "USUARIO_CREAR", true);
        Permiso editar = permiso(2L, "USUARIO_EDITAR", true);
        when(rolRepository.findWithPermisosById(1L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(crear, editar));
        when(rolRepository.save(any(Rol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rol actualizado = rolService.asignarPermisos(1L, Set.of(1L, 2L));

        assertThat(actualizado.getPermisos()).containsExactlyInAnyOrder(crear, editar);
    }

    @Test
    void asignarPermisosRechazaPermisoInexistente() {
        when(rolRepository.findWithPermisosById(1L)).thenReturn(Optional.of(rol("ADMIN", true)));
        when(permisoRepository.findAllById(Set.of(1L, 99L))).thenReturn(List.of(permiso(1L, "USUARIO_CREAR", true)));

        assertThatThrownBy(() -> rolService.asignarPermisos(1L, Set.of(1L, 99L)))
                .isInstanceOf(PermisoNoEncontradoException.class);
    }

    @Test
    void asignarPermisosRechazaPermisoInactivo() {
        when(rolRepository.findWithPermisosById(1L)).thenReturn(Optional.of(rol("ADMIN", true)));
        when(permisoRepository.findAllById(Set.of(1L))).thenReturn(List.of(permiso(1L, "USUARIO_CREAR", false)));

        assertThatThrownBy(() -> rolService.asignarPermisos(1L, Set.of(1L)))
                .isInstanceOf(PermisoInactivoException.class);
    }

    private Rol rol(String codigo, Boolean activo) {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setCodigo(codigo);
        rol.setNombre("Administrador");
        rol.setActivo(activo);
        return rol;
    }

    private Permiso permiso(Long id, String codigo, Boolean activo) {
        Permiso permiso = new Permiso();
        permiso.setId(id);
        permiso.setCodigo(codigo);
        permiso.setNombre(codigo);
        permiso.setActivo(activo);
        return permiso;
    }
}
