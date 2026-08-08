package com.umg.sgau.rol.controller;

import com.umg.sgau.permiso.dto.PermisoSummaryDTO;
import com.umg.sgau.permiso.mapper.PermisoMapper;
import com.umg.sgau.rol.dto.RolCreateRequestDTO;
import com.umg.sgau.rol.dto.RolPermisosRequestDTO;
import com.umg.sgau.rol.dto.RolResponseDTO;
import com.umg.sgau.rol.dto.RolStatusRequestDTO;
import com.umg.sgau.rol.dto.RolUpdateRequestDTO;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.mapper.RolMapper;
import com.umg.sgau.rol.service.RolService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
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
@RequestMapping("/api/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping
    public ResponseEntity<RolResponseDTO> crear(@Valid @RequestBody RolCreateRequestDTO request) {
        Rol rol = rolService.crear(RolMapper.aEntidad(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(RolMapper.aResponseDTO(rol));
    }

    @GetMapping
    public ResponseEntity<Page<RolResponseDTO>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        return ResponseEntity.ok(rolService.listar(texto, activo, pageable)
                .map(RolMapper::aResponseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RolMapper.aResponseDTO(rolService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RolUpdateRequestDTO request) {
        Rol datos = new Rol();
        RolMapper.actualizarEntidad(request, datos);
        return ResponseEntity.ok(RolMapper.aResponseDTO(rolService.actualizar(id, datos)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RolResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody RolStatusRequestDTO request) {
        return ResponseEntity.ok(RolMapper.aResponseDTO(rolService.cambiarEstado(id, request.getActivo())));
    }

    @PutMapping("/{id}/permisos")
    public ResponseEntity<RolResponseDTO> asignarPermisos(
            @PathVariable Long id,
            @Valid @RequestBody RolPermisosRequestDTO request) {
        return ResponseEntity.ok(RolMapper.aResponseDTO(
                rolService.asignarPermisos(id, request.getPermisoIds())));
    }

    @GetMapping("/{id}/permisos")
    public ResponseEntity<Set<PermisoSummaryDTO>> obtenerPermisos(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerPermisosDelRol(id)
                .stream()
                .map(PermisoMapper::aSummaryDTO)
                .collect(Collectors.toSet()));
    }
}
