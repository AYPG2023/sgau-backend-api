package com.umg.sgau.permiso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermisoStatusRequestDTO {

    @NotNull(message = "El estado del permiso es obligatorio")
    private Boolean activo;
}
