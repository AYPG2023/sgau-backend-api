package com.umg.sgau.nota.serviceimpl;

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
import com.umg.sgau.inscripcion.service.InscripcionService;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.nota.exception.CursoInactivoParaNotaException;
import com.umg.sgau.nota.exception.CursoInvalidoParaNotaException;
import com.umg.sgau.nota.exception.EstudianteInactivoParaNotaException;
import com.umg.sgau.nota.exception.EstudianteInvalidoParaNotaException;
import com.umg.sgau.nota.exception.InscripcionActivaNoEncontradaException;
import com.umg.sgau.nota.exception.NotaDuplicadaException;
import com.umg.sgau.nota.repository.NotaRepository;
import java.math.BigDecimal;
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
class NotaServiceImplTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private EstudianteService estudianteService;

    @Mock
    private CursoService cursoService;

    @Mock
    private InscripcionService inscripcionService;

    private NotaServiceImpl notaService;

    @BeforeEach
    void setUp() {
        notaService = new NotaServiceImpl(
                notaRepository,
                estudianteService,
                cursoService,
                inscripcionService);
    }

    @Test
    void crearNotaConEstudianteCursoEInscripcionActivosGuardaNormalizada() {
        Nota nota = notaNueva(" parcial ");
        prepararReferenciasActivas(nota);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(true);
        when(notaRepository.save(any(Nota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Nota creada = notaService.crear(nota);

        assertThat(creada.getTipoEvaluacion()).isEqualTo("PARCIAL");
        assertThat(creada.getActivo()).isTrue();
        verify(notaRepository).save(nota);
    }

    @Test
    void crearRechazaEstudianteInexistente() {
        Nota nota = notaNueva("PARCIAL");
        when(estudianteService.obtenerPorId(1L)).thenThrow(new EstudianteNoEncontradoException(1L));

        assertThatThrownBy(() -> notaService.crear(nota))
                .isInstanceOf(EstudianteInvalidoParaNotaException.class);

        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void crearRechazaEstudianteInactivo() {
        Nota nota = notaNueva("PARCIAL");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, false));

        assertThatThrownBy(() -> notaService.crear(nota))
                .isInstanceOf(EstudianteInactivoParaNotaException.class);

        verify(cursoService, never()).obtenerPorId(100L);
        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void crearRechazaCursoInexistente() {
        Nota nota = notaNueva("PARCIAL");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenThrow(new CursoNoEncontradoException(100L));

        assertThatThrownBy(() -> notaService.crear(nota))
                .isInstanceOf(CursoInvalidoParaNotaException.class);

        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void crearRechazaCursoInactivo() {
        Nota nota = notaNueva("PARCIAL");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, false));

        assertThatThrownBy(() -> notaService.crear(nota))
                .isInstanceOf(CursoInactivoParaNotaException.class);

        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void crearRechazaCuandoNoExisteInscripcionActiva() {
        Nota nota = notaNueva("PARCIAL");
        prepararReferenciasActivas(nota);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(false);

        assertThatThrownBy(() -> notaService.crear(nota))
                .isInstanceOf(InscripcionActivaNoEncontradaException.class);

        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void crearRechazaDuplicadoActivo() {
        Nota nota = notaNueva(" parcial ");
        prepararReferenciasActivas(nota);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(true);
        when(notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
                1L, 100L, 2026, "PARCIAL")).thenReturn(true);

        assertThatThrownBy(() -> notaService.crear(nota))
                .isInstanceOf(NotaDuplicadaException.class);

        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void crearPermiteNuevaNotaSiNoHayDuplicadoActivo() {
        Nota nota = notaNueva("PARCIAL");
        prepararReferenciasActivas(nota);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(true);
        when(notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
                1L, 100L, 2026, "PARCIAL")).thenReturn(false);
        when(notaRepository.save(any(Nota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Nota creada = notaService.crear(nota);

        assertThat(creada).isSameAs(nota);
    }

    @Test
    void actualizarSoloCamposPermitidos() {
        Nota existente = notaExistente(1L, true, "PARCIAL", "80.00");
        Nota cambios = notaNueva(" final ");
        cambios.setEstudianteId(99L);
        cambios.setCursoId(999L);
        cambios.setCicloAnio(2030);
        cambios.setCalificacion(new BigDecimal("95.00"));
        cambios.setObservaciones("Mejorada");
        when(notaRepository.findById(1L)).thenReturn(Optional.of(existente));
        prepararReferenciasActivas(existente);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(true);
        when(notaRepository.save(any(Nota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Nota actualizada = notaService.actualizar(1L, cambios);

        assertThat(actualizada.getEstudianteId()).isEqualTo(1L);
        assertThat(actualizada.getCursoId()).isEqualTo(100L);
        assertThat(actualizada.getCicloAnio()).isEqualTo(2026);
        assertThat(actualizada.getTipoEvaluacion()).isEqualTo("FINAL");
        assertThat(actualizada.getCalificacion()).isEqualByComparingTo("95.00");
        assertThat(actualizada.getObservaciones()).isEqualTo("Mejorada");
    }

    @Test
    void actualizarVerificaDuplicidadAlCambiarTipoEvaluacion() {
        Nota existente = notaExistente(1L, true, "PARCIAL", "80.00");
        Nota cambios = notaNueva("FINAL");
        when(notaRepository.findById(1L)).thenReturn(Optional.of(existente));
        prepararReferenciasActivas(existente);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(true);
        when(notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
                1L, 100L, 2026, "FINAL", 1L)).thenReturn(true);

        assertThatThrownBy(() -> notaService.actualizar(1L, cambios))
                .isInstanceOf(NotaDuplicadaException.class);

        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void actualizarRechazaNotaSiLaInscripcionFueAnulada() {
        Nota existente = notaExistente(1L, true, "PARCIAL", "80.00");
        Nota cambios = notaNueva("PARCIAL");
        when(notaRepository.findById(1L)).thenReturn(Optional.of(existente));
        prepararReferenciasActivas(existente);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(false);

        assertThatThrownBy(() -> notaService.actualizar(1L, cambios))
                .isInstanceOf(InscripcionActivaNoEncontradaException.class);
        verify(notaRepository, never()).save(any(Nota.class));
    }

    @Test
    void crearRechazaCicloDistintoAlCursoAunqueExistaOtraInscripcion() {
        Nota nota = notaNueva("PARCIAL");
        nota.setCicloAnio(2027);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));

        assertThatThrownBy(() -> notaService.crear(nota))
                .isInstanceOf(com.umg.sgau.nota.exception.NotaInvalidaException.class)
                .hasMessageContaining("ciclo");
        verify(inscripcionService, never()).existeInscripcionActiva(any(), any(), any());
    }

    @Test
    void obtenerNotasActivasPorEstudianteExcluyeInactivas() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(notaRepository.findByEstudianteIdAndActivoTrue(1L)).thenReturn(List.of(
                notaExistente(1L, true, "PARCIAL", "80.00")));

        List<Nota> notas = notaService.obtenerNotasActivasPorEstudiante(1L);

        assertThat(notas).extracting(Nota::getActivo).containsExactly(true);
    }

    @Test
    void obtenerNotasPorCursoValidaCurso() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));
        when(notaRepository.findByCursoId(100L, pageable))
                .thenReturn(new PageImpl<>(List.of(notaExistente(1L, true, "PARCIAL", "80.00")), pageable, 1));

        Page<Nota> notas = notaService.obtenerNotasPorCurso(100L, pageable);

        assertThat(notas.getContent()).hasSize(1);
    }

    @Test
    void calcularPromedioRedondeaADosDecimales() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(notaRepository.findByEstudianteIdAndActivoTrue(1L)).thenReturn(List.of(
                notaExistente(1L, true, "PARCIAL", "80.00"),
                notaExistente(2L, true, "FINAL", "89.99")));

        BigDecimal promedio = notaService.calcularPromedioGeneral(1L);

        assertThat(promedio).isEqualByComparingTo("85.00");
    }

    @Test
    void calcularPromedioDevuelveCeroSinNotasActivas() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(notaRepository.findByEstudianteIdAndActivoTrue(1L)).thenReturn(List.of());

        BigDecimal promedio = notaService.calcularPromedioGeneral(1L);

        assertThat(promedio).isEqualByComparingTo("0.00");
    }

    @Test
    void inhabilitarNoEliminaFisicamente() {
        Nota nota = notaExistente(1L, true, "PARCIAL", "80.00");
        when(notaRepository.findById(1L)).thenReturn(Optional.of(nota));
        when(notaRepository.save(any(Nota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Nota inactiva = notaService.cambiarEstado(1L, false);

        assertThat(inactiva.getActivo()).isFalse();
        verify(notaRepository).save(nota);
        verify(notaRepository, never()).delete(any(Nota.class));
    }

    @Test
    void reactivarValidaReferenciasInscripcionYDuplicidad() {
        Nota nota = notaExistente(1L, false, "PARCIAL", "80.00");
        when(notaRepository.findById(1L)).thenReturn(Optional.of(nota));
        prepararReferenciasActivas(nota);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(true);
        when(notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
                1L, 100L, 2026, "PARCIAL", 1L)).thenReturn(false);
        when(notaRepository.save(any(Nota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Nota reactivada = notaService.cambiarEstado(1L, true);

        assertThat(reactivada.getActivo()).isTrue();
    }

    @Test
    void reactivarRechazaDuplicadoActivo() {
        Nota nota = notaExistente(1L, false, "PARCIAL", "80.00");
        when(notaRepository.findById(1L)).thenReturn(Optional.of(nota));
        prepararReferenciasActivas(nota);
        when(inscripcionService.existeInscripcionActiva(1L, 100L, 2026)).thenReturn(true);
        when(notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
                1L, 100L, 2026, "PARCIAL", 1L)).thenReturn(true);

        assertThatThrownBy(() -> notaService.cambiarEstado(1L, true))
                .isInstanceOf(NotaDuplicadaException.class);

        verify(notaRepository, never()).save(any(Nota.class));
    }

    private void prepararReferenciasActivas(Nota nota) {
        when(estudianteService.obtenerPorId(nota.getEstudianteId()))
                .thenReturn(estudiante(nota.getEstudianteId(), true));
        when(cursoService.obtenerPorId(nota.getCursoId()))
                .thenReturn(curso(nota.getCursoId(), true));
    }

    private Nota notaNueva(String tipoEvaluacion) {
        return Nota.builder()
                .estudianteId(1L)
                .cursoId(100L)
                .cicloAnio(2026)
                .tipoEvaluacion(tipoEvaluacion)
                .calificacion(new BigDecimal("80.00"))
                .observaciones("Correcta")
                .build();
    }

    private Nota notaExistente(Long id, Boolean activo, String tipoEvaluacion, String calificacion) {
        return Nota.builder()
                .id(id)
                .estudianteId(1L)
                .cursoId(100L)
                .cicloAnio(2026)
                .tipoEvaluacion(tipoEvaluacion)
                .calificacion(new BigDecimal(calificacion))
                .observaciones("Registrada")
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

    private Curso curso(Long id, Boolean activo) {
        return Curso.builder()
                .id(id)
                .codigo("CUR-" + id)
                .nombre("Curso " + id)
                .descripcion("Curso de prueba")
                .creditos(5)
                .horasSemanales(6)
                .carreraId(10L)
                .cicloAnio(2026)
                .activo(activo)
                .build();
    }
}
