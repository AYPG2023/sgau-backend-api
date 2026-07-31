package com.umg.sgau.colegiatura.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColegiaturaSummaryDTO {

    private Long id;

    private Long estudianteId;

    private BigDecimal monto;

    private LocalDate fechaVencimiento;

    private Boolean pagada;

}