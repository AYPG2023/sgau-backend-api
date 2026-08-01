package com.umg.sgau.nota.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class NotaCreateRequestDTO {

    @NotNull(message = "El estudiante es obligatorio")
    @Positive(message = "El identificador del estudiante debe ser positivo")
    private Long estudianteId;

    @NotNull(message = "El curso es obligatorio")
    @Positive(message = "El identificador del curso debe ser positivo")
    private Long cursoId;

    @NotNull(message = "El ciclo academico es obligatorio")
    @Min(2020)
    @Max(2100)
    private Integer cicloAnio;

    @NotBlank(message = "El tipo de evaluacion es obligatorio")
    @Size(max = 50)
    private String tipoEvaluacion;

    @NotNull(message = "La calificacion es obligatoria")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal calificacion;

    @Size(max = 250)
    private String observaciones;
}
