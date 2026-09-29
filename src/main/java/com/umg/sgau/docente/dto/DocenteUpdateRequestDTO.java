package com.umg.sgau.docente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Actualiza únicamente campos académicos del perfil. La identidad de un perfil vinculado se administra desde Usuario.")
public class DocenteUpdateRequestDTO {

    @NotBlank(message = "El codigo del docente es obligatorio")
    @Size(max = 20, message = "El codigo del docente no puede superar los 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El codigo solo puede contener letras, numeros y guiones")
    private String codigoDocente;

    @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
    @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "El telefono solo puede contener numeros y simbolos validos")
    private String telefono;

    @Size(max = 100, message = "La especialidad no puede superar los 100 caracteres")
    private String especialidad;
}
