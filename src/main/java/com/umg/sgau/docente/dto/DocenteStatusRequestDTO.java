package com.umg.sgau.docente.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocenteStatusRequestDTO {

    @NotNull(message = "El estado del docente es obligatorio")
    private Boolean activo;
}
