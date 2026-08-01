package com.umg.sgau.estudiante.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteStatusRequestDTO {

    @NotNull(message = "El estado del estudiante es obligatorio.")
    private Boolean activo;
}
