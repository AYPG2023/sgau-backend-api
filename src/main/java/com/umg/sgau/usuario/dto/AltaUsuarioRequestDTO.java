package com.umg.sgau.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Alta administrativa atomica de una cuenta, sus roles y los perfiles academicos requeridos. Nunca reutiliza un usuario existente.")
public class AltaUsuarioRequestDTO {
    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no puede superar los 50 caracteres")
    @Schema(example = "jperez")
    private String username;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    @Schema(format = "password", example = "Segura123*")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60, message = "El apellido no puede superar los 60 caracteres")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String correo;

    @NotEmpty(message = "Debe seleccionar al menos un rol")
    @Schema(description = "IDs de roles activos que se asignaran", example = "[2]")
    private Set<Long> rolIds;

    @Valid
    private AltaUsuarioDocenteDTO docente;

    @Valid
    private AltaUsuarioEstudianteDTO estudiante;
}
