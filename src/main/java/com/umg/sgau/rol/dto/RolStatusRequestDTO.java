package com.umg.sgau.rol.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolStatusRequestDTO {

    @NotNull(message = "El estado del rol es obligatorio")
    private Boolean activo;
}
