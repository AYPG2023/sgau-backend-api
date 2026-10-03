package com.umg.sgau.auditoria.controller;

import com.umg.sgau.auditoria.dto.AuditoriaResponseDTO;
import com.umg.sgau.auditoria.service.AuditoriaService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {
    private final AuditoriaService service;
    public AuditoriaController(AuditoriaService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDITORIA_LEER')")
    public Page<AuditoriaResponseDTO> buscar(
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required=false) Long usuarioId, @RequestParam(required=false) String username,
            @RequestParam(required=false) String modulo, @RequestParam(required=false) String accion,
            @RequestParam(required=false) String tipoEntidad, @RequestParam(required=false) String entidadId,
            @PageableDefault(sort = {"fechaHora", "id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return service.buscar(desde, hasta, usuarioId, username, modulo, accion, tipoEntidad, entidadId, pageable);
    }
}
