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
    @PreAuthorize("hasRole('ESTUDIANTE') and hasAuthority('COLEGIATURAS_LEER')")
    public ResponseEntity<SolicitudPagoDTOs.Respuesta> registrar(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody SolicitudPagoDTOs.Registro request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(auth, id, request));
    }
    @GetMapping("/api/colegiaturas/pagos/pendientes")
    @PreAuthorize("@accessScope.esAdmin(authentication)")
    public List<SolicitudPagoDTOs.Respuesta> pendientes() { return service.pendientes(); }
    @PatchMapping("/api/colegiaturas/pagos/{id}/revision")
    @PreAuthorize("@accessScope.esAdmin(authentication)")
    public SolicitudPagoDTOs.Respuesta revisar(@PathVariable Long id, @Valid @RequestBody SolicitudPagoDTOs.Revision request) { return service.revisar(id, request); }
}
