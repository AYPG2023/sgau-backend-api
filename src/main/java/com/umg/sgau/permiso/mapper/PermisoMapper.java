package com.umg.sgau.permiso.mapper;

import com.umg.sgau.permiso.dto.PermisoCreateRequestDTO;
import com.umg.sgau.permiso.dto.PermisoResponseDTO;
import com.umg.sgau.permiso.dto.PermisoSummaryDTO;
import com.umg.sgau.permiso.dto.PermisoUpdateRequestDTO;
import com.umg.sgau.permiso.entity.Permiso;

public final class PermisoMapper {

    private PermisoMapper() {
    }

    public static Permiso aEntidad(PermisoCreateRequestDTO dto) {
        Permiso permiso = new Permiso();
        permiso.setCodigo(dto.getCodigo());
        permiso.setNombre(dto.getNombre());
        permiso.setDescripcion(dto.getDescripcion());
        return permiso;
    }

    public static PermisoResponseDTO aResponseDTO(Permiso permiso) {
        return PermisoResponseDTO.builder()
                .id(permiso.getId())
                .codigo(permiso.getCodigo())
                .nombre(permiso.getNombre())
                .descripcion(permiso.getDescripcion())
                .activo(permiso.getActivo())
                .fechaCreacion(permiso.getFechaCreacion())
                .fechaActualizacion(permiso.getFechaActualizacion())
                .build();
    }

    public static PermisoSummaryDTO aSummaryDTO(Permiso permiso) {
        return PermisoSummaryDTO.builder()
                .id(permiso.getId())
                .codigo(permiso.getCodigo())
                .nombre(permiso.getNombre())
                .build();
    }

    public static void actualizarEntidad(PermisoUpdateRequestDTO dto, Permiso permiso) {
        permiso.setCodigo(dto.getCodigo());
        permiso.setNombre(dto.getNombre());
        permiso.setDescripcion(dto.getDescripcion());
    }
}
