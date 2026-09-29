package com.umg.sgau.estudiante.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteResponseDTO {

    private Long id;

    private String codigoEstudiantil;

    private String numeroIdentificacion;

    private String nombres;

    private String apellidos;

    private LocalDate fechaNacimiento;

    private String correo;

    private String telefono;

    private String direccion;

    private Boolean activo;
    private Long usuarioId;
    private Boolean accesoApp;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;
}
