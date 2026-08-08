package com.umg.sgau.permiso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermisoUpdateRequestDTO {

    @NotBlank(message = "El codigo del permiso es obligatorio")
    @Size(min = 3, max = 80)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "El codigo solo puede contener letras, numeros y guion bajo")
    private String codigo;

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(min = 3, max = 120)
    private String nombre;

    @Size(max = 300)
    private String descripcion;
}
