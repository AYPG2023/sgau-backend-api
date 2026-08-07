package com.umg.sgau.config;

import com.umg.sgau.usuario.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String secret;
    private final long expiration;
    private final String issuer;

    public JwtService(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration:3600000}") long expiration,
            @Value("${jwt.issuer:sgau-backend-api}") String issuer) {
        this.secret = secret;
        this.expiration = expiration;
        this.issuer = issuer;
    }

    public String generarToken(UserDetails userDetails) {
        return generarToken(new HashMap<>(), userDetails);
    }

    public String generarToken(Usuario usuario, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId());
        claims.put("email", usuario.getEmail());
        claims.put("role", usuario.getRol() == null ? null : usuario.getRol().name());
        return generarToken(claims, userDetails);
    }

    public String generarToken(Map<String, Object> claims, UserDetails userDetails) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey())
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public Date extraerExpiracion(String token) {
        return extraerClaims(token).getExpiration();
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        String username = extraerUsername(token);
        return username.equals(userDetails.getUsername()) && !estaExpirado(token);
    }

    public long getExpirationSeconds() {
        return expiration / 1000;
    }

    private boolean estaExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey secretKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET no esta configurado.");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
