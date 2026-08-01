package com.umg.sgau.historialacademico.service;

import com.umg.sgau.historialacademico.dto.HistorialAcademicoResponseDTO;

public interface HistorialAcademicoService {

    HistorialAcademicoResponseDTO generarHistorial(Long estudianteId);

    HistorialAcademicoResponseDTO generarHistorialPorCiclo(Long estudianteId, Integer cicloAnio);
}
