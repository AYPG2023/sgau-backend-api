package com.umg.sgau.usuario.controller;

import com.umg.sgau.rol.dto.RolSummaryDTO;
import com.umg.sgau.rol.mapper.RolMapper;
import com.umg.sgau.usuario.dto.UsuarioRequestDTO;
import com.umg.sgau.usuario.dto.UsuarioResponseDTO;
import com.umg.sgau.usuario.dto.UsuarioRolesRequestDTO;
import com.umg.sgau.usuario.dto.AltaUsuarioRequestDTO;
import com.umg.sgau.usuario.dto.AltaUsuarioResponseDTO;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.exception.UsuarioNoEncontradoException;
import com.umg.sgau.usuario.mapper.UsuarioMapper;
import com.umg.sgau.usuario.service.UsuarioService;
import com.umg.sgau.usuario.service.AltaUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AltaUsuarioService altaUsuarioService;

    // Inyección por constructor 
    public UsuarioController(UsuarioService usuarioService, AltaUsuarioService altaUsuarioService) {
        this.usuarioService = usuarioService;
        this.altaUsuarioService = altaUsuarioService;
    }

    @PostMapping("/alta-conjunta")
    @Operation(summary = "Alta administrativa conjunta y transaccional",
            description = "Crea una cuenta nueva, sus roles y los perfiles DOCENTE y/o ESTUDIANTE en una sola transaccion. No reutiliza cuentas existentes por correo. Ambos perfiles pueden crearse para una misma cuenta.")
    public ResponseEntity<AltaUsuarioResponseDTO> altaConjunta(
            @Valid @RequestBody AltaUsuarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(altaUsuarioService.crear(request));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRequestDTO request) {
        Usuario usuarioCreado = usuarioService.crear(UsuarioMapper.aEntidad(request));
        UsuarioResponseDTO response = UsuarioMapper.aResponseDTO(usuarioCreado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.obtenerPorId(id);
            return ResponseEntity.ok(UsuarioMapper.aResponseDTO(usuario));
        } catch (UsuarioNoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodos() {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        List<UsuarioResponseDTO> response = UsuarioMapper.aResponseDTOList(usuarios);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO request) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizar(id, UsuarioMapper.aEntidad(request));
            return ResponseEntity.ok(UsuarioMapper.aResponseDTO(usuarioActualizado));
        } catch (UsuarioNoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (UsuarioNoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UsuarioResponseDTO> asignarRoles(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRolesRequestDTO request) {
        Usuario usuario = usuarioService.asignarRoles(id, request.getRolIds());
        return ResponseEntity.ok(UsuarioMapper.aResponseDTO(usuario));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<RolSummaryDTO>> obtenerRoles(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerRoles(id)
                .stream()
                .map(RolMapper::aSummaryDTO)
                .collect(Collectors.toSet()));
    }
}
