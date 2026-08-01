package com.umg.sgau.usuario.mapper;

import com.umg.sgau.usuario.dto.UsuarioRequestDTO;
import com.umg.sgau.usuario.dto.UsuarioResponseDTO;
import com.umg.sgau.usuario.entity.Usuario;
import java.util.List;
import java.util.stream.Collectors;

//solo expone métodos estáticos
public class UsuarioMapper {

    // Constructor privado para evitar instanciación
    private UsuarioMapper() {}

    // Convierte un RequestDTO en una entidad Usuario
    public static Usuario aEntidad(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword()); // se cifrará en ServiceImpl con BCrypt
        usuario.setEmail(dto.getEmail());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setActivo(dto.getActivo());
        return usuario;
    }

    // Convierte una entidad Usuario en un ResponseDTO
    public static UsuarioResponseDTO aResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        return dto;
    }

    // Convierte una lista de entidades en una lista de ResponseDTO 
    public static List<UsuarioResponseDTO> aResponseDTOList(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(UsuarioMapper::aResponseDTO) // map() aplica la conversión a cada elemento
                .collect(Collectors.toList());    // collect() junta los resultados en una lista
    }
}
