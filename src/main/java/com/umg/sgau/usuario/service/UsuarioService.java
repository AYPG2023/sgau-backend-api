package com.umg.sgau.usuario.service;

import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.usuario.entity.Usuario;
import java.util.List;
import java.util.Set;

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

    Usuario asignarRoles(Long usuarioId, Set<Long> rolIds);

    Set<Rol> obtenerRoles(Long usuarioId);

    Set<String> obtenerAutoridades(String username);
}
