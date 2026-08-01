package com.umg.sgau.colegiatura.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.exception.ColegiaturaDuplicadaException;
import com.umg.sgau.colegiatura.exception.ColegiaturaSinSaldoPendienteException;
import com.umg.sgau.colegiatura.exception.EstudianteInactivoParaColegiaturaException;
import com.umg.sgau.colegiatura.exception.EstudianteInvalidoParaColegiaturaException;
import com.umg.sgau.colegiatura.exception.PagoColegiaturaInvalidoException;
import com.umg.sgau.colegiatura.exception.PagoExcedeSaldoException;
import com.umg.sgau.colegiatura.model.EstadoCuentaEstudiante;
import com.umg.sgau.colegiatura.repository.ColegiaturaRepository;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
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
class ColegiaturaServiceImplTest {

    @Mock
    private ColegiaturaRepository colegiaturaRepository;

    @Mock
    private EstudianteService estudianteService;

    private ColegiaturaServiceImpl colegiaturaService;

    @BeforeEach
    void setUp() {
        colegiaturaService = new ColegiaturaServiceImpl(colegiaturaRepository, estudianteService);
    }

    @Test
    void crearColegiaturaParaEstudianteActivoNormalizaYGuardaCargo() {
        Colegiatura colegiatura = cargoNuevo(" mensualidad enero ", "500.005");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.save(any(Colegiatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Colegiatura creada = colegiaturaService.crear(colegiatura);

        assertThat(creada.getConcepto()).isEqualTo("MENSUALIDAD ENERO");
        assertThat(creada.getMontoTotal()).isEqualByComparingTo("500.01");
        assertThat(creada.getMontoPagado()).isEqualByComparingTo("0.00");
        assertThat(creada.getSaldoPendiente()).isEqualByComparingTo("500.01");
        assertThat(creada.getEstado()).isEqualTo("PENDIENTE");
        assertThat(creada.getActivo()).isTrue();
    }

    @Test
    void crearRechazaEstudianteInexistente() {
        Colegiatura colegiatura = cargoNuevo("ENERO", "500.00");
        when(estudianteService.obtenerPorId(1L)).thenThrow(new EstudianteNoEncontradoException(1L));

        assertThatThrownBy(() -> colegiaturaService.crear(colegiatura))
                .isInstanceOf(EstudianteInvalidoParaColegiaturaException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void crearRechazaEstudianteInactivo() {
        Colegiatura colegiatura = cargoNuevo("ENERO", "500.00");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, false));

        assertThatThrownBy(() -> colegiaturaService.crear(colegiatura))
                .isInstanceOf(EstudianteInactivoParaColegiaturaException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void crearRechazaMontoCeroONegativo() {
        Colegiatura colegiatura = cargoNuevo("ENERO", "0.00");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));

        assertThatThrownBy(() -> colegiaturaService.crear(colegiatura))
                .isInstanceOf(PagoColegiaturaInvalidoException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void crearRechazaCargoActivoDuplicado() {
        Colegiatura colegiatura = cargoNuevo(" enero ", "500.00");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrue(
                1L, 2026, "ENERO")).thenReturn(true);

        assertThatThrownBy(() -> colegiaturaService.crear(colegiatura))
                .isInstanceOf(ColegiaturaDuplicadaException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void crearPermiteNuevoRegistroSiNoHayDuplicadoActivo() {
        Colegiatura colegiatura = cargoNuevo("ENERO", "500.00");
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrue(
                1L, 2026, "ENERO")).thenReturn(false);
        when(colegiaturaRepository.save(any(Colegiatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Colegiatura creada = colegiaturaService.crear(colegiatura);

        assertThat(creada).isSameAs(colegiatura);
    }

    @Test
    void registrarPagoParcialActualizaSaldoYEstado() {
        Colegiatura colegiatura = cargoExistente(1L, true, "500.00", "0.00", "500.00", "PENDIENTE");
        when(colegiaturaRepository.findById(1L)).thenReturn(Optional.of(colegiatura));
        when(colegiaturaRepository.save(any(Colegiatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Colegiatura pagada = colegiaturaService.registrarPago(1L, new BigDecimal("200.00"));

        assertThat(pagada.getMontoPagado()).isEqualByComparingTo("200.00");
        assertThat(pagada.getSaldoPendiente()).isEqualByComparingTo("300.00");
        assertThat(pagada.getEstado()).isEqualTo("PARCIAL");
    }

    @Test
    void registrarPagoTotalMarcaComoPagada() {
        Colegiatura colegiatura = cargoExistente(1L, true, "500.00", "200.00", "300.00", "PARCIAL");
        when(colegiaturaRepository.findById(1L)).thenReturn(Optional.of(colegiatura));
        when(colegiaturaRepository.save(any(Colegiatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Colegiatura pagada = colegiaturaService.registrarPago(1L, new BigDecimal("300.00"));

        assertThat(pagada.getSaldoPendiente()).isEqualByComparingTo("0.00");
        assertThat(pagada.getEstado()).isEqualTo("PAGADA");
    }

    @Test
    void registrarPagoRechazaMontoCeroONegativo() {
        when(colegiaturaRepository.findById(1L))
                .thenReturn(Optional.of(cargoExistente(1L, true, "500.00", "0.00", "500.00", "PENDIENTE")));

        assertThatThrownBy(() -> colegiaturaService.registrarPago(1L, BigDecimal.ZERO))
                .isInstanceOf(PagoColegiaturaInvalidoException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void registrarPagoRechazaPagoMayorAlSaldo() {
        when(colegiaturaRepository.findById(1L))
                .thenReturn(Optional.of(cargoExistente(1L, true, "500.00", "0.00", "500.00", "PENDIENTE")));

        assertThatThrownBy(() -> colegiaturaService.registrarPago(1L, new BigDecimal("501.00")))
                .isInstanceOf(PagoExcedeSaldoException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void registrarPagoRechazaColegiaturaInactiva() {
        when(colegiaturaRepository.findById(1L))
                .thenReturn(Optional.of(cargoExistente(1L, false, "500.00", "0.00", "500.00", "ANULADA")));

        assertThatThrownBy(() -> colegiaturaService.registrarPago(1L, new BigDecimal("100.00")))
                .isInstanceOf(PagoColegiaturaInvalidoException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void registrarPagoRechazaCargoSinSaldoPendiente() {
        when(colegiaturaRepository.findById(1L))
                .thenReturn(Optional.of(cargoExistente(1L, true, "500.00", "500.00", "0.00", "PAGADA")));

        assertThatThrownBy(() -> colegiaturaService.registrarPago(1L, new BigDecimal("1.00")))
                .isInstanceOf(ColegiaturaSinSaldoPendienteException.class);

        verify(colegiaturaRepository, never()).save(any(Colegiatura.class));
    }

    @Test
    void actualizarNoAlteraSaldoArbitrariamenteYRecalculaConMontoTotal() {
        Colegiatura existente = cargoExistente(1L, true, "500.00", "200.00", "300.00", "PARCIAL");
        Colegiatura cambios = Colegiatura.builder()
                .concepto(" febrero ")
                .montoTotal(new BigDecimal("600.00"))
                .fechaEmision(LocalDate.of(2026, 2, 1))
                .fechaVencimiento(LocalDate.of(2026, 2, 28))
                .saldoPendiente(BigDecimal.ZERO)
                .build();
        when(colegiaturaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(colegiaturaRepository.save(any(Colegiatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Colegiatura actualizada = colegiaturaService.actualizar(1L, cambios);

        assertThat(actualizada.getConcepto()).isEqualTo("FEBRERO");
        assertThat(actualizada.getMontoPagado()).isEqualByComparingTo("200.00");
        assertThat(actualizada.getSaldoPendiente()).isEqualByComparingTo("400.00");
        assertThat(actualizada.getEstado()).isEqualTo("PARCIAL");
    }

    @Test
    void obtenerActivasPorEstudianteExcluyeInactivas() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(1L))
                .thenReturn(List.of(cargoExistente(1L, true, "500.00", "0.00", "500.00", "PENDIENTE")));

        List<Colegiatura> activas = colegiaturaService.obtenerActivasPorEstudiante(1L);

        assertThat(activas).extracting(Colegiatura::getActivo).containsExactly(true);
    }

    @Test
    void obtenerPendientesPorEstudianteConsideraSaldoMayorQueCero() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository
                .findByEstudianteIdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
                        1L, new BigDecimal("0.00")))
                .thenReturn(List.of(cargoExistente(1L, true, "500.00", "200.00", "300.00", "PARCIAL")));

        List<Colegiatura> pendientes = colegiaturaService.obtenerPendientesPorEstudiante(1L);

        assertThat(pendientes).extracting(Colegiatura::getSaldoPendiente).containsExactly(new BigDecimal("300.00"));
    }

    @Test
    void calcularSaldoPendienteSumaPendientesActivos() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository
                .findByEstudianteIdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
                        1L, new BigDecimal("0.00")))
                .thenReturn(List.of(
                        cargoExistente(1L, true, "500.00", "200.00", "300.00", "PARCIAL"),
                        cargoExistente(2L, true, "400.00", "0.00", "400.00", "PENDIENTE")));

        BigDecimal saldo = colegiaturaService.calcularSaldoPendiente(1L);

        assertThat(saldo).isEqualByComparingTo("700.00");
    }

    @Test
    void generarEstadoCuentaCalculaTotales() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(1L))
                .thenReturn(List.of(
                        cargoExistente(1L, true, "500.00", "200.00", "300.00", "PARCIAL"),
                        cargoExistente(2L, true, "400.00", "400.00", "0.00", "PAGADA")));

        EstadoCuentaEstudiante estadoCuenta = colegiaturaService.generarEstadoCuenta(1L);

        assertThat(estadoCuenta.totalCargos()).isEqualByComparingTo("900.00");
        assertThat(estadoCuenta.totalPagado()).isEqualByComparingTo("600.00");
        assertThat(estadoCuenta.saldoPendiente()).isEqualByComparingTo("300.00");
        assertThat(estadoCuenta.cantidadCargos()).isEqualTo(2);
        assertThat(estadoCuenta.cantidadPendientes()).isEqualTo(1);
    }

    @Test
    void generarEstadoCuentaVacioDevuelveCeros() {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(1L))
                .thenReturn(List.of());

        EstadoCuentaEstudiante estadoCuenta = colegiaturaService.generarEstadoCuenta(1L);

        assertThat(estadoCuenta.totalCargos()).isEqualByComparingTo("0.00");
        assertThat(estadoCuenta.totalPagado()).isEqualByComparingTo("0.00");
        assertThat(estadoCuenta.saldoPendiente()).isEqualByComparingTo("0.00");
        assertThat(estadoCuenta.detalle()).isEmpty();
    }

    @Test
    void inhabilitarNoEliminaFisicamente() {
        Colegiatura colegiatura = cargoExistente(1L, true, "500.00", "0.00", "500.00", "PENDIENTE");
        when(colegiaturaRepository.findById(1L)).thenReturn(Optional.of(colegiatura));
        when(colegiaturaRepository.save(any(Colegiatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Colegiatura inactiva = colegiaturaService.cambiarEstado(1L, false);

        assertThat(inactiva.getActivo()).isFalse();
        assertThat(inactiva.getEstado()).isEqualTo("ANULADA");
        verify(colegiaturaRepository, never()).delete(any(Colegiatura.class));
    }

    @Test
    void reactivarValidaEstudianteYDuplicidadAntesDeActivar() {
        Colegiatura colegiatura = cargoExistente(1L, false, "500.00", "0.00", "500.00", "ANULADA");
        when(colegiaturaRepository.findById(1L)).thenReturn(Optional.of(colegiatura));
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrueAndIdNot(
                1L, 2026, "ENERO", 1L)).thenReturn(false);
        when(colegiaturaRepository.save(any(Colegiatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Colegiatura reactivada = colegiaturaService.cambiarEstado(1L, true);

        assertThat(reactivada.getActivo()).isTrue();
        assertThat(reactivada.getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void obtenerActivasPorEstudiantePaginadoValidaEstudiante() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudiante(1L, true));
        when(colegiaturaRepository.findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(cargoExistente(1L, true, "500.00", "0.00", "500.00", "PENDIENTE")), pageable, 1));

        Page<Colegiatura> pagina = colegiaturaService.obtenerActivasPorEstudiante(1L, pageable);

        assertThat(pagina.getContent()).hasSize(1);
    }

    private Colegiatura cargoNuevo(String concepto, String montoTotal) {
        return Colegiatura.builder()
                .estudianteId(1L)
                .cicloAnio(2026)
                .concepto(concepto)
                .montoTotal(new BigDecimal(montoTotal))
                .fechaEmision(LocalDate.of(2026, 1, 1))
                .fechaVencimiento(LocalDate.of(2026, 1, 31))
                .build();
    }

    private Colegiatura cargoExistente(
            Long id,
            Boolean activo,
            String montoTotal,
            String montoPagado,
            String saldoPendiente,
            String estado) {
        return Colegiatura.builder()
                .id(id)
                .estudianteId(1L)
                .cicloAnio(2026)
                .concepto("ENERO")
                .montoTotal(new BigDecimal(montoTotal))
                .montoPagado(new BigDecimal(montoPagado))
                .saldoPendiente(new BigDecimal(saldoPendiente))
                .fechaEmision(LocalDate.of(2026, 1, 1))
                .fechaVencimiento(LocalDate.of(2026, 1, 31))
                .estado(estado)
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
}
