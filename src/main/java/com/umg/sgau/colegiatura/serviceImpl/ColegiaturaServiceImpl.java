package com.umg.sgau.colegiatura.serviceimpl;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.exception.ColegiaturaDuplicadaException;
import com.umg.sgau.colegiatura.exception.ColegiaturaNoEncontradaException;
import com.umg.sgau.colegiatura.exception.ColegiaturaSinSaldoPendienteException;
import com.umg.sgau.colegiatura.exception.EstudianteInactivoParaColegiaturaException;
import com.umg.sgau.colegiatura.exception.EstudianteInvalidoParaColegiaturaException;
import com.umg.sgau.colegiatura.exception.PagoColegiaturaInvalidoException;
import com.umg.sgau.colegiatura.exception.PagoExcedeSaldoException;
import com.umg.sgau.colegiatura.model.EstadoCuentaEstudiante;
import com.umg.sgau.colegiatura.repository.ColegiaturaRepository;
import com.umg.sgau.colegiatura.service.ColegiaturaService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import com.umg.sgau.notificacion.service.EventoNotificacion;

@Service
@RequiredArgsConstructor
@Transactional
public class ColegiaturaServiceImpl implements ColegiaturaService {
    @Autowired private ApplicationEventPublisher events;

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_PARCIAL = "PARCIAL";
    private static final String ESTADO_PAGADA = "PAGADA";
    private static final String ESTADO_ANULADA = "ANULADA";

    private final ColegiaturaRepository colegiaturaRepository;
    private final EstudianteService estudianteService;

    @Override
    public Colegiatura crear(Colegiatura colegiatura, Long estudianteId) {
        Estudiante estudiante = validarEstudianteActivo(estudianteId);
        validarCiclo(colegiatura.getCicloAnio());
        validarFechas(colegiatura);

        String conceptoNormalizado = normalizarConcepto(colegiatura.getConcepto());
        BigDecimal montoTotal = normalizarMonto(colegiatura.getMontoTotal());
        validarMontoMayorQueCero(montoTotal);
        validarDuplicadoActivo(estudianteId, colegiatura.getCicloAnio(), conceptoNormalizado, null);

        colegiatura.setEstudiante(estudiante);
        colegiatura.setConcepto(conceptoNormalizado);
        colegiatura.setMontoTotal(montoTotal);
        colegiatura.setMontoPagado(CERO);
        colegiatura.setSaldoPendiente(montoTotal);
        colegiatura.setEstado(ESTADO_PENDIENTE);
        colegiatura.setActivo(true);

        Colegiatura creada=colegiaturaRepository.save(colegiatura);
        if(events!=null&&estudiante.getUsuario()!=null)events.publishEvent(new EventoNotificacion(estudiante.getUsuario().getId(),"COLEGIATURA:"+creada.getId()+":"+System.nanoTime(),"COLEGIATURA","Nueva colegiatura","Se generó una colegiatura en tu cuenta.","COLEGIATURA",creada.getId(),false));
        return creada;
    }

    @Override
    @Transactional(readOnly = true)
    public Colegiatura obtenerPorId(Long id) {
        return colegiaturaRepository.findById(id)
                .orElseThrow(() -> new ColegiaturaNoEncontradaException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Colegiatura> listar(
            Long estudianteId,
            Integer cicloAnio,
            String estado,
            Boolean activo,
            String concepto,
            Pageable pageable) {

        String conceptoNormalizado = concepto == null || concepto.isBlank()
                ? null
                : normalizarConcepto(concepto);
        String estadoNormalizado = estado == null || estado.isBlank()
                ? null
                : estado.trim().toUpperCase(Locale.ROOT);

        return colegiaturaRepository.buscar(
                estudianteId,
                cicloAnio,
                estadoNormalizado,
                activo,
                conceptoNormalizado,
                pageable
        );
    }

    @Override
    public Colegiatura actualizar(Long id, Colegiatura colegiatura) {
        Colegiatura existente = obtenerPorId(id);

        String concepto = colegiatura.getConcepto() == null
                ? existente.getConcepto()
                : normalizarConcepto(colegiatura.getConcepto());
        BigDecimal montoTotal = colegiatura.getMontoTotal() == null
                ? existente.getMontoTotal()
                : normalizarMonto(colegiatura.getMontoTotal());

        if (colegiatura.getFechaEmision() != null) {
            existente.setFechaEmision(colegiatura.getFechaEmision());
        }
        if (colegiatura.getFechaVencimiento() != null) {
            existente.setFechaVencimiento(colegiatura.getFechaVencimiento());
        }

        validarFechas(existente);
        validarMontoMayorQueCero(montoTotal);
        validarMontoNoMenorAlPagado(montoTotal, existente.getMontoPagado());
        validarDuplicadoActivo(getEstudianteId(existente), existente.getCicloAnio(), concepto, id);

        existente.setConcepto(concepto);
        existente.setMontoTotal(montoTotal);
        existente.setMontoPagado(normalizarMonto(existente.getMontoPagado()));
        existente.setSaldoPendiente(montoTotal.subtract(existente.getMontoPagado()).setScale(2, RoundingMode.HALF_UP));
        validarConsistenciaMonetaria(existente);
        recalcularEstado(existente);

        return colegiaturaRepository.save(existente);
    }

    @Override
    public Colegiatura registrarPago(Long id, BigDecimal montoPago) {
        Colegiatura colegiatura = obtenerPorId(id);

        if (!Boolean.TRUE.equals(colegiatura.getActivo())) {
            throw new PagoColegiaturaInvalidoException("La colegiatura esta inactiva.");
        }

        BigDecimal pago = normalizarMonto(montoPago);
        validarMontoMayorQueCero(pago);
        validarConsistenciaMonetaria(colegiatura);

        if (colegiatura.getSaldoPendiente().compareTo(CERO) == 0) {
            throw new ColegiaturaSinSaldoPendienteException(id);
        }

        if (pago.compareTo(colegiatura.getSaldoPendiente()) > 0) {
            throw new PagoExcedeSaldoException(id);
        }

        colegiatura.setMontoPagado(colegiatura.getMontoPagado().add(pago).setScale(2, RoundingMode.HALF_UP));
        colegiatura.setSaldoPendiente(colegiatura.getSaldoPendiente().subtract(pago).setScale(2, RoundingMode.HALF_UP));
        recalcularEstado(colegiatura);

        return colegiaturaRepository.save(colegiatura);
    }

    @Override
    public Colegiatura cambiarEstado(Long id, Boolean activo) {
        Colegiatura colegiatura = obtenerPorId(id);

        if (Boolean.TRUE.equals(activo)) {
            validarEstudianteActivo(getEstudianteId(colegiatura));
            validarDuplicadoActivo(
                    getEstudianteId(colegiatura),
                    colegiatura.getCicloAnio(),
                    normalizarConcepto(colegiatura.getConcepto()),
                    id);
            validarConsistenciaMonetaria(colegiatura);
            colegiatura.setActivo(true);
            recalcularEstado(colegiatura);
        } else {
            colegiatura.setActivo(false);
            colegiatura.setEstado(ESTADO_ANULADA);
        }

        return colegiaturaRepository.save(colegiatura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Colegiatura> obtenerPendientes() {
        return colegiaturaRepository.findByActivoTrue()
                .stream()
                .filter(this::tieneSaldoPendiente)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BigDecimal> obtenerSaldosPendientes() {
        return obtenerPendientes()
                .stream()
                .map(Colegiatura::getSaldoPendiente)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Colegiatura> obtenerHistorialPorEstudiante(Long estudianteId) {
        validarEstudianteExistente(estudianteId);
        return colegiaturaRepository.findByEstudianteId(estudianteId, Pageable.unpaged())
                .getContent()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Colegiatura> obtenerActivasPorEstudiante(Long estudianteId) {
        validarEstudianteExistente(estudianteId);
        return colegiaturaRepository.findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(estudianteId)
                .stream()
                .filter(colegiatura -> Boolean.TRUE.equals(colegiatura.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Colegiatura> obtenerActivasPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return colegiaturaRepository.findByEstudianteIdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(
                estudianteId,
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Colegiatura> obtenerPendientesPorEstudiante(Long estudianteId) {
        validarEstudianteExistente(estudianteId);
        return colegiaturaRepository
                .findByEstudianteIdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
                        estudianteId,
                        CERO)
                .stream()
                .filter(this::tieneSaldoPendiente)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Colegiatura> obtenerPendientesPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return colegiaturaRepository
                .findByEstudianteIdAndActivoTrueAndSaldoPendienteGreaterThanOrderByCicloAnioDescFechaEmisionDesc(
                        estudianteId,
                        CERO,
                        pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoPendiente(Long estudianteId) {
        return obtenerPendientesPorEstudiante(estudianteId)
                .stream()
                .map(Colegiatura::getSaldoPendiente)
                .reduce(CERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoCuentaEstudiante generarEstadoCuenta(Long estudianteId) {
        Estudiante estudiante = validarEstudianteExistente(estudianteId);
        List<Colegiatura> activas = obtenerActivasPorEstudiante(estudianteId);

        BigDecimal totalCargos = activas.stream()
                .map(Colegiatura::getMontoTotal)
                .reduce(CERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPagado = activas.stream()
                .map(Colegiatura::getMontoPagado)
                .reduce(CERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoPendiente = activas.stream()
                .map(Colegiatura::getSaldoPendiente)
                .reduce(CERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        int cantidadPendientes = (int) activas.stream()
                .filter(this::tieneSaldoPendiente)
                .count();

        return new EstadoCuentaEstudiante(
                estudianteId,
                estudiante.getNombres() + " " + estudiante.getApellidos(),
                totalCargos,
                totalPagado,
                saldoPendiente,
                activas.size(),
                cantidadPendientes,
                activas);
    }

    private Estudiante validarEstudianteActivo(Long estudianteId) {
        Estudiante estudiante = validarEstudianteExistente(estudianteId);
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new EstudianteInactivoParaColegiaturaException(estudianteId);
        }
        return estudiante;
    }

    private Estudiante validarEstudianteExistente(Long estudianteId) {
        if (estudianteId == null || estudianteId <= 0) {
            throw new EstudianteInvalidoParaColegiaturaException(estudianteId);
        }

        try {
            return estudianteService.obtenerPorId(estudianteId);
        } catch (EstudianteNoEncontradoException exception) {
            throw new EstudianteInvalidoParaColegiaturaException(estudianteId);
        }
    }

    private void validarCiclo(Integer cicloAnio) {
        if (cicloAnio == null || cicloAnio < 2020 || cicloAnio > 2100) {
            throw new IllegalArgumentException("El ciclo academico debe estar entre 2020 y 2100.");
        }
    }

    private void validarFechas(Colegiatura colegiatura) {
        if (colegiatura.getFechaEmision() == null || colegiatura.getFechaVencimiento() == null) {
            throw new IllegalArgumentException("Las fechas de emision y vencimiento son obligatorias.");
        }
        if (colegiatura.getFechaVencimiento().isBefore(colegiatura.getFechaEmision())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de emision.");
        }
    }

    private String normalizarConcepto(String concepto) {
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio.");
        }
        return concepto.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal normalizarMonto(BigDecimal monto) {
        if (monto == null) {
            throw new PagoColegiaturaInvalidoException("El monto es obligatorio.");
        }
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarMontoMayorQueCero(BigDecimal monto) {
        if (monto.compareTo(CERO) <= 0) {
            throw new PagoColegiaturaInvalidoException("El monto debe ser mayor que cero.");
        }
    }

    private void validarMontoNoMenorAlPagado(BigDecimal montoTotal, BigDecimal montoPagado) {
        if (montoTotal.compareTo(normalizarMonto(montoPagado)) < 0) {
            throw new PagoColegiaturaInvalidoException("El monto total no puede ser menor que el monto ya pagado.");
        }
    }

    private void validarDuplicadoActivo(Long estudianteId, Integer cicloAnio, String concepto, Long idExcluir) {
        boolean duplicada = idExcluir == null
                ? colegiaturaRepository.existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrue(
                estudianteId,
                cicloAnio,
                concepto)
                : colegiaturaRepository.existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrueAndIdNot(
                estudianteId,
                cicloAnio,
                concepto,
                idExcluir);

        if (duplicada) {
            throw new ColegiaturaDuplicadaException(estudianteId, cicloAnio, concepto);
        }
    }

    private void validarConsistenciaMonetaria(Colegiatura colegiatura) {
        colegiatura.setMontoTotal(normalizarMonto(colegiatura.getMontoTotal()));
        colegiatura.setMontoPagado(normalizarMonto(colegiatura.getMontoPagado()));
        colegiatura.setSaldoPendiente(normalizarMonto(colegiatura.getSaldoPendiente()));

        if (colegiatura.getMontoPagado().compareTo(CERO) < 0 || colegiatura.getSaldoPendiente().compareTo(CERO) < 0) {
            throw new PagoColegiaturaInvalidoException("Los montos pagados y saldos no pueden ser negativos.");
        }

        BigDecimal saldoEsperado = colegiatura.getMontoTotal()
                .subtract(colegiatura.getMontoPagado())
                .setScale(2, RoundingMode.HALF_UP);
        if (saldoEsperado.compareTo(colegiatura.getSaldoPendiente()) != 0) {
            throw new PagoColegiaturaInvalidoException("El saldo pendiente no es consistente con el monto total y pagado.");
        }
    }

    private void recalcularEstado(Colegiatura colegiatura) {
        if (!Boolean.TRUE.equals(colegiatura.getActivo())) {
            colegiatura.setEstado(ESTADO_ANULADA);
            return;
        }

        if (colegiatura.getSaldoPendiente().compareTo(CERO) == 0) {
            colegiatura.setEstado(ESTADO_PAGADA);
        } else if (colegiatura.getMontoPagado().compareTo(CERO) > 0) {
            colegiatura.setEstado(ESTADO_PARCIAL);
        } else {
            colegiatura.setEstado(ESTADO_PENDIENTE);
        }
    }

    private boolean tieneSaldoPendiente(Colegiatura colegiatura) {
        return colegiatura.getSaldoPendiente() != null
                && colegiatura.getSaldoPendiente().compareTo(CERO) > 0;
    }

    private Long getEstudianteId(Colegiatura colegiatura) {
        return colegiatura.getEstudiante() == null ? null : colegiatura.getEstudiante().getId();
    }
}
