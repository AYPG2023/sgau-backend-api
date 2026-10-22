package com.umg.sgau.colegiatura.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenFinancieroCicloDTO(
        Long estudianteId,
        Integer cicloAnio,
        BigDecimal totalCargos,
        BigDecimal totalPagosAprobados,
        BigDecimal saldoPendiente,
        BigDecimal pagosPendientesRevision,
        List<ColegiaturaResponseDTO> colegiaturas) {
}
