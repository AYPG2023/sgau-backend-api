package com.umg.sgau.nota.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class NotaUpdateRequestDTO {

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
