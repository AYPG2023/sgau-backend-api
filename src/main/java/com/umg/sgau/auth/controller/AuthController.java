package com.umg.sgau.auth.controller;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.dto.PasswordChangeRequestDTO;
import com.umg.sgau.auth.dto.PerfilResponseDTO;
import com.umg.sgau.auth.dto.PerfilUpdateRequestDTO;
import com.umg.sgau.auth.dto.RegisterRequestDTO;
import com.umg.sgau.auth.service.AuthService;
import com.umg.sgau.usuario.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar el perfil propio y sus permisos efectivos")
    public ResponseEntity<PerfilResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.obtenerPerfil(authentication.getName()));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar los datos personales del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o vencido"),
            @ApiResponse(responseCode = "409", description = "Username o email duplicado")
    })
    public ResponseEntity<PerfilResponseDTO> actualizarMiPerfil(
            Authentication authentication,
            @Valid @RequestBody PerfilUpdateRequestDTO request) {
        return ResponseEntity.ok(authService.actualizarPerfil(authentication.getName(), request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Cambiar la contrasena del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contrasena actualizada"),
            @ApiResponse(responseCode = "400", description = "Nueva contrasena invalida"),
            @ApiResponse(responseCode = "401", description = "Token invalido o contrasena actual incorrecta"),
            @ApiResponse(responseCode = "429", description = "Limite de intentos alcanzado")
    })
    public ResponseEntity<Void> cambiarMiPassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequestDTO request) {
        authService.cambiarPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
