package com.umg.sgau.historialacademico.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialCursoResponseDTO {

    private Long inscripcionId;
    private Long cursoId;
    private String codigoCurso;
    private String nombreCurso;
    private Long carreraId;
    private Integer cicloAnio;
    private String grado;
    private String seccion;
    private String estadoInscripcion;
    private Boolean inscripcionActiva;
    private Boolean cursoActivo;
    private BigDecimal promedioCurso;
    private String resultado;
    private List<HistorialNotaResponseDTO> notas;
}
