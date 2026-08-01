package com.umg.sgau.colegiatura.dto;

import java.math.BigDecimal;
import java.util.List;
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
public class EstadoCuentaResponseDTO {

    private Long estudianteId;
    private String estudianteNombre;
    private BigDecimal totalCargos;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private Integer cantidadCargos;
    private Integer cantidadPendientes;
    private List<ColegiaturaResponseDTO> detalle;
}
