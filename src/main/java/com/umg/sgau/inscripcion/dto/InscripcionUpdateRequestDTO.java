package com.umg.sgau.inscripcion.dto;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionUpdateRequestDTO {

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

    @Size(max = 500)
    private String observaciones;
}

