package com.umg.sgau.colegiatura.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColegiaturaCreateRequestDTO {

    @NotNull(message = "El estudiante es obligatorio")
    @Positive(message = "El identificador del estudiante debe ser positivo")
    private Long estudianteId;

    @NotNull(message = "El ciclo académico es obligatorio")
    @Min(2020)
    @Max(2100)
    private Integer cicloAnio;

    @NotBlank(message = "El concepto es obligatorio")
    @Size(max = 120)
    private String concepto;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01")
    private BigDecimal montoTotal;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

}