package com.umg.sgau.estudiante.dto;


import jakarta.validation.constraints.*;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteCreateRequestDTO {

    @NotBlank(message = "El código estudiantil es obligatorio.")
    @Size(max = 20, message = "El código estudiantil no puede exceder los 20 caracteres.")
    private String codigoEstudiantil;

    @NotBlank(message = "El número de identificación es obligatorio.")
    @Size(max = 20, message = "El número de identificación no puede exceder los 20 caracteres.")
    private String numeroIdentificacion;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100, message = "Los nombres no pueden exceder los 100 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres.")
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Debe ingresar un correo válido.")
    @Size(max = 150, message = "El correo no puede exceder los 150 caracteres.")
    private String correo;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    @Size(max = 250, message = "La dirección no puede exceder los 250 caracteres.")
    private String direccion;

    private Boolean accesoApp;
    private Long usuarioId;

    @Size(max = 50, message = "El username no puede superar los 50 caracteres.")
    private String username;

    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres.")
    private String password;
}



