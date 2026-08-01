package com.umg.sgau.nota.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromedioResponseDTO {

    private Long estudianteId;
    private Integer cicloAnio;
    private BigDecimal promedioGeneral;
    private Integer cantidadNotas;
}
