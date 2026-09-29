package com.umg.sgau.estudiante.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteSummaryDTO {

    private Long id;

    private String codigoEstudiantil;

    private String nombres;

    private String apellidos;

    private Boolean activo;

    private String identidadFuente;
}
