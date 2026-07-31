package com.umg.sgau.colegiatura.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColegiaturaUpdateRequestDTO {

    private String concepto;

    private BigDecimal montoTotal;

    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;

}