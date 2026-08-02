package com.umg.sgau.usuario.service;

import com.umg.sgau.usuario.entity.Usuario;
import java.util.List;

public interface UsuarioService {

    // Crear un nuevo usuario
    Usuario crear(Usuario usuario);

    // Obtener usuario por ID
    Usuario obtenerPorId(Long id);

    // Listar todos los usuarios
    List<Usuario> obtenerTodos();

    // Actualizar usuario existente
    Usuario actualizar(Long id, Usuario usuario);

    // Eliminar usuario por ID
    void eliminar(Long id);
}
