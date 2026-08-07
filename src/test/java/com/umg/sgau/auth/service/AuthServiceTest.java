package com.umg.sgau.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.config.JwtService;
import com.umg.sgau.usuario.entity.Rol;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, usuarioRepository, jwtService);
    }

    @Test
    void loginConCredencialesValidasRetornaTokenYPerfilBasico() {
        LoginRequestDTO request = loginRequest();
        UserDetails principal = userDetails("admin");
        Usuario usuario = usuario("admin", true);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("admin", "admin"))
                .thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(usuario, principal)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponseDTO response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUsuarioId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRol()).isEqualTo("ADMIN");
    }

    @Test
    void loginConCredencialesInvalidasNoGeneraToken() {
        LoginRequestDTO request = loginRequest();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any(Usuario.class), any(UserDetails.class));
    }

    @Test
    void loginConUsuarioInactivoRetornaCredencialesInvalidas() {
        LoginRequestDTO request = loginRequest();
        UserDetails principal = userDetails("admin");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("admin", "admin"))
                .thenReturn(Optional.of(usuario("admin", false)));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any(Usuario.class), any(UserDetails.class));
    }

    private LoginRequestDTO loginRequest() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador("admin");
        request.setPassword("secret");
        return request;
    }

    private UserDetails userDetails(String username) {
        return new User(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Usuario usuario(String username, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setEmail(username + "@sgau.test");
        usuario.setNombre("Admin");
        usuario.setApellido("SGAU");
        usuario.setActivo(activo);
        usuario.setRol(Rol.ADMIN);
        return usuario;
    }
}
