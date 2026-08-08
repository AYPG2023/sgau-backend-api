package com.umg.sgau.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.service.AuthService;
import com.umg.sgau.usuario.dto.UsuarioResponseDTO;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String LOGIN_URL = "/api/auth/login";

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new AuthExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void loginConCredencialesValidasRetornaToken() throws Exception {
        LoginResponseDTO response = LoginResponseDTO.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .usuarioId(1L)
                .username("admin")
                .nombre("Admin")
                .apellido("SGAU")
                .roles(Set.of("ADMIN"))
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin123*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    void loginConPasswordIncorrectoRetorna401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales invalidas"));
    }

    @Test
    void loginConUsuarioInexistenteRetorna401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"no_existe\",\"password\":\"Admin123*\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales invalidas"));
    }

    @Test
    void loginConUsuarioInactivoRetorna401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin123*\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales invalidas"));
    }

    @Test
    void loginConCuerpoJsonInvalidoRetorna400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud debe ser un JSON valido"));
    }

    @Test
    void loginSinUsernameRetorna400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Admin123*\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("username")));
    }

    @Test
    void registerConDatosValidosRetorna201SinRoles() throws Exception {
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(2L);
        response.setUsername("nuevo");
        response.setEmail("nuevo@sgau.test");
        response.setNombre("Nuevo");
        response.setApellido("Usuario");
        response.setActivo(true);
        response.setRoles(Set.of());

        when(authService.registrar(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "nuevo",
                                  "password": "Usuario123*",
                                  "email": "nuevo@sgau.test",
                                  "nombre": "Nuevo",
                                  "apellido": "Usuario"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.username").value("nuevo"))
                .andExpect(jsonPath("$.roles").isEmpty());
    }

    @Test
    void registerConUsernameDuplicadoRetorna409() throws Exception {
        when(authService.registrar(any()))
                .thenThrow(new IllegalArgumentException("Ya existe un usuario con ese username"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "Usuario123*",
                                  "email": "admin@sgau.test",
                                  "nombre": "Admin",
                                  "apellido": "Sistema"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe un usuario con ese username"));
    }

    @Test
    void registerConPasswordCortaRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "nuevo",
                                  "password": "123",
                                  "email": "nuevo@sgau.test",
                                  "nombre": "Nuevo",
                                  "apellido": "Usuario"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("contrasena")));
    }
}
