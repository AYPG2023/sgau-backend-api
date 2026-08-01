package com.umg.sgau.colegiatura.serviceImpl;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.repository.ColegiaturaRepository;
import com.umg.sgau.colegiatura.service.ColegiaturaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ColegiaturaServiceImpl implements ColegiaturaService {

    private final ColegiaturaRepository colegiaturaRepository;

    @Override
    public Colegiatura crear(Colegiatura colegiatura) {

        if (colegiatura.getEstudianteId() == null
                || colegiatura.getEstudianteId() <= 0) {
            throw new IllegalArgumentException(
                    "El estudiante es obligatorio.");
        }

        if (colegiatura.getCicloAnio() == null
                || colegiatura.getCicloAnio() <= 0) {
            throw new IllegalArgumentException(
                    "El ciclo académico es obligatorio.");
        }

        if (colegiaturaRepository
                .existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrue(
                        colegiatura.getEstudianteId(),
                        colegiatura.getCicloAnio(),
                        colegiatura.getConcepto())) {

            throw new IllegalArgumentException(
                    "Ya existe una colegiatura activa para este estudiante.");
        }

        if (colegiatura.getFechaVencimiento()
                .isBefore(colegiatura.getFechaEmision())) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento no puede ser anterior a la fecha de emisión.");
        }

        colegiatura.setMontoPagado(BigDecimal.ZERO);
        colegiatura.setSaldoPendiente(colegiatura.getMontoTotal());
        colegiatura.setEstado("PENDIENTE");
        colegiatura.setActivo(true);

        return colegiaturaRepository.save(colegiatura);
    }

    @Override
    @Transactional(readOnly = true)
    public Colegiatura obtenerPorId(Long id) {

        return colegiaturaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Colegiatura no encontrada con id: " + id));
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

        if (concepto != null && concepto.isBlank()) {
            concepto = null;
        }

        return colegiaturaRepository.buscar(
                estudianteId,
                cicloAnio,
                estado,
                activo,
                concepto,
                pageable
        );
    }

    @Override
    public Colegiatura actualizar(
            Long id,
            Colegiatura colegiatura) {

        Colegiatura existente = obtenerPorId(id);

        if (colegiaturaRepository
                .existsByEstudianteIdAndCicloAnioAndConceptoAndActivoTrueAndIdNot(
                        existente.getEstudianteId(),
                        existente.getCicloAnio(),
                        colegiatura.getConcepto(),
                        id)) {

            throw new IllegalArgumentException(
                    "Ya existe una colegiatura activa para este estudiante.");
        }

        if (colegiatura.getFechaVencimiento()
                .isBefore(colegiatura.getFechaEmision())) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento no puede ser anterior a la fecha de emisión.");
        }

        existente.setConcepto(
                colegiatura.getConcepto());

        existente.setMontoTotal(
                colegiatura.getMontoTotal());

        existente.setFechaEmision(
                colegiatura.getFechaEmision());

        existente.setFechaVencimiento(
                colegiatura.getFechaVencimiento());

        existente.setSaldoPendiente(
                existente.getMontoTotal()
                        .subtract(existente.getMontoPagado()));

        return colegiaturaRepository.save(existente);
    }

    @Override
    public Colegiatura registrarPago(
            Long id,
            BigDecimal montoPago) {

        Colegiatura colegiatura = obtenerPorId(id);

        if (!Boolean.TRUE.equals(colegiatura.getActivo())) {
            throw new IllegalStateException(
                    "La colegiatura está inactiva.");
        }

        if ("ANULADA".equals(colegiatura.getEstado())) {
            throw new IllegalStateException(
                    "No se pueden registrar pagos sobre una colegiatura anulada.");
        }

        if (montoPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero.");
        }

        if (montoPago.compareTo(
                colegiatura.getSaldoPendiente()) > 0) {

            throw new IllegalArgumentException(
                    "El pago supera el saldo pendiente.");
        }

        BigDecimal nuevoPagado =
                colegiatura.getMontoPagado()
                        .add(montoPago);

        BigDecimal nuevoSaldo =
                colegiatura.getSaldoPendiente()
                        .subtract(montoPago);

        colegiatura.setMontoPagado(
                nuevoPagado);

        colegiatura.setSaldoPendiente(
                nuevoSaldo);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            colegiatura.setEstado("PAGADA");
        } else {
            colegiatura.setEstado("PARCIAL");
        }

        return colegiaturaRepository.save(colegiatura);
    }

    @Override
    public Colegiatura cambiarEstado(
            Long id,
            Boolean activo) {

        Colegiatura colegiatura =
                obtenerPorId(id);

        colegiatura.setActivo(activo);

        if (!activo) {
            colegiatura.setEstado("ANULADA");
        }

        return colegiaturaRepository.save(colegiatura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Colegiatura> obtenerPendientes() {

        return colegiaturaRepository.findAll()
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .filter(c -> c.getSaldoPendiente()
                        .compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BigDecimal> obtenerSaldosPendientes() {

        return colegiaturaRepository.findAll()
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .filter(c -> c.getSaldoPendiente()
                        .compareTo(BigDecimal.ZERO) > 0)
                .map(Colegiatura::getSaldoPendiente)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Colegiatura> obtenerHistorialPorEstudiante(
            Long estudianteId) {

        return colegiaturaRepository.findAll()
                .stream()
                .filter(c -> c.getEstudianteId()
                        .equals(estudianteId))
                .collect(Collectors.toList());
    }

}