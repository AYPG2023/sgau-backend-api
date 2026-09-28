package com.umg.sgau.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class PasswordChangeRateLimiter {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Attempts> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        String key = canonical(username);
        Attempts current = attempts.get(key);
        if (current == null) return false;
        if (current.startedAt().plus(WINDOW).isBefore(Instant.now())) {
            attempts.remove(key, current);
            return false;
        }
        return current.count().get() >= MAX_FAILURES;
    }

    public void recordFailure(String username) {
        attempts.compute(canonical(username), (key, current) -> {
            if (current == null || current.startedAt().plus(WINDOW).isBefore(Instant.now())) {
                return new Attempts(Instant.now(), new AtomicInteger(1));
            }
            current.count().incrementAndGet();
            return current;
        });
    }

    public void clear(String username) {
        attempts.remove(canonical(username));
    }

    private String canonical(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    private record Attempts(Instant startedAt, AtomicInteger count) { }
}
