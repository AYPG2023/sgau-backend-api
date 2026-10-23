package com.umg.sgau.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.umg.sgau.auth.dto.EncryptedLoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

class EncryptedLoginServiceTest {
    private RSAKey key;
    private AuthService authService;
    private LoginChallengeService challenges;
    private EncryptedLoginService service;

    @BeforeEach
    void setUp() throws Exception {
        key = new RSAKeyGenerator(2048).keyID("login-key-1").generate();
        authService = mock(AuthService.class);
        challenges = mock(LoginChallengeService.class);
        service = new EncryptedLoginService(authService, challenges, new ObjectMapper(),
                key.toJSONString(), "login-key-1");
    }

    @Test
    void loginValidoReutilizaAutenticacionActualYGeneraJwt() throws Exception {
        LoginResponseDTO response = LoginResponseDTO.builder().accessToken("jwt").build();
        when(challenges.consume("challenge")).thenReturn(true);
        when(authService.login(any())).thenReturn(response);

        assertThat(service.login(request("login-key-1", JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A256GCM, "challenge"))).isSameAs(response);
        verify(authService).login(argThat(value -> value.getUsername().equals("ada")
                && value.getPassword().equals("secret")));
    }

    @Test
    void credencialesIncorrectasSeRechazan() throws Exception {
        when(challenges.consume("challenge")).thenReturn(true);
        when(authService.login(any())).thenThrow(new BadCredentialsException("Credenciales invalidas"));
        assertThatThrownBy(() -> service.login(request("login-key-1", JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A256GCM, "challenge"))).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void algoritmoNoPermitidoSeRechaza() throws Exception {
        assertThatThrownBy(() -> service.login(request("login-key-1", JWEAlgorithm.RSA_OAEP,
                EncryptionMethod.A256GCM, "challenge"))).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(authService, challenges);
    }

    @Test
    void kidDesconocidoSeRechaza() throws Exception {
        assertThatThrownBy(() -> service.login(request("otra-clave", JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A256GCM, "challenge"))).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(authService, challenges);
    }

    @Test
    void jweAlteradoSeRechaza() throws Exception {
        String compact = request("login-key-1", JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A256GCM, "challenge").encryptedCredentials();
        String altered = compact.substring(0, compact.length() - 1) + (compact.endsWith("A") ? "B" : "A");
        assertThatThrownBy(() -> service.login(new EncryptedLoginRequestDTO(altered)))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(authService, challenges);
    }

    @Test
    void desafioVencidoOReutilizadoSeRechazaAntesDeAutenticar() throws Exception {
        when(challenges.consume("challenge")).thenReturn(false);
        assertThatThrownBy(() -> service.login(request("login-key-1", JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A256GCM, "challenge"))).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(authService);
    }

    private EncryptedLoginRequestDTO request(String kid, JWEAlgorithm algorithm,
            EncryptionMethod encryptionMethod, String challenge) throws Exception {
        JWEHeader header = new JWEHeader.Builder(algorithm, encryptionMethod).keyID(kid).type(new JOSEObjectType("JWE")).build();
        JWEObject jwe = new JWEObject(header,
                new Payload("{\"username\":\"ada\",\"password\":\"secret\",\"challenge\":\"" + challenge + "\"}"));
        jwe.encrypt(new RSAEncrypter(key.toPublicJWK()));
        return new EncryptedLoginRequestDTO(jwe.serialize());
    }
}
