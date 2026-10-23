package com.umg.sgau.auth.controller;

import com.umg.sgau.auth.dto.EncryptedLoginRequestDTO;
import com.umg.sgau.auth.dto.LoginEncryptionDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.service.EncryptedLoginService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(name = "login.encryption.enabled", havingValue = "true")
public class EncryptedAuthController {
    private final EncryptedLoginService encryptedLoginService;

    public EncryptedAuthController(EncryptedLoginService encryptedLoginService) {
        this.encryptedLoginService = encryptedLoginService;
    }

    @GetMapping("/encryption")
    public ResponseEntity<LoginEncryptionDTO> encryption() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(encryptedLoginService.encryptionConfig());
    }

    @PostMapping("/login/encrypted")
    public ResponseEntity<LoginResponseDTO> encryptedLogin(@Valid @RequestBody EncryptedLoginRequestDTO request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(encryptedLoginService.login(request));
    }
}
