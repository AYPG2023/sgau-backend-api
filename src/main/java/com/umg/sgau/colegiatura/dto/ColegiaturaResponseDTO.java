package com.umg.sgau.colegiatura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColegiaturaResponseDTO {

    private Long id;
    private Long estudianteId;
    private Integer cicloAnio;
    private String concepto;

    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    private String estado;
    private Boolean activo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

}