package com.umg.sgau.curso.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.exception.CarreraNoEncontradaException;
import com.umg.sgau.carrera.service.CarreraService;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CarreraCursoInvalidaException;
import com.umg.sgau.curso.exception.CarreraInactivaParaCursoException;
import com.umg.sgau.curso.exception.CursoInactivoException;
import com.umg.sgau.curso.exception.CursoSinDocenteException;
import com.umg.sgau.curso.exception.DocenteCursoInvalidoException;
import com.umg.sgau.curso.exception.DocenteInactivoParaCursoException;
import com.umg.sgau.curso.repository.CursoRepository;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.exception.DocenteNoEncontradoException;
import com.umg.sgau.docente.service.DocenteService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CursoServiceImplTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private CarreraService carreraService;

    @Mock
    private DocenteService docenteService;

    private CursoServiceImpl cursoService;

    @BeforeEach
    void setUp() {
        cursoService = new CursoServiceImpl(cursoRepository, carreraService, docenteService);
    }

    @Test
    void crearCursoConCarreraActivaGuardaCursoSinDocente() {
        Curso curso = cursoNuevo(1L);
        curso.setDocenteId(5L);
        when(carreraService.obtenerPorId(1L)).thenReturn(carrera(1L, true));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Curso creado = cursoService.crear(curso);

        assertThat(creado.getCodigo()).isEqualTo("PROG-1");
        assertThat(creado.getDocenteId()).isNull();
        verify(cursoRepository).save(curso);
    }

    @Test
    void crearCursoRechazaCarreraInexistente() {
        Curso curso = cursoNuevo(99L);
        when(carreraService.obtenerPorId(99L)).thenThrow(new CarreraNoEncontradaException(99L));

        assertThatThrownBy(() -> cursoService.crear(curso))
                .isInstanceOf(CarreraCursoInvalidaException.class);

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void crearCursoRechazaCarreraInactiva() {
        Curso curso = cursoNuevo(1L);
        when(carreraService.obtenerPorId(1L)).thenReturn(carrera(1L, false));

        assertThatThrownBy(() -> cursoService.crear(curso))
                .isInstanceOf(CarreraInactivaParaCursoException.class);

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void asignarDocenteActivoACursoActivo() {
        Curso curso = cursoExistente(1L, 1L, null, true);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(docenteService.obtenerPorId(5L)).thenReturn(docente(5L, true));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Curso actualizado = cursoService.asignarDocente(1L, 5L);

        assertThat(actualizado.getDocenteId()).isEqualTo(5L);
        verify(cursoRepository).save(curso);
    }

    @Test
    void asignarDocenteRechazaDocenteInexistente() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoExistente(1L, 1L, null, true)));
        when(docenteService.obtenerPorId(99L)).thenThrow(new DocenteNoEncontradoException(99L));

        assertThatThrownBy(() -> cursoService.asignarDocente(1L, 99L))
                .isInstanceOf(DocenteCursoInvalidoException.class);

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void asignarDocenteRechazaDocenteInactivo() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoExistente(1L, 1L, null, true)));
        when(docenteService.obtenerPorId(5L)).thenReturn(docente(5L, false));

        assertThatThrownBy(() -> cursoService.asignarDocente(1L, 5L))
                .isInstanceOf(DocenteInactivoParaCursoException.class);

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void asignarDocenteRechazaCursoInactivo() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoExistente(1L, 1L, null, false)));

        assertThatThrownBy(() -> cursoService.asignarDocente(1L, 5L))
                .isInstanceOf(CursoInactivoException.class);

        verify(docenteService, never()).obtenerPorId(5L);
        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void obtenerDocenteAsignadoDevuelveDocente() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoExistente(1L, 1L, 5L, true)));
        when(docenteService.obtenerPorId(5L)).thenReturn(docente(5L, true));

        Docente docente = cursoService.obtenerDocenteAsignado(1L);

        assertThat(docente.getId()).isEqualTo(5L);
    }

    @Test
    void obtenerDocenteAsignadoRechazaCursoSinDocente() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoExistente(1L, 1L, null, true)));

        assertThatThrownBy(() -> cursoService.obtenerDocenteAsignado(1L))
                .isInstanceOf(CursoSinDocenteException.class);
    }

    @Test
    void obtenerCursosPorDocenteExcluyeInactivos() {
        when(docenteService.obtenerPorId(5L)).thenReturn(docente(5L, true));
        when(cursoRepository.findByDocenteId(5L)).thenReturn(List.of(
                cursoExistente(1L, 1L, 5L, true),
                cursoExistente(2L, 1L, 5L, false)));

        List<Curso> cursos = cursoService.obtenerCursosPorDocente(5L);

        assertThat(cursos).extracting(Curso::getId).containsExactly(1L);
    }

    @Test
    void obtenerCursosActivosPorCarreraExcluyeInactivos() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(carreraService.obtenerPorId(1L)).thenReturn(carrera(1L, true));
        when(cursoRepository.findByCarreraId(1L, pageable)).thenReturn(new PageImpl<>(List.of(
                cursoExistente(1L, 1L, null, true),
                cursoExistente(2L, 1L, null, false)), pageable, 2));

        Page<Curso> cursos = cursoService.obtenerCursosActivosPorCarrera(1L, pageable);

        assertThat(cursos.getContent()).extracting(Curso::getId).containsExactly(1L);
    }

    @Test
    void actualizarCursoNoBorraDocenteAsignado() {
        Curso existente = cursoExistente(1L, 1L, 5L, true);
        Curso cambios = cursoNuevo(2L);
        cambios.setCodigo("BD-1");
        cambios.setNombre("Bases de Datos I");
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(carreraService.obtenerPorId(2L)).thenReturn(carrera(2L, true));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Curso actualizado = cursoService.actualizar(1L, cambios);

        assertThat(actualizado.getCarreraId()).isEqualTo(2L);
        assertThat(actualizado.getDocenteId()).isEqualTo(5L);
    }

    @Test
    void retirarDocenteSoloLimpiaAsignacion() {
        Curso curso = cursoExistente(1L, 1L, 5L, true);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Curso actualizado = cursoService.retirarDocente(1L);

        assertThat(actualizado.getDocenteId()).isNull();
        assertThat(actualizado.getActivo()).isTrue();
        verify(cursoRepository).save(curso);
    }

    @Test
    void cambiarEstadoNoEliminaFisicamente() {
        Curso curso = cursoExistente(1L, 1L, 5L, true);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Curso actualizado = cursoService.cambiarEstado(1L, false);

        assertThat(actualizado.getActivo()).isFalse();
        verify(cursoRepository).save(curso);
        verify(cursoRepository, never()).delete(any(Curso.class));
    }

    private Curso cursoNuevo(Long carreraId) {
        return Curso.builder()
                .codigo(" prog-1 ")
                .nombre(" Programacion I ")
                .descripcion(" Intro ")
                .creditos(5)
                .horasSemanales(6)
                .carreraId(carreraId)
                .cicloAnio(2026)
                .build();
    }

    private Curso cursoExistente(Long id, Long carreraId, Long docenteId, Boolean activo) {
        return Curso.builder()
                .id(id)
                .codigo("PROG-" + id)
                .nombre("Programacion " + id)
                .descripcion("Intro")
                .creditos(5)
                .horasSemanales(6)
                .carreraId(carreraId)
                .docenteId(docenteId)
                .cicloAnio(2026)
                .activo(activo)
                .build();
    }

    private Carrera carrera(Long id, Boolean activo) {
        return Carrera.builder()
                .id(id)
                .codigo("CAR-" + id)
                .nombre("Carrera " + id)
                .duracionAnios(5)
                .activo(activo)
                .build();
    }

    private Docente docente(Long id, Boolean activo) {
        Docente docente = new Docente();
        docente.setId(id);
        docente.setCodigoDocente("DOC-" + id);
        docente.setNombre("Docente");
        docente.setApellido("Prueba");
        docente.setEmail("docente" + id + "@sgau.test");
        docente.setActivo(activo);
        return docente;
    }
}
