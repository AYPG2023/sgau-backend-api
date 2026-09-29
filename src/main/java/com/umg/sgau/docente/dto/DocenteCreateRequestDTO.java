package com.umg.sgau.docente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Crea un perfil docente. nombre, apellido y email son la identidad única; si se crea o vincula una cuenta, esa identidad se guarda en Usuario. Si accesoApp es false, el perfil conserva los datos históricos como respaldo.")
public class DocenteCreateRequestDTO {

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

    private Boolean accesoApp;
    private Long usuarioId;

    @Size(max = 50, message = "El username no puede superar los 50 caracteres")
    private String username;

    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    private String password;
}
