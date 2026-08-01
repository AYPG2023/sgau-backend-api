package com.umg.sgau.historialacademico.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.service.CursoService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.historialacademico.dto.HistorialAcademicoResponseDTO;
import com.umg.sgau.historialacademico.dto.HistorialCursoResponseDTO;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.service.InscripcionService;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.nota.service.NotaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class HistorialAcademicoServiceImplTest {

    @Mock
    private EstudianteService estudianteService;

    @Mock
    private InscripcionService inscripcionService;

    @Mock
    private CursoService cursoService;

    @Mock
    private NotaService notaService;

    private HistorialAcademicoServiceImpl historialService;

    @BeforeEach
    void setUp() {
        historialService = new HistorialAcademicoServiceImpl(
                estudianteService,
                inscripcionService,
                cursoService,
                notaService);
    }

    @Test
    void generarHistorialParaEstudianteExistenteIncluyeCursoYNotas() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(inscripcion(10L, 100L, 2026, true))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of(
                nota(1L, 100L, 2026, "PARCIAL", "80.00", true),
                nota(2L, 100L, 2026, "FINAL", "90.00", true)));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getEstudianteId()).isEqualTo(1L);
        assertThat(historial.getNombreCompleto()).isEqualTo("Ana Perez");
        assertThat(historial.getPromedioGeneral()).isEqualByComparingTo("85.00");
        assertThat(historial.getTotalCursos()).isEqualTo(1);
        assertThat(historial.getCursosAprobados()).isEqualTo(1);
        assertThat(historial.getDetalleCursos()).hasSize(1);
        assertThat(historial.getDetalleCursos().get(0).getNotas()).hasSize(2);
    }

    @Test
    void rechazaEstudianteInexistente() {
        when(estudianteService.obtenerPorId(99L)).thenThrow(new EstudianteNoEncontradoException(99L));

        assertThatThrownBy(() -> historialService.generarHistorial(99L))
                .isInstanceOf(EstudianteNoEncontradoException.class);
    }

    @Test
    void permiteHistorialDeEstudianteInactivo() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, false));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of());

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getEstudianteActivo()).isFalse();
        assertThat(historial.getTotalCursos()).isZero();
    }

    @Test
    void devuelveHistorialVacioParaEstudianteSinInscripciones() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of());

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getPromedioGeneral()).isEqualByComparingTo("0.00");
        assertThat(historial.getDetalleCursos()).isEmpty();
        assertThat(historial.getCursosSinCalificacion()).isZero();
    }

    @Test
    void incluyeCursosDeInscripcionesActivasEHistoricas() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(
                        inscripcion(10L, 100L, 2026, true),
                        inscripcion(11L, 101L, 2025, false))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of());
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));
        when(cursoService.obtenerPorId(101L)).thenReturn(curso(101L, false));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getDetalleCursos())
                .extracting(HistorialCursoResponseDTO::getCursoId)
                .containsExactly(100L, 101L);
        assertThat(historial.getDetalleCursos())
                .extracting(HistorialCursoResponseDTO::getCursoActivo)
                .containsExactly(true, false);
    }

    @Test
    void noMezclaNotasDeCiclosDistintos() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(inscripcion(10L, 100L, 2026, true))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of(
                nota(1L, 100L, 2026, "PARCIAL", "80.00", true),
                nota(2L, 100L, 2025, "FINAL", "10.00", true)));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getDetalleCursos().get(0).getPromedioCurso()).isEqualByComparingTo("80.00");
        assertThat(historial.getDetalleCursos().get(0).getNotas()).hasSize(1);
    }

    @Test
    void calculaPromedioGeneralPorCursoSinPonderarPorCantidadDeEvaluaciones() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(
                        inscripcion(10L, 100L, 2026, true),
                        inscripcion(11L, 101L, 2026, true))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of(
                nota(1L, 100L, 2026, "A", "100.00", true),
                nota(2L, 100L, 2026, "B", "100.00", true),
                nota(3L, 100L, 2026, "C", "100.00", true),
                nota(4L, 101L, 2026, "A", "50.00", true)));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));
        when(cursoService.obtenerPorId(101L)).thenReturn(curso(101L, true));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getPromedioGeneral()).isEqualByComparingTo("75.00");
    }

    @Test
    void redondeaPromediosADosDecimales() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(inscripcion(10L, 100L, 2026, true))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of(
                nota(1L, 100L, 2026, "A", "80.00", true),
                nota(2L, 100L, 2026, "B", "81.00", true),
                nota(3L, 100L, 2026, "C", "82.00", true)));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getDetalleCursos().get(0).getPromedioCurso()).isEqualByComparingTo("81.00");
    }

    @Test
    void identificaAprobadoReprobadoSinCalificacionYEnCurso() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(
                        inscripcion(10L, 100L, 2026, false),
                        inscripcion(11L, 101L, 2026, false),
                        inscripcion(12L, 102L, 2026, false),
                        inscripcion(13L, 103L, 2026, true))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of(
                nota(1L, 100L, 2026, "FINAL", "61.00", true),
                nota(2L, 101L, 2026, "FINAL", "60.99", true)));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));
        when(cursoService.obtenerPorId(101L)).thenReturn(curso(101L, true));
        when(cursoService.obtenerPorId(102L)).thenReturn(curso(102L, true));
        when(cursoService.obtenerPorId(103L)).thenReturn(curso(103L, true));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getDetalleCursos())
                .extracting(HistorialCursoResponseDTO::getResultado)
                .containsExactly("APROBADO", "REPROBADO", "SIN_CALIFICACION", "EN_CURSO");
    }

    @Test
    void noDuplicaCursosDelMismoCicloYCacheaCurso() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(
                        inscripcion(10L, 100L, 2026, false),
                        inscripcion(11L, 100L, 2026, true))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of());
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorial(1L);

        assertThat(historial.getDetalleCursos()).hasSize(1);
        assertThat(historial.getDetalleCursos().get(0).getInscripcionId()).isEqualTo(11L);
        verify(cursoService, times(1)).obtenerPorId(100L);
    }

    @Test
    void generaHistorialPorCiclo() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(
                        inscripcion(10L, 100L, 2026, true),
                        inscripcion(11L, 101L, 2025, true))));
        when(notaService.obtenerNotasActivasPorEstudiante(1L)).thenReturn(List.of(
                nota(1L, 100L, 2026, "FINAL", "90.00", true),
                nota(2L, 101L, 2025, "FINAL", "10.00", true)));
        when(cursoService.obtenerPorId(100L)).thenReturn(curso(100L, true));

        HistorialAcademicoResponseDTO historial = historialService.generarHistorialPorCiclo(1L, 2026);

        assertThat(historial.getDetalleCursos()).hasSize(1);
        assertThat(historial.getPromedioGeneral()).isEqualByComparingTo("90.00");
    }

    private Estudiante estudiante(Long id, Boolean activo) {
        return Estudiante.builder()
                .id(id)
                .codigoEstudiantil("EST-" + id)
                .numeroIdentificacion("ID-" + id)
                .nombres("Ana")
                .apellidos("Perez")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .correo("ana" + id + "@sgau.test")
                .activo(activo)
                .build();
    }

    private Inscripcion inscripcion(Long id, Long cursoId, Integer cicloAnio, Boolean activo) {
        return Inscripcion.builder()
                .id(id)
                .estudianteId(1L)
                .carreraId(20L)
                .cursoId(cursoId)
                .grado("Primero")
                .seccion("A")
                .cicloAnio(cicloAnio)
                .fechaInscripcion(LocalDate.of(cicloAnio, 1, 10))
                .estado(Boolean.TRUE.equals(activo) ? "ACTIVA" : "ANULADA")
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
                .carreraId(20L)
                .cicloAnio(2026)
                .activo(activo)
                .build();
    }

    private Nota nota(Long id, Long cursoId, Integer cicloAnio, String tipoEvaluacion, String calificacion, Boolean activo) {
        return Nota.builder()
                .id(id)
                .estudianteId(1L)
                .cursoId(cursoId)
                .cicloAnio(cicloAnio)
                .tipoEvaluacion(tipoEvaluacion)
                .calificacion(new BigDecimal(calificacion))
                .observaciones("Nota")
                .activo(activo)
                .build();
    }
}
