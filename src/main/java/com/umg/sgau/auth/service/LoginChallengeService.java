package com.umg.sgau.auth.service;

import com.umg.sgau.auth.entity.LoginChallenge;
import com.umg.sgau.auth.repository.LoginChallengeRepository;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginChallengeService {
    private final LoginChallengeRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration ttl;

    public LoginChallengeService(LoginChallengeRepository repository,
            @Value("${login.encryption.challenge-ttl-seconds:120}") long ttlSeconds) {
        this.repository = repository;
        this.ttl = Duration.ofSeconds(Math.max(30, Math.min(ttlSeconds, 300)));
    }

    @Transactional
    public IssuedChallenge issue() {
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        Instant expiresAt = Instant.now().plus(ttl);
        repository.save(new LoginChallenge(hash(value), expiresAt));
        return new IssuedChallenge(value, expiresAt);
    }

    @Transactional
    public boolean consume(String challenge) {
        return challenge != null && repository.consumeIfValid(hash(challenge)) == 1;
    }

    private String hash(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 no disponible", impossible);
        }
    }

    public record IssuedChallenge(String value, Instant expiresAt) {}
}
