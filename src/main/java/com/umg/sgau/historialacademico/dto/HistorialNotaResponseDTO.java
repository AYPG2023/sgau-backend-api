package com.umg.sgau.historialacademico.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialNotaResponseDTO {

    private Long notaId;
    private String tipoEvaluacion;
    private BigDecimal calificacion;
    private String observaciones;
    private Boolean activo;
}
