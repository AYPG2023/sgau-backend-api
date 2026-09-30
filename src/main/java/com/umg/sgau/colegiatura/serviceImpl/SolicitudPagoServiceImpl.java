package com.umg.sgau.colegiatura.serviceimpl;

import com.umg.sgau.colegiatura.dto.SolicitudPagoDTOs;
import com.umg.sgau.colegiatura.entity.*;
import com.umg.sgau.colegiatura.exception.*;
import com.umg.sgau.colegiatura.repository.*;
import com.umg.sgau.colegiatura.service.SolicitudPagoService;
import com.umg.sgau.config.AccessScopeService;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class SolicitudPagoServiceImpl implements SolicitudPagoService {
    private final AccessScopeService accessScope;
    private final EstudianteRepository estudiantes;
    private final ColegiaturaRepository colegiaturas;
    private final SolicitudPagoRepository solicitudes;

    public SolicitudPagoDTOs.Respuesta registrar(Authentication auth, Long colegiaturaId, SolicitudPagoDTOs.Registro r) {
        Long estudianteId = accessScope.idEstudiante(auth).orElseThrow(() -> new AccessDeniedException("Perfil de estudiante no vinculado."));
        Colegiatura c = colegiaturas.findByIdForUpdate(colegiaturaId).orElseThrow(() -> new ColegiaturaNoEncontradaException(colegiaturaId));
        if (c.getEstudiante() == null || !estudianteId.equals(c.getEstudiante().getId())) throw new AccessDeniedException("La colegiatura no pertenece al estudiante autenticado.");
        return solicitudes.findByEstudiante_IdAndIdempotencyKey(estudianteId, r.idempotencyKey().trim())
                .map(existing -> {
                    if (!existing.getColegiatura().getId().equals(colegiaturaId)) throw new PagoColegiaturaInvalidoException("La clave de idempotencia ya fue utilizada.");
                    return dto(existing);
                }).orElseGet(() -> {
                    var monto = r.monto().setScale(2, RoundingMode.HALF_UP);
                    if (!Boolean.TRUE.equals(c.getActivo()) || c.getSaldoPendiente().signum() <= 0) throw new PagoColegiaturaInvalidoException("La colegiatura no admite pagos.");
                    if (monto.compareTo(c.getSaldoPendiente()) > 0) throw new PagoExcedeSaldoException(colegiaturaId);
                    SolicitudPago s = SolicitudPago.builder().colegiatura(c).estudiante(estudiantes.getReferenceById(estudianteId))
                            .monto(monto).fechaPago(r.fechaPago()).referencia(r.referencia().trim())
                            .metodoPago(limpiar(r.metodoPago())).comprobanteUrl(limpiar(r.comprobanteUrl()))
                            .idempotencyKey(r.idempotencyKey().trim()).estado("PENDIENTE").build();
                    return dto(solicitudes.save(s));
                });
    }

    @Transactional(readOnly=true) public List<SolicitudPagoDTOs.Respuesta> propias(Authentication auth) {
        Long id = accessScope.idEstudiante(auth).orElseThrow(() -> new AccessDeniedException("Perfil de estudiante no vinculado."));
        return solicitudes.findByEstudiante_IdOrderByFechaCreacionDesc(id).stream().map(this::dto).toList();
    }
    @Transactional(readOnly=true) public List<SolicitudPagoDTOs.Respuesta> pendientes() {
        return solicitudes.findByEstadoOrderByFechaCreacionAsc("PENDIENTE").stream().map(this::dto).toList();
    }
    public SolicitudPagoDTOs.Respuesta revisar(Long solicitudId, SolicitudPagoDTOs.Revision r) {
        SolicitudPago s = solicitudes.findById(solicitudId).orElseThrow(() -> new PagoColegiaturaInvalidoException("Solicitud de pago no encontrada."));
        if (!"PENDIENTE".equals(s.getEstado())) return dto(s); // revisión idempotente
        if ("RECHAZADO".equals(r.estado())) {
            if (r.motivo() == null || r.motivo().isBlank()) throw new PagoColegiaturaInvalidoException("Debe indicar el motivo del rechazo.");
            s.setEstado("RECHAZADO"); s.setMotivoRechazo(r.motivo().trim()); s.setFechaRevision(LocalDateTime.now());
            return dto(solicitudes.save(s));
        }
        Colegiatura c = colegiaturas.findByIdForUpdate(s.getColegiatura().getId()).orElseThrow(() -> new ColegiaturaNoEncontradaException(s.getColegiatura().getId()));
        if (s.getMonto().compareTo(c.getSaldoPendiente()) > 0) throw new PagoExcedeSaldoException(c.getId());
        c.setMontoPagado(c.getMontoPagado().add(s.getMonto()).setScale(2, RoundingMode.HALF_UP));
        c.setSaldoPendiente(c.getSaldoPendiente().subtract(s.getMonto()).setScale(2, RoundingMode.HALF_UP));
        c.setEstado(c.getSaldoPendiente().signum() == 0 ? "PAGADA" : "PARCIAL");
        colegiaturas.save(c); s.setEstado("APROBADO"); s.setFechaRevision(LocalDateTime.now());
        return dto(solicitudes.save(s));
    }
    private SolicitudPagoDTOs.Respuesta dto(SolicitudPago s) { return new SolicitudPagoDTOs.Respuesta(s.getId(), s.getColegiatura().getId(), s.getMonto(), s.getFechaPago(), s.getReferencia(), s.getMetodoPago(), s.getComprobanteUrl(), s.getEstado(), s.getMotivoRechazo(), s.getFechaCreacion(), s.getFechaRevision()); }
    private String limpiar(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
