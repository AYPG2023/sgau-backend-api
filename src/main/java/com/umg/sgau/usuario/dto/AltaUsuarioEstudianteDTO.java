package com.umg.sgau.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos propios del perfil estudiante. Es obligatorio cuando rolIds incluye ESTUDIANTE.")
public class AltaUsuarioEstudianteDTO {
    @NotBlank(message = "El codigo estudiantil es obligatorio")
    @Size(max = 20, message = "El codigo estudiantil no puede superar los 20 caracteres")
    @Schema(example = "EST-2026-001")
    private String codigoEstudiantil;

    @NotBlank(message = "El numero de identificacion es obligatorio")
    @Size(max = 20, message = "El numero de identificacion no puede superar los 20 caracteres")
    @Schema(example = "3012345670101")
    private String numeroIdentificacion;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
    private String telefono;

    @Size(max = 250, message = "La direccion no puede superar los 250 caracteres")
    private String direccion;
}
