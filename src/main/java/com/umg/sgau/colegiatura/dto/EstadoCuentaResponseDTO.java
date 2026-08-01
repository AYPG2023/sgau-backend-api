package com.umg.sgau.colegiatura.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCuentaResponseDTO {

    private BigDecimal montoTotal;

    private BigDecimal montoPagado;

    private BigDecimal saldoPendiente;

}