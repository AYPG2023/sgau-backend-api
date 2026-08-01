package com.umg.sgau.usuario.serviceimpl;

import com.umg.sgau.exception.UsuarioNoEncontradoException;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import com.umg.sgau.usuario.service.UsuarioService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyección por constructor
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario crear(Usuario usuario) {
        // Cifrar la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario obtenerPorId(Long id) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findById(id);
        if (usuarioEncontrado.isEmpty()) {
            throw new UsuarioNoEncontradoException(id);
        }
        return usuarioEncontrado.get();
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new UsuarioNoEncontradoException(id);
        }
        Usuario usuarioActual = usuarioExistente.get();
        usuarioActual.setUsername(usuario.getUsername());
        usuarioActual.setEmail(usuario.getEmail());
        usuarioActual.setNombre(usuario.getNombre());
        usuarioActual.setApellido(usuario.getApellido());
        usuarioActual.setActivo(usuario.getActivo());

        // Si viene un nuevo password en el update, cifrarlo
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuarioActual.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        return usuarioRepository.save(usuarioActual);
    }

    @Override
    public void eliminar(Long id) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new UsuarioNoEncontradoException(id);
        }
        usuarioRepository.deleteById(id);
    }
}
