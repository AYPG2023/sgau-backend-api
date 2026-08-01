package com.umg.sgau.nota.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaResponseDTO {

    private Long id;
    private Long estudianteId;
    private Long cursoId;
    private Integer cicloAnio;
    private String tipoEvaluacion;
    private BigDecimal calificacion;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
