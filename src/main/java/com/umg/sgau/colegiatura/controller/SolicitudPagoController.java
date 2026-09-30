package com.umg.sgau.colegiatura.controller;

import com.umg.sgau.colegiatura.dto.SolicitudPagoDTOs;
import com.umg.sgau.colegiatura.service.SolicitudPagoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
public class SolicitudPagoController {
    private final SolicitudPagoService service;
    @GetMapping("/api/academico/estudiante/me/pagos")
    @PreAuthorize("hasRole('ESTUDIANTE') and hasAuthority('COLEGIATURAS_LEER')")
    public List<SolicitudPagoDTOs.Respuesta> propias(Authentication auth) { return service.propias(auth); }
    @PostMapping("/api/academico/estudiante/me/colegiaturas/{id}/pagos")
    @PreAuthorize("hasRole('ESTUDIANTE') and hasAuthority('COLEGIATURAS_LEER') and hasAuthority('COLEGIATURAS_REGISTRAR_PAGO')")
    public ResponseEntity<SolicitudPagoDTOs.Respuesta> registrar(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody SolicitudPagoDTOs.Registro request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(auth, id, request));
    }
    @GetMapping("/api/colegiaturas/pagos/pendientes")
    @PreAuthorize("@accessScope.esAdminActivoConPermiso(authentication, 'COLEGIATURAS_CAMBIAR_ESTADO')")
    public List<SolicitudPagoDTOs.Respuesta> pendientes() { return service.pendientes(); }
    @GetMapping("/api/colegiaturas/pagos")
    @PreAuthorize("@accessScope.esAdminActivoConPermiso(authentication, 'COLEGIATURAS_CAMBIAR_ESTADO')")
    public org.springframework.data.domain.Page<SolicitudPagoDTOs.RevisionItem> buscar(
            @RequestParam(required=false) String estado, @RequestParam(required=false) String q,
            org.springframework.data.domain.Pageable pageable) { return service.buscarRevision(estado,q,pageable); }
    @GetMapping("/api/colegiaturas/pagos/{id}")
    @PreAuthorize("@accessScope.esAdminActivoConPermiso(authentication, 'COLEGIATURAS_CAMBIAR_ESTADO')")
    public SolicitudPagoDTOs.RevisionItem detalle(@PathVariable Long id) { return service.detalleRevision(id); }
    @PatchMapping("/api/colegiaturas/pagos/{id}/revision")
    @PreAuthorize("@accessScope.esAdminActivoConPermiso(authentication, 'COLEGIATURAS_CAMBIAR_ESTADO')")
    public SolicitudPagoDTOs.Respuesta revisar(Authentication auth, @PathVariable Long id, @Valid @RequestBody SolicitudPagoDTOs.Revision request) { return service.revisar(auth,id,request); }
}
