package com.umg.sgau.usuario.serviceimpl;

import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.exception.UsuarioNoEncontradoException;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import com.umg.sgau.usuario.service.UsuarioService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario crear(Usuario usuario) {
        usuario.setPassword(cifrarSiHaceFalta(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        Usuario usuarioActual = obtenerPorId(id);

        usuarioActual.setUsername(usuario.getUsername());
        usuarioActual.setEmail(usuario.getEmail());
        usuarioActual.setNombre(usuario.getNombre());
        usuarioActual.setApellido(usuario.getApellido());
        if (usuario.getRol() != null) {
            usuarioActual.setRol(usuario.getRol());
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuarioActual.setPassword(cifrarSiHaceFalta(usuario.getPassword()));
        }

        return usuarioRepository.save(usuarioActual);
    }

    @Override
    public void eliminar(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
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
