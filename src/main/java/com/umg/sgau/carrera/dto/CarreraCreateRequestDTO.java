package com.umg.sgau.carrera.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;

/**
 * DTO para crear una carrera.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraCreateRequestDTO {

    @NotBlank(message = "El código de la carrera es obligatorio")
    @Size(
            min = 2,
            max = 20,
            message = "El código debe contener entre 2 y 20 caracteres")
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "El código solo puede contener letras, números y guiones")
    private String codigo;

    @NotBlank(message = "El nombre de la carrera es obligatorio")
    @Size(
            min = 3,
            max = 120,
            message = "El nombre debe contener entre 3 y 120 caracteres")
    private String nombre;

    @Size(
            max = 500,
            message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "La duración de la carrera es obligatoria")
    @Min(
            value = 1,
            message = "La duración debe ser de al menos 1 año")
    @Max(
            value = 10,
            message = "La duración no puede superar los 10 años")
    private Integer duracionAnios;

    @DecimalMin(value = "0.01")
    private BigDecimal mensualidad;
    @Min(1) @Max(24)
    private Integer cantidadCuotas;
    @Min(1) @Max(31)
    private Integer diaVencimiento;
}
