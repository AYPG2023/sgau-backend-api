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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import com.umg.sgau.notificacion.service.EventoNotificacion;

@Service @RequiredArgsConstructor @Transactional
public class SolicitudPagoServiceImpl implements SolicitudPagoService {
    @Autowired private ApplicationEventPublisher events;
    private final AccessScopeService accessScope;
    private final EstudianteRepository estudiantes;
    private final ColegiaturaRepository colegiaturas;
    private final SolicitudPagoRepository solicitudes;

    public SolicitudPagoDTOs.Respuesta registrar(Authentication auth, Long colegiaturaId, SolicitudPagoDTOs.Registro r) {
        Long estudianteId = accessScope.idEstudiante(auth).orElseThrow(() -> new AccessDeniedException("Perfil de estudiante no vinculado."));
        var estudiante = estudiantes.findByIdForUpdate(estudianteId).orElseThrow(() -> new AccessDeniedException("Perfil de estudiante no vinculado."));
        if (!Boolean.TRUE.equals(estudiante.getActivo())) throw new AccessDeniedException("El perfil de estudiante está inactivo.");
        Colegiatura c = colegiaturas.findByIdForUpdate(colegiaturaId).orElseThrow(() -> new ColegiaturaNoEncontradaException(colegiaturaId));
        if (c.getEstudiante() == null || !estudianteId.equals(c.getEstudiante().getId())) throw new AccessDeniedException("La colegiatura no pertenece al estudiante autenticado.");
        return solicitudes.findByEstudiante_IdAndIdempotencyKey(estudianteId, r.idempotencyKey().trim())
                .map(existing -> {
                    if (!existing.getColegiatura().getId().equals(colegiaturaId)) throw new PagoColegiaturaInvalidoException("La clave de idempotencia ya fue utilizada.");
                    return dto(existing);
                }).orElseGet(() -> {
                    var monto = r.monto().setScale(2, RoundingMode.HALF_UP);
                    if (!Boolean.TRUE.equals(c.getActivo()) || c.getSaldoPendiente().signum() <= 0) throw new PagoColegiaturaInvalidoException("La colegiatura no admite pagos.");
                    var reservados = solicitudes.sumarPendientes(colegiaturaId);
                    if (reservados == null) reservados = java.math.BigDecimal.ZERO;
                    var disponible = c.getSaldoPendiente().subtract(reservados);
                    if (monto.compareTo(disponible) > 0 || disponible.signum() <= 0) throw new PagoExcedeSaldoException(colegiaturaId);
                    String medio = r.metodoPago().trim();
                    String boleta = r.referencia().trim();
                    String referenciaUnica = medio.toLowerCase(java.util.Locale.ROOT) + ":" + boleta.toLowerCase(java.util.Locale.ROOT);
                    if (solicitudes.existsByReferenciaUnica(referenciaUnica)) throw new PagoColegiaturaInvalidoException("Ya existe una boleta con ese número para el mismo medio de pago.");
                    SolicitudPago s = SolicitudPago.builder().colegiatura(c).estudiante(estudiante)
                            .monto(monto).fechaPago(r.fechaPago()).referencia(boleta)
                            .metodoPago(medio).referenciaUnica(referenciaUnica).comprobanteUrl(limpiar(r.comprobanteUrl()))
                            .idempotencyKey(r.idempotencyKey().trim()).estado("PENDIENTE").build();
                    SolicitudPago saved=solicitudes.save(s);
                    if (events != null) events.publishEvent(new EventoNotificacion(null,"ADMIN_PAGO_PENDIENTE:"+saved.getId(),"PAGO_PENDIENTE","Pago pendiente de revisión","Hay un pago pendiente de revisión administrativa.","REVISION_PAGO",saved.getId(),true));
                    if(events!=null){if(saved.getEstudiante().getUsuario()!=null)events.publishEvent(new EventoNotificacion(saved.getEstudiante().getUsuario().getId(),"PAGO_PENDIENTE:"+saved.getId(),"PAGO_PENDIENTE","Pago registrado, pendiente de revisión","Tu pago fue registrado y está pendiente de revisión administrativa.","COLEGIATURA",colegiaturaId,false));}
                    return dto(saved);
                });
    }

    @Transactional(readOnly=true) public List<SolicitudPagoDTOs.Respuesta> propias(Authentication auth) {
        Long id = accessScope.idEstudiante(auth).orElseThrow(() -> new AccessDeniedException("Perfil de estudiante no vinculado."));
        return solicitudes.findByEstudiante_IdOrderByFechaCreacionDesc(id).stream().map(this::dto).toList();
    }
    @Transactional(readOnly=true) public List<SolicitudPagoDTOs.Respuesta> pendientes() {
        return solicitudes.findByEstadoOrderByFechaCreacionAsc("PENDIENTE").stream().map(this::dto).toList();
    }
    public SolicitudPagoDTOs.Respuesta revisar(Authentication auth, Long solicitudId, SolicitudPagoDTOs.Revision r) {
        SolicitudPago s = solicitudes.findByIdForUpdate(solicitudId).orElseThrow(() -> new PagoColegiaturaInvalidoException("Solicitud de pago no encontrada."));
        if (!"PENDIENTE".equals(s.getEstado())) throw new PagoColegiaturaInvalidoException("Este pago ya fue revisado y no se puede procesar otra vez.");
        if ("RECHAZADO".equals(r.estado())) {
            if (r.motivo() == null || r.motivo().isBlank()) throw new PagoColegiaturaInvalidoException("Debe indicar el motivo del rechazo.");
            s.setEstado("RECHAZADO"); s.setMotivoRechazo(r.motivo().trim()); s.setFechaRevision(LocalDateTime.now());
            if (auth != null) s.setRevisadoPorUsuarioId(accessScope.idUsuario(auth).orElseThrow(() -> new AccessDeniedException("Usuario administrador no disponible.")));
            if(events!=null&&s.getEstudiante().getUsuario()!=null)events.publishEvent(new EventoNotificacion(s.getEstudiante().getUsuario().getId(),"PAGO_RECHAZADO:"+s.getId(),"PAGO_RECHAZADO","Pago rechazado","Tu pago fue rechazado. Consulta el buzón para ver el motivo.","COLEGIATURA",s.getColegiatura().getId(),false));
            return dto(solicitudes.save(s));
        }
        Colegiatura c = colegiaturas.findByIdForUpdate(s.getColegiatura().getId()).orElseThrow(() -> new ColegiaturaNoEncontradaException(s.getColegiatura().getId()));
        if (s.getMonto().compareTo(c.getSaldoPendiente()) > 0) throw new PagoExcedeSaldoException(c.getId());
        c.setMontoPagado(c.getMontoPagado().add(s.getMonto()).setScale(2, RoundingMode.HALF_UP));
        c.setSaldoPendiente(c.getSaldoPendiente().subtract(s.getMonto()).setScale(2, RoundingMode.HALF_UP));
        c.setEstado(c.getSaldoPendiente().signum() == 0 ? "PAGADA" : "PARCIAL");
        colegiaturas.save(c); s.setEstado("APROBADO"); s.setFechaRevision(LocalDateTime.now());
        if (auth != null) s.setRevisadoPorUsuarioId(accessScope.idUsuario(auth).orElseThrow(() -> new AccessDeniedException("Usuario administrador no disponible.")));
        if(events!=null&&s.getEstudiante().getUsuario()!=null)events.publishEvent(new EventoNotificacion(s.getEstudiante().getUsuario().getId(),"PAGO_APROBADO:"+s.getId(),"PAGO_APROBADO","Pago confirmado","Tu pago fue aprobado y aplicado a tu colegiatura.","COLEGIATURA",c.getId(),false));
        return dto(solicitudes.save(s));
    }
    public SolicitudPagoDTOs.Respuesta revisar(Long solicitudId, SolicitudPagoDTOs.Revision r) { return revisar(null, solicitudId, r); }
    @Transactional(readOnly=true)
    public org.springframework.data.domain.Page<SolicitudPagoDTOs.RevisionItem> buscarRevision(String estado, String texto, org.springframework.data.domain.Pageable pageable) {
        String filtro = estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado) ? null : estado.trim().toUpperCase(java.util.Locale.ROOT);
        return solicitudes.buscarRevision(filtro, texto == null || texto.isBlank() ? null : texto.trim(), pageable).map(this::revisionItem);
    }
    @Transactional(readOnly=true)
    public SolicitudPagoDTOs.RevisionItem detalleRevision(Long id) {
        return solicitudes.findById(id).map(this::revisionItem).orElseThrow(() -> new PagoColegiaturaInvalidoException("Solicitud de pago no encontrada."));
    }
    private SolicitudPagoDTOs.Respuesta dto(SolicitudPago s) { return new SolicitudPagoDTOs.Respuesta(s.getId(), s.getColegiatura().getId(), s.getMonto(), s.getFechaPago(), s.getReferencia(), s.getMetodoPago(), s.getComprobanteUrl(), s.getEstado(), s.getMotivoRechazo(), s.getFechaCreacion(), s.getFechaRevision(), s.getRevisadoPorUsuarioId()); }
    private SolicitudPagoDTOs.RevisionItem revisionItem(SolicitudPago s) {
        var e=s.getEstudiante(); var c=s.getColegiatura(); var m=c.getInscripcionCarrera();
        return new SolicitudPagoDTOs.RevisionItem(s.getId(),e.getId(),e.getNombres()+" "+e.getApellidos(),e.getCodigoEstudiantil(),m==null?null:m.getCarrera().getNombre(),c.getId(),c.getConcepto(),s.getMonto(),s.getFechaPago(),s.getReferencia(),s.getMetodoPago(),s.getComprobanteUrl(),s.getEstado(),s.getMotivoRechazo(),s.getFechaCreacion(),s.getFechaRevision(),s.getRevisadoPorUsuarioId());
    }
    private String limpiar(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
