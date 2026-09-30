package com.umg.sgau.carrera.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta completa de la api.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer duracionAnios;
    private BigDecimal mensualidad;
    private Integer cantidadCuotas;
    private Integer diaVencimiento;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
