package com.umg.sgau.inscripcion.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionCreateRequestDTO {

    @NotNull(message = "El estudiante es obligatorio")
    @Positive(message = "El identificador del estudiante debe ser positivo")
    private Long estudianteId;

    @NotNull(message = "La carrera es obligatoria")
    @Positive(message = "El identificador de la carrera debe ser positivo")
    private Long carreraId;

    @Positive(message = "El identificador del curso debe ser positivo")
    private Long cursoId;

    @NotBlank(message = "El grado es obligatorio")
    @Size(max = 50)
    private String grado;

    @NotBlank(message = "La sección es obligatoria")
    @Size(max = 20)
    private String seccion;

    @NotNull(message = "El ciclo académico es obligatorio")
    @Min(value = 2020)
    @Max(value = 2100)
    private Integer cicloAnio;

    @NotNull(message = "La fecha de inscripción es obligatoria")
    @PastOrPresent(message = "La fecha no puede estar en el futuro")
    private LocalDate fechaInscripcion;

    @Size(max = 500)
    private String observaciones;
}
