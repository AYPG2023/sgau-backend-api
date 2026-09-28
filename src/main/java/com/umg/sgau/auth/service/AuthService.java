package com.umg.sgau.auth.service;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.dto.PerfilResponseDTO;
import com.umg.sgau.auth.dto.RegisterRequestDTO;
import com.umg.sgau.config.JwtService;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.usuario.dto.UsuarioResponseDTO;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.mapper.UsuarioMapper;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import com.umg.sgau.usuario.service.UsuarioService;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
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
        return PerfilResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .roles(obtenerCodigosRolesActivos(usuario))
                .permisos(obtenerCodigosPermisosActivos(usuario))
                .activo(usuario.getActivo())
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
