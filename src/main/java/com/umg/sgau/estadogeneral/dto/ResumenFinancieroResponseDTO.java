package com.umg.sgau.estadogeneral.dto;

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
public class ResumenFinancieroResponseDTO {

    private BigDecimal totalCargos;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private Integer cantidadCargos;
    private Integer cantidadPendientes;
    private Integer cantidadPagadas;
    private String estadoFinanciero;
}
