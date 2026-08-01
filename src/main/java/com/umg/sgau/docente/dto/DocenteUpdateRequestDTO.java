package com.umg.sgau.docente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocenteUpdateRequestDTO {

    @NotBlank(message = "El codigo del docente es obligatorio")
    @Size(max = 20, message = "El codigo del docente no puede superar los 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El codigo solo puede contener letras, numeros y guiones")
    private String codigoDocente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60, message = "El apellido no puede superar los 60 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato valido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
    @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "El telefono solo puede contener numeros y simbolos validos")
    private String telefono;

    @Size(max = 100, message = "La especialidad no puede superar los 100 caracteres")
    private String especialidad;
}
