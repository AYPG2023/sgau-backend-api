package com.umg.sgau.curso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para cambiar el estado de un curso.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoStatusRequestDTO {

    @NotNull(message = "El estado del curso es obligatorio")
    private Boolean activo;
}
