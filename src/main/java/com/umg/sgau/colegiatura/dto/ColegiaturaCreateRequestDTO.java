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
    @Min(value = 2020, message = "El ciclo académico debe ser 2020 o posterior")
    @Max(value = 2100, message = "El ciclo académico no puede ser posterior a 2100")
    private Integer cicloAnio;

    @NotBlank(message = "El concepto es obligatorio")
    @Size(max = 120, message = "El concepto no puede superar 120 caracteres")
    private String concepto;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El monto total admite hasta 8 enteros y 2 decimales")
    private BigDecimal montoTotal;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

}
