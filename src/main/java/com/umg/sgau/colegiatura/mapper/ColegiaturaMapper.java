package com.umg.sgau.colegiatura.mapper;

import com.umg.sgau.colegiatura.dto.*;
import com.umg.sgau.colegiatura.entity.Colegiatura;

import java.util.List;
import java.util.stream.Collectors;

public class ColegiaturaMapper {

    private ColegiaturaMapper() {
    }

    public static Colegiatura toEntity(
            ColegiaturaCreateRequestDTO dto) {

        Colegiatura colegiatura = new Colegiatura();

        colegiatura.setEstudianteId(dto.getEstudianteId());
        colegiatura.setCicloAnio(dto.getCicloAnio());
        colegiatura.setConcepto(dto.getConcepto());
        colegiatura.setMontoTotal(dto.getMontoTotal());
        colegiatura.setFechaEmision(dto.getFechaEmision());
        colegiatura.setFechaVencimiento(
                dto.getFechaVencimiento());

        return colegiatura;
    }

    public static ColegiaturaResponseDTO toResponseDTO(
            Colegiatura colegiatura) {

        return ColegiaturaResponseDTO.builder()
                .id(colegiatura.getId())
                .estudianteId(colegiatura.getEstudianteId())
                .cicloAnio(colegiatura.getCicloAnio())
                .concepto(colegiatura.getConcepto())
                .montoTotal(colegiatura.getMontoTotal())
                .montoPagado(colegiatura.getMontoPagado())
                .saldoPendiente(colegiatura.getSaldoPendiente())
                .fechaEmision(colegiatura.getFechaEmision())
                .fechaVencimiento(colegiatura.getFechaVencimiento())
                .estado(colegiatura.getEstado())
                .activo(colegiatura.getActivo())
                .fechaCreacion(colegiatura.getFechaCreacion())
                .fechaActualizacion(colegiatura.getFechaActualizacion())
                .build();
    }

    public static EstadoCuentaResponseDTO toEstadoCuentaDTO(
            Colegiatura colegiatura) {

        return EstadoCuentaResponseDTO.builder()
                .montoTotal(colegiatura.getMontoTotal())
                .montoPagado(colegiatura.getMontoPagado())
                .saldoPendiente(colegiatura.getSaldoPendiente())
                .build();
    }

    public static List<ColegiaturaResponseDTO> toResponseDTOList(
            List<Colegiatura> colegiaturas) {

        return colegiaturas.stream()
                .map(ColegiaturaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntity(
            ColegiaturaUpdateRequestDTO dto,
            Colegiatura colegiatura) {

        colegiatura.setConcepto(dto.getConcepto());
        colegiatura.setMontoTotal(dto.getMontoTotal());
        colegiatura.setFechaEmision(dto.getFechaEmision());
        colegiatura.setFechaVencimiento(
                dto.getFechaVencimiento());

        // No se modifican:
        // id
        // estudianteId
        // cicloAnio
        // montoPagado
        // saldoPendiente
        // estado
        // activo
        // fechaCreacion
        // fechaActualizacion
    }

}