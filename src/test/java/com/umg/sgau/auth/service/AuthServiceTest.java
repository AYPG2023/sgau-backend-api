package com.umg.sgau.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.dto.RegisterRequestDTO;
import com.umg.sgau.auth.service.PasswordChangeRateLimiter;
import com.umg.sgau.config.JwtService;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.usuario.dto.UsuarioResponseDTO;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import com.umg.sgau.usuario.service.UsuarioService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordChangeRateLimiter passwordChangeRateLimiter;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private DocenteRepository docenteRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, usuarioRepository, usuarioService, jwtService,
                passwordEncoder, passwordChangeRateLimiter, estudianteRepository, docenteRepository);
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
        when(usuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(usuario, principal)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponseDTO response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUsuarioId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRoles()).containsExactly("ADMIN");
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
        when(usuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(usuario("admin", false)));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any(Usuario.class), any(UserDetails.class));
    }

    @Test
    void loginConUsuarioInexistenteRetornaCredencialesInvalidas() {
        LoginRequestDTO request = loginRequest();
        UserDetails principal = userDetails("admin");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any(Usuario.class), any(UserDetails.class));
    }

    @Test
    void registrarCreaUsuarioActivoSinRoles() {
        RegisterRequestDTO request = registerRequest();
        Usuario creado = usuario("nuevo", true);
        creado.setRoles(new HashSet<>());

        when(usuarioService.crear(any(Usuario.class))).thenReturn(creado);

        UsuarioResponseDTO response = authService.registrar(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioService).crear(usuarioCaptor.capture());
        Usuario usuarioParaCrear = usuarioCaptor.getValue();

        assertThat(usuarioParaCrear.getUsername()).isEqualTo("nuevo");
        assertThat(usuarioParaCrear.getPassword()).isEqualTo("Usuario123*");
        assertThat(usuarioParaCrear.getActivo()).isTrue();
        assertThat(usuarioParaCrear.getRoles()).isEmpty();
        assertThat(response.getUsername()).isEqualTo("nuevo");
        assertThat(response.getRoles()).isEmpty();
    }

    private LoginRequestDTO loginRequest() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("admin");
        request.setPassword("secret");
        return request;
    }

    private RegisterRequestDTO registerRequest() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("nuevo");
        request.setPassword("Usuario123*");
        request.setEmail("nuevo@sgau.test");
        request.setNombre("Nuevo");
        request.setApellido("Usuario");
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
        usuario.setRoles(new HashSet<>(List.of(rolAdmin())));
        return usuario;
    }

    private Rol rolAdmin() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setCodigo("ADMIN");
        rol.setNombre("Administrador");
        rol.setActivo(true);
        return rol;
    }
}
