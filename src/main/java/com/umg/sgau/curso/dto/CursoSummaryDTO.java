package com.umg.sgau.curso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta resumida de un curso.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoSummaryDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private Long carreraId;
    private Boolean activo;
}
