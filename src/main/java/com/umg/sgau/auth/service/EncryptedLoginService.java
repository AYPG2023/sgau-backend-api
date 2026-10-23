package com.umg.sgau.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.umg.sgau.auth.dto.*;
import java.text.ParseException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "login.encryption.enabled", havingValue = "true")
public class EncryptedLoginService {
    private static final int MAX_JWE_LENGTH = 16_384;
    private final AuthService authService;
    private final LoginChallengeService challengeService;
    private final ObjectMapper objectMapper;
    private final RSAKey encryptionKey;
    private final String kid;

    public EncryptedLoginService(AuthService authService, LoginChallengeService challengeService,
            ObjectMapper objectMapper,
            @Value("${login.encryption.private-jwk:}") String privateJwk,
            @Value("${login.encryption.kid:}") String configuredKid) {
        this.authService = authService;
        this.challengeService = challengeService;
        this.objectMapper = objectMapper;
        try {
            this.encryptionKey = RSAKey.parse(privateJwk);
        } catch (ParseException exception) {
            throw new IllegalStateException("LOGIN_ENCRYPTION_PRIVATE_JWK debe ser un JWK RSA privado valido", exception);
        }
        if (!encryptionKey.isPrivate() || encryptionKey.size() < 2048) {
            throw new IllegalStateException("La clave de cifrado de login debe ser RSA privada de al menos 2048 bits");
        }
        this.kid = configuredKid.isBlank() ? encryptionKey.getKeyID() : configuredKid;
        if (kid == null || kid.isBlank()) throw new IllegalStateException("LOGIN_ENCRYPTION_KID es obligatorio");
    }

    public LoginEncryptionDTO encryptionConfig() {
        LoginChallengeService.IssuedChallenge challenge = challengeService.issue();
        Map<String, Object> publicJwk = encryptionKey.toPublicJWK().toJSONObject();
        publicJwk.put("kid", kid);
        publicJwk.put("alg", JWEAlgorithm.RSA_OAEP_256.getName());
        publicJwk.put("use", "enc");
        return new LoginEncryptionDTO(kid, publicJwk, challenge.value(), challenge.expiresAt());
    }

    public LoginResponseDTO login(EncryptedLoginRequestDTO request) {
        String compact = request.encryptedCredentials();
        if (compact == null || compact.length() > MAX_JWE_LENGTH || compact.split("\\.", -1).length != 5) throw invalid();
        try {
            JWEObject jwe = JWEObject.parse(compact);
            if (!JWEAlgorithm.RSA_OAEP_256.equals(jwe.getHeader().getAlgorithm())
                    || !EncryptionMethod.A256GCM.equals(jwe.getHeader().getEncryptionMethod())
                    || !kid.equals(jwe.getHeader().getKeyID())) throw invalid();
            jwe.decrypt(new RSADecrypter(encryptionKey));
            Credentials credentials = objectMapper.readValue(jwe.getPayload().toBytes(), Credentials.class);
            if (!credentials.valid() || !challengeService.consume(credentials.challenge())) throw invalid();
            return authService.login(LoginRequestDTO.builder()
                    .username(credentials.username().trim()).password(credentials.password()).build());
        } catch (BadCredentialsException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid();
        }
    }

    private BadCredentialsException invalid() { return new BadCredentialsException("Solicitud de acceso invalida"); }

    private record Credentials(String username, String password, String challenge) {
        boolean valid() {
            return username != null && !username.isBlank() && username.length() <= 100
                    && password != null && !password.isBlank() && password.length() <= 200
                    && challenge != null && !challenge.isBlank() && challenge.length() <= 128;
        }
    }
}
