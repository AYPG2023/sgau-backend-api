package com.umg.sgau.docente.dto;

import java.time.LocalDateTime;
// ESTO EQUIVALE A ESTO public DocenteResponseDTO() 
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class DocenteResponseDTO {

    private Long id;
    private String codigoDocente;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String especialidad;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}