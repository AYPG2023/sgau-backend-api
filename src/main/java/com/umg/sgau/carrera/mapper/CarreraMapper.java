package com.umg.sgau.carrera.mapper;

import com.umg.sgau.carrera.dto.CarreraCreateRequestDTO;
import com.umg.sgau.carrera.dto.CarreraResponseDTO;
import com.umg.sgau.carrera.dto.CarreraSummaryDTO;
import com.umg.sgau.carrera.dto.CarreraUpdateRequestDTO;
import com.umg.sgau.carrera.entity.Carrera;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilidad de mapeo entre la entidad Carrera y sus DTO.
 */
public final class CarreraMapper {

    private CarreraMapper() {
    }

    public static Carrera aEntidad(CarreraCreateRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El DTO de creacion de carrera es obligatorio");
        }

        return Carrera.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .duracionAnios(dto.getDuracionAnios())
                .mensualidad(dto.getMensualidad()).cantidadCuotas(dto.getCantidadCuotas()).diaVencimiento(dto.getDiaVencimiento())
                .build();
    }

    public static CarreraResponseDTO aResponseDTO(Carrera carrera) {
        if (carrera == null) {
            throw new IllegalArgumentException("La entidad carrera es obligatoria");
        }

        return CarreraResponseDTO.builder()
                .id(carrera.getId())
                .codigo(carrera.getCodigo())
                .nombre(carrera.getNombre())
                .descripcion(carrera.getDescripcion())
                .duracionAnios(carrera.getDuracionAnios())
                .mensualidad(carrera.getMensualidad()).cantidadCuotas(carrera.getCantidadCuotas()).diaVencimiento(carrera.getDiaVencimiento())
                .activo(carrera.getActivo())
                .fechaCreacion(carrera.getFechaCreacion())
                .fechaActualizacion(carrera.getFechaActualizacion())
                .build();
    }

    public static CarreraSummaryDTO aSummaryDTO(Carrera carrera) {
        if (carrera == null) {
            throw new IllegalArgumentException("La entidad carrera es obligatoria");
        }

        return CarreraSummaryDTO.builder()
                .id(carrera.getId())
                .codigo(carrera.getCodigo())
                .nombre(carrera.getNombre())
                .activo(carrera.getActivo())
                .build();
    }

    public static List<CarreraResponseDTO> aResponseDTOList(List<Carrera> carreras) {
        if (carreras == null) {
            return Collections.emptyList();
        }

        return carreras.stream()
                .map(CarreraMapper::aResponseDTO)
                .collect(Collectors.toList());
    }

    public static void actualizarEntidad(CarreraUpdateRequestDTO dto, Carrera carrera) {
        if (dto == null || carrera == null) {
            return;
        }

        carrera.setCodigo(dto.getCodigo());
        carrera.setNombre(dto.getNombre());
        carrera.setDescripcion(dto.getDescripcion());
        carrera.setDuracionAnios(dto.getDuracionAnios());
        carrera.setMensualidad(dto.getMensualidad()); carrera.setCantidadCuotas(dto.getCantidadCuotas()); carrera.setDiaVencimiento(dto.getDiaVencimiento());
    }
}
