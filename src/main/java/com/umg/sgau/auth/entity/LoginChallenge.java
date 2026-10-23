package com.umg.sgau.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "login_challenges")
public class LoginChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "challenge_hash", nullable = false, unique = true, length = 64)
    private String challengeHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected LoginChallenge() {}
    public LoginChallenge(String challengeHash, Instant expiresAt) {
        this.challengeHash = challengeHash;
        this.expiresAt = expiresAt;
    }
}
