package com.umg.sgau.auth.service;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.dto.PerfilResponseDTO;
import com.umg.sgau.config.JwtService;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
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
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getIdentificador(),
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
                    .rol(usuario.getRol() == null ? null : usuario.getRol().name())
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
                .rol(usuario.getRol() == null ? null : usuario.getRol().name())
                .activo(usuario.getActivo())
                .build();
    }

    private Usuario buscarUsuarioActivo(String identificador) {
        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(identificador, identificador)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("El usuario se encuentra inactivo");
        }

        return usuario;
    }
}
