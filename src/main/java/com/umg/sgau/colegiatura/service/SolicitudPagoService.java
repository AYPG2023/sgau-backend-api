package com.umg.sgau.colegiatura.service;
import com.umg.sgau.colegiatura.dto.SolicitudPagoDTOs;
import java.util.List;
import org.springframework.security.core.Authentication;
public interface SolicitudPagoService {
    SolicitudPagoDTOs.Respuesta registrar(Authentication auth, Long colegiaturaId, SolicitudPagoDTOs.Registro request);
    List<SolicitudPagoDTOs.Respuesta> propias(Authentication auth);
    List<SolicitudPagoDTOs.Respuesta> pendientes();
    SolicitudPagoDTOs.Respuesta revisar(Long solicitudId, SolicitudPagoDTOs.Revision request);
    SolicitudPagoDTOs.Respuesta revisar(Authentication auth, Long solicitudId, SolicitudPagoDTOs.Revision request);
    org.springframework.data.domain.Page<SolicitudPagoDTOs.RevisionItem> buscarRevision(String estado, String texto, org.springframework.data.domain.Pageable pageable);
    SolicitudPagoDTOs.RevisionItem detalleRevision(Long id);
}
