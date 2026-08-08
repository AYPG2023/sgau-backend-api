package com.umg.sgau.permiso.controller;

import com.umg.sgau.permiso.dto.PermisoCreateRequestDTO;
import com.umg.sgau.permiso.dto.PermisoResponseDTO;
import com.umg.sgau.permiso.dto.PermisoStatusRequestDTO;
import com.umg.sgau.permiso.dto.PermisoSummaryDTO;
import com.umg.sgau.permiso.dto.PermisoUpdateRequestDTO;
import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.permiso.mapper.PermisoMapper;
import com.umg.sgau.permiso.service.PermisoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @PostMapping
    public ResponseEntity<PermisoResponseDTO> crear(@Valid @RequestBody PermisoCreateRequestDTO request) {
        Permiso permiso = permisoService.crear(PermisoMapper.aEntidad(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PermisoMapper.aResponseDTO(permiso));
    }

    @GetMapping
    public ResponseEntity<Page<PermisoResponseDTO>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        return ResponseEntity.ok(permisoService.listar(texto, activo, pageable)
                .map(PermisoMapper::aResponseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermisoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(PermisoMapper.aResponseDTO(permisoService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermisoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PermisoUpdateRequestDTO request) {
        Permiso datos = new Permiso();
        PermisoMapper.actualizarEntidad(request, datos);
        return ResponseEntity.ok(PermisoMapper.aResponseDTO(permisoService.actualizar(id, datos)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PermisoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody PermisoStatusRequestDTO request) {
        return ResponseEntity.ok(PermisoMapper.aResponseDTO(
                permisoService.cambiarEstado(id, request.getActivo())));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<PermisoSummaryDTO>> obtenerActivos() {
        return ResponseEntity.ok(permisoService.obtenerPermisosActivos()
                .stream()
                .map(PermisoMapper::aSummaryDTO)
                .toList());
    }
}
