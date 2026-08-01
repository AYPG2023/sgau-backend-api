package com.umg.sgau.estadogeneral.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.model.EstadoCuentaEstudiante;
import com.umg.sgau.colegiatura.service.ColegiaturaService;
import com.umg.sgau.estadogeneral.model.EstadoGeneralEstudiante;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.historialacademico.dto.HistorialAcademicoResponseDTO;
import com.umg.sgau.historialacademico.dto.HistorialCursoResponseDTO;
import com.umg.sgau.historialacademico.service.HistorialAcademicoService;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.service.InscripcionService;
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
class EstadoGeneralEstudianteServiceImplTest {

    @Mock
    private EstudianteService estudianteService;

    @Mock
    private HistorialAcademicoService historialAcademicoService;

    @Mock
    private InscripcionService inscripcionService;

    @Mock
    private ColegiaturaService colegiaturaService;

    private EstadoGeneralEstudianteServiceImpl estadoGeneralService;

    @BeforeEach
    void setUp() {
        estadoGeneralService = new EstadoGeneralEstudianteServiceImpl(
                estudianteService,
                historialAcademicoService,
                inscripcionService,
                colegiaturaService);
    }

    @Test
    void generaEstadoGeneralDeEstudianteExistente() {
        prepararEscenario(estudiante(true), historial(List.of(curso("APROBADO"))), cuentaSinSaldo(), inscripciones(true));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estudiante().getId()).isEqualTo(1L);
        assertThat(estado.historialAcademico().getPromedioGeneral()).isEqualByComparingTo("85.00");
        assertThat(estado.estadoFinanciero()).isEqualTo("AL_DIA");
        assertThat(estado.estadoGeneral()).isEqualTo("REGULAR");
    }

    @Test
    void rechazaEstudianteInexistente() {
        when(estudianteService.obtenerPorId(99L)).thenThrow(new EstudianteNoEncontradoException(99L));

        assertThatThrownBy(() -> estadoGeneralService.obtenerEstadoGeneral(99L))
                .isInstanceOf(EstudianteNoEncontradoException.class);
    }

    @Test
    void rechazaIdentificadorInvalido() {
        assertThatThrownBy(() -> estadoGeneralService.obtenerEstadoGeneral(0L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(estudianteService, never()).obtenerPorId(0L);
    }

    @Test
    void permiteEstudianteInactivoConHistorialYSaldos() {
        prepararEscenario(estudiante(false), historial(List.of(curso("REPROBADO"))), cuentaConSaldo(), inscripciones(false));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estudiante().getActivo()).isFalse();
        assertThat(estado.estadoAcademico()).isEqualTo("INACTIVO");
        assertThat(estado.estadoFinanciero()).isEqualTo("PENDIENTE");
        assertThat(estado.estadoGeneral()).isEqualTo("INACTIVO");
        assertThat(estado.estadoCuenta().saldoPendiente()).isEqualByComparingTo("300.00");
    }

    @Test
    void generaRespuestaSinInformacionAcademicaNiFinanciera() {
        prepararEscenario(estudiante(true), historial(List.of()), cuentaVacia(), List.of());

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estadoAcademico()).isEqualTo("SIN_INFORMACION");
        assertThat(estado.estadoFinanciero()).isEqualTo("SIN_REGISTROS");
        assertThat(estado.estadoGeneral()).isEqualTo("SIN_INFORMACION");
        assertThat(estado.totalInscripciones()).isZero();
    }

    @Test
    void cuentaCursosAprobadosReprobadosYEnCurso() {
        prepararEscenario(estudiante(true), historial(List.of(
                curso("APROBADO"),
                curso("REPROBADO"),
                curso("EN_CURSO"))), cuentaSinSaldo(), inscripciones(true));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.historialAcademico().getCursosAprobados()).isEqualTo(1);
        assertThat(estado.historialAcademico().getCursosReprobados()).isEqualTo(1);
        assertThat(estado.cursosEnCurso()).isEqualTo(1);
        assertThat(estado.estadoAcademico()).isEqualTo("CON_CURSOS_REPROBADOS");
    }

    @Test
    void determinaEstadoFinancieroAlDia() {
        prepararEscenario(estudiante(true), historial(List.of(curso("APROBADO"))), cuentaSinSaldo(), inscripciones(false));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estadoFinanciero()).isEqualTo("AL_DIA");
    }

    @Test
    void determinaEstadoFinancieroPendiente() {
        prepararEscenario(estudiante(true), historial(List.of(curso("APROBADO"))), cuentaConSaldo(), inscripciones(false));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estadoFinanciero()).isEqualTo("PENDIENTE");
    }

    @Test
    void determinaEstadoGeneralConPendientesAcademicos() {
        prepararEscenario(estudiante(true), historial(List.of(curso("REPROBADO"))), cuentaSinSaldo(), inscripciones(false));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estadoGeneral()).isEqualTo("CON_PENDIENTES_ACADEMICOS");
    }

    @Test
    void determinaEstadoGeneralConSaldoPendiente() {
        prepararEscenario(estudiante(true), historial(List.of(curso("APROBADO"))), cuentaConSaldo(), inscripciones(false));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estadoGeneral()).isEqualTo("CON_SALDO_PENDIENTE");
    }

    @Test
    void determinaEstadoGeneralConPendientesAcademicosYFinancieros() {
        prepararEscenario(estudiante(true), historial(List.of(curso("REPROBADO"))), cuentaConSaldo(), inscripciones(false));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.estadoGeneral()).isEqualTo("CON_PENDIENTES_ACADEMICOS_Y_FINANCIEROS");
    }

    @Test
    void cuentaInscripcionesActivasYCicloMasReciente() {
        prepararEscenario(estudiante(true), historial(List.of(curso("EN_CURSO"))), cuentaVacia(), List.of(
                inscripcion(2025, false),
                inscripcion(2026, true)));

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.totalInscripciones()).isEqualTo(2);
        assertThat(estado.inscripcionesActivas()).isEqualTo(1);
        assertThat(estado.cicloMasReciente()).isEqualTo(2026);
    }

    @Test
    void cuentaCargosPagadosYConservaDosDecimales() {
        prepararEscenario(estudiante(true), historial(List.of()), cuentaMixta(), List.of());

        EstadoGeneralEstudiante estado = estadoGeneralService.obtenerEstadoGeneral(1L);

        assertThat(estado.cantidadPagadas()).isEqualTo(1);
        assertThat(estado.estadoCuenta().totalCargos()).isEqualByComparingTo("900.00");
        assertThat(estado.estadoCuenta().totalPagado()).isEqualByComparingTo("600.00");
        assertThat(estado.estadoCuenta().saldoPendiente()).isEqualByComparingTo("300.00");
    }

    private void prepararEscenario(
            Estudiante estudiante,
            HistorialAcademicoResponseDTO historial,
            EstadoCuentaEstudiante cuenta,
            List<Inscripcion> inscripciones) {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante);
        when(historialAcademicoService.generarHistorial(1L)).thenReturn(historial);
        when(colegiaturaService.generarEstadoCuenta(1L)).thenReturn(cuenta);
        when(inscripcionService.historialPorEstudiante(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(inscripciones));
    }

    private HistorialAcademicoResponseDTO historial(List<HistorialCursoResponseDTO> cursos) {
        int aprobados = (int) cursos.stream().filter(curso -> "APROBADO".equals(curso.getResultado())).count();
        int reprobados = (int) cursos.stream().filter(curso -> "REPROBADO".equals(curso.getResultado())).count();
        int sinCalificacion = (int) cursos.stream().filter(curso -> "SIN_CALIFICACION".equals(curso.getResultado())).count();

        return HistorialAcademicoResponseDTO.builder()
                .estudianteId(1L)
                .nombreCompleto("Ana Perez")
                .estudianteActivo(true)
                .promedioGeneral(cursos.isEmpty() ? new BigDecimal("0.00") : new BigDecimal("85.00"))
                .totalCursos(cursos.size())
                .cursosAprobados(aprobados)
                .cursosReprobados(reprobados)
                .cursosSinCalificacion(sinCalificacion)
                .detalleCursos(cursos)
                .build();
    }

    private HistorialCursoResponseDTO curso(String resultado) {
        return HistorialCursoResponseDTO.builder()
                .inscripcionId(1L)
                .cursoId(100L)
                .codigoCurso("CUR-100")
                .nombreCurso("Curso 100")
                .cicloAnio(2026)
                .promedioCurso(new BigDecimal("85.00"))
                .resultado(resultado)
                .notas(List.of())
                .build();
    }

    private EstadoCuentaEstudiante cuentaVacia() {
        return new EstadoCuentaEstudiante(1L, "Ana Perez",
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                0, 0, List.of());
    }

    private EstadoCuentaEstudiante cuentaSinSaldo() {
        return new EstadoCuentaEstudiante(1L, "Ana Perez",
                new BigDecimal("500.00"), new BigDecimal("500.00"), new BigDecimal("0.00"),
                1, 0, List.of(colegiatura("500.00", "500.00", "0.00", "PAGADA")));
    }

    private EstadoCuentaEstudiante cuentaConSaldo() {
        return new EstadoCuentaEstudiante(1L, "Ana Perez",
                new BigDecimal("500.00"), new BigDecimal("200.00"), new BigDecimal("300.00"),
                1, 1, List.of(colegiatura("500.00", "200.00", "300.00", "PARCIAL")));
    }

    private EstadoCuentaEstudiante cuentaMixta() {
        return new EstadoCuentaEstudiante(1L, "Ana Perez",
                new BigDecimal("900.00"), new BigDecimal("600.00"), new BigDecimal("300.00"),
                2, 1, List.of(
                colegiatura("500.00", "500.00", "0.00", "PAGADA"),
                colegiatura("400.00", "100.00", "300.00", "PARCIAL")));
    }

    private Colegiatura colegiatura(String total, String pagado, String saldo, String estado) {
        return Colegiatura.builder()
                .id(1L)
                .estudianteId(1L)
                .cicloAnio(2026)
                .concepto("ENERO")
                .montoTotal(new BigDecimal(total))
                .montoPagado(new BigDecimal(pagado))
                .saldoPendiente(new BigDecimal(saldo))
                .fechaEmision(LocalDate.of(2026, 1, 1))
                .fechaVencimiento(LocalDate.of(2026, 1, 31))
                .estado(estado)
                .activo(true)
                .build();
    }

    private List<Inscripcion> inscripciones(boolean activa) {
        return List.of(inscripcion(2026, activa));
    }

    private Inscripcion inscripcion(Integer cicloAnio, Boolean activa) {
        return Inscripcion.builder()
                .id(1L)
                .estudianteId(1L)
                .carreraId(10L)
                .cursoId(100L)
                .grado("Primero")
                .seccion("A")
                .cicloAnio(cicloAnio)
                .fechaInscripcion(LocalDate.of(cicloAnio, 1, 1))
                .estado(Boolean.TRUE.equals(activa) ? "ACTIVA" : "ANULADA")
                .activo(activa)
                .build();
    }

    private Estudiante estudiante(Boolean activo) {
        return Estudiante.builder()
                .id(1L)
                .codigoEstudiantil("EST-1")
                .numeroIdentificacion("ID-1")
                .nombres("Ana")
                .apellidos("Perez")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .correo("ana@sgau.test")
                .activo(activo)
                .build();
    }
}
