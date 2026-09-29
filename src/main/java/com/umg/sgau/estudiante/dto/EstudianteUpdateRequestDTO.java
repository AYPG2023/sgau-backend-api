package com.umg.sgau.estudiante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Actualiza únicamente datos académicos y de contacto propios del perfil. La identidad se administra desde Usuario.")
public class EstudianteUpdateRequestDTO {

    @NotBlank(message = "El código estudiantil es obligatorio.")
    @Size(max = 20, message = "El código estudiantil no puede exceder los 20 caracteres.")
    private String codigoEstudiantil;

    @NotBlank(message = "El número de identificación es obligatorio.")
    @Size(max = 20, message = "El número de identificación no puede exceder los 20 caracteres.")
    private String numeroIdentificacion;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    private LocalDate fechaNacimiento;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    @Size(max = 250, message = "La dirección no puede exceder los 250 caracteres.")
    private String direccion;
}