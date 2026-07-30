package com.umg.sgau.carrera.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para cambiar el estado activo o inactivo de una carrera.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraStatusRequestDTO {

    @NotNull(message = "El estado de la carrera es obligatorio")
    private Boolean activo;
}
