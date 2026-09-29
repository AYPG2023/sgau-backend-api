package com.umg.sgau.auth.service;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.dto.PerfilResponseDTO;
import com.umg.sgau.auth.dto.PerfilUpdateRequestDTO;
import com.umg.sgau.auth.dto.PasswordChangeRequestDTO;
import com.umg.sgau.auth.dto.RegisterRequestDTO;
import com.umg.sgau.config.JwtService;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.usuario.dto.UsuarioResponseDTO;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.mapper.UsuarioMapper;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import com.umg.sgau.usuario.service.UsuarioService;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("SECURITY_EVENTS");
    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordChangeRateLimiter passwordChangeRateLimiter;
    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            PasswordChangeRateLimiter passwordChangeRateLimiter,
            EstudianteRepository estudianteRepository,
            DocenteRepository docenteRepository) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.passwordChangeRateLimiter = passwordChangeRateLimiter;
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Usuario usuario = buscarUsuarioActivo(userDetails.getUsername());
            String token = jwtService.generarToken(usuario, userDetails);

            return LoginResponseDTO.builder()
                    .accessToken(token)
                    .tokenType(TOKEN_TYPE)
                    .expiresIn(jwtService.getExpirationSeconds())
                    .usuarioId(usuario.getId())
                    .docenteId(docenteRepository.findByUsuarioId(usuario.getId()).map(d -> d.getId()).orElse(null))
                    .estudianteId(estudianteRepository.findByUsuarioId(usuario.getId()).map(e -> e.getId()).orElse(null))
                    .username(usuario.getUsername())
                    .nombre(usuario.getNombre())
                    .apellido(usuario.getApellido())
                    .roles(obtenerCodigosRolesActivos(usuario))
                    .permisos(obtenerCodigosPermisosActivos(usuario))
                    .build();
        } catch (AuthenticationException exception) {
            throw new BadCredentialsException("Credenciales invalidas");
        }
    }

    public PerfilResponseDTO obtenerPerfil(String username) {
        Usuario usuario = buscarUsuarioActivo(username);
        return perfilDesdeUsuario(usuario);
    }

    @Transactional
    public PerfilResponseDTO actualizarPerfil(String usernameAutenticado, PerfilUpdateRequestDTO request) {
        Usuario usuario = buscarUsuarioActivo(usernameAutenticado);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(username, usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese username");
        }
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        boolean usernameCambio = !usuario.getUsername().equals(username);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        Usuario actualizado = usuarioRepository.save(usuario);
        SECURITY_LOG.info("Evento seguridad: perfil actualizado para usuarioId={}, usernameCambio={}",
                actualizado.getId(), usernameCambio);
        PerfilResponseDTO response = perfilDesdeUsuario(actualizado);
        response.setRequiereNuevoLogin(usernameCambio);
        return response;
    }

    @Transactional
    public void cambiarPassword(String usernameAutenticado, PasswordChangeRequestDTO request) {
        if (passwordChangeRateLimiter.isBlocked(usernameAutenticado)) {
            SECURITY_LOG.warn("Evento seguridad: cambio de contrasena bloqueado por limite para usuario={}",
                    usernameAutenticado);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Intente nuevamente mas tarde.");
        }
        Usuario usuario = buscarUsuarioActivo(usernameAutenticado);
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            passwordChangeRateLimiter.recordFailure(usernameAutenticado);
            SECURITY_LOG.warn("Evento seguridad: fallo de verificacion para cambio de contrasena usuarioId={}",
                    usuario.getId());
            throw new BadCredentialsException("La contrasena actual es incorrecta");
        }
        if (passwordEncoder.matches(request.getNewPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("La nueva contrasena debe ser diferente a la actual");
        }
        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);
        passwordChangeRateLimiter.clear(usernameAutenticado);
        SECURITY_LOG.info("Evento seguridad: contrasena actualizada para usuarioId={}", usuario.getId());
    }

    private PerfilResponseDTO perfilDesdeUsuario(Usuario usuario) {
        return PerfilResponseDTO.builder()
                .id(usuario.getId())
                .usuarioId(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .roles(obtenerCodigosRolesActivos(usuario))
                .permisos(obtenerCodigosPermisosActivos(usuario))
                .activo(usuario.getActivo())
                .requiereNuevoLogin(false)
                .build();
    }

    public UsuarioResponseDTO registrar(RegisterRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(request.getPassword());
        usuario.setEmail(request.getEmail());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setActivo(true);

        return UsuarioMapper.aResponseDTO(usuarioService.crear(usuario));
    }

    private Usuario buscarUsuarioActivo(String username) {
        Usuario usuario = usuarioRepository
                .findWithRolesAndPermisosByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("El usuario se encuentra inactivo");
        }

        return usuario;
    }

    private Set<String> obtenerCodigosRolesActivos(Usuario usuario) {
        return usuario.getRoles().stream()
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .map(Rol::getCodigo)
                .collect(Collectors.toSet());
    }

    private Set<String> obtenerCodigosPermisosActivos(Usuario usuario) {
        return usuario.getRoles().stream()
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .flatMap(rol -> rol.getPermisos().stream())
                .filter(permiso -> Boolean.TRUE.equals(permiso.getActivo()))
                .map(Permiso::getCodigo)
                .collect(Collectors.toSet());
    }
}
