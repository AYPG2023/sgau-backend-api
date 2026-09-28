package com.umg.sgau.auth.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.auth.service.PasswordChangeRateLimiter;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "jwt.secret=clave-de-pruebas-segura-de-al-menos-32-bytes")
@AutoConfigureMockMvc
@Transactional
class AuthSelfServiceIntegrationTest {

    private static final String USERNAME = "self_service_user";
    private static final String PASSWORD = "ContrasenaActual123*";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired EstudianteRepository estudianteRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PasswordChangeRateLimiter passwordChangeRateLimiter;

    @AfterEach
    void limpiarLimiteEntrePruebas() {
        passwordChangeRateLimiter.clear(USERNAME);
    }

    @BeforeEach
    void prepararUsuarioSinPermisosAdministrativos() {
        Rol rol = new Rol();
        rol.setCodigo("AUTOSERVICIO_TEST");
        rol.setNombre("Autoservicio test");
        rol.setActivo(true);
        rol.setPermisos(new HashSet<>());
        rol = rolRepository.save(rol);

        Usuario usuario = new Usuario();
        usuario.setUsername(USERNAME);
        usuario.setEmail("self-service@sgau.test");
        usuario.setNombre("Nombre original");
        usuario.setApellido("Apellido original");
        usuario.setPassword(passwordEncoder.encode(PASSWORD));
        usuario.setActivo(true);
        usuario.setRoles(new HashSet<>(java.util.Set.of(rol)));
        usuarioRepository.save(usuario);

        estudianteRepository.save(Estudiante.builder()
                .codigoEstudiantil("SELF001")
                .numeroIdentificacion("IDSELF001")
                .nombres("Nombre original")
                .apellidos("Apellido original")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .correo("self-service@sgau.test")
                .activo(true)
                .build());
    }

    @Test
    void cualquierUsuarioAutenticadoActualizaSoloSusDatosSinPermisoAdministrativo() throws Exception {
        String token = login(USERNAME, PASSWORD);

        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"nuevo_self_service",
                                  "email":"nuevo-self-service@sgau.test",
                                  "nombre":"Nombre nuevo",
                                  "apellido":"Apellido nuevo",
                                  "roles":[],
                                  "permisos":["USUARIOS_EDITAR"],
                                  "activo":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").isNumber())
                .andExpect(jsonPath("$.username").value("nuevo_self_service"))
                .andExpect(jsonPath("$.email").value("nuevo-self-service@sgau.test"))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.permisos", hasSize(0)))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.requiereNuevoLogin").value(true));

        org.assertj.core.api.Assertions.assertThat(
                estudianteRepository.findByCorreoIgnoreCase("nuevo-self-service@sgau.test")).isPresent();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultarPerfilIncluyeLosCamposSolicitados() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + login(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").isNumber())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.email").value("self-service@sgau.test"))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.permisos", hasSize(0)));
    }

    @Test
    void usernameYEmailDuplicadosRetornan409() throws Exception {
        Usuario otro = new Usuario();
        otro.setUsername("otro_self_service");
        otro.setEmail("otro-self-service@sgau.test");
        otro.setNombre("Otro");
        otro.setApellido("Usuario");
        otro.setPassword(passwordEncoder.encode(PASSWORD));
        otro.setActivo(true);
        otro.setRoles(new HashSet<>());
        usuarioRepository.save(otro);
        String token = login(USERNAME, PASSWORD);

        mockMvc.perform(put("/api/auth/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profile("otro_self_service", "unico@sgau.test")))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/auth/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profile("username_unico", "otro-self-service@sgau.test")))
                .andExpect(status().isConflict());
    }

    @Test
    void perfilInvalidoRetorna400() throws Exception {
        mockMvc.perform(put("/api/auth/me").header("Authorization", "Bearer " + login(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profile("bad username!", "correo-invalido")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambioPasswordCompruebaLaActualYLaPolitica() throws Exception {
        String token = login(USERNAME, PASSWORD);
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson("incorrecta", "NuevaContrasena123*")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(PASSWORD, "corta")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(PASSWORD, PASSWORD)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(PASSWORD, "NuevaContrasena123*")))
                .andExpect(status().isNoContent());

        // Los tokens existentes permanecen validos hasta expirar; la nueva clave aplica al siguiente login.
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        login(USERNAME, "NuevaContrasena123*");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilYPasswordSinTokenOConTokenVencidoRetornan401() throws Exception {
        mockMvc.perform(put("/api/auth/me").contentType(MediaType.APPLICATION_JSON)
                        .content(profile("nuevo_user", "nuevo@sgau.test")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/auth/me/password").contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(PASSWORD, "NuevaContrasena123*")))
                .andExpect(status().isUnauthorized());

        String expired = Jwts.builder()
                .subject(USERNAME)
                .issuedAt(Date.from(Instant.parse("2020-01-01T00:00:00Z")))
                .expiration(Date.from(Instant.parse("2020-01-01T00:01:00Z")))
                .signWith(Keys.hmacShaKeyFor("clave-de-pruebas-segura-de-al-menos-32-bytes".getBytes(StandardCharsets.UTF_8)))
                .compact();
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + expired)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(PASSWORD, "NuevaContrasena123*")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bloqueaReintentosDePasswordYReiniciaElContadorTrasExito() throws Exception {
        String token = login(USERNAME, PASSWORD);
        for (int intento = 0; intento < 5; intento++) {
            mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(passwordJson("password-incorrecta", "NuevaContrasena123*")))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(PASSWORD, "NuevaContrasena123*")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void unCambioExitosoLimpiaLosIntentosFallidos() throws Exception {
        String token = login(USERNAME, PASSWORD);
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson("password-incorrecta", "NuevaContrasena123*")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(PASSWORD, "NuevaContrasena123*")))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson("password-incorrecta", "OtraContrasena123*")))
                .andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private String profile(String username, String email) {
        return "{\"username\":\"" + username + "\",\"email\":\"" + email
                + "\",\"nombre\":\"Nombre\",\"apellido\":\"Apellido\"}";
    }

    private String passwordJson(String current, String next) {
        return "{\"currentPassword\":\"" + current + "\",\"newPassword\":\"" + next + "\"}";
    }
}
