package com.umg.sgau.rol.mapper;

import com.umg.sgau.permiso.mapper.PermisoMapper;
import com.umg.sgau.rol.dto.RolCreateRequestDTO;
import com.umg.sgau.rol.dto.RolResponseDTO;
import com.umg.sgau.rol.dto.RolSummaryDTO;
import com.umg.sgau.rol.dto.RolUpdateRequestDTO;
import com.umg.sgau.rol.entity.Rol;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class RolMapper {

    private RolMapper() {
    }

    public static Rol aEntidad(RolCreateRequestDTO dto) {
        Rol rol = new Rol();
        rol.setCodigo(dto.getCodigo());
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
        return rol;
    }

    public static RolResponseDTO aResponseDTO(Rol rol) {
        return RolResponseDTO.builder()
                .id(rol.getId())
                .codigo(rol.getCodigo())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .permisos(aPermisosSummary(rol))
                .fechaCreacion(rol.getFechaCreacion())
                .fechaActualizacion(rol.getFechaActualizacion())
                .build();
    }

    public static RolSummaryDTO aSummaryDTO(Rol rol) {
        return RolSummaryDTO.builder()
                .id(rol.getId())
                .codigo(rol.getCodigo())
                .nombre(rol.getNombre())
                .build();
    }

    public static void actualizarEntidad(RolUpdateRequestDTO dto, Rol rol) {
        rol.setCodigo(dto.getCodigo());
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
    }

    private static Set<com.umg.sgau.permiso.dto.PermisoSummaryDTO> aPermisosSummary(Rol rol) {
        if (rol.getPermisos() == null) {
            return Collections.emptySet();
        }
        return rol.getPermisos()
                .stream()
                .map(PermisoMapper::aSummaryDTO)
                .collect(Collectors.toSet());
    }
}
