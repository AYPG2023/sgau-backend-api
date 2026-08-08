package com.umg.sgau.config;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.Set;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository
                .findWithRolesAndPermisosByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("El usuario se encuentra inactivo");
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(obtenerAutoridades(usuario).toArray(String[]::new))
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }

    private Set<String> obtenerAutoridades(Usuario usuario) {
        Set<String> autoridades = new HashSet<>();
        for (Rol rol : usuario.getRoles()) {
            if (!Boolean.TRUE.equals(rol.getActivo())) {
                continue;
            }
            autoridades.add("ROLE_" + rol.getCodigo());
            for (Permiso permiso : rol.getPermisos()) {
                if (Boolean.TRUE.equals(permiso.getActivo())) {
                    autoridades.add(permiso.getCodigo());
                }
            }
        }
        return autoridades;
    }
}
