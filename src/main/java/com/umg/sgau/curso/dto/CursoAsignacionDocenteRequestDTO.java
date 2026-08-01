package com.umg.sgau.curso.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para asignar un docente a un curso.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoAsignacionDocenteRequestDTO {

    @NotNull(message = "El docente es obligatorio")
    @Positive(message = "El identificador del docente debe ser positivo")
    private Long docenteId;
}
