package com.umg.sgau.permiso.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.permiso.exception.CodigoPermisoDuplicadoException;
import com.umg.sgau.permiso.repository.PermisoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PermisoServiceImplTest {

    @Mock
    private PermisoRepository permisoRepository;

    private PermisoServiceImpl permisoService;

    @BeforeEach
    void setUp() {
        permisoService = new PermisoServiceImpl(permisoRepository);
    }

    @Test
    void crearNormalizaCodigoYAsignaActivo() {
        Permiso permiso = permiso(" usuario_crear ", "Crear usuarios", true);
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Permiso creado = permisoService.crear(permiso);

        assertThat(creado.getCodigo()).isEqualTo("USUARIO_CREAR");
        assertThat(creado.getActivo()).isTrue();
        verify(permisoRepository).save(permiso);
    }

    @Test
    void crearRechazaCodigoDuplicado() {
        when(permisoRepository.existsByCodigoIgnoreCase("USUARIO_CREAR")).thenReturn(true);

        assertThatThrownBy(() -> permisoService.crear(permiso("USUARIO_CREAR", "Crear usuarios", true)))
                .isInstanceOf(CodigoPermisoDuplicadoException.class);

        verify(permisoRepository, never()).save(any(Permiso.class));
    }

    @Test
    void cambiarEstadoInhabilitaSinEliminar() {
        Permiso permiso = permiso("NOTA_EDITAR", "Editar notas", true);
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Permiso actualizado = permisoService.cambiarEstado(1L, false);

        assertThat(actualizado.getActivo()).isFalse();
        verify(permisoRepository, never()).delete(any(Permiso.class));
    }

    @Test
    void listarUsaFiltrosPaginados() {
        PageRequest pageable = PageRequest.of(0, 10);

        permisoService.listar("nota", true, pageable);

        verify(permisoRepository).buscarConFiltros("nota", true, pageable);
    }

    private Permiso permiso(String codigo, String nombre, Boolean activo) {
        Permiso permiso = new Permiso();
        permiso.setCodigo(codigo);
        permiso.setNombre(nombre);
        permiso.setDescripcion("Descripcion");
        permiso.setActivo(activo);
        return permiso;
    }
}
