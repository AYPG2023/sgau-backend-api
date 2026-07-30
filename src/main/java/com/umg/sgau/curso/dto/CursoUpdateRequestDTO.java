package com.umg.sgau.curso.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para actualizar un curso.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoUpdateRequestDTO {

    @NotBlank(message = "El codigo del curso es obligatorio")
    @Size(
            min = 2,
            max = 20,
            message = "El codigo debe contener entre 2 y 20 caracteres")
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "El codigo solo puede contener letras, numeros y guiones")
    private String codigo;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Size(
            min = 3,
            max = 120,
            message = "El nombre debe contener entre 3 y 120 caracteres")
    private String nombre;

    @Size(
            max = 500,
            message = "La descripcion no puede superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "Los creditos son obligatorios")
    @Min(
            value = 1,
            message = "Los creditos deben ser como minimo 1")
    @Max(
            value = 20,
            message = "Los creditos no pueden superar 20")
    private Integer creditos;

    @NotNull(message = "Las horas semanales son obligatorias")
    @Min(
            value = 1,
            message = "Las horas semanales deben ser como minimo 1")
    @Max(
            value = 40,
            message = "Las horas semanales no pueden superar 40")
    private Integer horasSemanales;

    @NotNull(message = "La carrera es obligatoria")
    @Positive(message = "El identificador de la carrera debe ser positivo")
    private Long carreraId;

    @Positive(message = "El identificador del docente debe ser positivo")
    private Long docenteId;

    @NotNull(message = "El ciclo academico es obligatorio")
    @Min(
            value = 2020,
            message = "El anio del ciclo academico no puede ser menor a 2020")
    @Max(
            value = 2100,
            message = "El anio del ciclo academico no puede ser mayor a 2100")
    private Integer cicloAnio;
}
