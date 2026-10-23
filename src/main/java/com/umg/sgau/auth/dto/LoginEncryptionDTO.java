package com.umg.sgau.auth.dto;

import java.time.Instant;
import java.util.Map;

public record LoginEncryptionDTO(String kid, Map<String, Object> publicKey, String challenge, Instant expiresAt) {}
