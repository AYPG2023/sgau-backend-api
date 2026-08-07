package com.umg.sgau.config;

import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(identificador, identificador)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("El usuario se encuentra inactivo");
        }

        String rol = usuario.getRol() == null ? "ESTUDIANTE" : usuario.getRol().name();
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities("ROLE_" + rol)
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
