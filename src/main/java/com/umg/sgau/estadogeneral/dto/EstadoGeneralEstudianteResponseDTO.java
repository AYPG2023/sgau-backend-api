package com.umg.sgau.estadogeneral.dto;

import com.umg.sgau.colegiatura.dto.ColegiaturaResponseDTO;
import com.umg.sgau.historialacademico.dto.HistorialCursoResponseDTO;
import java.time.LocalDateTime;
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
public class EstadoGeneralEstudianteResponseDTO {

    private Long estudianteId;
    private String nombreCompleto;
    private String correo;
    private String codigoEstudiantil;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private String estadoGeneral;
    private ResumenAcademicoResponseDTO resumenAcademico;
    private ResumenFinancieroResponseDTO resumenFinanciero;
    private List<HistorialCursoResponseDTO> detalleAcademico;
    private List<ColegiaturaResponseDTO> detalleFinanciero;
}
