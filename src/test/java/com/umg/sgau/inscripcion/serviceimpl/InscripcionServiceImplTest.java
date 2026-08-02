package com.umg.sgau.inscripcion.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.service.CursoService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.exception.CursoInactivoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.CursoInvalidoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.CursoNoPerteneceCarreraException;
import com.umg.sgau.inscripcion.exception.EstudianteInactivoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.EstudianteInvalidoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.InscripcionDuplicadaException;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import java.time.LocalDate;
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
class InscripcionServiceImplTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private EstudianteService estudianteService;

    @Mock
    private CursoService cursoService;

    private InscripcionServiceImpl inscripcionService;

    @BeforeEach
    void setUp() {
        inscripcionService = new InscripcionServiceImpl(
                inscripcionRepository,
                estudianteService,
                cursoService);
    }

    @Test
    void registrarConEstudianteYCursoActivosGuardaInscripcion() {
        Inscripcion inscripcion = inscripcionNueva(1L, 10L, 100L);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 10L, true));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inscripcion registrada = inscripcionService.registrar(inscripcion);

        assertThat(registrada.getEstudianteId()).isEqualTo(1L);
        assertThat(registrada.getCursoId()).isEqualTo(100L);
        verify(inscripcionRepository).save(inscripcion);
    }

    @Test
    void registrarRechazaEstudianteInexistente() {
        Inscripcion inscripcion = inscripcionNueva(99L, 10L, 100L);
        when(estudianteService.obtenerPorId(99L)).thenThrow(new EstudianteNoEncontradoException(99L));

        assertThatThrownBy(() -> inscripcionService.registrar(inscripcion))
                .isInstanceOf(EstudianteInvalidoParaInscripcionException.class);

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void registrarRechazaEstudianteInactivo() {
        Inscripcion inscripcion = inscripcionNueva(1L, 10L, 100L);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, false));

        assertThatThrownBy(() -> inscripcionService.registrar(inscripcion))
                .isInstanceOf(EstudianteInactivoParaInscripcionException.class);

        verify(cursoService, never()).obtenerPorId(100L);
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void registrarRechazaCursoInexistente() {
        Inscripcion inscripcion = inscripcionNueva(1L, 10L, 100L);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenThrow(new CursoNoEncontradoException(100L));

        assertThatThrownBy(() -> inscripcionService.registrar(inscripcion))
                .isInstanceOf(CursoInvalidoParaInscripcionException.class);

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void registrarRechazaCursoInactivo() {
        Inscripcion inscripcion = inscripcionNueva(1L, 10L, 100L);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 10L, false));

        assertThatThrownBy(() -> inscripcionService.registrar(inscripcion))
                .isInstanceOf(CursoInactivoParaInscripcionException.class);

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void registrarRechazaCursoDeOtraCarrera() {
        Inscripcion inscripcion = inscripcionNueva(1L, 10L, 100L);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 20L, true));

        assertThatThrownBy(() -> inscripcionService.registrar(inscripcion))
                .isInstanceOf(CursoNoPerteneceCarreraException.class);

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void registrarRechazaDuplicadoActivoConCurso() {
        Inscripcion inscripcion = inscripcionNueva(1L, 10L, 100L);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 10L, true));
        when(inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
                1L, 100L, 2026)).thenReturn(true);

        assertThatThrownBy(() -> inscripcionService.registrar(inscripcion))
                .isInstanceOf(InscripcionDuplicadaException.class);

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void registrarPermiteNuevaInscripcionSiDuplicadoActivoNoExiste() {
        Inscripcion inscripcion = inscripcionNueva(1L, 10L, 100L);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 10L, true));
        when(inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
                1L, 100L, 2026)).thenReturn(false);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inscripcion registrada = inscripcionService.registrar(inscripcion);

        assertThat(registrada).isSameAs(inscripcion);
    }

    @Test
    void historialPorEstudianteValidaExistencia() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionRepository.findByEstudianteId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(inscripcionExistente(1L, true)), pageable, 1));

        Page<Inscripcion> resultado = inscripcionService.historialPorEstudiante(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    void inscripcionesPorCursoValidaExistencia() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 10L, true));
        when(inscripcionRepository.findByCursoId(100L, pageable))
                .thenReturn(new PageImpl<>(List.of(inscripcionExistente(1L, true)), pageable, 1));

        Page<Inscripcion> resultado = inscripcionService.inscripcionesPorCurso(100L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    void inscripcionesActivasPorEstudianteExcluyeInactivasDesdeRepositorio() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionRepository.findByEstudianteIdAndActivoTrue(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(inscripcionExistente(1L, true)), pageable, 1));

        Page<Inscripcion> resultado = inscripcionService.inscripcionesActivasPorEstudiante(1L, pageable);

        assertThat(resultado.getContent()).extracting(Inscripcion::getActivo).containsExactly(true);
    }

    @Test
    void actualizarNoBorraCursoCuandoNoVieneEnSolicitud() {
        Inscripcion existente = inscripcionExistente(1L, true);
        Inscripcion cambios = inscripcionNueva(1L, 10L, null);
        cambios.setGrado("Segundo");
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 10L, true));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inscripcion actualizada = inscripcionService.actualizar(1L, cambios);

        assertThat(actualizada.getCursoId()).isEqualTo(100L);
        assertThat(actualizada.getGrado()).isEqualTo("Segundo");
    }

    @Test
    void anularNoEliminaFisicamente() {
        Inscripcion existente = inscripcionExistente(1L, true);
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inscripcion anulada = inscripcionService.anular(1L, "Retiro");

        assertThat(anulada.getActivo()).isFalse();
        assertThat(anulada.getEstado()).isEqualTo("ANULADA");
        verify(inscripcionRepository, never()).delete(any(Inscripcion.class));
    }

    @Test
    void reactivarRechazaDuplicadoActivo() {
        Inscripcion inactiva = inscripcionExistente(1L, false);
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inactiva));
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, 10L, true));
        when(inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrueAndIdNot(
                1L, 100L, 2026, 1L)).thenReturn(true);

        assertThatThrownBy(() -> inscripcionService.reactivar(1L))
                .isInstanceOf(InscripcionDuplicadaException.class);

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void existeInscripcionActivaConsultaPorEstudianteCursoYCiclo() {
        when(inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
                1L, 100L, 2026)).thenReturn(true);

        boolean existe = inscripcionService.existeInscripcionActiva(1L, 100L, 2026);

        assertThat(existe).isTrue();
    }

    private Inscripcion inscripcionNueva(Long estudianteId, Long carreraId, Long cursoId) {
        return Inscripcion.builder()
                .estudianteId(estudianteId)
                .carreraId(carreraId)
                .cursoId(cursoId)
                .grado("Primero")
                .seccion("A")
                .cicloAnio(2026)
                .fechaInscripcion(LocalDate.of(2026, 1, 10))
                .observaciones("Ingreso")
                .build();
    }

    private Inscripcion inscripcionExistente(Long id, Boolean activo) {
        return Inscripcion.builder()
                .id(id)
                .estudianteId(1L)
                .carreraId(10L)
                .cursoId(100L)
                .grado("Primero")
                .seccion("A")
                .cicloAnio(2026)
                .fechaInscripcion(LocalDate.of(2026, 1, 10))
                .estado(Boolean.TRUE.equals(activo) ? "ACTIVA" : "ANULADA")
                .observaciones("Ingreso")
                .activo(activo)
                .build();
    }

    private Estudiante estudiante(Long id, Boolean activo) {
        return Estudiante.builder()
                .id(id)
                .codigoEstudiantil("EST-" + id)
                .numeroIdentificacion("ID-" + id)
                .nombres("Estudiante")
                .apellidos("Prueba")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .correo("estudiante" + id + "@sgau.test")
                .activo(activo)
                .build();
    }

    private Curso curso(Long id, Long carreraId, Boolean activo) {
        return Curso.builder()
                .id(id)
                .codigo("CUR-" + id)
                .nombre("Curso " + id)
                .descripcion("Curso de prueba")
                .creditos(5)
                .horasSemanales(6)
                .carreraId(carreraId)
                .cicloAnio(2026)
                .activo(activo)
                .build();
    }
}
