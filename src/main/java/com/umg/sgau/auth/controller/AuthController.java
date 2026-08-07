package com.umg.sgau.auth.controller;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.dto.PerfilResponseDTO;
import com.umg.sgau.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/me")
    public ResponseEntity<PerfilResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.obtenerPerfil(authentication.getName()));
    }
}
