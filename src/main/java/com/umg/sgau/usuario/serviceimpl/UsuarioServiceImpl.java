package com.umg.sgau.usuario.serviceimpl;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.exception.RolInactivoException;
import com.umg.sgau.rol.exception.RolNoEncontradoException;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.exception.UsuarioNoEncontradoException;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import com.umg.sgau.usuario.service.UsuarioService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario crear(Usuario usuario) {
        usuario.setPassword(cifrarSiHaceFalta(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findWithRolesById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional
    public Usuario actualizar(Long id, Usuario usuario) {
        Usuario usuarioActual = obtenerPorId(id);

        usuarioActual.setUsername(usuario.getUsername());
        usuarioActual.setEmail(usuario.getEmail());
        usuarioActual.setNombre(usuario.getNombre());
        usuarioActual.setApellido(usuario.getApellido());
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuarioActual.setPassword(cifrarSiHaceFalta(usuario.getPassword()));
        }

        return usuarioRepository.save(usuarioActual);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario asignarRoles(Long usuarioId, Set<Long> rolIds) {
        if (rolIds == null) {
            throw new IllegalArgumentException("La lista de roles es obligatoria");
        }

        Usuario usuario = usuarioRepository.findWithRolesById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("No se pueden asignar roles a un usuario inactivo");
        }

        List<Rol> rolesEncontrados = rolRepository.findAllById(rolIds);
        if (rolesEncontrados.size() != rolIds.size()) {
            throw new RolNoEncontradoException("Uno o mas roles no existen");
        }

        boolean contieneInactivos = rolesEncontrados.stream()
                .anyMatch(rol -> !Boolean.TRUE.equals(rol.getActivo()));
        if (contieneInactivos) {
            throw new RolInactivoException("No se pueden asignar roles inactivos");
        }

        usuario.setRoles(new HashSet<>(rolesEncontrados));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Rol> obtenerRoles(Long usuarioId) {
        Usuario usuario = usuarioRepository.findWithRolesById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
        return new HashSet<>(usuario.getRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> obtenerAutoridades(String username) {
        Usuario usuario = usuarioRepository.findWithRolesAndPermisosByUsername(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado: " + username));

        Set<String> autoridades = new HashSet<>();
        usuario.getRoles().stream()
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .forEach(rol -> {
                    autoridades.add("ROLE_" + rol.getCodigo());
                    rol.getPermisos().stream()
                            .filter(permiso -> Boolean.TRUE.equals(permiso.getActivo()))
                            .map(Permiso::getCodigo)
                            .forEach(autoridades::add);
                });

        return autoridades;
    }

    private String cifrarSiHaceFalta(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria.");
        }
        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
            return password;
        }
        return passwordEncoder.encode(password);
    }
}
