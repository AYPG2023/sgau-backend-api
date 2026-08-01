package com.umg.sgau.colegiatura.model;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import java.math.BigDecimal;
import java.util.List;

public record EstadoCuentaEstudiante(
        Long estudianteId,
        String estudianteNombre,
        BigDecimal totalCargos,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente,
        int cantidadCargos,
        int cantidadPendientes,
        List<Colegiatura> detalle
) {
}
