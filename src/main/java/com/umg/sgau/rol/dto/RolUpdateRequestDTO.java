package com.umg.sgau.rol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolUpdateRequestDTO {

    @NotBlank(message = "El codigo del rol es obligatorio")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "El codigo solo puede contener letras, numeros y guion bajo")
    private String codigo;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 100)
    private String nombre;

    @Size(max = 300)
    private String descripcion;
}
